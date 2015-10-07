package org.artifactory.aql.result.rows;

import static org.artifactory.aql.model.AqlDomainEnum.buildProperties;

/**
 * @author Gidi Shabat
 */
@QueryTypes(buildProperties)
public interface AqlBuildProperty extends AqlRowResult {
    String getBuildPropKey();

    String getBuildPropValue();
}
