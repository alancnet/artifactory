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

package org.artifactory.build;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.thoughtworks.xstream.XStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.blackduck.BlackDuckAddon;
import org.artifactory.addon.license.LicensesAddon;
import org.artifactory.addon.plugin.PluginsAddon;
import org.artifactory.addon.plugin.build.AfterBuildSaveAction;
import org.artifactory.addon.plugin.build.BeforeBuildSaveAction;
import org.artifactory.api.build.BuildProps;
import org.artifactory.api.build.BuildRunComparators;
import org.artifactory.api.build.GeneralBuild;
import org.artifactory.api.build.ImportableExportableBuild;
import org.artifactory.api.build.ModuleArtifact;
import org.artifactory.api.build.ModuleDependency;
import org.artifactory.api.build.PublishedModule;
import org.artifactory.api.build.diff.BuildParams;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.rest.artifact.PromotionResult;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.aql.AqlConverts;
import org.artifactory.aql.AqlService;
import org.artifactory.aql.api.domain.sensitive.AqlApiBuild;
import org.artifactory.aql.api.domain.sensitive.AqlApiItem;
import org.artifactory.aql.api.internal.AqlBase;
import org.artifactory.aql.model.AqlComparatorEnum;
import org.artifactory.aql.model.AqlFieldEnum;
import org.artifactory.aql.result.AqlEagerResult;
import org.artifactory.aql.result.rows.AqlBaseFullRowImpl;
import org.artifactory.aql.util.AqlUtils;
import org.artifactory.common.MutableStatusHolder;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.factory.xstream.XStreamFactory;
import org.artifactory.fs.FileInfo;
import org.artifactory.repo.RepoPath;
import org.artifactory.sapi.common.ExportSettings;
import org.artifactory.sapi.common.ImportSettings;
import org.artifactory.spring.Reloadable;
import org.artifactory.storage.StorageException;
import org.artifactory.storage.build.service.BuildStoreService;
import org.artifactory.storage.db.DbService;
import org.artifactory.util.CollectionUtils;
import org.artifactory.version.CompoundVersionDetails;
import org.jfrog.build.api.Artifact;
import org.jfrog.build.api.Build;
import org.jfrog.build.api.BuildAgent;
import org.jfrog.build.api.BuildType;
import org.jfrog.build.api.Dependency;
import org.jfrog.build.api.Issue;
import org.jfrog.build.api.Issues;
import org.jfrog.build.api.Module;
import org.jfrog.build.api.release.Promotion;
import org.jfrog.build.api.release.PromotionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.getLast;
import static com.google.common.collect.Lists.newArrayList;
import static org.artifactory.aql.api.internal.AqlBase.and;

/**
 * Build service main implementation
 *
 * @author Noam Y. Tenne
 */
@Service
@Reloadable(beanClass = InternalBuildService.class, initAfter = {DbService.class})
public class BuildServiceImpl implements InternalBuildService {
    public static final String BUILDS_EXPORT_DIR = "builds";
    private static final Logger log = LoggerFactory.getLogger(BuildServiceImpl.class);
    @Autowired
    private AddonsManager addonsManager;

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private BuildStoreService buildStoreService;

    @Autowired
    private DbService dbService;

    @Autowired(required = false)
    private Builds builds;

    @Autowired
    private AqlService aqlService;

    private XStream buildXStream;

    @Override
    public void init() {
        buildXStream = XStreamFactory.create(ImportableExportableBuild.class);
    }

    @Override
    public void reload(CentralConfigDescriptor oldDescriptor) {
        buildXStream = XStreamFactory.create(ImportableExportableBuild.class);
    }

    @Override
    public void destroy() {
        //Nothing to destroy
    }

    @Override
    public void convert(CompoundVersionDetails source, CompoundVersionDetails target) {
    }

    @Override
    public void addBuild(@Nonnull DetailedBuildRun detailedBuildRun) {
        getTransactionalMe().addBuild(((DetailedBuildRunImpl) detailedBuildRun).build, false);
    }

    @Override
    public void addBuild(final Build build) {
        getTransactionalMe().addBuild(build, true);
    }

    @Override
    public void addBuild(final Build build, boolean activateCallbacks) {
        String buildName = build.getName();
        String buildNumber = build.getNumber();
        String buildStarted = build.getStarted();
        String currentUser = authorizationService.currentUsername();

        log.debug("Adding info for build '{}' #{} d{}", buildName, buildNumber, buildStarted);

        build.setArtifactoryPrincipal(currentUser);
        buildStoreService.populateMissingChecksums(build);

        aggregatePreviousBuildIssues(build);

        DetailedBuildRun detailedBuildRun = new DetailedBuildRunImpl(build);
        PluginsAddon pluginsAddon = addonsManager.addonByType(PluginsAddon.class);
        pluginsAddon.execPluginActions(BeforeBuildSaveAction.class, builds, detailedBuildRun);

        buildStoreService.addBuild(build);

        log.debug("Added info for build '{}' #{}", buildName, buildNumber);

        log.debug("Running License check on build '{}' #{}", buildName, buildNumber);
        LicensesAddon licensesAddon = addonsManager.addonByType(LicensesAddon.class);
        licensesAddon.performOnBuildArtifacts(build);

        log.debug("Governance check will attempt to run (if enabled) on build '{}' #{}", buildName, buildNumber);
        BlackDuckAddon blackDuckAddon = addonsManager.addonByType(BlackDuckAddon.class);
        blackDuckAddon.performBlackDuckOnBuildArtifacts(build);

        //Abort calling plugin actions if called from papi to avoid accidental endless loops
        if (activateCallbacks) {
            pluginsAddon.execPluginActions(AfterBuildSaveAction.class, builds, detailedBuildRun);
        }
    }

