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

package org.artifactory.api.repo;

import org.artifactory.repo.RepoPath;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * This service supports the repository browsing done in the web UI.
 *
 * @author Tomer Cohen
 */
public interface RepositoryBrowsingService {
    /**
     * @param repoPath Path to a virtual repo item.
     * @return Virtual repo item with all real repo keys the real item exists and accessible to the current user. Null
     *         if not found or user has no permissions.
     */
    @Nullable
    VirtualRepoItem getVirtualRepoItem(RepoPath repoPath);

    /**
     * @param virtualFolderPath Path to a virtual repo folder.
     * @return List of virtual items (files and folders) under the virtual folders. Will return empty list if item
     *         doesn't exist, not a file of no permissions.
     */
    List<VirtualRepoItem> getVirtualRepoItems(RepoPath virtualFolderPath);

    /**
     * Creates a list of local and cached repo children items for all the simple browsers. <br> This method is not to be
     * used by non-ui clients for simple child discovery. It is also intended to serve only local and cache
     * repositories. Use {@link RepositoryService#getChildren(org.artifactory.repo.RepoPath)} instead.
     *
     * @param criteria Browsable item conditions
     * @return Null if given a non-existent or non-folder repo path. Otherwise, the list of children
     */
    @Nonnull
    List<BaseBrowsableItem> getLocalRepoBrowsableChildren(@Nonnull BrowsableItemCriteria criteria);

    /**
     * Get external browsable children from a remote repo. Using {@link org.apache.ivy.util.url.ApacheURLLister#listAll}
     * of Ivy in order to get a remote listing.
     *
     * @param criteria Browsable item conditions
     * @return A list of all browsable items (local and remote)
     */
    @Nonnull
    List<BaseBrowsableItem> getRemoteRepoBrowsableChildren(@Nonnull BrowsableItemCriteria criteria);

    /**
     * Creates a list of virtual repo children items for all the simple browsers.<br> This method is not to be used by
     * non-ui clients for simple child discovery. Use {@link RepositoryService#getVirtualRepoItems(org.artifactory.repo.RepoPath)}
     * instead.
     *
     * @param criteria Browsable item conditions
     * @return Null if given a non-existent or non-folder repo path. Otherwise, the list of children
     */
    @Nonnull
    List<BaseBrowsableItem> getVirtualRepoBrowsableChildren(@Nonnull BrowsableItemCriteria criteria);

    /**
     * Returns a browsable item for the given repo path. It also checks if the artifacts exists and if the repository is
     * blacked out.
     *
     * @param repoPath The item repo path (file or folder)
     * @return Browsable item for the repo path. Null if the item exists but the repository is blacked out.
     * @throws ItemNotFoundRuntimeException if the item is not found
     */
    @Nullable
    BrowsableItem getLocalRepoBrowsableItem(RepoPath repoPath);

    /**
     * Returns a virtual browsable item for the given repo path.
     *
     * @param repoPath The item repo path (file or folder)
     * @return Virtual browsable item for the repo path
     */
    @Nullable
    VirtualBrowsableItem getVirtualRepoBrowsableItem(RepoPath repoPath);

    /**
     * Creates a list of local and cached repo children items for all the simple browsers. <br> This method is not to be
     * used by non-ui clients for simple child discovery. It is also intended to serve only local and cache
     * repositories. Use {@link RepositoryService#getChildren(org.artifactory.repo.RepoPath)} instead.
     * the browsableItemAccept is update with indication if browsable item list is empty because have no Read rights
     * @param criteria Browsable item conditions
     * @return Null if given a non-existent or non-folder repo path. Otherwise, the list of children
     */
    @Nullable
    public List<BaseBrowsableItem> getLocalRepoBrowsableChildren(@Nonnull BrowsableItemCriteria criteria,
            boolean updateRootNodesFilterFlag,RootNodesFilterResult rootNodesFilterResult);

    /**
     * Get external browsable children from a remote repo. Using {@link org.apache.ivy.util.url.ApacheURLLister#listAll}
     * of Ivy in order to get a remote listing.
     * the browsableItemAccept is update with indication if browsable item list is empty because have no Read rights
     * @param criteria Browsable item conditions
     * @return A list of all browsable items (local and remote)
     */
    @Nullable
    public List<BaseBrowsableItem> getRemoteRepoBrowsableChildren(@Nonnull BrowsableItemCriteria criteria,boolean
            updateRootNodesFilterFlag , RootNodesFilterResult rootNodesFilterResult);


    /**
     * Creates a list of virtual repo children items for all the simple browsers.<br> This method is not to be used by
     * non-ui clients for simple child discovery. Use {@link RepositoryService#getVirtualRepoItems(org.artifactory.repo.RepoPath)}
     * instead.
     * the browsableItemAccept is update with indication if browsable item list is empty because have no Read rights
     * @param criteria Browsable item conditions
     * @return Null if given a non-existent or non-folder repo path. Otherwise, the list of children
     */
    @Nullable
    public List<BaseBrowsableItem> getVirtualRepoBrowsableChildren(@Nonnull BrowsableItemCriteria criteria,
            boolean updateRootNodesFilterFlag,RootNodesFilterResult rootNodesFilterResult);


    }
