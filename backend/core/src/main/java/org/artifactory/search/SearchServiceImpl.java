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

package org.artifactory.search;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.repo.RepositoryBrowsingService;
import org.artifactory.api.repo.VirtualRepoItem;
import org.artifactory.api.rest.search.common.RestDateFieldName;
import org.artifactory.api.search.ItemSearchResults;
import org.artifactory.api.search.SearchControls;
import org.artifactory.api.search.VersionSearchResults;
import org.artifactory.api.search.archive.ArchiveSearchControls;
import org.artifactory.api.search.archive.ArchiveSearchResult;
import org.artifactory.api.search.artifact.ArtifactSearchControls;
import org.artifactory.api.search.artifact.ArtifactSearchResult;
import org.artifactory.api.search.artifact.ChecksumSearchControls;
import org.artifactory.api.search.deployable.VersionUnitSearchControls;
import org.artifactory.api.search.gavc.GavcSearchControls;
import org.artifactory.api.search.gavc.GavcSearchResult;
import org.artifactory.api.search.property.PropertySearchControls;
import org.artifactory.api.search.property.PropertySearchResult;
import org.artifactory.api.search.stats.StatsSearchControls;
import org.artifactory.api.search.stats.StatsSearchResult;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.aql.AqlService;
import org.artifactory.build.BuildRun;
import org.artifactory.common.ConstantValues;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.fs.ItemInfo;
import org.artifactory.mime.MavenNaming;
import org.artifactory.mime.NamingUtils;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.LocalCacheRepo;
import org.artifactory.repo.LocalRepo;
import org.artifactory.repo.RemoteRepoBase;
import org.artifactory.repo.Repo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.sapi.common.RepositoryRuntimeException;
import org.artifactory.sapi.search.VfsQuery;
import org.artifactory.sapi.search.VfsQueryResult;
import org.artifactory.sapi.search.VfsQueryRow;
import org.artifactory.sapi.search.VfsQueryService;
import org.artifactory.schedule.CachedThreadPoolTaskExecutor;
import org.artifactory.search.archive.ArchiveSearcher;
import org.artifactory.search.archive.ArchiveSearcherAql;
import org.artifactory.search.build.BuildSearcher;
import org.artifactory.search.deployable.VersionUnitSearcher;
import org.artifactory.search.fields.FieldNameConverter;
import org.artifactory.search.gavc.GavcSearcher;
import org.artifactory.search.property.PropertySearcherAql;
import org.artifactory.search.stats.LastDownloadedSearcher;
import org.artifactory.security.AccessLogger;
import org.artifactory.spring.Reloadable;
import org.artifactory.util.GlobalExcludes;
import org.artifactory.util.PathMatcher;
import org.artifactory.version.CompoundVersionDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.artifactory.sapi.search.VfsBoolType.OR;
import static org.artifactory.sapi.search.VfsComparatorType.*;
import static org.artifactory.sapi.search.VfsQueryResultType.FILE;

/**
 * @author Frederic Simon
 * @author Yoav Landman
 */
@Service
@Reloadable(beanClass = InternalSearchService.class, initAfter = {InternalRepositoryService.class})
public class SearchServiceImpl implements InternalSearchService {
    private static final Logger log = LoggerFactory.getLogger(SearchServiceImpl.class);

    @Autowired
    private VfsQueryService vfsQueryService;

    @Autowired
    private InternalRepositoryService repoService;

    @Autowired
    private RepositoryBrowsingService repoBrowsingService;

    @Autowired
    private AuthorizationService authService;

    @Autowired
    private CachedThreadPoolTaskExecutor executor;

    @Autowired
    AqlService aqlService;

    @Override
    public ItemSearchResults<ArtifactSearchResult> searchArtifacts(ArtifactSearchControls controls) {
        if (shouldReturnEmptyResults(controls)) {
            return new ItemSearchResults<>(Lists.<ArtifactSearchResult>newArrayList());
        }
        ArtifactSearcher searcher = new ArtifactSearcher();
        ItemSearchResults<ArtifactSearchResult> results = searcher.search(controls);
        return results;
    }

    @Override
    public Set<RepoPath> searchArtifactsByChecksum(ChecksumSearchControls searchControls) {
        ArtifactSearcher searcher = new ArtifactSearcher();
        Collection<ItemInfo> itemInfos = searcher.searchArtifactsByChecksum(searchControls);
        Set<RepoPath> results = Sets.newHashSet(
                Iterables.transform(itemInfos, new Function<ItemInfo, RepoPath>() {
                    @Override
                    public RepoPath apply(@Nullable ItemInfo input) {
                        return input == null ? null : input.getRepoPath();
                    }
                })
        );
        return results;
    }

