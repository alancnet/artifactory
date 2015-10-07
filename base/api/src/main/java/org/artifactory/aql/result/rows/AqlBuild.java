package org.artifactory.aql.result.rows;

import java.util.Date;

import static org.artifactory.aql.model.AqlDomainEnum.builds;

/**
 * @author Gidi Shabat
 */
@QueryTypes(builds)
public interface AqlBuild extends AqlRowResult {
    String getBuildUrl();

    String getBuildName();

    String getBuildNumber();

    Date getBuildCreated();

    String getBuildCreatedBy();

    Date getBuildModified();

    String getBuildModifiedBy();
}
