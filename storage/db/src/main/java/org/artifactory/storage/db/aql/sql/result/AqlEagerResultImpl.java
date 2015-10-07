package org.artifactory.storage.db.aql.sql.result;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.artifactory.aql.model.AqlFieldEnum;
import org.artifactory.aql.model.AqlItemTypeEnum;
import org.artifactory.aql.model.DomainSensitiveField;
import org.artifactory.aql.result.AqlEagerResult;
import org.artifactory.aql.result.rows.AqlBaseFullRowImpl;
import org.artifactory.aql.result.rows.AqlRowResult;
import org.artifactory.storage.db.aql.sql.builder.query.sql.SqlQuery;
import org.artifactory.storage.db.aql.sql.model.AqlFieldExtensionEnum;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * In-Memory query result
 *
 * @author Gidi Shabat
 */
public class AqlEagerResultImpl<T extends AqlRowResult> implements AqlEagerResult {
    private List<Map<AqlFieldEnum, Object>> rows = Lists.newArrayList();

    public AqlEagerResultImpl(ResultSet resultSet, SqlQuery sqlQuery) throws SQLException {
        long limit = sqlQuery.getLimit();
        while (resultSet.next() && rows.size() < limit) {
            Map<AqlFieldEnum, Object> map = Maps.newHashMap();
            for (DomainSensitiveField field : sqlQuery.getResultFields()) {
                AqlFieldEnum fieldEnum = field.getField();
                AqlFieldExtensionEnum fieldsExtensionEnum = AqlFieldExtensionEnum.getExtensionFor(fieldEnum);
                switch (fieldEnum.type) {
                    case date: {
                        Long aLong = resultSet.getLong(fieldsExtensionEnum.tableField.name());
                        map.put(fieldEnum, aLong == 0 ? null : new Date(aLong));
                        break;
                    }
                    case longInt: {
                        map.put(fieldEnum, resultSet.getLong(fieldsExtensionEnum.tableField.name()));
                        break;
                    }
                    case integer: {
                        map.put(fieldEnum, resultSet.getInt(fieldsExtensionEnum.tableField.name()));
                        break;
                    }
                    case string: {
                        map.put(fieldEnum, resultSet.getString(fieldsExtensionEnum.tableField.name()));
                        break;
                    }
                    case itemType: {
                        int type = resultSet.getInt(fieldsExtensionEnum.tableField.name());
                        AqlItemTypeEnum aqlItemTypeEnum = AqlItemTypeEnum.fromTypes(type);
                        map.put(fieldEnum, aqlItemTypeEnum);
                        break;
                    }
                }
            }
            rows.add(map);
        }
    }

    @Override
    public int getSize() {
        return rows.size();
    }

    /**
     * @return True if the result set is empty
     */
    public boolean isEmpty() {
        return rows.isEmpty();
    }

    @Override
    public T getResult(int i) {
        return (T) new AqlBaseFullRowImpl(rows.get(i));
    }

    @Override
    public List<T> getResults() {
        List result = Lists.newArrayList();
        for (Map<AqlFieldEnum, Object> row : rows) {
            result.add(new AqlBaseFullRowImpl(row));
        }
        return result;
    }
}