    @Override
    public ItemSearchResults<ArtifactSearchResult> getArtifactsByChecksumResults(ChecksumSearchControls searchControls) {
        List<ArtifactSearchResult> resultList = Lists.newArrayList();
        ArtifactSearcher searcher = new ArtifactSearcher();
        Collection<ItemInfo> itemInfos = searcher.searchArtifactsByChecksum(searchControls);
        for (ItemInfo item : itemInfos) {
            resultList.add(new ArtifactSearchResult(item));
        }
        return new ItemSearchResults<>(resultList, resultList.size());
    }

    @Override
    public ItemSearchResults<ArchiveSearchResult> searchArchiveContent(ArchiveSearchControls controls) {
        if (shouldReturnEmptyResults(controls)) {
            return new ItemSearchResults<>(Lists.<ArchiveSearchResult>newArrayList());
        }
        ArchiveSearcher searcher = new ArchiveSearcher();
        ItemSearchResults<ArchiveSearchResult> results = searcher.search(controls);
        return results;
    }

    @Override
    public ItemSearchResults<ArchiveSearchResult> searchArchiveContentAql(ArchiveSearchControls controls) {
        if (shouldReturnEmptyResults(controls)) {
            return new ItemSearchResults<>(Lists.<ArchiveSearchResult>newArrayList());
        }
        ArchiveSearcherAql searcher = new ArchiveSearcherAql();
        ItemSearchResults<ArchiveSearchResult> results = searcher.search(controls);
        return results;
    }

    @Override
    public ItemSearchResults<StatsSearchResult> searchArtifactsNotDownloadedSince(StatsSearchControls controls) {
        if (shouldReturnEmptyResults(controls)) {
            return new ItemSearchResults<>(Lists.<StatsSearchResult>newArrayList());
        }
        return new LastDownloadedSearcher().search(controls);
    }

    @Override
    public ItemSearchResults<GavcSearchResult> searchGavc(GavcSearchControls controls) {
        if (shouldReturnEmptyResults(controls)) {
            return new ItemSearchResults<>(Lists.<GavcSearchResult>newArrayList());
        }

        GavcSearcher searcher = new GavcSearcher();
        ItemSearchResults<GavcSearchResult> results = searcher.search(controls);

        return results;
    }

    @Override
    public ItemSearchResults<PropertySearchResult> searchProperty(PropertySearchControls controls) {
        if (shouldReturnEmptyResults(controls)) {
            return new ItemSearchResults<>(Lists.<PropertySearchResult>newArrayList());
        }

        PropertySearcherAql searcher = new PropertySearcherAql();
        ItemSearchResults<PropertySearchResult> results = searcher.search(controls);

        return results;
    }

    @Override
    public ItemSearchResults<PropertySearchResult> searchPropertyAql(PropertySearchControls controls) {
        if (shouldReturnEmptyResults(controls)) {
            return new ItemSearchResults<>(Lists.<PropertySearchResult>newArrayList());
        }

        PropertySearcherAql searcher = new PropertySearcherAql();
        ItemSearchResults<PropertySearchResult> results = searcher.search(controls);

        return results;
    }

    @Override
    public ItemSearchResults<ArtifactSearchResult> searchArtifactsInRange(
            Calendar from,
            Calendar to,
            List<String> reposToSearch,
            RestDateFieldName... dates) {
        if (from == null && to == null) {
            log.warn("Received search artifacts in range with no range!");
            return new ItemSearchResults<>(Collections.<ArtifactSearchResult>emptyList(), 0L);
        }
        if (dates == null || dates.length == 0) {
            log.warn("Received search artifacts in range with no date field!");
            return new ItemSearchResults<>(Collections.<ArtifactSearchResult>emptyList(), 0);
        }

        // all artifactory files that were created or modified after input date
        VfsQuery query = vfsQueryService.createQuery().expectedResult(FILE)
                .setRepoKeys(reposToSearch)
                .name(MavenNaming.MAVEN_METADATA_NAME).comp(NOT_EQUAL);
        // If only one date make it simple
        if (dates.length == 1) {
            addDateRangeFilter(query, from, to, dates[0]);
        } else {
            query.startGroup();
            for (int i = 0; i < dates.length; i++) {
                RestDateFieldName date = dates[i];
                query.startGroup();
                addDateRangeFilter(query, from, to, date);
                // The last one is null end group
                if (i < dates.length - 1) {
                    query.endGroup(OR);
                } else {
                    query.endGroup(null);
                }
            }
            query.endGroup(null);
        }
        VfsQueryResult queryResult = query.execute(Integer.MAX_VALUE);
        // There are no limit here the the getCount is really the total amount
        List<ArtifactSearchResult> results = new ArrayList<>((int) queryResult.getCount());
        for (VfsQueryRow vfsQueryRow : queryResult.getAllRows()) {
            ItemInfo item = vfsQueryRow.getItem();
            if (isRangeResultValid(item.getRepoPath(), reposToSearch)) {
                results.add(new ArtifactSearchResult(item));
            }
        }
        return new ItemSearchResults<>(results, queryResult.getCount());
    }

