package org.artifactory.maven.snapshot;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.artifact.repository.metadata.Snapshot;
import org.apache.maven.artifact.repository.metadata.SnapshotVersion;
import org.artifactory.api.module.ModuleInfo;
import org.artifactory.mime.MavenNaming;
import org.artifactory.storage.fs.tree.ItemNode;

/**
 * @author Gidi Shabat
 */
public class BuildNumberSnapshotComparator implements SnapshotComparator {

    private static final BuildNumberSnapshotComparator instance = new BuildNumberSnapshotComparator();

    /**
     * The more specific, strings only version comparator
     */

    public static BuildNumberSnapshotComparator get() {
        return instance;
    }

    @Override
    public int compare(ItemNode o1, ItemNode o2) {
        int buildNumber1 = MavenNaming.getUniqueSnapshotVersionBuildNumber(o1.getName());
        int buildNumber2 = MavenNaming.getUniqueSnapshotVersionBuildNumber(o2.getName());
        return buildNumber1 - buildNumber2;
    }

    @Override
    public int compare(Snapshot o1, Snapshot o2) {
        int buildNumber1 = o1.getBuildNumber();
        int buildNumber2 = o2.getBuildNumber();
        return buildNumber1 - buildNumber2;
    }

    @Override
    public int compare(SnapshotVersion o1, SnapshotVersion o2) {
        int buildNumber1 = Integer.valueOf(StringUtils.substringAfterLast(o1.getVersion(), "-"));
        int buildNumber2 = Integer.valueOf(StringUtils.substringAfterLast(o2.getVersion(), "-"));
        return buildNumber1 - buildNumber2;
    }

    @Override
    public int compare(ModuleInfo folderItemModuleInfo, ModuleInfo latestSnapshotVersion) {
        int folderItemBuildNumber = Integer.parseInt(StringUtils.substringAfter(
                folderItemModuleInfo.getFileIntegrationRevision(), "-"));
        int latestSnapshotVersionBuildNumber = Integer.parseInt(StringUtils.substringAfter(
                latestSnapshotVersion.getFileIntegrationRevision(), "-"));
        return folderItemBuildNumber - latestSnapshotVersionBuildNumber;
    }
}