    /**
     * Check if the latest build available has issues, add them all the our newly created build
     * only if the previous build status is not in "Released" status (configurable status from our plugins).
     * This way we collect all previous issues related to the same version which is not released yet.
     *
     * @param newBuild the newly created build to add previous issues to
     */
    private void aggregatePreviousBuildIssues(Build newBuild) {
        Issues newBuildIssues = newBuild.getIssues();
        if (newBuildIssues == null) {
            return;
        }

        if (!newBuildIssues.isAggregateBuildIssues()) {
            return;
        }

        Build latestBuild = getLatestBuildByNameAndStatus(newBuild.getName(), LATEST_BUILD);
        if (latestBuild == null) {
            return;
        }

        // Only aggregate if the previous build does not equal to the user requested status (e.g: "Released")
        // this way we only aggregate the issues related to the current release
        List<PromotionStatus> statuses = latestBuild.getStatuses();
        if (statuses != null) {
            String aggregationBuildStatus = newBuildIssues.getAggregationBuildStatus();
            for (PromotionStatus status : statuses) {
                if (status.getStatus().equalsIgnoreCase(aggregationBuildStatus)) {
                    return;
                }
            }
        }

        // It is important to create new Issue instance so we won't mess up previous ones
        Issues previousIssues = latestBuild.getIssues();
        if (previousIssues != null) {
            Set<Issue> affectedIssues = previousIssues.getAffectedIssues();
            if (affectedIssues != null) {
                for (Issue issue : affectedIssues) {
                    Issue issueToAdd = new Issue(issue.getKey(), issue.getUrl(), issue.getSummary());
                    issueToAdd.setAggregated(true);
                    newBuildIssues.addIssue(issueToAdd);
                }
            }
        }
    }

    @Override
    public BuildRun getBuildRun(String buildName, String buildNumber, String buildStarted) {
        return buildStoreService.getBuildRun(buildName, buildNumber, buildStarted);
    }

    @Override
    public Build getBuild(BuildRun buildRun) {
        return buildStoreService.getBuildJson(buildRun);
    }

    @Override
    public String getBuildAsJson(BuildRun buildRun) {
        return buildStoreService.getBuildAsJson(buildRun);
    }

    @Override
    public void deleteBuild(String buildName, boolean deleteArtifacts, BasicStatusHolder multiStatusHolder) {
        if (deleteArtifacts) {
            Set<BuildRun> existingBuilds = searchBuildsByName(buildName);
            for (BuildRun existingBuild : existingBuilds) {
                removeBuildArtifacts(existingBuild, multiStatusHolder);
            }
        }
        buildStoreService.deleteAllBuilds(buildName);
    }

    @Override
    public void deleteBuild(BuildRun buildRun, boolean deleteArtifacts, BasicStatusHolder multiStatusHolder) {
        multiStatusHolder.debug("Starting to remove build '" + buildRun.getName() +
                "' #" + buildRun.getNumber(), log);
        String buildName = buildRun.getName();
        if (deleteArtifacts) {
            removeBuildArtifacts(buildRun, multiStatusHolder);
        }
        buildStoreService.deleteBuild(buildName, buildRun.getNumber(), buildRun.getStarted());
        Set<BuildRun> remainingBuilds = searchBuildsByName(buildName);
        if (remainingBuilds.isEmpty()) {
            deleteBuild(buildName, false, multiStatusHolder);
        }
        multiStatusHolder.debug("Finished removing build '" + buildRun.getName() +
                "' #" + buildRun.getNumber(), log);
    }

    private void removeBuildArtifacts(BuildRun buildRun, BasicStatusHolder status) {
        String buildName = buildRun.getName();
        String buildNumber = buildRun.getNumber();
        Build build = getBuild(buildRun);
        status.debug("Starting to remove the artifacts of build '" + buildName + "' #" + buildNumber, log);
        Set<ArtifactoryBuildArtifact> buildArtifactsInfos = getBuildArtifactsFileInfos(build, false, StringUtils.EMPTY);
        for (ArtifactoryBuildArtifact artifact : buildArtifactsInfos) {
            if (artifact.getFileInfo() != null) {
                RepoPath repoPath = artifact.getFileInfo().getRepoPath();
                BasicStatusHolder undeployStatus = repositoryService.undeploy(repoPath, true, true);
                status.merge(undeployStatus);
            }
        }
        status.debug("Finished removing the artifacts of build '" + buildName + "' #" + buildNumber, log);
    }

    @Override
    public Build getLatestBuildByNameAndStatus(String buildName, final String buildStatus) {
        if (StringUtils.isBlank(buildName)) {
            return null;
        }
        if (StringUtils.isBlank(buildStatus)) {
            return null;
        }
        //let's find all builds
        Set<BuildRun> buildsByName = searchBuildsByName(buildName);
        if (buildsByName == null || buildsByName.isEmpty()) { //no builds - no glory
            return null;
        }
        List<BuildRun> buildRuns = newArrayList(buildsByName);
        Collections.sort(buildRuns, BuildRunComparators.getComparatorFor(buildRuns));
        BuildRun latestBuildRun;

        if (buildStatus.equals(LATEST_BUILD)) {
            latestBuildRun = getLast(buildRuns, null);
        } else {
            latestBuildRun = getLast(filter(buildRuns, new Predicate<BuildRun>() {
                @Override
                public boolean apply(BuildRun buildRun) {
                    // Search for the latest build by the given status
                    return buildStatus.equals(buildRun.getReleaseStatus());
                }
            }), null);

        }
        return latestBuildRun == null ? null :
                getBuild(latestBuildRun);
    }

