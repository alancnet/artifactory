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

import org.artifactory.fs.FileInfo;
import org.artifactory.fs.MutableItemInfo;
import org.artifactory.repo.RepoPath;

import java.util.List;

/**
 * Virtual repo display item for all the simple browsers
 *
 * @author Noam Y. Tenne
 */
public class VirtualBrowsableItem extends BaseBrowsableItem {

    private static final long serialVersionUID = 1L;

    private RepoPath repoPath;
    private List<String> repoKeys;

    /**
     * Main constructor.<br> Please use factory methods for normal object creation.
     *
     * @param name         Item display name
     * @param folder       True if the item represents a folder
     * @param created      Item creation time
     * @param lastModified Item last modified time
     * @param size         Item size (applicable only to files)
     * @param repoPath     Item repo path
     * @param repoKeys     List of keys of the repos that the item appears in
     */
    public VirtualBrowsableItem(String name, boolean folder, long created, long lastModified, long size,
            RepoPath repoPath,
            List<String> repoKeys) {
        super(name, folder, created, lastModified, size);
        this.repoPath = repoPath;
        this.repoKeys = repoKeys;
    }

    /**
     * Creates a standard browsable item
     *
     * @param repoPath Item's virtual repo path
     * @param itemInfo Backing item info
     * @param repoKeys List of keys of the repos that the item appears in
     * @return Browsable item
     */
    public static <T extends MutableItemInfo> VirtualBrowsableItem getItem(RepoPath repoPath, T itemInfo,
            List<String> repoKeys) {
        if (itemInfo.isFolder()) {
            return new VirtualBrowsableItem(itemInfo.getName(), true, itemInfo.getCreated(),
                    itemInfo.getLastModified(), 0, repoPath, repoKeys);
        }
        return new VirtualBrowsableItem(itemInfo.getName(), false, itemInfo.getCreated(), itemInfo.getLastModified(),
                ((FileInfo) itemInfo).getSize(), repoPath, repoKeys);
    }

    @Override
    public RepoPath getRepoPath() {
        return repoPath;
    }

    @Override
    public String getRepoKey() {
        return repoPath.getRepoKey();
    }

    public void addRepoKey(String repoKey) {
        repoKeys.add(repoKey);
    }

    @Override
    public String getRelativePath() {
        return repoPath.getPath();
    }

    public List<String> getRepoKeys() {
        return repoKeys;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        VirtualBrowsableItem item = (VirtualBrowsableItem) o;
        return repoPath.getPath().equals(item.getRelativePath());
    }

    @Override
    public int hashCode() {
        return repoPath.getPath().hashCode();
    }

    @Override
    public int compareTo(BaseBrowsableItem o) {

        if (name.equals(UP) || (isFolder() && !o.isFolder())) {
            return -1;
        }

        if (o.name.equals(UP) || (!isFolder() && o.isFolder())) {
            return 1;
        }

        return this.repoPath.getPath().compareTo(o.getRelativePath());
    }
}
