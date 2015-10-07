package org.artifactory.aql.result.rows;

import static org.artifactory.aql.model.AqlDomainEnum.artifacts;

/**
 * @author Gidi Shabat
 */
@QueryTypes(value = artifacts)
public interface AqlBuildArtifact extends AqlRowResult {
    String getBuildArtifactName();

    String getBuildArtifactType();
}
