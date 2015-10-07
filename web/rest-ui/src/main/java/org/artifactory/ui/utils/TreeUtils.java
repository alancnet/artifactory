package org.artifactory.ui.utils;

import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.SnapshotVersionBehavior;

import static org.artifactory.mime.MavenNaming.isNonUniqueSnapshot;
import static org.artifactory.mime.MavenNaming.isUniqueSnapshot;

/**
 * @author Noam Y. Tenne
 */
public class TreeUtils {

    /**
     * Indicates whether a link to the tree item of the deployed artifact should be provided. Links should be
     * provided if deploying a snapshot file to repository with different snapshot version policy.
     *
     * @param repo
     * @param artifactPath The artifact deploy path
     * @return True if should provide the link
     */
    public static boolean shouldProvideTreeLink(LocalRepoDescriptor repo, String artifactPath) {
        SnapshotVersionBehavior repoSnapshotBehavior = repo.getSnapshotVersionBehavior();

        boolean uniqueToNonUnique = isUniqueSnapshot(artifactPath)
                && SnapshotVersionBehavior.NONUNIQUE.equals(repoSnapshotBehavior);

        boolean nonUniqueToNonUnique = isNonUniqueSnapshot(artifactPath)
                && SnapshotVersionBehavior.UNIQUE.equals(repoSnapshotBehavior);

        return !uniqueToNonUnique && !nonUniqueToNonUnique;
    }
}
