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
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.module.ModuleInfo;
import org.artifactory.api.module.ModuleInfoUtils;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.request.TranslatedArtifactoryRequest;
import org.artifactory.common.ConstantValues;
import org.artifactory.descriptor.repo.RepoLayout;
import org.artifactory.mime.MavenNaming;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.Repo;
import org.artifactory.repo.RepoPath;
import org.artifactory.request.ArtifactoryRequest;
import org.artifactory.request.DownloadRequestContext;
import org.artifactory.request.InternalRequestContext;
import org.artifactory.request.NullRequestContext;

/**
 * Resolves the latest unique snapshot version given a non-unique Maven snapshot artifact request or a request with
 * [RELEASE]/[INTEGRATION] place holders for non-maven.
 *
 * @author Shay Yaakov
 */
public abstract class LatestVersionResolver {

    /**
     * Retrieves a transformed request context with the actual latest release/integration version.
     * Request with the [RELEASE] token will be transformed to the actual release version.
     * For maven a SNAPSHOT request will be transformed to a unique snapshot version while for non-maven
     * the [INTEGRATION] token will be transformed to the actual integration version according the the underlying repo layout.
     */
    public InternalRequestContext getDynamicVersionContext(Repo repo, InternalRequestContext originalRequestContext) {
        String path = originalRequestContext.getResourcePath();
        RepoLayout repoLayout = repo.getDescriptor().getRepoLayout();
        if (!ConstantValues.requestDisableVersionTokens.getBoolean() && StringUtils.isNotBlank(
                path) && repoLayout != null) {

            if (repo.getDescriptor().isMavenRepoLayout() && shouldSkipMavenRequest(path)) {
                return originalRequestContext;
            }

            ModuleInfo originalModuleInfo = ModuleInfoUtils.moduleInfoFromArtifactPath(path, repoLayout, true);
            if (!originalModuleInfo.isValid()) {
                return originalRequestContext;
            }

            return getRequestContext(originalRequestContext, repo, originalModuleInfo);
        }

        return originalRequestContext;
    }

    /**
     * For maven, only process non unique snapshot requests or path with release tokens
     */
    private boolean shouldSkipMavenRequest(String path) {
        return !MavenNaming.isNonUniqueSnapshot(path) && !StringUtils.contains(path, "[RELEASE]");
    }

    /**
     * Derived classes should implement their logic and return the transformed {@link InternalRequestContext}
     *
     * @param requestContext     the original request context to transform
     * @param repo
     * @param path
     * @param originalModuleInfo
     * @return
     */
    protected abstract InternalRequestContext getRequestContext(InternalRequestContext requestContext, Repo repo,
            ModuleInfo originalModuleInfo);

    protected RepositoryService getRepositoryService() {
        return ContextHelper.get().getRepositoryService();
    }

    /**
     * Returns a new request context if the translated path is different from the original request path
     *
     * @param repo    Target repository
     * @param context Request context to translate
     * @return Translated context if needed, original if not needed or if there is insufficient info
     */
    protected InternalRequestContext translateRepoRequestContext(InternalRequestContext context, Repo repo,
            String translatedPath) {
        String originalPath = context.getResourcePath();
        if (originalPath.equals(translatedPath)) {
            return context;
        }

        if (context instanceof NullRequestContext) {
            return new NullRequestContext(repo.getRepoPath(translatedPath));
        }

        ArtifactoryRequest artifactoryRequest = ((DownloadRequestContext) context).getRequest();
        RepoPath translatedRepoPath = InternalRepoPathFactory.create(artifactoryRequest.getRepoKey(), translatedPath);
        TranslatedArtifactoryRequest translatedRequest = new TranslatedArtifactoryRequest(translatedRepoPath,
                artifactoryRequest);
        return new DownloadRequestContext(translatedRequest);
    }
}
