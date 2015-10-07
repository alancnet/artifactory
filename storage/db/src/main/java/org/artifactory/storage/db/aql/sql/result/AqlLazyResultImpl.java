package org.artifactory.storage.db.aql.sql.result;

import com.google.common.collect.Maps;
import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.aql.model.AqlFieldEnum;
import org.artifactory.aql.model.AqlPermissionProvider;
import org.artifactory.aql.model.DomainSensitiveField;
import org.artifactory.aql.result.AqlLazyResult;
import org.artifactory.storage.db.aql.sql.builder.query.sql.SqlQuery;
import org.artifactory.storage.db.aql.sql.model.AqlFieldExtensionEnum;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

/**
 * @author Gidi Shabat
 */
public class AqlLazyResultImpl implements AqlLazyResult {
    private final long limit;
    private final long offset;
    private final List<DomainSensitiveField> fields;
    private ResultSet resultSet;
    private Map<AqlFieldEnum, String> dbFieldNames;
    private AqlDomainEnum domain;
    private AqlPermissionProvider aqlPermissionProvider;

    public AqlLazyResultImpl(ResultSet resultSet, SqlQuery sqlQuery, AqlPermissionProvider aqlPermissionProvider) {
        this.aqlPermissionProvider = aqlPermissionProvider;
        limit = sqlQuery.getLimit();
        offset = sqlQuery.getOffset();
        fields = sqlQuery.getResultFields();
        this.resultSet = resultSet;
        dbFieldNames = Maps.newHashMap();
        this.domain=sqlQuery.getDomain();
        for (DomainSensitiveField field : fields) {
            AqlFieldEnum fieldEnum = field.getField();
            dbFieldNames.put(fieldEnum, AqlFieldExtensionEnum.getExtensionFor(fieldEnum).tableField.name());
        }
    }

    @Override
    public AqlPermissionProvider getPermissionProvider() {
        return aqlPermissionProvider;
    }

    @Override
    public List<DomainSensitiveField> getFields() {
        return fields;
    }

    @Override
    public ResultSet getResultSet() {
        return resultSet;
    }

    @Override
    public long getLimit() {
        return limit;
    }

    @Override
    public long getOffset() {
        return offset;
    }

    @Override
    public Map<AqlFieldEnum, String> getDbFieldNames() {
        return dbFieldNames;
    }

    @Override
    public AqlDomainEnum getDomain() {
        return domain;
    }
}
