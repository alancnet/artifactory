package org.artifactory.search;

import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.search.ItemSearchResult;
import org.artifactory.api.search.SavedSearchResults;
import org.artifactory.fs.FileInfo;
import org.artifactory.fs.ItemInfo;
import org.artifactory.fs.MutableFileInfo;
import org.artifactory.repo.RepoPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author chen keinan
 */
public abstract class SearchTreeBuilder {

    private static final Logger log = LoggerFactory.getLogger(SearchTreeBuilder.class);

    private SearchTreeBuilder() {
        // utility class
    }

    public static SavedSearchResults buildFullArtifactsList(String name, List<? extends ItemSearchResult> searchResults,
            boolean completeVersion) {
        List<org.artifactory.fs.FileInfo> artifacts = removeFolders(searchResults);
        removeDuplicateFiles(artifacts);
        if (completeVersion) {
            artifacts = getAllDeploymentUnitFiles(artifacts);
        }

        return new SavedSearchResults(name, artifacts);
    }

    public static List<org.artifactory.fs.FileInfo> removeFolders(List<? extends ItemSearchResult> searchResults) {
        List<org.artifactory.fs.FileInfo> artifacts = new ArrayList<>();
        for (ItemSearchResult result : searchResults) {
            ItemInfo itemInfo = result.getItemInfo();
            if (!itemInfo.isFolder()) {
                artifacts.add((FileInfo) itemInfo);
            }
        }
        return artifacts;
    }

    private static void removeDuplicateFiles(List<org.artifactory.fs.FileInfo> searchResults) {
        // the search might return the same files that exist under different repositories
        // we don't/can't display both so we remove the duplicates
        Map<String, FileInfo> searchResultByRelativePath = new HashMap<>();
        Iterator<FileInfo> iter = searchResults.iterator();
        while (iter.hasNext()) {
            org.artifactory.fs.FileInfo result = iter.next();
            String relativePath = result.getRepoPath().getPath();
            if (!searchResultByRelativePath.containsKey(relativePath)) {
                searchResultByRelativePath.put(relativePath, result);
            } else {
                iter.remove();
            }
        }
    }

    // the search tree will display all the files that belong to the same version unit of the result files.
    // for example if the user searched for pom files, the search tree will display the pom file and all the artifacts
    // under the same directory (same version unit)

    private static List<FileInfo> getAllDeploymentUnitFiles(List<org.artifactory.fs.FileInfo> results) {
        List<RepoPath> processedPaths = new ArrayList<>();
        List<FileInfo> allFiles = new ArrayList<>();
        for (org.artifactory.fs.FileInfo result : results) {
            RepoPath resultRepoPath = result.getRepoPath();
            RepoPath resultParentRepoPath = resultRepoPath.getParent(); // the version unit path
            if (!processedPaths.contains(resultParentRepoPath)) {
                // get all the files under the current version unit
                RepositoryService repoService = ContextHelper.get().getRepositoryService();

                List<ItemInfo> children = repoService.getChildren(resultParentRepoPath);
                for (ItemInfo child : children) {
                    /**
                     * It is unlikely to receive a folder that contains both artifacts and subfolders, but it can happen
                     * Protect in any case
                     */
                    if (child instanceof MutableFileInfo) {
                        allFiles.add((org.artifactory.fs.FileInfo) child);
                    }
                }
                processedPaths.add(resultParentRepoPath);
            }
        }
        return allFiles;
    }
}
