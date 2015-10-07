package org.artifactory.aql.result.rows;

import org.artifactory.aql.model.AqlFieldEnum;
import org.artifactory.aql.model.AqlItemTypeEnum;

import java.util.Date;
import java.util.Map;

import static org.artifactory.aql.model.AqlDomainEnum.items;
import static org.artifactory.aql.model.AqlFieldEnum.*;

/**
 * @author Gidi Shabat
 */
@QueryTypes(value = items,
        fields = {itemId, itemType, itemRepo, itemPath, itemName, itemDepth, itemCreated, itemCreatedBy,
                itemModified, itemModifiedBy, itemUpdated, itemSize, itemActualSha1, itemOriginalSha1, itemActualMd5,
                // stats
                statDownloaded, statDownloads, statDownloadedBy,
                // properties
                propertyKey, propertyValue,
                // archive entries
                archiveEntryName, archiveEntryPath,
                // builds
                moduleName, buildDependencyName, buildDependencyScope, buildDependencyType, buildDependencySha1,
                buildDependencyMd5, buildArtifactName, buildArtifactType, buildArtifactSha1, buildArtifactMd5, buildPropertyKey,
                buildPropertyValue, buildUrl, buildName, buildNumber, buildCreated, buildCreatedBy, buildModified, buildModifiedBy
        })
public class AqlBaseFullRowImpl
        implements AqlRowResult, FullRow, AqlItem, AqlBaseItem, AqlArchiveItem, AqlBuildArtifact, AqlBuildDependency,
        AqlProperty, AqlBuild, AqlStatisticItem, AqlBuildProperty, AqlStatistics,AqlBuildModule {

    Map<AqlFieldEnum, Object> map;

    public AqlBaseFullRowImpl(Map<AqlFieldEnum, Object> map) {
        this.map = map;
    }

    @Override
    public Date getCreated() {
        return (Date) map.get(AqlFieldEnum.itemCreated);
    }

    @Override
    public Date getModified() {
        return (Date) map.get(AqlFieldEnum.itemModified);
    }

    @Override
    public Date getUpdated() {
        return (Date) map.get(AqlFieldEnum.itemUpdated);
    }

    @Override
    public String getCreatedBy() {
        return (String) map.get(AqlFieldEnum.itemCreatedBy);
    }

    @Override
    public String getModifiedBy() {
        return (String) map.get(AqlFieldEnum.itemModifiedBy);
    }

    @Override
    public Date getDownloaded() {
        return (Date) map.get(AqlFieldEnum.statDownloaded);
    }

    @Override
    public int getDownloads() {
        return (int) map.get(AqlFieldEnum.statDownloads);
    }

    @Override
    public String getDownloadedBy() {
        return (String) map.get(AqlFieldEnum.statDownloadedBy);
    }

    @Override
    public AqlItemTypeEnum getType() {
        return (AqlItemTypeEnum) map.get(AqlFieldEnum.itemType);
    }

    @Override
    public String getRepo() {
        return (String) map.get(AqlFieldEnum.itemRepo);
    }

    @Override
    public String getPath() {
        return (String) map.get(AqlFieldEnum.itemPath);
    }

    @Override
    public String getName() {
        return (String) map.get(AqlFieldEnum.itemName);
    }

    @Override
    public long getSize() {
        return (long) map.get(AqlFieldEnum.itemSize);
    }

    @Override
    public int getDepth() {
        return (int) map.get(AqlFieldEnum.itemDepth);
    }

    @Override
    public long getNodeId() {
        return (int) map.get(AqlFieldEnum.itemId);
    }

    @Override
    public String getOriginalMd5() {
        return (String) map.get(AqlFieldEnum.itemOriginalMd5);
    }

    @Override
    public String getActualMd5() {
        return (String) map.get(AqlFieldEnum.itemActualMd5);
    }

    @Override
    public String getOriginalSha1() {
        return (String) map.get(AqlFieldEnum.itemOriginalSha1);
    }

    @Override
    public String getActualSha1() {
        return (String) map.get(AqlFieldEnum.itemActualSha1);
    }

    @Override
    public String getKey() {
        return (String) map.get(AqlFieldEnum.propertyKey);
    }

    @Override
    public String getValue() {
        return (String) map.get(AqlFieldEnum.propertyValue);
    }

    @Override
    public String getEntryName() {
        return (String) map.get(AqlFieldEnum.archiveEntryName);
    }

    @Override
    public String getEntryPath() {
        return (String) map.get(AqlFieldEnum.archiveEntryPath);
    }

    @Override
    public String getBuildModuleName() {
        return (String) map.get(AqlFieldEnum.moduleName);
    }

    @Override
    public Long getBuildModuleId() {
        return (Long) map.get(AqlFieldEnum.moduleId);
    }

    @Override
    public String getBuildDependencyName() {
        return (String) map.get(AqlFieldEnum.buildDependencyName);
    }

    @Override
    public String getBuildDependencyScope() {
        return (String) map.get(AqlFieldEnum.buildDependencyScope);
    }

    @Override
    public String getBuildDependencyType() {
        return (String) map.get(AqlFieldEnum.buildDependencyType);
    }

    @Override
    public String getBuildDependencySha1() {
        return (String) map.get(AqlFieldEnum.buildDependencySha1);
    }

    @Override
    public String getBuildDependencyMd5() {
        return (String) map.get(AqlFieldEnum.buildDependencyMd5);
    }

    @Override
    public String getBuildArtifactName() {
        return (String) map.get(AqlFieldEnum.buildArtifactName);
    }

    @Override
    public String getBuildArtifactType() {
        return (String) map.get(AqlFieldEnum.buildArtifactType);
    }

    @Override
    public String getBuildArtifactSha1() {
        return (String) map.get(AqlFieldEnum.buildArtifactSha1);
    }

    @Override
    public String getBuildArtifactMd5() {
        return (String) map.get(AqlFieldEnum.buildArtifactMd5);
    }

    @Override
    public String getBuildPropKey() {
        return (String) map.get(AqlFieldEnum.buildPropertyKey);
    }

    @Override
    public String getBuildPropValue() {
        return (String) map.get(AqlFieldEnum.buildPropertyValue);
    }

    @Override
    public String getBuildUrl() {
        return (String) map.get(AqlFieldEnum.buildUrl);
    }

    @Override
    public String getBuildName() {
        return (String) map.get(AqlFieldEnum.buildName);
    }

    @Override
    public String getBuildNumber() {
        return (String) map.get(AqlFieldEnum.buildNumber);
    }

    @Override
    public Date getBuildCreated() {
        return (Date) map.get(AqlFieldEnum.buildCreated);
    }

    @Override
    public String getBuildCreatedBy() {
        return (String) map.get(AqlFieldEnum.buildCreatedBy);
    }

    @Override
    public Date getBuildModified() {
        return (Date) map.get(AqlFieldEnum.buildModified);
    }

    @Override
    public String getBuildModifiedBy() {
        return (String) map.get(AqlFieldEnum.buildModifiedBy);
    }

    @Override
    public String toString() {
        return "AqlBaseFullRowImpl{map=" + map + "}";
    }
}
