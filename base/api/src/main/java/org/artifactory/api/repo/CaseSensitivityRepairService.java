package org.artifactory.api.repo;

import org.artifactory.api.rest.artifact.RepairPathConflictsResult;
import org.artifactory.fs.ItemInfo;

import java.util.List;

/**
 * @author Yoav Luft
 */
public interface CaseSensitivityRepairService {
    /**
     * Finds all paths rooted at basePath that have letter case conflicting names.
     *
     * @param path root path for search, only elements belonging to it's subtree will be searched
     * @return {@link RepairPathConflictsResult} element containing all found conflicts, their count and status message
     */
    RepairPathConflictsResult findPathConflicts(String path);

    /**
     * Attempts to automatically fix all parent-child path case conflicts for the given list of paths
     *
     * @param conflicts paths to be fixed
     * @return {@link RepairPathConflictsResult} element containing all found conflicts, their count, count of repaired
     * paths and a status message
     */
    RepairPathConflictsResult fixCaseConflicts(List<RepairPathConflictsResult.PathConflict> conflicts);

    List<ItemInfo> getOrphanItems(String path);
}
