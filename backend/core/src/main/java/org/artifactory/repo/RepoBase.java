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

package org.artifactory.repo;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.filteredresources.FilteredResourcesAddon ;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.module.ModuleInfo;
import org.artifactory.api.module.ModuleInfoUtils;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.RepoLayout;
import org.artifactory.fs.RepoResource;
import org.artifactory.mime.MavenNaming;
import org.artifactory.mime.NamingUtils;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.spring.InternalContextHelper;
import org.artifactory.util.GlobalExcludes;
import org.artifactory.util.PathMatcher;
import org.artifactory.util.PathUtils;

import java.io.IOException;
import java.util.List;

public abstract class RepoBase<T extends RepoDescriptor> implements Repo<T> {
    private final T descriptor;
    private final InternalRepositoryService repositoryService;
    protected List<String> excludes;
    private List<String> includes;

    protected RepoBase(T descriptor, InternalRepositoryService repositoryService) {
        this.repositoryService = repositoryService;
        this.descriptor = descriptor;
        this.includes = PathUtils.includesExcludesPatternToStringList(descriptor.getIncludesPattern());
        this.excludes = PathUtils.includesExcludesPatternToStringList(descriptor.getExcludesPattern());
        excludes.addAll(GlobalExcludes.getGlobalExcludes());
    }

    @Override
    public T getDescriptor() {
        return descriptor;
    }

    @Override
    public RepoPath getRepoPath(String path) {
        return InternalRepoPathFactory.create(getKey(), path);
    }

    @Override
    public InternalRepositoryService getRepositoryService() {
        return repositoryService;
    }

    @Override
    public ModuleInfo getItemModuleInfo(String itemPath) {
        ModuleInfo moduleInfo = getDescriptorModuleInfo(itemPath);

        if (!moduleInfo.isValid()) {
            moduleInfo = getArtifactModuleInfo(itemPath);
        }

        return moduleInfo;
    }

    @Override
    public ModuleInfo getArtifactModuleInfo(String artifactPath) {
        RepoLayout repoLayout = getDescriptor().getRepoLayout();
        if (org.apache.commons.lang.StringUtils.isBlank(artifactPath) || (repoLayout == null)) {
            return new ModuleInfo();
        }
        return ModuleInfoUtils.moduleInfoFromArtifactPath(artifactPath, getDescriptor().getRepoLayout());
    }

    @Override
    public ModuleInfo getDescriptorModuleInfo(String descriptorPath) {
        RepoLayout repoLayout = getDescriptor().getRepoLayout();
        if (org.apache.commons.lang.StringUtils.isBlank(descriptorPath) || (repoLayout == null)) {
            return new ModuleInfo();
        }
        return ModuleInfoUtils.moduleInfoFromDescriptorPath(descriptorPath, repoLayout);
    }

    @Override
    public String getKey() {
        return descriptor.getKey();
    }

    @Override
    public String getDescription() {
        return descriptor.getDescription();
    }

    @Override
    public boolean isReal() {
        return getDescriptor().isReal();
    }

    @Override
    public boolean isCache() {
        return false;
    }

    public boolean accepts(RepoPath repoPath) {
        if (repoPath.isRoot()) {
            return true;    // always accepts the root path
        }
        String path = repoPath.getPath();
        if (NamingUtils.isSystem(path)) {
            // includes/excludes should not affect system paths
            return true;
        }

        String toCheck = path;
        //For artifactory metadata the pattern apply to the object it represents
        if (NamingUtils.isProperties(path)) {
            toCheck = NamingUtils.getMetadataParentPath(path);
        } else if (NamingUtils.isChecksum(path)) {
            toCheck = FilenameUtils.removeExtension(path);
        }
        return isPathPatternValid(repoPath, toCheck);
    }

    /**
     * @param repoPath repo path to validate
     * @param path       folder path
     * @return True if the path pattern is valid according to include / exclude policy
     */
    public boolean isPathPatternValid(RepoPath repoPath, String path) {
        return StringUtils.isBlank(path) || PathMatcher.matches(path, includes, excludes, repoPath.isFolder());
    }

    @Override
    public String getChecksum(String checksumPath, RepoResource resource) throws IOException {
        if (resource == null || !resource.isFound()) {
            throw new IOException("Could not get resource stream. Path not found: " + checksumPath + ".");
        }
        ChecksumType checksumType = ChecksumType.forFilePath(checksumPath);
        if (checksumType == null) {
            throw new IllegalArgumentException("Checksum type not found for path " + checksumPath);
        }
        return getChecksumPolicy().getChecksum(checksumType, resource.getInfo().getChecksums(), resource.getRepoPath());
    }

    @Override
    public String toString() {
        return getKey();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RepoBase)) {
            return false;
        }
        RepoBase base = (RepoBase) o;
        return descriptor.equals(base.descriptor);
    }

    @Override
    public int hashCode() {
        return descriptor.hashCode();
    }

    @Override
    public void destroy() {
    }

    protected final AuthorizationService getAuthorizationService() {
        return InternalContextHelper.get().getAuthorizationService();
    }

    /**
     * Checks if the resource should not be cached by the requesting end, and marks it expirable if so.
     *
     * @param res resource to be checked
     */
    protected void checkAndMarkExpirableResource(RepoResource res) {
        boolean markAsExpired = false;
        if (res.isFound() && !res.isExpirable()) {
            //Maven metadata
            if (MavenNaming.isMavenMetadata(res.getRepoPath().getPath())) {
                markAsExpired = true;
            }
            //Maven non-unique snapshot
            if (MavenNaming.isNonUniqueSnapshot(res.getRepoPath().getPath())) {
                markAsExpired = true;
            }
            //Filtered Resource
            FilteredResourcesAddon filteredResourcesAddon = ContextHelper.get().beanForType(AddonsManager.class)
                    .addonByType(FilteredResourcesAddon.class);
            if (filteredResourcesAddon.isFilteredResourceFile(res.getRepoPath())) {
                markAsExpired = true;
            }

            if (markAsExpired) {
                res.expirable();
            }
        }
    }
}