    @Override
    public
    @Nullable
    Build getLatestBuildByNameAndNumber(String buildName, String buildNumber) {
        if (StringUtils.isBlank(buildName)) {
            return null;
        }
        return buildStoreService.getLatestBuild(buildName, buildNumber);
    }

    @Override
    public Set<BuildRun> searchBuildsByName(String buildName) {
        return buildStoreService.findBuildsByName(buildName);
    }

    @Override
    public List<String> getBuildNames() {
        List<String> allBuildNames = buildStoreService.getAllBuildNames();
        Collections.sort(allBuildNames, null);
        return allBuildNames;
    }

    @Override
    public List<BuildRun> getAllPreviousBuilds(String buildName, String buildNumber, String buildStarted) {
        final BuildRun currentBuildRun = getTransactionalMe().getBuildRun(buildName, buildNumber, buildStarted);
        Set<BuildRun> buildRuns = searchBuildsByName(buildName);
        final Comparator<BuildRun> buildNumberComparator = BuildRunComparators.getBuildStartDateComparator();
        Iterables.removeIf(buildRuns, new Predicate<BuildRun>() {
            @Override
            public boolean apply(@Nullable BuildRun input) {
                // Remove all builds equals or after the current one
                return buildNumberComparator.compare(currentBuildRun, input) <= 0;
            }
        });

        List<BuildRun> buildRunsList = Lists.newArrayList(buildRuns);
        Comparator<BuildRun> reverseComparator = Collections.reverseOrder(buildNumberComparator);
        Collections.sort(buildRunsList, reverseComparator);

        return buildRunsList;
    }

    @Override
    public Set<BuildRun> searchBuildsByNameAndNumber(String buildName, String buildNumber) {
        return buildStoreService.findBuildsByNameAndNumber(buildName, buildNumber);
    }

    @Override
    public Set<ArtifactoryBuildArtifact> getBuildArtifactsFileInfos(Build build, boolean useFallBack,
            String sourceRepo) {
        AqlBase.AndClause and = and();
        log.debug("Executing Artifacts search for build {}:{}", build.getName(), build.getNumber());
        if (StringUtils.isNotBlank(sourceRepo)) {
            log.debug("Search limited to repo: {}", sourceRepo);
            and.append(
                    AqlApiItem.repo().equal(sourceRepo)
            );
        }
        and.append(AqlApiItem.property().property("build.name", AqlComparatorEnum.equals, build.getName()));
        and.append(AqlApiItem.property().property("build.number", AqlComparatorEnum.equals, build.getNumber()));
        AqlBase buildArtifactsQuery = AqlApiItem.create().filter(and);

        AqlEagerResult<AqlBaseFullRowImpl> aqlResult = aqlService.executeQueryEager(buildArtifactsQuery);
        log.debug("Search returned {} artifacts", aqlResult.getSize());
        Multimap<String, Artifact> buildArtifacts = BuildServiceUtils.getBuildArtifacts(build);
        log.debug("This build contains {} artifacts (taken from build info)", buildArtifacts.size());
        List<String> virtualRepoKeys = getVirtualRepoKeys();
        Set<ArtifactoryBuildArtifact> matchedArtifacts = matchArtifactsToFileInfos(aqlResult.getResults(),
                buildArtifacts, virtualRepoKeys);
        log.debug("Matched {} build artifacts to actual paths returned by search", matchedArtifacts.size());

        //buildArtifacts contains all remaining artifacts that weren't matched - match them with the weak search
        //only if indicated and if such remaining unmatched artifacts still exist in the map.
        if (useFallBack && !buildArtifacts.isEmpty()) {
            log.debug("Unmatched artifacts exist and 'use weak match fallback' flag is lit - executing weak match");
            Set<ArtifactoryBuildArtifact> weaklyMatchedArtifacts = matchUnmatchedArtifactsNonStrict(build, sourceRepo,
                    buildArtifacts, virtualRepoKeys);
            log.debug("Weak match has matched {} additional artifacts", weaklyMatchedArtifacts);
            matchedArtifacts.addAll(weaklyMatchedArtifacts);
        }
        //Lastly, populate matchedArtifacts with all remaining unmatched artifacts with null values to help users of
        //this function know if all build artifacts were found.
        log.debug("{} artifacts were not matched to actual paths", buildArtifacts.size());
        for (Artifact artifact : buildArtifacts.values()) {
            matchedArtifacts.add(new ArtifactoryBuildArtifact(artifact, null));
        }
        return matchedArtifacts;
    }

    /**
     * Matches FileInfos to build artifacts(created from an aql query's result) by checksum and by artifact name.
     * If indicated by {@param matchAnyChecksum} matches only by checksum if no exact match by name was found
     */
    /* This matching logic is kinda partial as we still can't match exactly sha1 to RepoPath because there's
    * not enough information from the BuildInfo (just sha1 and artifact id). It will not harm promotion \ push to
    * bintray etc. as ALL sha1's are still returned so nothing is skipped, this more of a UI issue that we are
    * currently reluctant to resolve (i.e. by introducing a unique id in the BuildInfo and put it as a property)
    */
    private Set<ArtifactoryBuildArtifact> matchArtifactsToFileInfos(List<AqlBaseFullRowImpl> queryResults,
            Multimap<String, Artifact> checksumToArtifactsMap, List<String> virtualRepoKeys) {
        //Map<Artifact, FileInfo> foundResults = Maps.newHashMap();
        Set<ArtifactoryBuildArtifact> results = Sets.newHashSet();
        for (final AqlBaseFullRowImpl result : queryResults) {
            //Don't include results from virtual repos
            if (!virtualRepoKeys.contains(result.getRepo())) {
                Collection<Artifact> artifacts = checksumToArtifactsMap.get(result.getActualSha1());
                if (CollectionUtils.notNullOrEmpty(artifacts)) {
                    //Try to match exactly by artifact name
                    try {
                        tryExactArtifactToFileInfoMatch(results, result, artifacts);
                    } catch (Exception e) {
                        //If no match just take the first artifact (it did match the checksum)
                        matchAnyArtifactToFileInfo(results, result, artifacts);
                    }
                }
            }
        }
        return results;
    }

