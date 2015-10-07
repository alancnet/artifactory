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

package org.artifactory.addon.rest;

import org.artifactory.addon.license.LicenseStatus;
import org.artifactory.addon.plugin.ResponseCtx;
import org.artifactory.api.archive.ArchiveType;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.rest.artifact.ItemPermissions;
import org.artifactory.api.rest.artifact.PromotionResult;
import org.artifactory.api.rest.build.artifacts.BuildArtifactsRequest;
import org.artifactory.api.rest.replication.ReplicationRequest;
import org.artifactory.api.rest.search.result.ArtifactVersionsResult;
import org.artifactory.api.rest.search.result.LicensesSearchResult;
import org.artifactory.fs.FileInfo;
import org.artifactory.fs.ItemInfo;
import org.artifactory.repo.RepoPath;
import org.artifactory.resource.ResourceStreamHandle;
import org.artifactory.rest.common.list.KeyValueList;
import org.artifactory.rest.common.list.StringList;
import org.artifactory.rest.resource.artifact.legacy.DownloadResource;
import org.jfrog.build.api.Build;
import org.jfrog.build.api.BuildRetention;
import org.jfrog.build.api.dependency.BuildPatternArtifacts;
import org.jfrog.build.api.dependency.BuildPatternArtifactsRequest;
import org.jfrog.build.api.release.Promotion;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Default implementation of the rest add-on
 *
 * @author Yoav Landman
 */
@Component
public class RestAddonDefaultImpl implements RestAddon {

    @Override
    public boolean isDefault() {
        return true;
    }

    @Override
    public Response copy(String path, String target, int dryRun, int suppressLayouts, int failFast)
            throws Exception {
        throw new MissingRestAddonException();
    }

    @Override
    public Response move(String path, String target, int dryRun, int suppressLayouts, int failFast)
            throws Exception {
        throw new MissingRestAddonException();
    }

    @Override
    public Response download(String path, DownloadResource.Content content, int mark,
            HttpServletResponse response) throws Exception {
        throw new MissingRestAddonException();
    }

    @Override
    public Set<String> searchArtifactsByPattern(String pattern) {
        throw new MissingRestAddonException();
    }

    @Override
    public PromotionResult promoteBuild(String buildName, String buildNumber, Promotion promotion) {
        throw new MissingRestAddonException();
    }

    @Override
    public Response replicate(RepoPath repoPath, ReplicationRequest replicationRequest) {
        throw new MissingRestAddonException();
    }

    @Override
    public void renameBuilds(String from, String to) {
        throw new MissingRestAddonException();
    }

    @Override
    public void renameBuildsAsync(String from, String to) {
        throw new MissingRestAddonException();
    }

    @Override
    public void deleteBuilds(HttpServletResponse response, String buildName, StringList buildNumbers, int artifacts,
            int deleteAll) {
        throw new MissingRestAddonException();
    }

    @Override
    public void discardOldBuilds(String name, BuildRetention discard, BasicStatusHolder multiStatusHolder) {
        // nop
    }

    @Override
    public ItemInfo getLastModified(String pathToSearch) {
        throw new MissingRestAddonException();
    }

    @Override
    public LicensesSearchResult findLicensesInRepos(LicenseStatus status, Set<String> repos, String servletContextUrl) {
        throw new MissingRestAddonException();
    }

    @Override
    public Response deleteRepository(String repoKey) {
        throw new MissingRestAddonException();
    }

    @Override
    public Response getRepositoryConfiguration(String repoKey, MediaType mediaType) {
        throw new MissingRestAddonException();
    }

    @Override
    public Response createOrReplaceRepository(String repoKey, Map repositoryConfig, MediaType mediaType,
            int position) {
        throw new MissingRestAddonException();
    }

    @Override
    public Response updateRepository(String repoKey, Map repositoryConfig, MediaType mediaType) {
        throw new MissingRestAddonException();
    }

    @Override
    public Set<RepoPath> searchArtifactsByChecksum(String md5Checksum, String sha1Checksum, StringList reposToSearch) {
        throw new MissingRestAddonException();
    }

