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

import org.artifactory.common.StatusHolder;
import org.artifactory.fs.FileInfo;
import org.artifactory.fs.FileLayoutInfo;
import org.artifactory.fs.ItemInfo;
import org.artifactory.fs.StatsInfo;
import org.artifactory.md.Properties;
import org.artifactory.resource.ResourceStreamHandle;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

/**
 * Public API for working with repositories
 */
@SuppressWarnings("UnusedDeclaration")
public interface Repositories {

    /**
     * Returns a list of keys of local repositories
     *
     * @return keys of local repositories
     */
    public List<String> getLocalRepositories();

    /**
     * Returns a list of keys of remote repositories
     *
     * @return keys of remote repositories
     */
    public List<String> getRemoteRepositories();

    /**
     * Returns a list of keys of virtual repositories
     *
     * @return keys of virtual repositories
     */
    public List<String> getVirtualRepositories();

    RepositoryConfiguration getRepositoryConfiguration(String repoKey);

    /**
     * @param repoPath Repository path of the item
     * @return Folder or file info. Throws exception if the path doesn't exist.
     */
    ItemInfo getItemInfo(RepoPath repoPath);

    /**
     * @param repoPath Repository path of the file
     * @return The file info. Throws exception if the path doesn't exist or it doesn't point to a file.
     */
    FileInfo getFileInfo(RepoPath repoPath);

    List<ItemInfo> getChildren(RepoPath repoPath);

    /**
     * Get the content of a file as a string
     *
     * @param fileInfo
     * @return The file's content as string
     * @deprecated Use {@link #getStringContent(RepoPath)} ()}
     */
    @Deprecated
    String getStringContent(FileInfo fileInfo);

    /**
     * Get the content of a file as a string
     *
     * @param repoPath The repoPath of the file
     * @return The file's content as string
     * @since 2.4.0
     */
    String getStringContent(RepoPath repoPath);

    /**
     * Get a stream handle for the file content
     *
     * @param repoPath The repoPath of the file
     * @return The content stream handle for an existing file or a null-stream handler for a non exiting one.<br/> Note:
     *         The user <i>must</i> manually call {@link ResourceStreamHandle#close()} on the resourceStreamHandle after
     *         usage, to avoid leaking resources!
     * @since 2.4.0
     */
    ResourceStreamHandle getContent(RepoPath repoPath);

    Properties getProperties(RepoPath repoPath);

    boolean hasProperty(RepoPath repoPath, String propertyName);

    public Set<String> getPropertyValues(RepoPath repoPath, String propertyName);

    public String getProperty(RepoPath repoPath, String propertyName);

    Properties setProperty(RepoPath repoPath, String propertyName, String... values);

    Properties setPropertyRecursively(RepoPath repoPath, String propertyName, String... values);

    /**
     * Deletes the property from the item.
     *
     * @param repoPath     The item repo path
     * @param propertyName Property name to delete
     */
    void deleteProperty(RepoPath repoPath, String propertyName);


    boolean exists(RepoPath repoPath);

    /**
     * Deploy an artifact
     *
     * @param repoPath
     * @param inputStream
     * @return The result status for the deploy operation
     */
    StatusHolder deploy(RepoPath repoPath, InputStream inputStream);

    /**
     * Deletes the specified repoPath
     *
     * @param repoPath The repository path to delete
     * @return Deletion status
     * @since 2.4.0
     */
    StatusHolder delete(RepoPath repoPath);

    /**
     * @param repoPath
     * @return Result of the undeploy operation
     * @deprecated Use {@link #delete(RepoPath)} instead
     */
    @Deprecated
    StatusHolder undeploy(RepoPath repoPath);

    /**
     * Checks if the specified repoPath is handled by the snapshot(integration)/release policy of the repoPath's
     * repository.
     *
     * @param repoPath
     * @return True if repoPath is handled by the snapshot(integration)/release policy of the repoPath's repository
     */
    boolean isRepoPathHandled(RepoPath repoPath);

    /**
     * @deprecated Use {@link #isRepoPathHandled(RepoPath)} ()}
     */
    @Deprecated
    boolean isLcoalRepoPathHandled(RepoPath repoPath);

    /**
     * Checks if the specified repoPath is accepted by the include/exclude rules of the repoPath's repository.
     *
     * @param repoPath
     * @return True if the specified repoPath is accepted by the include/exclude rules of the repoPath's repository
     */
    boolean isRepoPathAccepted(RepoPath repoPath);


    /**
     * @deprecated Use {@link #isRepoPathAccepted(RepoPath)} ()}
     */
    @Deprecated
    boolean isLocalRepoPathAccepted(RepoPath repoPath);

    /**
     * Moves the source repoPath to the targetRepoPath
     *
     * @param source - A source repository path
     * @param target - A target repository path
     * @return The result status for the move operation
     */
    StatusHolder move(RepoPath source, RepoPath target);

    /**
     * Copies the source repoPath to the targetRepoPath
     *
     * @param source - A source repository path
     * @param target - A target repository path
     * @return The result status for the copy operation
     */
    StatusHolder copy(RepoPath source, RepoPath target);

    /**
     * Returns module related information (group, artifact, version, etc.) for given file, as it was extracted according
     * to the layout of the repository the file is part of.
     * * @param repoPath the file path
     *
     * @return the file layout information, which can be empty in case of incorrect repoPath or when the layout can't be determined
     */
    public FileLayoutInfo getLayoutInfo(RepoPath repoPath);

    /**
     * Translates the path of a file from source repository layout to target's one.
     *
     * @param source        the source repository path
     * @param targetRepoKey the target repository key
     * @return the file path according to target repository layout
     */
    String translateFilePath(RepoPath source, String targetRepoKey);

    /**
     * Returns actual repository path for layout information (group, artifact, version, etc.) of an artifact
     * as it is built according to the layout of the repository.
     *
     * @param layoutInfo the layout information to build the path from
     * @param repoKey    the repository the path will be in
     * @return the repository path to the file
     */
    RepoPath getArtifactRepoPath(FileLayoutInfo layoutInfo, String repoKey);

    /**
     * Returns actual repository path for layout information (group, artifact, version, etc.) of a descriptor
     * as it is built according to the layout of the repository.
     *
     * @param layoutInfo the layout information to build the path from
     * @param repoKey    the repository the path will be in
     * @return the repository path to the file
     */
    RepoPath getDescriptorRepoPath(FileLayoutInfo layoutInfo, String repoKey);

    /**
     * Returns the total number of file artifacts under the provided directory repo path. Repository root repo path will
     * return the total number of artifacts in the given repository.
     *
     * @param repoPath Repo path to count artifacts under
     * @return Total number of artifacts under the given repo path.
     */
    long getArtifactsCount(RepoPath repoPath);

    /**
     * Returns the total storage size of the file artifacts under the given directory repo path.
     *
     * @param repoPath Repo path to return total storage under
     * @return Total size (in bytes) of the artifacts under the given repo path
     */
    long getArtifactsSize(RepoPath repoPath);

    /**
     * Returns statistics for the {@code repoPath}, which include downloads count, last download time and last
     * downloader's name.
     * @param repoPath to return downloads statistics for
     * @return {@link StatsInfo} which provides data about downloads, may be null
     */
    @Nullable
    StatsInfo getStats(RepoPath repoPath);
}
