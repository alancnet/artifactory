package org.artifactory.repo.service;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.artifactory.api.common.MoveMultiStatusHolder;
import org.artifactory.api.maven.MavenMetadataService;
import org.artifactory.api.repo.CaseSensitivityRepairService;
import org.artifactory.api.rest.artifact.RepairPathConflictsResult;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.fs.ItemInfo;
import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.Repo;
import org.artifactory.repo.RepoPath;
import org.artifactory.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

import static org.artifactory.api.rest.artifact.RepairPathConflictsResult.PathConflict;

/**
 * @author Yoav Luft
 */
@Service
public class CaseSensitivityRepairServiceImpl implements CaseSensitivityRepairService {

    private static final Logger log = LoggerFactory.getLogger(CaseSensitivityRepairServiceImpl.class);

    @Autowired
    private InternalRepositoryService repositoryService;

    @Autowired
    private MavenMetadataService mavenMetadataService;

    @Override
    public RepairPathConflictsResult findPathConflicts(String path) {
        RepoPath basePath;
        Repo repo;
        try {
            basePath = InternalRepoPathFactory.create(path);
            repo = repositoryService.getRepoRepoPath(basePath).getRepo();
        } catch (Exception e) {
            return new RepairPathConflictsResult("Invalid path: " + path);
        }
        if (!repo.isLocal()) {
            return new RepairPathConflictsResult("Error: Can only repair paths of local repositories");
        }
        Stack<ItemInfo> parents = new Stack<>();
        List<PathConflict> conflictingPaths = Lists.newArrayList();
        log.info("Looking for path conflicts in {}", basePath);
        if (repositoryService.exists(basePath)) {
            findPathConflictsInternal(repositoryService.getItemInfo(basePath), parents, conflictingPaths);
            return new RepairPathConflictsResult(conflictingPaths, "Completed");
        } else {
            log.warn("{} is not a valid path", basePath);
            return new RepairPathConflictsResult("Invalid path: " + path);
        }
    }

    /**
     * Recursive method for iterating depth first the whole node tree, adding any conflicts to conflictingPaths.
     * The parents stack is used to identify the real path for each node.
     * @param currentItem
     * @param parents
     * @param conflictingPaths
     */
    private void findPathConflictsInternal(ItemInfo currentItem, Stack<ItemInfo> parents,
            List<PathConflict> conflictingPaths) {
        log.trace("Recursing into {}", currentItem.getRepoPath());
        addConflicts(currentItem, parents, conflictingPaths);
        if (currentItem.isFolder()) {
            parents.push(currentItem);
            List<ItemInfo> children = repositoryService.getChildren(currentItem.getRepoPath());
            findConflictInSiblings(children, conflictingPaths);
            for (ItemInfo child : children) {
                findPathConflictsInternal(child, parents, conflictingPaths);
            }
            parents.pop();
        }
    }

    /**
     * Determines whether an item is in conflict with the parents stack.
     * @param itemInfo
     * @param parents
     * @return
     */
    private void addConflicts(ItemInfo itemInfo, Stack<ItemInfo> parents, List<PathConflict> pathConflicts) {
        PathConflict conflict = null;
        for (ItemInfo parent : parents) {
            if (!itemInfo.getRelPath().startsWith(parent.getRelPath())) {
                log.debug("Found conflict in {} with it's parent {}", itemInfo.getRepoPath(), parent.getRepoPath());
                if (conflict == null) {
                    conflict = new PathConflict(itemInfo.getRepoPath().toPath());
                    pathConflicts.add(conflict);
                }
                conflict.add(parent.getRepoPath().toPath());
            }
        }
    }

    /**
     * Finds sibling nodes which only differ from each other by their letters case.
     *
     * Search is done by ordering the siblings by name in a case insensitive way,
     * and then scanning for sequences of case insensitive equivalent names.
     * @param siblings
     * @param pathConflicts
     */
    private void findConflictInSiblings(List<ItemInfo> siblings, List<PathConflict> pathConflicts) {
        // We are aggregating all itemInfos with a lowercase representative,
        // creating sets of case-insensitive equivalent names.
        Multimap<String, ItemInfo> multiMap = Multimaps.index(siblings, new Function<ItemInfo, String>() {
            @Override
            public String apply(ItemInfo item) {
                return item.getName().toLowerCase();
            }
        });
        // For every set which is larger than 1 (Thus has a conflict) we use the first name for the conflict path and
        // append the other names as conflicting paths
        for (String key : multiMap.keySet()) {
            Collection<ItemInfo> itemInfos = multiMap.get(key);
            if (itemInfos.size() > 1) {
                PathConflict conflict = null;
                for (ItemInfo item : itemInfos) {
                    if (conflict == null) {
                        conflict = new PathConflict(item.getRepoPath().toPath());
                    } else {
                        conflict.add(item.getRepoPath().toPath());
                    }
                }
                pathConflicts.add(conflict);
            }
        }
    }