    private void addDateRangeFilter(VfsQuery query, Calendar from, Calendar to, RestDateFieldName dateField) {
        if (from != null) {
            query.prop(FieldNameConverter.fromRest(dateField).propName).comp(GREATER_THAN).val(from);
        }
        if (to != null) {
            query.prop(FieldNameConverter.fromRest(dateField).propName).comp(LOWER_THAN_EQUAL).val(to);
        }
    }

    @Override
    public VersionSearchResults searchVersionUnits(VersionUnitSearchControls controls) {
        VersionUnitSearcher searcher = new VersionUnitSearcher(aqlService, authService);
        return searcher.doSearch(controls);
    }

    @Override
    public Set<BuildRun> getLatestBuilds() {
        BuildSearcher searcher = new BuildSearcher();
        try {
            return searcher.getLatestBuildsByName();
        } catch (Exception e) {
            throw new RepositoryRuntimeException(e);
        }
    }

    @Override
    public Set<BuildRun> findBuildsByArtifactChecksum(String sha1, String md5) {
        BuildSearcher searcher = new BuildSearcher();
        return searcher.findBuildsByArtifactChecksum(sha1, md5);
    }

    @Override
    public Set<BuildRun> findBuildsByDependencyChecksum(String sha1, String md5) {
        BuildSearcher searcher = new BuildSearcher();
        return searcher.findBuildsByDependencyChecksum(sha1, md5);
    }

    @Override
    public Set<String> searchArtifactsByPattern(String pattern) throws ExecutionException, InterruptedException,
            TimeoutException {
        if (StringUtils.isBlank(pattern)) {
            throw new IllegalArgumentException("Unable to search for an empty pattern");
        }

        pattern = pattern.trim();
        if (!pattern.contains(":")) {
            throw new IllegalArgumentException("Pattern must be formatted like [repo-key]:[pattern/to/search/for]");
        }

        if (pattern.contains("**")) {
            throw new IllegalArgumentException("Pattern cannot contain the '**' wildcard");
        }

        String[] patternTokens = StringUtils.split(pattern, ":", 2);
        String repoKey = patternTokens[0];

        final Repo repo = repoService.repositoryByKey(repoKey);

        if ((repo == null) || (patternTokens.length == 1) || (StringUtils.isBlank(patternTokens[1]))) {
            return Sets.newHashSet();
        }

        final String innerPattern = StringUtils.replace(patternTokens[1], "\\", "/");

        Callable<Set<String>> callable = new Callable<Set<String>>() {

            @Override
            public Set<String> call() throws Exception {
                Set<String> pathsToReturn = Sets.newHashSet();
                List<String> patternFragments = Lists.newArrayList(StringUtils.split(innerPattern, "/"));

                if (repo.isReal()) {
                    String repoKey;
                    if (repo.isLocal() || repo.isCache()) {
                        repoKey = repo.getKey();
                    } else {
                        LocalCacheRepo localCacheRepo = ((RemoteRepoBase) repo).getLocalCacheRepo();
                        if (localCacheRepo != null) {
                            repoKey = localCacheRepo.getKey();
                        } else {
                            repoKey = null;
                        }
                    }
                    if (repoKey != null) {
                        collectLocalRepoItemsRecursively(patternFragments, pathsToReturn,
                                InternalRepoPathFactory.create(repoKey, ""));
                    }
                } else {
                    collectVirtualRepoItemsRecursively(patternFragments, pathsToReturn,
                            InternalRepoPathFactory.create(repo.getKey(), ""));
                }
                return pathsToReturn;
            }
        };

        Future<Set<String>> future = executor.submit(callable);
        return future.get(ConstantValues.searchPatternTimeoutSecs.getLong(), TimeUnit.SECONDS);
    }

