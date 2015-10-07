package org.artifactory.aql.result.rows;

import static org.artifactory.aql.model.AqlDomainEnum.archives;
import static org.artifactory.aql.model.AqlFieldEnum.*;

/**
 * @author Gidi Shabat
 */
@QueryTypes(value = archives, fields = {itemId, itemType, itemRepo, itemPath, itemName,
        itemDepth, itemCreated, itemCreatedBy, itemModified, itemModifiedBy, itemUpdated,
        itemSize, itemActualSha1, itemOriginalSha1, itemActualMd5, itemOriginalMd5})
public interface AqlArchiveItem extends AqlItem {
    String getEntryName();

    String getEntryPath();
}
