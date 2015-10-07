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

package org.artifactory.repo.virtual.interceptor;

import org.apache.commons.io.IOUtils;
import org.artifactory.api.repo.exception.RepoRejectException;
import org.artifactory.api.repo.exception.maven.BadPomException;
import org.artifactory.descriptor.repo.PomCleanupPolicy;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.fs.MutableFileInfo;
import org.artifactory.fs.RepoResource;
import org.artifactory.mime.MavenNaming;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.Repo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.SaveResourceContext;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.repo.virtual.VirtualRepo;
import org.artifactory.repo.virtual.interceptor.transformer.PomTransformer;
import org.artifactory.request.InternalRequestContext;
import org.artifactory.resource.FileResource;
import org.artifactory.resource.ResourceStreamHandle;
import org.artifactory.resource.UnfoundRepoResource;
import org.artifactory.storage.StorageException;
import org.artifactory.util.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;

/**
 * Intercepts pom resources, transforms the pom according to the policy and saves it to the local storage.
 *
 * @author Eli Givoni
 */
@Component
public class PomInterceptor extends VirtualRepoInterceptorBase {
    private static final Logger log = LoggerFactory.getLogger(PomInterceptor.class);

    @Autowired
    private InternalRepositoryService repoService;

    @Override
    @Nonnull
    public RepoResource onBeforeReturn(VirtualRepo virtualRepo, InternalRequestContext context, RepoResource resource) {
        String resourcePath = resource.getResponseRepoPath().getPath();
        // intercept only poms and not if it is the global virtual repo
        if (VirtualRepoDescriptor.GLOBAL_VIRTUAL_REPO_KEY.equals(virtualRepo.getKey())
                || !MavenNaming.isPom(context.getResourcePath())) {
            return resource;
        }

        PomCleanupPolicy cleanupPolicy = virtualRepo.getPomRepositoryReferencesCleanupPolicy();
        if (cleanupPolicy.equals(PomCleanupPolicy.nothing)) {
            return resource;
        }

        String pomContent;
        try {
            pomContent = transformPomResource(context, resource, virtualRepo);
        } catch (RepoRejectException rre) {
            String message = "Failed to transform pom file";
            log.debug(message, rre);
            return new UnfoundRepoResource(resource.getRepoPath(), message + ": " + rre.getMessage(),
                    rre.getErrorCode());
        } catch (IOException e) {
            String message = "Failed to transform pom file";
            if (ExceptionUtils.getRootCause(e) instanceof BadPomException) {
                log.error(message + ":" + e.getMessage());
            } else {
                log.debug(message, e);
                log.error(e.getMessage());
            }
            return new UnfoundRepoResource(resource.getRepoPath(), message + ": " + e.getMessage());
        } catch (StorageException e) {
            String message = "Failed to transform pom file";
            log.debug(message, e);
            log.error(message + ":" + e.getMessage());
            return new UnfoundRepoResource(resource.getRepoPath(), message + ": " + e.getMessage());
        }

        RepoPath localStoragePath = InternalRepoPathFactory.create(virtualRepo.getKey(), resourcePath);
        MutableFileInfo fileInfo = InfoFactoryHolder.get().createFileInfo(localStoragePath);
        long now = System.currentTimeMillis();
        fileInfo.setCreated(now);
        fileInfo.setLastModified(now);
        fileInfo.createTrustedChecksums();
        fileInfo.setSize(pomContent.length());
        RepoResource transformedResource = new FileResource(fileInfo);

        try {
            SaveResourceContext saveResourceContext = new SaveResourceContext.Builder(transformedResource,
                    IOUtils.toInputStream(pomContent, "utf-8")).build();
            transformedResource = repoService.saveResource(virtualRepo, saveResourceContext);
        } catch (IOException e) {
            String message = "Failed to import file to local storage";
            log.error(message, e);
            return new UnfoundRepoResource(resource.getRepoPath(), message + ": " + e.getMessage());
        } catch (RepoRejectException rre) {
            String message = "Failed to import file to local storage";
            log.debug(message, rre);
            return new UnfoundRepoResource(resource.getRepoPath(), message + ": " + rre.getMessage(),
                    rre.getErrorCode());
        }

        return transformedResource;
    }

    private String transformPomResource(InternalRequestContext context, RepoResource resource, VirtualRepo virtualRepo)
            throws IOException, RepoRejectException {
        String repoKey = resource.getResponseRepoPath().getRepoKey();
        Repo repository = repoService.repositoryByKey(repoKey);
        ResourceStreamHandle handle;
        handle = repoService.getResourceStreamHandle(context, repository, resource);

        InputStream inputStream = handle.getInputStream();
        String pomAsString = "";
        if (inputStream != null) {
            try {
                pomAsString = IOUtils.toString(inputStream, "utf-8");
            } finally {
                IOUtils.closeQuietly(inputStream);
            }
        }
        PomCleanupPolicy cleanupPolicy = virtualRepo.getPomRepositoryReferencesCleanupPolicy();
        PomTransformer transformer = new PomTransformer(pomAsString, cleanupPolicy);
        return transformer.transform();
    }
}