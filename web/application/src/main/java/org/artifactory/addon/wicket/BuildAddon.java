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

package org.artifactory.addon.wicket;

import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.artifactory.addon.Addon;
import org.artifactory.addon.AddonType;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.rest.build.artifacts.BuildArtifactsRequest;
import org.artifactory.api.rest.build.diff.BuildsDiff;
import org.artifactory.build.ArtifactoryBuildArtifact;
import org.artifactory.fs.FileInfo;
import org.artifactory.repo.RepoPath;
import org.artifactory.sapi.common.Lock;
import org.artifactory.webapp.actionable.RepoAwareActionableItem;
import org.artifactory.webapp.wicket.page.build.actionable.ModuleArtifactActionableItem;
import org.artifactory.webapp.wicket.page.build.actionable.ModuleDependencyActionableItem;
import org.artifactory.webapp.wicket.page.search.SaveSearchResultsPanel;
import org.jfrog.build.api.Artifact;
import org.jfrog.build.api.BaseBuildFileBean;
import org.jfrog.build.api.Build;
import org.jfrog.build.api.BuildRetention;
import org.jfrog.build.api.Dependency;
import org.jfrog.build.api.Module;
import org.jfrog.build.api.dependency.BuildPatternArtifacts;
import org.jfrog.build.api.dependency.BuildPatternArtifactsRequest;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Addon for continuous integration info handling
 *
 * @author Noam Y. Tenne
 */
public interface BuildAddon extends Addon {


    /**
     * Returns the builds tab panel
     *
     * @param item Selected repo item
     * @return Builds tab panel
     */
    ITab getBuildsTab(RepoAwareActionableItem item);

    /**
     * Returns the module info tab panel
     *
     * @param build  The selected build
     * @param module Selected module
     * @return Module info tab panel
     */
    ITab getModuleInfoTab(Build build, Module module);

    /**
     * Returns the builds diff tab panel
     *
     * @param title            The Tab title
     * @param build            The selected build to perform comparison with
     * @param hasDeployOnLocal whether to display the actual tab or a disabled one
     * @return Builds diff tab panel
     */
    ITab getBuildDiffTab(String title, Build build, boolean hasDeployOnLocal);

    /**
     * Returns a customized delete confirmation message that adds alerts in case any selected items are used by builds
     *
     * @param item           Item to be deleted
     * @param defaultMessage Default message to display in any case
     * @return Delete confirmation message
     */
    String getDeleteItemWarningMessage(org.artifactory.fs.ItemInfo item, String defaultMessage);

    /**
     * Returns a customized delete confirmation message that adds alerts in case any selected items are used by builds
     *
     * @param versionPaths   Selected version repo paths
     * @param defaultMessage The message to display in any case
     * @return Delete confirmation message
     */
    String getDeleteVersionsWarningMessage(List<RepoPath> versionPaths, String defaultMessage);

    /**
     * Returns a list of build-dependency file info objects, as well as their descriptor according to the layout if
     * exists.
     *
     * @param build  Build to extract artifacts from
     * @param scopes Scopes to add. null = add all dependencies.
     * @return Dependency file info list
     */
    Set<FileInfo> getBuildDependencyFileInfos(Build build, Set<String> scopes);

    /**
     * Returns a list of build-artifact actionable items
     *
     * @param build     The searched build
     * @param artifacts Artifacts to create actionable items from
     * @return Artifact actionable item list
     */
    List<ModuleArtifactActionableItem> getModuleArtifactActionableItems(Build build, List<Artifact> artifacts);

    /**
     * Populates dependency actionable items with their corresponding repo paths (if exist)
     *
     * @param build        The searched build
     * @param dependencies Unpopulated actionable items
     * @return Dependency actionable item list
     */
    List<ModuleDependencyActionableItem> populateModuleDependencyActionableItem(Build build,
            List<ModuleDependencyActionableItem> dependencies);

    /**
     * Returns the build save search results panel
     *
     * @param requestingAddon The addon that requests the panel
     * @param build           Build to use for results
     * @return Build save search results panel
     */
    SaveSearchResultsPanel getBuildSearchResultsPanel(AddonType requestingAddon, Build build);

