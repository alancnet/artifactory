/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2012 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.artifactory.search.deployable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.module.ModuleInfo;
import org.artifactory.api.module.ModuleInfoBuilder;
import org.artifactory.api.module.VersionUnit;
import org.artifactory.api.search.VersionSearchResults;
import org.artifactory.api.search.deployable.VersionUnitSearchControls;
import org.artifactory.api.search.deployable.VersionUnitSearchResult;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.aql.AqlService;
import org.artifactory.aql.api.domain.sensitive.AqlApiItem;
import org.artifactory.aql.api.internal.AqlBase;
import org.artifactory.aql.model.AqlFieldEnum;
import org.artifactory.aql.result.AqlLazyResult;
import org.artifactory.aql.util.AqlSearchablePath;
import org.artifactory.aql.util.AqlUtils;
import org.artifactory.common.ConstantValues;
import org.artifactory.repo.Repo;
import org.artifactory.repo.RepoPath;
import org.artifactory.search.SearcherBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Holds the version unit search logic
 *
 * @author Noam Y. Tenne
 * @author Dan Feldman
 */
public class VersionUnitSearcher extends SearcherBase<VersionUnitSearchControls, VersionUnitSearchResult> {
    private static final Logger log = LoggerFactory.getLogger(VersionUnitSearcher.class);

    //The search will stop in any case if this threshold is exceeded (above the declared query limit)
    public static final int EXCEEDED_QUERY_THRESHOLD = 500;
    //5K (default) records limit
    public static final int QUERY_LIMIT = 5 * ConstantValues.searchUserQueryLimit.getInt();

    private AqlService aqlService;
    private AuthorizationService authorizationService;
    private HashMultimap<ModuleInfo, RepoPath> moduleInfoToRepoPaths = HashMultimap.create();
    private Repo repo = null;
    private boolean resultsWereFiltered = false;
    private boolean searchHadErrors = false;

    public VersionUnitSearcher(AqlService aqlService, AuthorizationService authorizationService) {
        super();
        this.aqlService = aqlService;
        this.authorizationService = authorizationService;
    }

    @Override
    public VersionSearchResults doSearch(VersionUnitSearchControls controls) {
        List<AqlSearchablePath> pathsToSearch = AqlUtils.getSearchablePathForCurrentFolderAndSubfolders(
                controls.getPathToSearchWithin());
        String repoKey = controls.getPathToSearchWithin().getRepoKey();
        repo = getRepoService().repositoryByKey(repoKey);
        if (repo == null || pathsToSearch.size() < 1) {
            log.error(repo == null ? "No such repo '" + repoKey + "' to search in"
                    : "path '" + controls.getPathToSearchWithin() + "' does not exist");
            return new VersionSearchResults(Sets.newHashSet(), 0, false, false, true);
        }
        List<RepoPath> results = searchPathsForFiles(pathsToSearch);
        results.stream().forEach(this::createModuleInfoAndInsertToMap);
        Set<VersionUnitSearchResult> versions = getVersionUnitResults();
        //Query exceeded allowed limit, should warn user
        boolean exceededLimit = results.size() > QUERY_LIMIT;
        return new VersionSearchResults(versions, versions.size(), resultsWereFiltered, exceededLimit, searchHadErrors);
    }

    private List<RepoPath> searchPathsForFiles(List<AqlSearchablePath> pathsToSearch) {
        List<RepoPath> foundPaths = Lists.newArrayList();
        AqlBase.OrClause pathSearchClause = AqlUtils.getSearchClauseForPaths(pathsToSearch);
        int currentRowNum = 0;
        AqlApiItem versionSearch = AqlApiItem.create().filter(pathSearchClause).sortBy(AqlFieldEnum.itemPath).asc();
        long start = System.currentTimeMillis();
        AqlLazyResult results = aqlService.executeQueryLazy(versionSearch);
        ResultSet resultSet = results.getResultSet();
        try {
            RepoPath lastPath = null;
            while (resultSet.next()) {
                String repo = resultSet.getString("repo");
                String path = resultSet.getString("node_path");
                String name = resultSet.getString("node_name");
                if (StringUtils.isBlank(repo) || StringUtils.isBlank(path) || StringUtils.isBlank(name)) {
                    log.debug("Got bad item info from query row: repo: {}, path: {}, name: {}", repo, path, name);
                    continue;
                }
                RepoPath currentPath = AqlUtils.fromAql(repo, path, name);
                currentRowNum++;
                if (shouldStopSearch(currentRowNum, lastPath, currentPath)) {
                    log.trace("Stopping version search, conditions met. current row is {}, limit is {}, " +
                            "current path is {}, last path was {}", currentRowNum, QUERY_LIMIT, currentPath, lastPath);
                    break;
                }
                lastPath = currentPath;
                foundPaths.add(currentPath);
            }
        } catch (Exception e) {
            log.error("Error executing version search query: {}", e.getMessage());
            log.debug("Caught exception while executing version search query: ", e);
            searchHadErrors = true;
        } finally {
            AqlUtils.closeResultSet(resultSet);
        }
        log.trace("Version search finished successfully, took {} ms", System.currentTimeMillis() - start);
        return foundPaths;
    }

