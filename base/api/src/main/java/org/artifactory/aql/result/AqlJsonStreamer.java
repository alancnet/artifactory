package org.artifactory.aql.result;

import org.apache.commons.lang.StringUtils;
import org.artifactory.aql.AqlException;
import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.aql.model.AqlFieldEnum;
import org.artifactory.aql.model.AqlItemTypeEnum;
import org.artifactory.aql.model.DomainSensitiveField;
import org.artifactory.aql.result.rows.InflatableRow;
import org.artifactory.aql.util.AqlUtils;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

/**
 * @author Gidi Shabat
 *         The class converts the AqlLazyResult to in-memory Aql Json result
 *         The Max number of rows allowed by this result is the actually artifactory searchUserQueryLimit:
 *         (ConstantValues.searchUserQueryLimit)
 */
public class AqlJsonStreamer extends AqlRestResult implements Cloneable {
    private static final Logger log = LoggerFactory.getLogger(AqlJsonStreamer.class);
    private static final String QUERY_PREFIX = "\n{\n\"results\" : [ ";
    private static final String NUMBER_OF_ROWS = "<NUMBER_OF_ROWS>";
    private static final String QUERY_POSTFIX = " ],\n\"range\" : " + NUMBER_OF_ROWS + "\n}\n";
    private final ResultSet resultSet;
    private final List<DomainSensitiveField> fields;
    private final Map<AqlFieldEnum, String> dbFieldNames;
    private final long limit;
    private final long offset;
    private long rowsCount;
    private Buffer buffer = new Buffer();
    private boolean ended;
    private String mainId = null;
    private Row mainRow;
    private AqlDomainEnum domain;

    public AqlJsonStreamer(AqlLazyResult lazyResult) {
        super(lazyResult.getPermissionProvider());
        this.resultSet = lazyResult.getResultSet();
        this.fields = lazyResult.getFields();
        this.dbFieldNames = lazyResult.getDbFieldNames();
        this.limit = lazyResult.getLimit();
        this.offset = lazyResult.getOffset();
        this.domain = lazyResult.getDomain();
        buffer.push(QUERY_PREFIX.getBytes());
    }

    /**
     * Read the ResultSet from db:
     * 1. In case of multi domain result the method merge multi rows into Json multi layer json result
     * 2. In case of single domain result the class convert single row into flat json.
     */
    private Row inflateRow() {

        try {
            while (resultSet.next()) {
                InflatableRow row = new InflatableRow();
                for (DomainSensitiveField field : fields) {
                    AqlFieldEnum fieldEnum = field.getField();
                    String dbFieldName = dbFieldNames.get(fieldEnum);
                    switch (fieldEnum.type) {
                        case date: {
                            Long valueLong = resultSet.getLong(dbFieldName);
                            String value = valueLong == 0 ? null : ISODateTimeFormat.dateTime().print(valueLong);
                            row.put(field, value);
                            break;
                        }
                        case longInt: {
                            long value = resultSet.getLong(dbFieldName);
                            row.put(field, value);
                            break;
                        }
                        case integer: {
                            int value = resultSet.getInt(dbFieldName);
                            row.put(field, value);
                            break;
                        }
                        case string: {
                            String value = resultSet.getString(dbFieldName);
                            row.put(field, value);
                            break;
                        }
                        case itemType: {
                            int type = resultSet.getInt(dbFieldName);
                            AqlItemTypeEnum aqlItemTypeEnum = AqlItemTypeEnum.fromTypes(type);
                            row.put(field, aqlItemTypeEnum);
                            break;
                        }
                    }
                }
                if (!canRead(domain, resultSet)) {
                    continue;
                }
                Map<String, Row> map = row.inflate();
                String newId = map.keySet().iterator().next();
                Row newRow = map.values().iterator().next();
                if (mainId == null) {
                    mainId = newId;
                    mainRow = newRow;
                } else {
                    if (!mainId.equals(newId)) {
                        Row temp = mainRow;
                        mainRow = newRow;
                        mainId = newId;
                        return temp.build();
                    } else {
                        mainRow.merge(newRow);
                    }
                }
            }
        } catch (Exception e) {
            throw new AqlException("Failed to fetch Aql result", e);
        }
        if (mainRow != null) {
            Row row = mainRow;
            mainRow = null;
            return row.build();
        } else {
            return null;
        }
    }

    /**
     * Reads Single row from The Json result
     * The method return null to signal end of stream
     *
     * @return Json row in byte array
     * @throws java.io.IOException
     */
    @Override
    public byte[] read() throws IOException {
        // Use the data in the buffer, reloading  the buffer is allowed only if it is empty
        if (!buffer.isEmpty()) {
            return buffer.getData();
        }
        // Fill the buffer from result-set
        byte[] data;
        if ((data = getNewRowFromDb()) != null) {
            rowsCount++;
            buffer.push(data);
            return buffer.getData();
        }
        // Fill the buffer from post fix
        if (!ended) {
            appendEndSection();
            return buffer.getData();
        }
        return null;
    }

    private void appendEndSection() {
        try {
            if (!ended) {
                String range = generateRangeJson();
                String summary = StringUtils.replace(QUERY_POSTFIX, NUMBER_OF_ROWS, "" + range);
                buffer.push(summary.getBytes());
                ended = true;
            }
        } catch (IOException e) {
            log.error("Failed to generate Aql result summery.", e);
        }
    }

    private String generateRangeJson() throws IOException {
        Range range = new Range(offset, rowsCount, rowsCount, limit);
        ObjectMapper mapper = new ObjectMapper();
        mapper.getSerializationConfig().withSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
        mapper.setVisibility(JsonMethod.ALL, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(JsonMethod.FIELD, JsonAutoDetect.Visibility.ANY);
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(range);
    }

    public byte[] getNewRowFromDb() {
        boolean isFirstElement = mainId == null;
        Row row = inflateRow();
        if (row != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.getSerializationConfig().withSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
                mapper.setVisibility(JsonMethod.ALL, JsonAutoDetect.Visibility.NONE);
                mapper.setVisibility(JsonMethod.FIELD, JsonAutoDetect.Visibility.ANY);
                String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(row);
                json = isFirstElement ? "" + json : "," + json;
                return json.getBytes();
            } catch (Exception e) {
                throw new AqlException("Failed to convert Aql Result to JSON", e);
            }
        }
        return null;
    }

    @Override
    public void close() {
        AqlUtils.closeResultSet(resultSet);
    }


    /**
     * Simplify the work with the stream during the read
     */
    private class Buffer {
        private byte[] buffer;

        public void push(byte[] bytes) {
            buffer = bytes;
        }

        public byte[] getData() {
            byte[] temp = buffer;
            buffer = null;
            return temp;
        }

        public boolean isEmpty() {
            return buffer == null;
        }
    }

}