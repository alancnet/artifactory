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

package org.artifactory.api.build;

import org.artifactory.api.build.diff.BuildParams;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.config.ImportableExportable;
import org.artifactory.api.rest.artifact.PromotionResult;
import org.artifactory.build.BuildRun;
import org.artifactory.sapi.common.ExportSettings;
import org.artifactory.sapi.common.ImportSettings;
import org.artifactory.sapi.common.Lock;
import org.jfrog.build.api.Build;
import org.jfrog.build.api.release.Promotion;
import org.jfrog.build.api.release.PromotionStatus;

import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Build service main interface
 *
 * @author Noam Y. Tenne
 */
public interface BuildService extends ImportableExportable {

    /**
     * In case a dependency contains a {@code null} scope, fill it with an unspecified scope that will be used for
     * filtering.
     */
    public static final String UNSPECIFIED_SCOPE = "unspecified";
    static String LATEST_BUILD = "LATEST";
    static String LAST_RELEASED_BUILD = "LAST_RELEASE";


    /**
     * Adds the given build to the DB
     *
     * @param build Build to add
     */
    @Lock
    void addBuild(Build build);

    /**
     * Returns the JSON string of the given build details
     *
     * @param buildRun@return Build JSON if parsing succeeded. Empty string if not
     */
    String getBuildAsJson(BuildRun buildRun);

    /**
     * Removes all the builds of the given name
     *
     * @param buildName         Name of builds to remove
     * @param deleteArtifacts   True if build artifacts should be deleted
     * @param multiStatusHolder Status holder
     */
    @Lock
    void deleteBuild(String buildName, boolean deleteArtifacts, BasicStatusHolder multiStatusHolder);

    /**
     * Removes the build of the given details
     *
     * @param buildRun          Build info details
     * @param deleteArtifacts   True if build artifacts should be deleted
     * @param multiStatusHolder Status holder
     */
    @Lock
    void deleteBuild(BuildRun buildRun, boolean deleteArtifacts, BasicStatusHolder multiStatusHolder);

    /**
     * Returns the build of the given details
     *
     * @param buildRun Build run retrieved
     * @return Build if found. Null if not
     */
    Build getBuild(BuildRun buildRun);

    /**
     * Returns the latest build for the given name and number
     *
     * @param buildName   Name of build to locate
     * @param buildNumber Number of build to locate
     * @return Latest build if found. Null if not
     */
    @Nullable
    Build getLatestBuildByNameAndNumber(String buildName, String buildNumber);

    /**
     * Locates builds that are named as the given name
     *
     * @param buildName Name of builds to locate
     * @return Set of builds with the given name
     */
    Set<BuildRun> searchBuildsByName(String buildName);

    List<String> getBuildNames();

    /**
     * Returns a sorted list of all previous builds to the given one
     *
     * @param buildName    Name of build to locate
     * @param buildNumber  Number of build to locate
     * @param buildStarted The started time of the given build, will return all builds prior to this value
     * @return Ordered list (newest first) of builds prior to the given one
     */
    List<BuildRun> getAllPreviousBuilds(String buildName, String buildNumber, String buildStarted);

    /**
     * Locates builds that are named and numbered as the given name and number
     *
     * @param buildName   Name of builds to locate
     * @param buildNumber Number of builds to locate
     * @return Set of builds with the given name
     */
    Set<BuildRun> searchBuildsByNameAndNumber(String buildName, String buildNumber);

    BuildRun getBuildRun(String buildName, String buildNumber, String buildStarted);

    @Override
    void exportTo(ExportSettings settings);

    Set<String> findScopes(Build build);

    /**
     * @return True if the build is not Ivy/Gradle/Maven.
     */
    boolean isGenericBuild(Build build);

    @Override
    void importFrom(ImportSettings settings);

    /**
     * Promotes a build
     *
     * @param buildRun  Basic info of build to promote
     * @param promotion Promotion settings
     * @return Promotion result
     */
    @Lock
    PromotionResult promoteBuild(BuildRun buildRun, Promotion promotion);

    /**
     * Renames the structure and content of build info objects
     *
     * @param from Name to replace
     * @param to   Replacement build name
     */
    void renameBuilds(String from, String to);

    /**
     * Updates the content of the given build. Please note that this method does nothing apart from updating
     * the JSON data. Other properties and data surrounding the build nodes (apart from mandatory) will not change
     *
     * @param build                     Updated content
     * @param refreshChecksumProperties True if the build's searchable checksum properties should be updated with any
     *                                  checksum modifications of the build model
     */
    @Lock
    void updateBuild(Build build, boolean refreshChecksumProperties);

    @Lock
    void addPromotionStatus(Build build, PromotionStatus promotion);

    @Nullable
    List<PublishedModule> getPublishedModules(String buildName, String date, String orderBy, String direction, String offset, String limit);

   int  getPublishedModulesCounts(String buildName, String date);

    List<ModuleArtifact> getModuleArtifact(String buildName,String buildNumber, String moduleId,String date, String orderBy, String direction, String offset, String limit);

     int getModuleArtifactCount(String buildNumber, String moduleId,String date);

    List<ModuleDependency> getModuleDependency(String buildNumber, String moduleId, String date, String orderBy, String direction, String offset, String limit);

    int getModuleDependencyCount(String buildNumber, String moduleId, String date);

    void deleteAllBuilds(String buildName);

    List<ModuleArtifact> getModuleArtifactsForDiffWithPaging(BuildParams buildParams, String offset, String limit);

    Map<String, ModuleArtifact> getAllModuleArtifacts(String buildNumber, String moduleId, String date, String orderBy, String direction, String offset, String limit);

    int getModuleArtifactsForDiffCount(BuildParams buildParams, String offset, String limit);

    List<ModuleDependency> getModuleDependencyForDiffWithPaging(BuildParams buildParams, String offset, String limit);

    int getModuleDependencyForDiffCount(BuildParams buildParams, String offset, String limit);

    List<GeneralBuild> getPrevBuildsList(String buildName, String buildDate);

    List<BuildProps> getBuildProps(BuildParams buildParams, String offset, String limit);

    int getPropsDiffCount(BuildParams buildParams);

    List<BuildProps> getBuildPropsData(BuildParams buildParams, String offset, String limit, String orderBy);

    long getBuildPropsCounts(BuildParams buildParams);

    Set<BuildRun> getLatestBuildsPaging(String offset, String orderBy, String direction, String limit);

    List<GeneralBuild> getBuildForNamePaging(String buildName, String orderBy, String direction, String offset,
            String limit) throws
            SQLException;

}
