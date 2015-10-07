/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2014 JFrog Ltd.
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

package org.artifactory.repo.remote.interceptor;

import org.apache.http.client.methods.HttpRequestBase;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.fs.RepoResource;
import org.artifactory.repo.RepoPath;

import java.util.Map;

/**
 * @author Gidi Shabat
 */
public interface RemoteRepoInterceptor {

    /**
     * Called just before an attempt to download a resource from remote repository. All the interceptors must return
     * true in order for the download to proceed.
     *
     * @param descriptor     The remote repository descriptor
     * @param remoteRepoPath Repo path of the remote resource
     * @return True if downloading the specified resource is allowed
     */
    boolean isRemoteDownloadAllowed(RemoteRepoDescriptor descriptor, RepoPath remoteRepoPath);

    /**
     * Called just before an attempt to list remote repository content. All the interceptors must return
     * true in order list remote items.
     *
     * @param descriptor          The remote repository descriptor
     * @param remoteDirectoryPath Path of the remote directory
     * @return True if listing the remote repo is allowed
     */
    boolean isRemoteRepoListingAllowed(RemoteRepoDescriptor descriptor, String remoteDirectoryPath);

    /**
     * Called at the end of a successful remote download
     */
    void afterRemoteDownload(RepoResource remoteResource);

    /**
     * Provides a chance to modify a remote request and it's headers before the execution in
     * {@link org.artifactory.repo.HttpRepo.retrieveInfo} and
     * {@link org.artifactory.repo.HttpRepo.downloadResource}
     *
     * @param request The request about to be executed
     * @param headers The request headers
     */
    void beforeRemoteHttpMethodExecution(HttpRequestBase request, Map<String, String> headers);
}