    @Override
    @Nonnull
    public Response searchBadChecksumArtifacts(String type, StringList reposToSearch,
            HttpServletRequest request) {
        throw new MissingRestAddonException();
    }

    @Override
    public Response savePathProperties(String path, String recursive, KeyValueList properties) {
        throw new MissingRestAddonException();
    }

    @Override
    public Response deletePathProperties(String path, String recursive, StringList properties) {
        throw new MissingRestAddonException();
    }

    @Override
    public ResponseCtx runPluginExecution(String executionName, String method, Map params, ResourceStreamHandle body,
            boolean async) {
        throw new MissingRestAddonException();
    }

    @Override
    public Response getStagingStrategy(String strategyName, String buildName, Map params) {
        throw new MissingRestAddonException();
    }

    @Override
    public ItemPermissions getItemPermissions(HttpServletRequest request, String path) {
        throw new MissingRestAddonException();
    }

    @Override
    public Response searchDependencyBuilds(HttpServletRequest request, String sha1) {
        throw new MissingRestAddonException();
    }

    @Override
    public Response calculateYumMetadata(String repoKey, int async) {
        throw new MissingRestAddonException();
    }

    @Override
    public Response getSecurityEntities(HttpServletRequest request, String entityType) {
        throw new MissingRestAddonException();
    }

    @Override
    public Response getSecurityEntity(String entityType, String entityKey) {
        throw new MissingRestAddonException();
    }

    @Override
    public Response deleteSecurityEntity(String entityType, String entityKey) {
        throw new MissingRestAddonException();
    }

    @Override
    public Response createOrReplaceSecurityEntity(String entityType, String entityKey, HttpServletRequest request) {
        throw new MissingRestAddonException();
    }

    @Override
    public Response updateSecurityEntity(String entityType, String entityKey, HttpServletRequest request) {
        throw new MissingRestAddonException();
    }

    @Override
    public Response getReplicationStatus(RepoPath repoPath) {
        throw new MissingRestAddonException();
    }

    @Override
    public Response runMavenIndexer(List<String> reposToIndex, int force) {
        throw new MissingRestAddonException();
    }

    @Override
    public Response getUserPluginInfo(@Nullable String pluginType) {
        throw new MissingRestAddonException();
    }

    @Override
    public BuildPatternArtifacts getBuildPatternArtifacts(
            @Nonnull BuildPatternArtifactsRequest buildPatternArtifactsRequest, String servletContextUrl) {
        throw new MissingRestAddonException();
    }

    @Override
    public Response getBuildsDiff(Build firstBuild, Build secondBuild, HttpServletRequest request) {
        throw new MissingRestAddonException();
    }

    @Override
    public Map<FileInfo, String> getBuildArtifacts(BuildArtifactsRequest buildArtifactsRequest) {
        throw new MissingRestAddonException();
    }

    @Override
    public File getBuildArtifactsArchive(BuildArtifactsRequest buildArtifactsRequest) {
        throw new MissingRestAddonException();
    }

    @Override
    public InputStream downloadFolderOrRepo(RepoPath pathToDownload, ArchiveType archiveType, BasicStatusHolder status)
            throws IOException {
        throw new MissingRestAddonException();
    }

    @Override
    public ResponseCtx promote(String promotionName, String buildName, String buildNumber, Map params) {
        throw new MissingRestAddonException();
    }

    @Override
    public ResponseCtx deployPlugin(Reader pluginContent, String scriptName) {
        throw new MissingRestAddonException();
    }

    @Override
    public ResponseCtx reloadPlugins() {
        throw new MissingRestAddonException();
    }

    @Override
    public ArtifactVersionsResult getArtifactVersions(String groupId, String artifactId, @Nullable String version,
            @Nullable StringList reposToSearch, boolean remote, boolean limitSearch) {
        throw new MissingRestAddonException();
    }

    @Override
    public void writeStreamingFileList(HttpServletResponse response, String requestUrl, String path, int deep,
            int depth, int listFolders, int mdTimestamps, int includeRootPath) {
        throw new MissingRestAddonException();
    }

    @Override
    public Response getLatestVersionByProperties(String repoKey, String path, Map<String, String[]> parameterMap,
            HttpServletRequest request) {
        throw new MissingRestAddonException();
    }

}
