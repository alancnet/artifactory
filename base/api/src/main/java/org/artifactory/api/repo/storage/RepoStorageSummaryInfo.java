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

package org.artifactory.api.repo.storage;

import javax.annotation.Nonnull;
import java.io.Serializable;

/**
 * Represents a repository nodes summary.
 *
 * @author Yossi Shaul
 */
public class RepoStorageSummaryInfo implements Serializable {

    public enum RepositoryType {
        LOCAL,
        CACHE,
        REMOTE,
        VIRTUAL,
        BROKEN, // Used to indicate that the repository's type could not be determined
        NA      // To be used with deliberately not stating type
    }

    private final String repoKey;
    private final RepositoryType repoType;
    private final long foldersCount;
    private final long filesCount;
    private final long usedSpace;
    private long itemsCount;
    private String type;

    /**
     * Creates a new empty repository summary.
     *
     * @param repoKey      The repository key
     * @param repoType
     * @param foldersCount Folders count of this repository
     * @param filesCount   Files count of this repository
     * @param usedSpace    Space, in bytes, used by the files in this repository
     */
    public RepoStorageSummaryInfo(@Nonnull String repoKey, RepositoryType repoType, long foldersCount, long filesCount,
            long usedSpace, String type) {
        this.repoKey = repoKey;
        this.repoType = repoType;

        this.filesCount = filesCount;
        this.foldersCount = foldersCount;
        this.usedSpace = usedSpace;
        this.type = type;
    }

    /**
     * @return The repository key
     */
    public String getRepoKey() {
        return repoKey;
    }

    /**
     * @return The repository type (Local, Cache, Remote, Virtual)
     */
    public RepositoryType getRepoType() {
        return repoType;
    }

    /**
     * @return Folders count of this repository (the repository folder itself is not included)
     */
    public long getFoldersCount() {
        return foldersCount;
    }

    /**
     * @return Files count of this repository
     */
    public long getFilesCount() {
        return filesCount;
    }

    /**
     * @return Space used by the files in this repository. In bytes.
     */
    public long getUsedSpace() {
        return usedSpace;
    }

    /**
     * @return Total items in the repository (sum of the folders and files count)
     */
    public long getItemsCount() {
        return getFoldersCount() + getFilesCount();
    }

    public String getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RepoStorageSummaryInfo that = (RepoStorageSummaryInfo) o;

        if (!repoKey.equals(that.repoKey)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return repoKey.hashCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RepoStorageSummaryInfo{");
        sb.append("repoKey='").append(repoKey).append('\'');
        sb.append(", repoType=").append(repoType);
        sb.append(", filesCount=").append(filesCount);
        sb.append(", foldersCount=").append(foldersCount);
        sb.append(", usedSpace=").append(usedSpace);
        sb.append('}');
        return sb.toString();
    }

}
