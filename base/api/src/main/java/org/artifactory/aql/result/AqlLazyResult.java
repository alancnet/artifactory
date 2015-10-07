package org.artifactory.aql.result;

import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.aql.model.AqlFieldEnum;
import org.artifactory.aql.model.AqlPermissionProvider;
import org.artifactory.aql.model.DomainSensitiveField;
import org.artifactory.aql.result.rows.AqlRowResult;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

/**
 * @author Gidi Shabat
 */
public interface AqlLazyResult <T extends AqlRowResult> {

    AqlPermissionProvider getPermissionProvider();
    List<DomainSensitiveField> getFields();

    ResultSet getResultSet();

    long getLimit();

    long getOffset();

    Map<AqlFieldEnum, String> getDbFieldNames();

    AqlDomainEnum getDomain();
}
