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

package org.artifactory.repo.service;

import com.google.common.collect.Lists;
import org.artifactory.fs.FolderInfo;
import org.artifactory.fs.ItemInfo;
import org.artifactory.repo.RepoPath;
import org.artifactory.storage.fs.service.PropertiesService;
import org.artifactory.storage.fs.service.WatchesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Builds a chain of compactable folders beyond a given one
 *
 * @author Noam Tenne
 */
@Component
public class FolderCompactor {

    @Autowired
    private InternalRepositoryService repositoryService;

    @Autowired
    private PropertiesService propertiesService;

    @Autowired
    private WatchesService watchesService;

    /**
     * Returns A list containing the given folder, and any folder beyond it that can be compacted with it.
     *
     * @param folder Folder to check beyond.
     * @return List of the of folders that can be compacted
     */
    public List<FolderInfo> getFolderWithCompactedChildren(FolderInfo folder) {
        List<FolderInfo> result = Lists.newArrayList();
        FolderInfo current = folder;
        while (current != null) {
            result.add(current);
            current = getNextCompactedFolder(current);
        }
        return result;
    }

    /**
     * Returns the next folder that can be compacted after the given one.
     *
     * @param folder Folder to check beyond.
     * @return FolderInfo if another folder to compact is found. Null if no folder is found.
     */
    private FolderInfo getNextCompactedFolder(FolderInfo folder) {
        RepoPath repoPath = folder.getRepoPath();

        // don't compact if folder has properties or watches to allow users to view them
        if (propertiesService.hasProperties(repoPath) || watchesService.hasWatches(repoPath)) {
            return null;
        }

        List<ItemInfo> children = repositoryService.getChildren(repoPath);

        //If we have no children or more than 1 children stop here
        if (children.size() != 1) {
            return null;
        }

        //If we have 1 child stop at it if it's a folder
        ItemInfo theOneChild = children.get(0);
        if (theOneChild.isFolder()) {
            return (FolderInfo) theOneChild;
        }

        return null;
    }
}