    /**
     * Returns a mapping of file info to artifact for all artifacts in this build, will return artifacts that were not
     * directly involved in the build (but same checksum!) if the build's artifacts themselves were'nt found based on
     * their properties.
     * NOTE: a mapping might be null if the artifact does not exist in artifactory
     *
     * @param build The searched build
     * @return file beans and file infos
     */
    Set<ArtifactoryBuildArtifact> getBuildArtifactsFileInfosWithFallback(Build build);

    /**
     * Returns a mapping of file info to artifact for all artifacts in this build.
     * NOTE: a mapping might be null if the artifact does not exist in artifactory
     *
     * @param build The searched build
     * @return file beans and file infos
     */
    Set<ArtifactoryBuildArtifact> getBuildArtifactsFileInfos(Build build);

    /**
     * Returns a file info of dependency object for a build file bean
     * NOTE: a mapping might be null if the dependency does not exist in artifactory
     *
     * @param build The searched build
     * @return file beans and file infos
     */
    Map<Dependency, FileInfo> getBuildDependenciesFileInfos(Build build);

    /**
     * Searches for artifacts with build name properties that contain 'from' as a value and renames them to 'to'
     *
     * @param from Build name property value to search for
     * @param to   Replacement build name
     */
    @Lock
    void renameBuildNameProperty(String from, String to);

    /**
     * Discard old builds according to a maximum date. If a build is below the {@code minimumBuildDate} then that build
     * will be discarded.
     *
     * @param buildName         The name of the build.
     * @param buildRetention    Build retention model that holds information about which build to discard
     * @param multiStatusHolder Status holder
     */
    @Lock
    void discardOldBuildsByDate(String buildName, BuildRetention buildRetention, BasicStatusHolder multiStatusHolder);

    /**
     * Discard old builds according to the maximum amount of builds that should be retained. if {@code count} is larger
     * than the size of the set of builds, all builds will be retained.
     *
     * @param buildName         The name of the build.
     * @param discard           Build retention model that holds information about which build to discard
     * @param multiStatusHolder Status holder
     */
    @Lock
    void discardOldBuildsByCount(String buildName, BuildRetention discard, BasicStatusHolder multiStatusHolder);

    /**
     * Returns the built artifacts matching the request
     *
     * @param buildPatternArtifactsRequest contains build name and build number or keyword
     * @param servletContextUrl            for building urls of current Artifactory instance
     * @return artifacts generated by the build
     */
    BuildPatternArtifacts getBuildPatternArtifacts(@Nonnull BuildPatternArtifactsRequest buildPatternArtifactsRequest,
            String servletContextUrl);

    /**
     * Returns build artifacts map according to the param input regexp patterns.
     *
     * @param buildArtifactsRequest A wrapper which contains the necessary parameters
     * @return A map from {@link FileInfo}s to their target directories relative paths
     * @see BuildArtifactsRequest
     */
    Map<FileInfo, String> getBuildArtifacts(BuildArtifactsRequest buildArtifactsRequest);

    /**
     * Returns an archive file according to the param archive type (zip/tar/tar.gz/tgz) which contains
     * all build artifacts according to the given build name and number (can be latest or latest by status).
     *
     * @param buildArtifactsRequest A wrapper which contains the necessary parameters
     * @return The archived file of build artifacts with their hierarchy rules
     * @see BuildArtifactsRequest
     */
    File getBuildArtifactsArchive(BuildArtifactsRequest buildArtifactsRequest) throws IOException;

    /**
     * Returns diff object between two given builds (same build name, different numbers)
     *
     * @param firstBuild         The first build to compare, must be newer than the second build
     * @param secondBuild        The second build to compare against
     * @param baseStorageInfoUri Base storage uri
     */
    BuildsDiff getBuildsDiff(Build firstBuild, Build secondBuild, String baseStorageInfoUri);

    /**
     * return artifact or dependency file info (in order to get repo path)
     *
     * @param artifact - artifact or dependency  (to get file info for)
     * @param build    - build data
     * @return file info instance of artifact or dependency
     */
    FileInfo getFileBeanInfo(BaseBuildFileBean artifact, Build build);

}