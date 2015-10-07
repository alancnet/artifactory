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

package org.artifactory.repo.snapshot;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Snapshot;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.module.ModuleInfo;
import org.artifactory.api.request.DownloadService;
import org.artifactory.api.request.InternalArtifactoryRequest;
import org.artifactory.maven.MavenModelUtils;
import org.artifactory.mime.MavenNaming;
import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RemoteRepo;
import org.artifactory.repo.Repo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.request.InternalCapturingResponse;
import org.artifactory.request.InternalRequestContext;
import org.artifactory.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves the latest unique snapshot version given a non-unique Maven snapshot artifact request for remote
 * repositories.
 *
 * @author Shay Yaakov
 */
public class RemoteLatestMavenVersionResolver extends LatestVersionResolver {
    private static final Logger log = LoggerFactory.getLogger(RemoteLatestMavenVersionResolver.class);

    /**
     * Downloads maven-metadata.xml from the remote and analyzes the latest version from it. If it does not exist, we
     * return the original request context.
     */
    @Override
    protected InternalRequestContext getRequestContext(InternalRequestContext requestContext, Repo repo,
            ModuleInfo originalModuleInfo) {
        if (!(repo instanceof RemoteRepo)) {
            return requestContext;
        }
        RemoteRepo remoteRepo = (RemoteRepo) repo;
        if (!remoteRepo.getDescriptor().isMavenRepoLayout()) {
            // Latest from remote is supported only for maven2 layout
            return requestContext;
        }
        if (remoteRepo.isOffline()) {
            // will fallback to local cache search
            return requestContext;
        }

        String path = requestContext.getResourcePath();
        if (MavenNaming.isMavenMetadata(path)) {
            // Recursive request for maven metadata, simply return the original request context
            return requestContext;
        }
        boolean searchForReleaseVersion = StringUtils.contains(path, "[RELEASE]");
        RepoPath repoPath;
        if (searchForReleaseVersion) {
            repoPath = InternalRepoPathFactory.create(repo.getKey(), path).getParent();
        } else {
            repoPath = InternalRepoPathFactory.create(repo.getKey(), path);
        }
        Metadata metadata = tryDownloadingMavenMetadata(repoPath);
        if (metadata != null) {
            try {
                Versioning versioning = metadata.getVersioning();
                if (versioning != null) {
                    if (searchForReleaseVersion) {
                        String release = versioning.getRelease();
                        if (StringUtils.isNotBlank(release)) {
                            String releaseRepoPath = path.replace("[RELEASE]", release);
                            requestContext = translateRepoRequestContext(requestContext, repo, releaseRepoPath);
                        }
                    } else {
                        Snapshot snapshot = versioning.getSnapshot();
                        if (snapshot != null) {
                            String timestamp = snapshot.getTimestamp();
                            int buildNumber = snapshot.getBuildNumber();
                            if (StringUtils.isNotBlank(timestamp) && buildNumber > 0) {
                                String originalFileName = PathUtils.getFileName(path);
                                String fileName = originalFileName.replaceFirst("SNAPSHOT",
                                        timestamp + "-" + buildNumber);
                                RepoPath parentRepoPath = repoPath.getParent();
                                String uniqueRepoPath = PathUtils.addTrailingSlash(parentRepoPath.getPath()) + fileName;
                                requestContext = translateRepoRequestContext(requestContext, repo, uniqueRepoPath);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Failed parsing maven metadata from remote repo '{}' for repoPath '{}'",
                        repoPath.getRepoKey(), repoPath.getPath());
            }
        }

        return requestContext;
    }

    private Metadata tryDownloadingMavenMetadata(RepoPath repoPath) {
        if (repoPath == null) {
            log.debug("Could not download remote maven metadata for null repo path");
            return null;
        }

        String parentFolder = PathUtils.getParent(repoPath.getPath());
        RepoPath parentRepoPath = RepoPathFactory.create(repoPath.getRepoKey(), parentFolder);
        RepoPath metadataRepoPath = new RepoPathImpl(parentRepoPath, MavenNaming.MAVEN_METADATA_NAME);
        InternalArtifactoryRequest req = new InternalArtifactoryRequest(metadataRepoPath);
        InternalCapturingResponse res = new InternalCapturingResponse();
        try {
            DownloadService downloadService = ContextHelper.get().beanForType(DownloadService.class);
            downloadService.process(req, res);
            if (res.getStatus() == HttpStatus.SC_OK) {
                String metadataStr = res.getResultAsString();
                return MavenModelUtils.toMavenMetadata(metadataStr);
            }
        } catch (Exception e) {
            log.info("Could not download remote maven metadata for repo '{}' and path '{}'",
                    new Object[]{metadataRepoPath.getRepoKey(), metadataRepoPath.getPath()}, e);
        }
        return null;
    }

}
