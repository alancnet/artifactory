package org.artifactory.maven.snapshot;

import org.apache.maven.artifact.repository.metadata.Snapshot;
import org.apache.maven.artifact.repository.metadata.SnapshotVersion;
import org.artifactory.api.module.ModuleInfo;
import org.artifactory.storage.fs.tree.ItemNode;

import java.util.Comparator;

/**
 * @author Gidi Shabat
 */
public interface SnapshotComparator extends Comparator<ItemNode> {

    int compare(Snapshot o1, Snapshot o2);

    int compare(SnapshotVersion o1, SnapshotVersion o2);

    int compare(ModuleInfo folderItemModuleInfo, ModuleInfo latestSnapshotVersion);
}
