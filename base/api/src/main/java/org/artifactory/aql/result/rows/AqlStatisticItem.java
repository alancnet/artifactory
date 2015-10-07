package org.artifactory.aql.result.rows;

import java.util.Date;

import static org.artifactory.aql.model.AqlDomainEnum.statistics;
import static org.artifactory.aql.model.AqlFieldEnum.*;

/**
 * @author Gidi Shabat
 */
@QueryTypes(value = statistics, fields = {itemId, itemType, itemRepo, itemPath, itemName,
        itemDepth, itemCreated, itemCreatedBy, itemModified, itemModifiedBy, itemUpdated,
        itemSize, itemActualSha1, itemOriginalSha1, itemActualMd5, itemOriginalMd5})
public interface AqlStatisticItem extends AqlItem {
    Date getDownloaded();

    int getDownloads();

    String getDownloadedBy();
}
