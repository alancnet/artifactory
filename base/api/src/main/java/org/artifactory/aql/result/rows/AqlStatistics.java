package org.artifactory.aql.result.rows;

import java.util.Date;

import static org.artifactory.aql.model.AqlDomainEnum.statistics;

/**
 * @author Gidi Shabat
 */
@QueryTypes(statistics)
public interface AqlStatistics extends AqlRowResult {
    Date getDownloaded();

    int getDownloads();

    String getDownloadedBy();
}
