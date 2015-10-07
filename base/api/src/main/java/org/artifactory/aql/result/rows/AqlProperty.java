package org.artifactory.aql.result.rows;

import static org.artifactory.aql.model.AqlDomainEnum.properties;

/**
 * @author Gidi Shabat
 */
@QueryTypes(properties)
public interface AqlProperty extends AqlRowResult {
    String getKey();

    String getValue();
}
