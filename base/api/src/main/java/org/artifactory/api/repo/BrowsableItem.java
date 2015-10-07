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

import org.artifactory.checksum.ChecksumType;
import org.artifactory.fs.FileInfo;
import org.artifactory.fs.ItemInfo;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;

/**
 * Local\cache repo display item for all the simple browsers
 *
 * @author Noam Y. Tenne
 */
public class BrowsableItem extends BaseBrowsableItem {

    private static final long serialVersionUID = 1L;

    private RepoPath repoPath;

    /**
     * Main constructor.<br> Please use factory methods for normal object creation.
     *
     * @param name         Item display name
     * @param folder       True if the item represents a folder
     * @param created
     * @param lastModified Item last modified time
     * @param size         Item size (applicable only to files)
     * @param repoPath     Item repo path
     */
    public BrowsableItem(String name, boolean folder, long created, long lastModified, long size, RepoPath repoPath) {
        super(name, folder, created, lastModified, size);
        this.repoPath = repoPath;
    }

    /**
     * Creates a standard browsable item
     *
     * @param itemInfo Backing item info
     * @return Browsable item
     */
    public static <T extends ItemInfo> BrowsableItem getItem(T itemInfo) {
        if (itemInfo.isFolder()) {
            return new BrowsableItem(itemInfo.getName(), true, itemInfo.getCreated(),
                    itemInfo.getLastModified(), 0, itemInfo.getRepoPath());
        }
        return new BrowsableItem(itemInfo.getName(), false, itemInfo.getCreated(),
                itemInfo.getLastModified(), ((FileInfo) itemInfo).getSize(), itemInfo.getRepoPath()
        );
    }

    /**
     * Creates a checksum browsable item
     *
     * @param browsableItem       Browsable item file that the checksum belongs to
     * @param checksumType        Type of checksum to create for
     * @param checksumValueLength Byte length of checksum value
     * @return Browsable item
     */
    public static BrowsableItem getChecksumItem(BrowsableItem browsableItem, ChecksumType checksumType,
            long checksumValueLength) {
        String checksumItemName = browsableItem.getName() + checksumType.ext();
        RepoPath repoPath = InternalRepoPathFactory.create(browsableItem.getRepoKey(),
                browsableItem.getRelativePath() + checksumType.ext());

        return new BrowsableItem(checksumItemName, false, browsableItem.getCreated(),
                browsableItem.getLastModified(), checksumValueLength, repoPath);
    }

    @Override
    public RepoPath getRepoPath() {
        return repoPath;
    }

    @Override
    public String getRepoKey() {
        return repoPath.getRepoKey();
    }

    @Override
    public String getRelativePath() {
        return repoPath.getPath();
    }

    @Override
    public int compareTo(BaseBrowsableItem o) {
        if (name.equals(o.name)) {
            return 0;
        }
        if (name.equals(UP) || (isFolder() && !o.isFolder())) {
            return -1;
        }
        if (o.name.equals(UP) || (!isFolder() && o.isFolder())) {
            return 1;
        }
        return name.compareTo(o.name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BrowsableItem)) {
            return false;
        }

        BrowsableItem that = (BrowsableItem) o;

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
