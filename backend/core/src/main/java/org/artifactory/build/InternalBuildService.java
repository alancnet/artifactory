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

import org.artifactory.api.build.BuildService;
import org.artifactory.api.build.ImportableExportableBuild;
import org.artifactory.fs.FileInfo;
import org.artifactory.sapi.common.ImportSettings;
import org.artifactory.sapi.common.Lock;
import org.artifactory.spring.ReloadableBean;
import org.jfrog.build.api.Artifact;
import org.jfrog.build.api.Build;
import org.jfrog.build.api.Dependency;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

/**
 * The system-internal interface of the build service
 *
 * @author Noam Y. Tenne
 */
public interface InternalBuildService extends ReloadableBean, BuildService {

    /**
     * Name of folder to temporarily store previous build backups during an incremental
     */
    String BACKUP_BUILDS_FOLDER = "builds.previous";

    /**
     * Returns a Mapping of {@link Artifact} to {@link FileInfo} that was  matched to it.
     * This method tries to search by build name and number properties first and if it can't match all Artifacts to
     * FileInfo objects it will fallback to a weaker search that's based on the build DB entries to fill in the missing
     * artifacts, if indicated by {@param useFallBack}
     * <p/>
     * **NOTE:**
     *
     * @param build            The searched build (searching within it's artifacts)
     * @param useFallBack      Indicates whether to fallback to the weaker search if not all artifacts were matched
     *                         against FileInfo objects
     * @param sourceRepository filtering by source repository key, not mandatory
     */
    Set<ArtifactoryBuildArtifact> getBuildArtifactsFileInfos(Build build, boolean useFallBack,
            @Nullable String sourceRepository);

    /**
     * Returns a map of build dependency and it's matching FileInfo
     *
     * @param build            The searched build (searching within it's dependencies)
     */
    Map<Dependency, FileInfo> getBuildDependenciesFileInfos(Build build);

    /**
     * Imports an exportable build info into the database. This is an internal method and should be used to import a
     * single build within a transaction.
     *
     * @param settings Import settings
     * @param build    The build to import
     */
    @Lock
    void importBuild(ImportSettings settings, ImportableExportableBuild build) throws Exception;

    /**
     * Renames the JSON content within a build
     *
     * @param buildRun Build to rename
     * @param to       Replacement build name
     */
    @Lock
    void renameBuild(BuildRun buildRun, String to);

    /**
     * Returns latest build by name and status (which can be {@link BuildService.LATEST_BUILD} or a status value (e.g: "Released")
     *
     * @param buildName   the name of the build
     * @param buildStatus the desired status (which can be {@link BuildService.LATEST_BUILD} or a status value (e.g: "Released")
     * @return the build (if found)
     */
    @Nullable
    Build getLatestBuildByNameAndStatus(String buildName, String buildStatus);

    /**
     * Adds the given build configuration to Artifactory
     *
     * @param detailedBuildRun Build to add
     */
    void addBuild(@Nonnull DetailedBuildRun detailedBuildRun);

    /**
     * Adds the given build to the DB, with a flag that indicates if the call is internal or not - for cases
     * where the call is made from the papi, this is to avoid accidental endless loops of plugin actions calling
     * one another indefinitely.
     *
     * @param build                   Build to add
     * @param activatePluginCallbacks flag that indicates if the call is internal
     */
    @Lock
    void addBuild(Build build, boolean activatePluginCallbacks);

    /**
     * Persists the changes made to the given existing build configuration
     *
     * @param detailedBuildRun Existing build configuration
     */
    void updateBuild(@Nonnull DetailedBuildRun detailedBuildRun);
}