    /**
     * Tries to match an artifact to a path based on it's name. If a match is found it is added to the result set
     * and removed from the liist
     */
    private void tryExactArtifactToFileInfoMatch(Set<ArtifactoryBuildArtifact> results, final AqlBaseFullRowImpl result,
            Collection<Artifact> artifacts) {
        Artifact idMatch = Iterables.find(artifacts, new Predicate<Artifact>() {
            @Override
            public boolean apply(Artifact input) {
                return input.getName() != null && input.getName().equals(result.getName());
            }
        });
        results.add(new ArtifactoryBuildArtifact(idMatch, (FileInfo) AqlConverts.toFileInfo.apply(result)));
        log.debug("Matched artifact {} to path {}", idMatch.getName(), AqlUtils.fromAql(result));
        artifacts.remove(idMatch);
    }

    private void matchAnyArtifactToFileInfo(Set<ArtifactoryBuildArtifact> results, AqlBaseFullRowImpl result,
            Collection<Artifact> artifacts) {
        Iterator<Artifact> artifactsIter = artifacts.iterator();
        Artifact matchedArtifact = artifactsIter.next();
        results.add(new ArtifactoryBuildArtifact(matchedArtifact, (FileInfo) AqlConverts.toFileInfo.apply(result)));
        log.debug("Matched artifact {} to path {}", matchedArtifact.getName(), AqlUtils.fromAql(result));
        //Remove artifact from list to ensure that we match everything
        artifactsIter.remove();
    }

    /**
     * The 'non-strict' variant of the artifact search is used as a fallback for artifacts that couldn't be matched by
     * the regular property based search.
     * AqlApiBuid.name() and AqlApiBuid.number() are considered 'weak' constraints as the linkage between aql domains is
     * performed by sha1 - so we might end up 'finding' the wrong artifact (meaning the wrong repoPath that has a
     * correct checksum - see todos above)
     */
    private Set<ArtifactoryBuildArtifact> matchUnmatchedArtifactsNonStrict(Build build, String sourceRepo,
            Multimap<String, Artifact> unmatchedArtifacts, List<String> virtualRepoKeys) {
        AqlBase.AndClause<AqlApiBuild> and = AqlApiBuild.and(
                AqlApiBuild.name().equal(build.getName()),
                AqlApiBuild.number().equal(build.getNumber())
        );
        log.debug("Executing 'non-strict' Artifacts search for build {}:{}", build.getName(), build.getNumber());
        if (StringUtils.isNotBlank(sourceRepo)) {
            log.debug("Search limited to repo: {}", sourceRepo);
            and.append(
                    AqlApiBuild.module().artifact().item().repo().equal(sourceRepo)
            );
        }
        AqlBase nonStrictQuery = AqlApiBuild.create().filter(and);
        nonStrictQuery.include(
                AqlApiBuild.module().artifact().item().sha1Actual(),
                AqlApiBuild.module().artifact().item().md5Actual(),
                AqlApiBuild.module().artifact().item().sha1Orginal(),
                AqlApiBuild.module().artifact().item().md5Orginal(),
                AqlApiBuild.module().artifact().item().created(),
                AqlApiBuild.module().artifact().item().modifiedBy(),
                AqlApiBuild.module().artifact().item().createdBy(),
                AqlApiBuild.module().artifact().item().updated(),
                AqlApiBuild.module().artifact().item().repo(),
                AqlApiBuild.module().artifact().item().path(),
                AqlApiBuild.module().artifact().item().size(),
                AqlApiBuild.module().artifact().item().name()
                //Ordering by the last updated field, in case of duplicates with the same checksum
                //Since this is match any checksum mode
        ).sortBy(AqlFieldEnum.itemUpdated).desc();


        AqlEagerResult<AqlBaseFullRowImpl> aqlResult = aqlService.executeQueryEager(nonStrictQuery);
        log.debug("Search returned {} artifacts", aqlResult.getSize());
        return matchArtifactsToFileInfos(aqlResult.getResults(), unmatchedArtifacts, virtualRepoKeys);
    }

