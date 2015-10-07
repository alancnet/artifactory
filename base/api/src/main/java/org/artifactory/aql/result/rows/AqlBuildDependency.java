package org.artifactory.aql.result.rows;

import static org.artifactory.aql.model.AqlDomainEnum.dependencies;

/**
 * @author Gidi Shabat
 */

@QueryTypes(value = dependencies)
public interface AqlBuildDependency extends AqlRowResult {
    String getBuildDependencyName();

    String getBuildDependencyScope();

    String getBuildDependencyType();
}