    @Override
    public RepairPathConflictsResult fixCaseConflicts(List<PathConflict> conflicts) {
        RepairPathConflictsResult result = new RepairPathConflictsResult(conflicts, "Successfully repaired paths");
        if (!canFixOnRepo(conflicts, result)) {
            return result;
        }
        if (conflicts.isEmpty()) {
            return new RepairPathConflictsResult("No conflicts to repair");
        }
        MoveMultiStatusHolder aggregatedStatus = new MoveMultiStatusHolder();
        // Sorting the list of files repair will put shorter path (that is, higher in hierarchy first. When fixing a
        // path conflict all the same conflict will be fix in the whole hierarchy, possible saving operations.
        Collections.sort(conflicts, new Comparator<PathConflict>() {
            @Override
            public int compare(PathConflict p1, PathConflict p2) {
                return p1.getPath().compareToIgnoreCase(p2.getPath());
            }
        });
        for (PathConflict conflict : conflicts) {
            MoveMultiStatusHolder statusHolder;
            try {
                statusHolder = fixParentFileCaseConflict(conflict.getPath());
            } catch (Exception e) {
                result.message = "Unexpected exception caught while repairing " + conflict.getPath() + ", aborting.\n" +
                        "Message: " + e.getMessage();
                return result;
            }
            if (statusHolder.isError()) {
                log.error("Error repairing path for {}: {}", conflict, statusHolder.getLastError());
                log.debug("Error repairing path for" + conflict, statusHolder.getException());
                result.message = "Encountered errors while repairing paths. Please see logs for details";
            }
            aggregatedStatus.merge(statusHolder);
            result.numRepaired++;
        }
        // We avoid calculation of maven metadata while fixing the path, so we call for calculation now.
        for (RepoPath candidate : aggregatedStatus.getCandidatesForMavenMetadataCalculation()) {
            mavenMetadataService.calculateMavenMetadataAsync(candidate, true);
        }
        return result;
    }

    @Override
    public List<ItemInfo> getOrphanItems(String path) {
        RepoPath basePath;
        Repo repo;
        try {
            basePath = InternalRepoPathFactory.create(path);
            repo = repositoryService.getRepoRepoPath(basePath).getRepo();
        } catch (Exception e) {
            log.error("Invalid path: " + path);
            return Lists.newArrayList();
        }
        if (!repo.isLocal()) {
            log.error("Error: Can only repair paths of local repositories");
            return Lists.newArrayList();
        }

        return repositoryService.getOrphanItems(basePath);
    }

    /**
     * Returns whether the repository can be repaired. If not, a proper message will be set in the supplied result
     * @param conflicts
     * @return
     */
    private boolean canFixOnRepo(List<PathConflict> conflicts, @Nonnull RepairPathConflictsResult result) {
        if (conflicts.isEmpty()) {
            return true;
        }
        RepoPath firstPath = InternalRepoPathFactory.create(conflicts.get(0).getPath());
        Repo repo = repositoryService.getRepoRepoPath(firstPath).getRepo();
        if (repo.isCache()) {
            log.error("Cannot repair paths on cache repositories, please repair the source repository");
            result.message = "Error: Cannot repair paths on cache repositories, please repair the source repository";
            return false;
        }
        if (repo.isLocal() && !((LocalRepoDescriptor) repo.getDescriptor()).isSuppressPomConsistencyChecks()) {
            log.error("\"Suppress POM Consistency Checks\" must be checked for repairing paths. " +
                    "It can be unchecked after paths have been repaired");
            result.message = "Error: \"Suppress POM Consistency Checks\" must be checked for repairing paths. " +
                    "It can be unchecked after paths have been repaired";
            return false;
        }
        return true;
    }

    /**
     * Repairing is done by iterating over the paths parents starting from the nearest ancestors and going to root,
     * and for each parent we replace the target path's prefix with the parent's true path.
     * Since every parent has at the least it's correct name (it's last path element) the process will leave us with the
     * correct path.
     * @param path
     * @return
     */
    private MoveMultiStatusHolder fixParentFileCaseConflict(String path) {
        RepoPath repoPath = InternalRepoPathFactory.create(path);
        RepoPath parent = repoPath;
        StringBuilder repairedPathBuilder = new StringBuilder(parent.getPath());
        while ((parent = parent.getParent()) != null) {
            RepoPath realParent = repositoryService.getItemInfo(parent).getRepoPath();
            String realParentPath = realParent.getPath();
            if (!repairedPathBuilder.toString().startsWith(realParentPath)) {
                log.debug("Fixing path for {} to match parent's path {}", path, realParentPath);
                repairedPathBuilder.replace(0, realParentPath.length() + 1, realParentPath + "/");
            }
        }
        String repairedPath = PathUtils.formatPath(repairedPathBuilder.toString());
        String tempSuffix = "_conflictFixTemporaryMarker";
        // If a parent path has been repaired, this path might have also been repaired and no operation is required
        if (repairedPath.equals(repositoryService.getItemInfo(repoPath).getRelPath())) {
            return new MoveMultiStatusHolder();
        }
        // Move works by copy & delete process, so we need to move to a temporary location and then move back.
        repositoryService.moveWithoutMavenMetadata(repoPath, new RepoPathImpl(repoPath.getRepoKey(), repairedPath + tempSuffix),
                false, true, true);
        MoveMultiStatusHolder move = repositoryService.moveWithoutMavenMetadata(
                new RepoPathImpl(repoPath.getRepoKey(), repairedPath + tempSuffix),
                new RepoPathImpl(repoPath.getRepoKey(), repairedPath), false, true, true);
        return move;
    }
}