    @Override
    public Map<Dependency, FileInfo> getBuildDependenciesFileInfos(Build build) {
        AqlBase.AndClause<AqlApiBuild> and = AqlApiBuild.and(
                AqlApiBuild.name().equal(build.getName()),
                AqlApiBuild.number().equal(build.getNumber())
        );
        log.debug("Executing dependencies search for build {}:{}", build.getName(), build.getNumber());

        AqlBase buildDependenciesQuery = AqlApiBuild.create().filter(and);
        buildDependenciesQuery.include(
                AqlApiBuild.module().dependecy().name(),
                AqlApiBuild.module().dependecy().item().sha1Actual(),
                AqlApiBuild.module().dependecy().item().md5Actual(),
                AqlApiBuild.module().dependecy().item().sha1Orginal(),
                AqlApiBuild.module().dependecy().item().md5Orginal(),
                AqlApiBuild.module().dependecy().item().created(),
                AqlApiBuild.module().dependecy().item().modifiedBy(),
                AqlApiBuild.module().dependecy().item().createdBy(),
                AqlApiBuild.module().dependecy().item().updated(),
                AqlApiBuild.module().dependecy().item().repo(),
                AqlApiBuild.module().dependecy().item().path(),
                AqlApiBuild.module().dependecy().item().name(),
                AqlApiBuild.module().dependecy().item().size()
                //Ordering by the last updated field, in case of duplicates with the same checksum.
        ).sortBy(AqlFieldEnum.itemUpdated).asc();

        AqlEagerResult<AqlBaseFullRowImpl> results = aqlService.executeQueryEager(buildDependenciesQuery);
        log.debug("Search returned {} dependencies", results.getSize());
        Multimap<String, Dependency> buildDependencies = BuildServiceUtils.getBuildDependencies(build);
        log.debug("This build contains {} dependencies (taken from build info)", buildDependencies.size());
        Map<Dependency, FileInfo> matchedDependencies = matchDependenciesToFileInfos(results.getResults(),
                buildDependencies);
        log.debug("Matched {} build dependencies to actual paths returned by search", matchedDependencies.size());

        //Lastly, populate matchedDependencies with all remaining unmatched dependencies with null values to help users
        //of this function know if all build artifacts were found.
        log.debug("{} dependencies were not matched to actual paths", buildDependencies.size());
        for (Dependency dependency : buildDependencies.values()) {
            if (!matchedDependencies.containsKey(dependency)) {
                matchedDependencies.put(dependency, null);
            }
        }
        return matchedDependencies;
    }

    /**
     * We're making a best effort to guess the relevant dependency according to the id given by the BuildInfo,
     * if indeed more than one dependency is a match for the checksum. if not - the one found is used.
     * Unlike matching the build's artifacts above, a weak match is enough and maybe even recommended here as
     * dependencies might move around or get deleted and we will still find a correct one from another repo.
     */
    private Map<Dependency, FileInfo> matchDependenciesToFileInfos(List<AqlBaseFullRowImpl> queryResults,
            Multimap<String, Dependency> checksumToDependencyMap) {

        List<String> virtualRepoKeys = getVirtualRepoKeys();
        Map<Dependency, FileInfo> foundResults = Maps.newHashMap();
        for (final AqlBaseFullRowImpl result : queryResults) {
            //Don't include results from virtual repos
            if (!virtualRepoKeys.contains(result.getRepo())) {
                Collection<Dependency> dependencies = checksumToDependencyMap.get(result.getActualSha1());
                if (!CollectionUtils.isNullOrEmpty(dependencies)) {
                    FileInfo dependencyFileInfo;
                    try {
                        dependencyFileInfo = (FileInfo) AqlConverts.toFileInfo.apply(result);
                        //Try matching dependencies exactly by id and add them to the results, overwriting previously
                        //found (maybe not exactly matched) dependencies
                        tryExactDependencyMatch(foundResults, result, dependencies, dependencyFileInfo);
                        //Add all dependencies that weren't matched yet to make sure we don't leave false unmatched ones
                        matchAnyDependencyToFileInfo(foundResults, dependencies, dependencyFileInfo);
                    } catch (Exception e) {
                        log.debug("Error creating path from aql result: {} :\n {}", result.toString(), e.getMessage());
                    }
                }
            }
        }
        return foundResults;
    }


    /**
     * Tries to match dependencies exactly to a path (file info) based on the dependency's name, and adds matched
     * dependencies to the result map.
     */
    private void tryExactDependencyMatch(Map<Dependency, FileInfo> foundResults, final AqlBaseFullRowImpl result,
            Collection<Dependency> dependencies, FileInfo dependencyFileInfo) {

        Iterable<Dependency> exactMatches = Iterables.filter(dependencies, new Predicate<Dependency>() {
            @Override
            public boolean apply(Dependency input) {
                return input.getId() != null && input.getId().contains(result.getBuildDependencyName());
            }
        });
        for (Dependency exactMatch : exactMatches) {
            log.debug("Exactly matched dependency {} to path {}", exactMatch.getId(),
                    dependencyFileInfo.getRepoPath().toString());
            foundResults.put(exactMatch, dependencyFileInfo);
        }
    }

    /**
     * Adds each dependency in the collection to the result map, if it was not already matched before.
     */
    private void matchAnyDependencyToFileInfo(Map<Dependency, FileInfo> foundResults,
            Collection<Dependency> dependencies, FileInfo dependencyFileInfo) {
        for (Dependency dependency : dependencies) {
            if (!foundResults.containsKey(dependency)) {
                log.debug("Matched dependency {} to path {}", dependency.getId(),
                        dependencyFileInfo.getRepoPath().toString());
                foundResults.put(dependency, dependencyFileInfo);
            }
        }
    }


