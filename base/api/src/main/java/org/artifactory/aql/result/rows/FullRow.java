package org.artifactory.aql.result.rows;

import org.artifactory.aql.model.AqlItemTypeEnum;

import java.util.Date;

/**
 * @author Gidi Shabat
 */
public interface FullRow {
    Date getCreated();

    Date getModified();

    Date getUpdated();

    String getCreatedBy();

    String getModifiedBy();

    Date getDownloaded();

    int getDownloads();

    String getDownloadedBy();

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

    String getKey();

    String getValue();

    String getEntryName();

    String getEntryPath();

    String getBuildModuleName();

    Long getBuildModuleId();

    String getBuildDependencyName();

    String getBuildDependencyScope();

    String getBuildDependencyType();

    String getBuildDependencySha1();

    String getBuildDependencyMd5();

    String getBuildArtifactName();

    String getBuildArtifactType();

    String getBuildArtifactSha1();

    String getBuildArtifactMd5();

    String getBuildPropKey();

    String getBuildPropValue();

    String getBuildUrl();

    String getBuildName();

    String getBuildNumber();

    Date getBuildCreated();

    String getBuildCreatedBy();

    Date getBuildModified();

    String getBuildModifiedBy();
}
