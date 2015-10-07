package org.artifactory.aql.result.rows;

import static org.artifactory.aql.model.AqlDomainEnum.modules;

/**
 * @author Gidi Shabat
 */
@QueryTypes(modules)
public interface AqlBuildModule extends AqlRowResult {
    String getBuildModuleName();
}