    @Override
    public void exportTo(ExportSettings settings) {
        MutableStatusHolder multiStatusHolder = settings.getStatusHolder();
        multiStatusHolder.debug("Starting build info export", log);

        File buildsFolder = new File(settings.getBaseDir(), BUILDS_EXPORT_DIR);
        prepareBuildsFolder(settings, multiStatusHolder, buildsFolder);
        if (multiStatusHolder.isError()) {
            return;
        }

        try {
            long exportedBuildCount = 1;
            List<String> buildNames = buildStoreService.getAllBuildNames();
            for (String buildName : buildNames) {
                Set<BuildRun> buildsByName = buildStoreService.findBuildsByName(buildName);
                for (BuildRun buildRun : buildsByName) {
                    String buildNumber = buildRun.getNumber();
                    try {
                        exportBuild(settings, buildRun, exportedBuildCount, buildsFolder);
                        exportedBuildCount++;
                    } catch (Exception e) {
                        String errorMessage = String.format("Failed to export build info: %s:%s", buildName,
                                buildNumber);
                        if (settings.isFailFast()) {
                            throw new Exception(errorMessage, e);
                        }
                        multiStatusHolder.error(errorMessage, e, log);
                    }
                }
            }
        } catch (Exception e) {
            multiStatusHolder.error("Error occurred during build info export.", e, log);
        }

        if (settings.isIncremental() && !multiStatusHolder.isError()) {
            try {
                log.debug("Cleaning previous builds backup folder.");

                File[] backupDirsToRemove = settings.getBaseDir().listFiles(new FilenameFilter() {

                    @Override
                    public boolean accept(File dir, String name) {
                        return name.startsWith(BACKUP_BUILDS_FOLDER);
                    }
                });
                if (backupDirsToRemove != null) {
                    for (File backupDirToRemove : backupDirsToRemove) {
                        log.debug("Cleaning previous build backup folder: {}", backupDirToRemove.getAbsolutePath());
                        FileUtils.forceDelete(backupDirToRemove);
                    }
                }
            } catch (IOException e) {
                multiStatusHolder.error("Failed to clean previous builds backup folder.", e, log);
            }
        }

        multiStatusHolder.debug("Finished build info export", log);
    }

    /**
     * Makes sure that all the correct build/backup dirs are prepared for backup
     *
     * @param settings          Export settings
     * @param multiStatusHolder Process status holder
     * @param buildsFolder      Builds folder within the backup
     */
    private void prepareBuildsFolder(ExportSettings settings, MutableStatusHolder multiStatusHolder,
            File buildsFolder) {
        if (buildsFolder.exists()) {
            // Backup previous builds folder if incremental
            if (settings.isIncremental()) {
                File tempBuildBackupDir = new File(settings.getBaseDir(),
                        BACKUP_BUILDS_FOLDER + "." + System.currentTimeMillis());
                try {
                    FileUtils.moveDirectory(buildsFolder, tempBuildBackupDir);
                    FileUtils.forceMkdir(buildsFolder);
                } catch (IOException e) {
                    multiStatusHolder.error(
                            "Failed to create incremental builds temp backup dir: " + tempBuildBackupDir, e, log);
                }
            }
        } else {
            try {
                FileUtils.forceMkdir(buildsFolder);
            } catch (IOException e) {
                multiStatusHolder.error("Failed to create builds backup dir: " + buildsFolder, e, log);
            }
        }
    }