    /**
     * This mechanism tries to include all found artifacts in the current path before terminating the search to
     * catch edge cases where the limit would cause only some of the version's files to be returned.
     * In any case going over the threshold will terminate the search so that it doesn't go over very very large trees.
     */
    private boolean shouldStopSearch(int currentRowNum, RepoPath lastPath, RepoPath currentPath) {
        //limit + 1 to signify search exceeded max allowable results
        return ((currentRowNum > QUERY_LIMIT + 1) && canStopSearch(lastPath, currentPath))
                || (currentRowNum > QUERY_LIMIT + EXCEEDED_QUERY_THRESHOLD);
    }

    private boolean canStopSearch(RepoPath lastPath, RepoPath currentPath) {
        if (lastPath == null || currentPath == null || lastPath.isRoot() || currentPath.isRoot()
                || lastPath.getParent() == null || currentPath.getParent() == null) {
            return false;
        }
        return !lastPath.getParent().getPath().equalsIgnoreCase(currentPath.getParent().getPath());
    }

    private void createModuleInfoAndInsertToMap(RepoPath path) {
        ModuleInfo moduleInfo = repo.getItemModuleInfo(path.getPath());
        if (moduleInfo.isValid()) {
            ModuleInfo stripped = stripModuleInfoFromUnnecessaryData(moduleInfo);
            moduleInfoToRepoPaths.put(stripped, path);
        }
    }

    private Set<VersionUnitSearchResult> getVersionUnitResults() {
        Set<VersionUnitSearchResult> searchResults = Sets.newHashSet();
        searchResults.addAll(moduleInfoToRepoPaths.keySet()
                .stream()
                .map(this::buildSearchResult)
                .filter(versionUnitSearchResult -> versionUnitSearchResult != null) //Build function might return nulls
                .collect(Collectors.toList()));
        return searchResults;
    }

    private VersionUnitSearchResult buildSearchResult(ModuleInfo moduleInfo) {
        Set<RepoPath> modulePaths = moduleInfoToRepoPaths.get(moduleInfo);
        //User doesn't have permissions to delete some \ all files of this module - don't return a result for it
        if (modulePaths.stream().filter(authorizationService::canDelete).count() != modulePaths.size()) {
            resultsWereFiltered = true; //warn user
            log.debug("Auth service filtered results for user {}, and module {}",
                    authorizationService.currentUsername(), moduleInfo.getPrettyModuleId());
            return null;
        }
        return new VersionUnitSearchResult(new VersionUnit(moduleInfo, Sets.newHashSet(modulePaths)));
    }

    private ModuleInfo stripModuleInfoFromUnnecessaryData(ModuleInfo moduleInfo) {
        ModuleInfoBuilder moduleInfoBuilder = new ModuleInfoBuilder().organization(moduleInfo.getOrganization()).
                module(moduleInfo.getModule()).baseRevision(moduleInfo.getBaseRevision());
        if (moduleInfo.isIntegration()) {
            String pathRevision = moduleInfo.getFolderIntegrationRevision();
            String artifactRevision = moduleInfo.getFileIntegrationRevision();

            boolean hasPathRevision = StringUtils.isNotBlank(pathRevision);
            boolean hasArtifactRevision = StringUtils.isNotBlank(artifactRevision);

            if (hasPathRevision && !hasArtifactRevision) {
                moduleInfoBuilder.folderIntegrationRevision(pathRevision);
                moduleInfoBuilder.fileIntegrationRevision(pathRevision);
            } else if (!hasPathRevision && hasArtifactRevision) {
                moduleInfoBuilder.fileIntegrationRevision(artifactRevision);
                moduleInfoBuilder.folderIntegrationRevision(artifactRevision);
            } else {
                moduleInfoBuilder.folderIntegrationRevision(pathRevision);
                moduleInfoBuilder.fileIntegrationRevision(artifactRevision);
            }
        }
        return moduleInfoBuilder.build();
    }
}