    private boolean shouldReturnEmptyResults(SearchControls controls) {
        return checkUnauthorized() || controls.isEmpty();
    }

    private boolean checkUnauthorized() {
        boolean unauthorized =
                !authService.isAuthenticated() || (authService.isAnonymous() && !authService.isAnonAccessEnabled());
        if (unauthorized) {
            AccessLogger.unauthorizedSearch();
        }
        return unauthorized;
    }

    @Override
    public void init() {
    }

    @Override
    public void reload(CentralConfigDescriptor oldDescriptor) {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void convert(CompoundVersionDetails source, CompoundVersionDetails target) {
    }

    /**
     * Recursively collect items matching a given pattern from a local repo
     *
     * @param patternFragments Accepted pattern fragments
     * @param pathsToReturn    Result path aggregator
     * @param repoPath         Repo path to search at
     */
    private void collectLocalRepoItemsRecursively(List<String> patternFragments, Set<String> pathsToReturn,
            RepoPath repoPath) {

        org.artifactory.fs.ItemInfo itemInfo = repoService.getItemInfo(repoPath);

        if (!patternFragments.isEmpty()) {

            if (itemInfo.isFolder()) {

                String firstFragment = patternFragments.get(0);
                if (StringUtils.isBlank(firstFragment)) {
                    return;
                }

                for (String childName : repoService.getChildrenNames(repoPath)) {

                    if (patternMatches(firstFragment, childName)) {

                        List<String> fragmentsToPass = Lists.newArrayList(patternFragments);
                        fragmentsToPass.remove(0);
                        collectLocalRepoItemsRecursively(fragmentsToPass, pathsToReturn,
                                InternalRepoPathFactory.create(repoPath, childName));
                    }
                }
            }
        } else if (!itemInfo.isFolder()) {
            pathsToReturn.add(repoPath.getPath());
        }
    }

    /**
     * Recursively collect items matching a given pattern from a virtual repo
     *
     * @param patternFragments Accepted pattern fragments
     * @param pathsToReturn    Result path aggregator
     * @param repoPath         Repo path to search at
     */
    private void collectVirtualRepoItemsRecursively(List<String> patternFragments, Set<String> pathsToReturn,
            RepoPath repoPath) {

        VirtualRepoItem itemInfo = repoBrowsingService.getVirtualRepoItem(repoPath);
        if (itemInfo == null) {
            return;
        }

        if (!patternFragments.isEmpty()) {

            if (itemInfo.isFolder()) {

                String firstFragment = patternFragments.get(0);
                if (StringUtils.isBlank(firstFragment)) {
                    return;
                }
                //TODO: [by tc] should not use the remote children
                for (VirtualRepoItem child : repoBrowsingService.getVirtualRepoItems(repoPath)) {

                    if (patternMatches(firstFragment, child.getName())) {

                        List<String> fragmentsToPass = Lists.newArrayList(patternFragments);
                        fragmentsToPass.remove(0);
                        collectVirtualRepoItemsRecursively(fragmentsToPass, pathsToReturn,
                                InternalRepoPathFactory.create(repoPath, child.getName()));
                    }
                }
            }
        } else if (!itemInfo.isFolder()) {
            pathsToReturn.add(repoPath.getPath());
        }
    }

    /**
     * Checks if the given repo-relative path matches any of the given accepted patterns
     *
     * @param includePattern Accepted pattern
     * @param path           Repo-relative path to check
     * @return True if the path matches any of the patterns
     */
    private boolean patternMatches(String includePattern, String path) {
        return PathMatcher.matches(path, Lists.newArrayList(includePattern), GlobalExcludes.getGlobalExcludes(), true);
    }

    /**
     * Indicates whether the range query result repo path is valid
     *
     * @param repoPath      Repo path of query result
     * @param reposToSearch Lists of repositories to search within
     * @return True if the repo path is valid and comes from a local repo
     */
    private boolean isRangeResultValid(RepoPath repoPath, List<String> reposToSearch) {
        if (repoPath == null) {
            return false;
        }
        if ((reposToSearch != null) && !reposToSearch.isEmpty()) {
            return true;
        }

        LocalRepo localRepo = repoService.localOrCachedRepositoryByKey(repoPath.getRepoKey());
        return (localRepo != null) && (!NamingUtils.isChecksum(repoPath.getPath()));
    }
}
