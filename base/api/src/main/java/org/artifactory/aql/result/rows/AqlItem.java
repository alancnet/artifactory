package org.artifactory.aql.result.rows;


import org.artifactory.aql.model.AqlItemTypeEnum;

import java.util.Date;

import static org.artifactory.aql.model.AqlDomainEnum.items;
import static org.artifactory.aql.model.AqlFieldEnum.*;

/**
 * @author Gidi Shabat
 */
@QueryTypes(value = items, fields = {itemId, itemType, itemRepo, itemPath, itemName,
        itemDepth, itemCreated, itemCreatedBy, itemModified, itemModifiedBy, itemUpdated,
        itemSize, itemActualSha1, itemOriginalSha1, itemActualMd5, itemOriginalMd5})
public interface AqlItem extends AqlRowResult {
    Date getCreated();

    Date getModified();

    Date getUpdated();

    String getCreatedBy();

    String getModifiedBy();

    AqlItemTypeEnum getType();

    String getRepo();

    String getPath();

    String getName();

    long getSize();

    int getDepth();

    long getNodeId();

    String getOriginalMd5();

    String getActualMd5();

    String getOriginalSha1();

    String getActualSha1();
}