    @Override
    public void importFrom(ImportSettings settings) {
        final MutableStatusHolder multiStatusHolder = settings.getStatusHolder();
        multiStatusHolder.status("Starting build info import", log);

        dbService.invokeInTransaction("BuildImport-deleteAllBuilds", new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                try {
                    // delete all existing builds
                    buildStoreService.deleteAllBuilds();
                } catch (Exception e) {
                    multiStatusHolder.error("Failed to delete builds root node", e, log);
                }
                return null;
            }
        });

        File buildsFolder = new File(settings.getBaseDir(), BUILDS_EXPORT_DIR);
        String buildsFolderPath = buildsFolder.getPath();
        if (!buildsFolder.exists()) {
            multiStatusHolder.status("'" + buildsFolderPath + "' folder is either non-existent or not a " +
                    "directory. Build info import was not performed", log);
            return;
        }

        IOFileFilter buildExportFileFilter = new AbstractFileFilter() {
            @Override
            public boolean accept(File file) {
                String fileName = file.getName();
                return fileName.startsWith("build") && fileName.endsWith(".xml");
            }
        };

        Collection<File> buildExportFiles =
                FileUtils.listFiles(buildsFolder, buildExportFileFilter, DirectoryFileFilter.DIRECTORY);

        if (buildExportFiles.isEmpty()) {
            multiStatusHolder.status("'" + buildsFolderPath + "' folder does not contain build export files. " +
                    "Build info import was not performed", log);
            return;
        }

        importBuildFiles(settings, buildExportFiles);
        multiStatusHolder.status("Finished build info import", log);
    }

    @Override
    public void importBuild(ImportSettings settings, ImportableExportableBuild build) throws Exception {
        String buildName = build.getBuildName();
        MutableStatusHolder multiStatusHolder = settings.getStatusHolder();
        String buildNumber = build.getBuildNumber();
        String buildStarted = build.getBuildStarted();
        try {
            multiStatusHolder.debug(
                    String.format("Beginning import of build: %s:%s:%s", buildName, buildNumber, buildStarted), log);
            buildStoreService.addBuild(build.getJson());
        } catch (Exception e) {
            String msg = "Could not import build " + buildName + ":" + buildNumber + ":" + buildStarted;
            // Print stack trace in debug
            log.debug(msg, e);
            multiStatusHolder.error(msg, e, log);
        }
        multiStatusHolder.debug(
                String.format("Finished import of build: %s:%s:%s", buildName, buildNumber, buildStarted), log);
    }

    @Override
    public Set<String> findScopes(Build build) {
        final Set<String> scopes = Sets.newHashSet();
        if (build.getModules() != null) {
            for (Module module : build.getModules()) {
                if (module.getDependencies() != null) {
                    for (Dependency dependency : module.getDependencies()) {
                        Set<String> dependencyScopes = dependency.getScopes();
                        if (dependencyScopes != null) {
                            for (String dependencyScope : dependencyScopes) {
                                if (StringUtils.isBlank(dependencyScope)) {
                                    scopes.add(UNSPECIFIED_SCOPE);
                                } else {
                                    scopes.add(dependencyScope);
                                }
                            }
                        }
                    }
                }
            }
        }
        return scopes;
    }

    @Override
    public boolean isGenericBuild(Build build) {
        BuildAgent buildAgent = build.getBuildAgent();
        if (buildAgent != null) {
            String buildAgentName = buildAgent.getName();
            return !"ivy".equalsIgnoreCase(buildAgentName) && !"maven".equalsIgnoreCase(buildAgentName) &&
                    !"gradle".equalsIgnoreCase(buildAgentName) && !"MSBuild".equalsIgnoreCase(buildAgentName);
        }

        BuildType type = build.getType();
        return BuildType.ANT.equals(type) || BuildType.GENERIC.equals(type);
    }

    @Override
    public PromotionResult promoteBuild(BuildRun buildRun, Promotion promotion) {
        BuildPromotionHelper buildPromotionHelper = new BuildPromotionHelper();
        return buildPromotionHelper.promoteBuild(buildRun, promotion);
    }

    @Override
    public void renameBuilds(String from, String to) {
        Set<BuildRun> buildsToRename = searchBuildsByName(from);
        if (buildsToRename.isEmpty()) {
            log.error("Could not find builds by the name '{}'. No builds were renamed.", from);
            return;
        }

        for (BuildRun buildToRename : buildsToRename) {
            try {
                getTransactionalMe().renameBuild(buildToRename, to);
                log.info("Renamed build number '{}' that started at '{}' from '{}' to '{}'.", new String[]{
                        buildToRename.getNumber(), buildToRename.getStarted(), buildToRename.getName(), to});
            } catch (Exception e) {
                log.error("Failed to rename build: '{}' #{} that started at {}.", new String[]{buildToRename.getName(),
                        buildToRename.getNumber(), buildToRename.getStarted()});
            }
        }
    }

    @Override
    public void renameBuild(BuildRun buildRun, String to) {
        Build build = buildStoreService.getBuildJson(buildRun);
        if (build == null) {
            throw new StorageException("Cannot rename non existent build " + buildRun);
        }
        boolean changed = false;
        if (!StringUtils.equals(build.getName(), to)) {
            build.setName(to);
            changed = true;
        }
        if (!StringUtils.equals(buildRun.getName(), to)) {
            changed = true;
        }
        if (!changed) {
            log.info("Build " + buildRun + " already named " + to + " nothing to do!");
        }
        buildStoreService.renameBuild(buildRun, build, authorizationService.currentUsername());
    }

    @Override
    //Deletes the existing build (which has the same name, number and started) and adds the new one instead.
    public void updateBuild(@Nonnull DetailedBuildRun detailedBuildRun) {
        //getTransactionalMe().updateBuild(((DetailedBuildRunImpl) detailedBuildRun).build, true);
        BasicStatusHolder status = new BasicStatusHolder();

        //Reaching update implies the same build (name, number and started) already exists
        log.info("Updating build {} Number {} that started at {}", detailedBuildRun.getName(),
                detailedBuildRun.getNumber(), detailedBuildRun.getStarted(), log);
        log.debug("Deleting build {} : {} : {}", detailedBuildRun.getName(), detailedBuildRun.getNumber(),
                detailedBuildRun.getStarted(), log);
        getTransactionalMe().deleteBuild(detailedBuildRun, false, status);
        if (status.hasErrors()) {
            log.error(status.getLastError().toString(), log);
        }
        log.debug("Adding new build {} : {} : {}", detailedBuildRun.getName(), detailedBuildRun.getNumber(),
                detailedBuildRun.getStarted(), log);
        getTransactionalMe().addBuild(detailedBuildRun);

        log.info("Update of build {} Number {} that started at {} completed successfully", detailedBuildRun.getName(),
                detailedBuildRun.getNumber(), detailedBuildRun.getStarted(), log);
    }

    @Override
    public void updateBuild(final Build build, boolean updateChecksumProperties) {
    }

    @Override
    public void addPromotionStatus(Build build, PromotionStatus promotion) {
        buildStoreService.addPromotionStatus(build, promotion, authorizationService.currentUsername());
    }

    @Nullable
    @Override
    public List<PublishedModule> getPublishedModules(String buildName, String date, String orderBy, String direction, String offset, String limit) {
        return buildStoreService.getPublishedModules(buildName, date, orderBy, direction, offset, limit);
    }

    @Nullable
    @Override
    public List<ModuleArtifact> getModuleArtifact(String buildName, String buildNumber, String moduleId, String date, String orderBy, String direction, String offset, String limit) {
        return buildStoreService.getModuleArtifact(buildName, buildNumber, moduleId, date, orderBy, direction, offset,
                limit);
    }

    @Nullable
    @Override
    public List<ModuleDependency> getModuleDependency(String buildNumber, String moduleId, String date, String orderBy, String direction, String offset, String limit) {
        return buildStoreService.getModuleDependency(buildNumber, moduleId, date, orderBy, direction, offset, limit);
    }


    @Nullable
    @Override
    public int getModuleArtifactCount(String buildNumber, String moduleId,String date) {
        return buildStoreService.getModuleArtifactCount(buildNumber, moduleId, date);
    }

    @Nullable
    @Override
    public int getModuleDependencyCount(String buildNumber, String moduleId, String date) {
        return buildStoreService.getModuleDependenciesCount(buildNumber, moduleId, date);
    }


    @Override
    public int  getPublishedModulesCounts(String buildName, String date) {
        return buildStoreService.getPublishedModulesCounts(buildName, date);
    }

    private void exportBuild(ExportSettings settings, BuildRun buildRun,
            long exportedBuildCount, File buildsFolder) throws Exception {
        MutableStatusHolder multiStatusHolder = settings.getStatusHolder();

        String buildName = buildRun.getName();
        String buildNumber = buildRun.getNumber();
        String buildStarted = buildRun.getStarted();
        multiStatusHolder.debug(
                String.format("Beginning export of build: %s:%s:%s", buildName, buildNumber, buildStarted), log);

        ImportableExportableBuild exportedBuild = buildStoreService.getExportableBuild(buildRun);

        File buildFile = new File(buildsFolder, "build" + Long.toString(exportedBuildCount) + ".xml");
        exportBuildToFile(exportedBuild, buildFile);

        multiStatusHolder.debug(
                String.format("Finished export of build: %s:%s:%s", buildName, buildNumber, buildStarted), log);
    }

    private void importBuildFiles(ImportSettings settings, Collection<File> buildExportFiles) {
        for (File buildExportFile : buildExportFiles) {
            FileInputStream inputStream = null;
            try {
                inputStream = new FileInputStream(buildExportFile);
                ImportableExportableBuild importableBuild = (ImportableExportableBuild) buildXStream
                        .fromXML(inputStream);
                // import each build in a separate transaction
                getTransactionalMe().importBuild(settings, importableBuild);
            } catch (Exception e) {
                settings.getStatusHolder().error("Error occurred during build info import", e, log);
                if (settings.isFailFast()) {
                    break;
                }
            } finally {
                IOUtils.closeQuietly(inputStream);
            }
        }
    }

    private void exportBuildToFile(ImportableExportableBuild exportedBuild, File buildFile) throws Exception {
        FileOutputStream buildFileOutputStream = null;
        try {
            buildFileOutputStream = new FileOutputStream(buildFile);
            buildXStream.toXML(exportedBuild, buildFileOutputStream);
        } finally {
            IOUtils.closeQuietly(buildFileOutputStream);
        }
    }

    /**
     * Returns an internal instance of the service
     *
     * @return InternalBuildService
     */
    private InternalBuildService getTransactionalMe() {
        return ContextHelper.get().beanForType(InternalBuildService.class);
    }

    private List<String> getVirtualRepoKeys() {
        List<String> virtualKeys = Lists.newArrayList();
        for (RepoDescriptor virtualDescriptor : repositoryService.getVirtualRepoDescriptors()) {
            virtualKeys.add(virtualDescriptor.getKey());
        }
        return virtualKeys;
    }

    @Override
    public void deleteAllBuilds(String buildName) {
        if (authorizationService.isAdmin()) {
            buildStoreService.deleteAllBuilds(buildName);
        }
    }

    @Override
    public List<ModuleArtifact> getModuleArtifactsForDiffWithPaging(BuildParams buildParams, String offset, String limit) {
        return buildStoreService.getModuleArtifactsForDiffWithPaging(buildParams, offset, limit);
    }

    @Override
    public int getModuleArtifactsForDiffCount(BuildParams buildParams, String offset, String limit) {
        return buildStoreService.getModuleArtifactsForDiffCount(buildParams, offset, limit);
    }

    @Override
    public List<ModuleDependency> getModuleDependencyForDiffWithPaging(BuildParams buildParams, String offset, String limit) {
        return buildStoreService.getModuleDependencyForDiffWithPaging(buildParams, offset, limit);
    }

    @Override
    public int getModuleDependencyForDiffCount(BuildParams buildParams, String offset, String limit) {
        return buildStoreService.getModuleDependencyForDiffCount(buildParams, offset, limit);
    }

    @Override
    public List<GeneralBuild> getPrevBuildsList(String buildName, String buildDate) {
        return buildStoreService.getPrevBuildsList(buildName, buildDate);
    }

    @Override
    public List<BuildProps> getBuildProps(BuildParams buildParams, String offset, String limit) {
        return buildStoreService.getBuildProps(buildParams, offset, limit);
    }

    @Override
    public int getPropsDiffCount(BuildParams buildParams) {
        return buildStoreService.getPropsDiffCount(buildParams);
    }

    @Override
    public List<BuildProps> getBuildPropsData(BuildParams buildParams, String offset, String limit, String orderBy) {
        return buildStoreService.getBuildPropsData(buildParams, offset, limit, orderBy);
    }

    @Override
    public long getBuildPropsCounts(BuildParams buildParams) {
        return buildStoreService.getBuildPropsCounts(buildParams);
    }

    @Override
    public Map<String, ModuleArtifact> getAllModuleArtifacts(String buildNumber, String moduleId, String date, String orderBy, String direction, String offset, String limit) {
        return null;
    }

    @Override
    public Set<BuildRun> getLatestBuildsPaging(String offset, String orderBy, String direction, String limit) {
        BuildStoreService buildStoreService = ContextHelper.get().beanForType(BuildStoreService.class);
        return buildStoreService.getLatestBuildsPaging(offset, orderBy, direction, limit);
    }

    @Override
    public List<GeneralBuild> getBuildForNamePaging(String buildName, String orderBy, String direction, String offset,
            String limit) throws
            SQLException {
        BuildStoreService buildStoreService = ContextHelper.get().beanForType(BuildStoreService.class);
        return buildStoreService.getBuildForNamePaging(buildName, orderBy, direction, offset, limit);
    }
}
