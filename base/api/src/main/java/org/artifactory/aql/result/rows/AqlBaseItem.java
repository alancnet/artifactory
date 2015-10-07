package org.artifactory.aql.result.rows;

import org.artifactory.aql.model.AqlItemTypeEnum;

import static org.artifactory.aql.model.AqlDomainEnum.items;


/**
 * @author Gidi Shabat
 */
@QueryTypes(items)
public interface AqlBaseItem extends AqlRowResult {
    AqlItemTypeEnum getType();

    String getRepo();

    String getPath();

    String getName();
}
