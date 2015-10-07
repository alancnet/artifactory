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

import org.artifactory.api.module.ModuleInfo;
import org.artifactory.api.repo.exception.FileExpectedException;
import org.artifactory.api.repo.exception.RepoRejectException;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.fs.RepoResource;
import org.artifactory.io.checksum.policy.ChecksumPolicy;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.request.InternalRequestContext;
import org.artifactory.resource.ResourceStreamHandle;

import java.io.IOException;

/**
 * @author Yoav Landman
 */
public interface Repo<T extends RepoDescriptor> {
    String getKey();

    String getDescription();

    void init();

    void destroy();

    /**
     * @see org.artifactory.descriptor.repo.RepoDescriptor#isReal()
     */
    boolean isReal();

    boolean isLocal();

    boolean isCache();

    InternalRepositoryService getRepositoryService();

    /**
     * Returns the resource info (not the resource content but the metadata)
     *
     * @param context Additional parameters to pass to the repository.
     * @return RepoResource. UnfoundRepoResource will be returned if the resource not found in this repo.
     */
    RepoResource getInfo(InternalRequestContext context) throws FileExpectedException;

    /**
     * Get the checksum of the resource based on the repository checksum policy (if there is one).
     *
     * @param checksumPath The url to the checksum file
     * @param res          The repo resource of the file the checksum is requested on
     * @return The checksum value. Might be null
     * @throws IOException If tried and failed to retrieve the checksum from db/remote source.
     */
    String getChecksum(String checksumPath, RepoResource res) throws IOException;

    ResourceStreamHandle getResourceStreamHandle(InternalRequestContext requestContext, RepoResource res)
            throws IOException, FileExpectedException, RepoRejectException;

    ModuleInfo getItemModuleInfo(String itemPath);

    ModuleInfo getDescriptorModuleInfo(String descriptorPath);

    ModuleInfo getArtifactModuleInfo(String artifactPath);

    RepoPath getRepoPath(String path);

    T getDescriptor();

    ChecksumPolicy getChecksumPolicy();

}