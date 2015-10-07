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

package org.artifactory.model.xstream.fs;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.artifactory.fs.MutableStatsInfo;
import org.artifactory.fs.StatsInfo;
import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.repo.RepoPath;

/**
 * @author Yoav Landman
 */
@XStreamAlias(StatsImpl.ROOT)
public class StatsImpl implements MutableStatsInfo {

    private long downloadCount;
    private long lastDownloaded;
    private String lastDownloadedBy;

    private long remoteDownloadCount;
    private long remoteLastDownloaded;
    private String remoteLastDownloadedBy;

    private String repoPath;

    public StatsImpl() {
    }

    public StatsImpl(StatsInfo statsInfo) {
        this.downloadCount = statsInfo.getDownloadCount();
    }

    @Override
    public long getDownloadCount() {
        return downloadCount;
    }

    @Override
    public void setDownloadCount(long downloadCount) {
        this.downloadCount = downloadCount;
    }

    @Override
    public long getLastDownloaded() {
        return lastDownloaded;
    }

    @Override
    public void setLastDownloaded(long lastDownloaded) {
        this.lastDownloaded = lastDownloaded;
    }

    @Override
    public String getLastDownloadedBy() {
        return lastDownloadedBy;
    }

    @Override
    public void setLastDownloadedBy(String lastDownloadedBy) {
        this.lastDownloadedBy = lastDownloadedBy;
    }

    @Override
    public void setRemoteDownloadCount(long remoteDownloadCount) {
        this.remoteDownloadCount = remoteDownloadCount;
    }

    @Override
    public void setRemoteLastDownloaded(long remoteLastDownloaded) {
        this.remoteLastDownloaded = remoteLastDownloaded;
    }

    @Override
    public void setRemoteLastDownloadedBy(String remoteLastDownloadedBy) {
        this.remoteLastDownloadedBy = remoteLastDownloadedBy;
    }

    public String getRepoPath() {
        return repoPath;
    }

    public void setRepoPath(String repoPath) {
        this.repoPath = repoPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StatsImpl stats = (StatsImpl) o;

        if (downloadCount != stats.downloadCount) {
            return false;
        }
        if (lastDownloaded != stats.lastDownloaded) {
            return false;
        }
        if (lastDownloadedBy != null ? !lastDownloadedBy.equals(stats.lastDownloadedBy) :
                stats.lastDownloadedBy != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {

        int result = (int) (downloadCount ^ (downloadCount >>> 32));
        result = 31 * result + (int) (lastDownloaded ^ (lastDownloaded >>> 32));
        result = 31 * result + (lastDownloadedBy != null ? lastDownloadedBy.hashCode() : 0);

        result = 31 * result + (int) (remoteDownloadCount ^ (remoteDownloadCount >>> 32));
        result = 31 * result + (int) (remoteLastDownloaded ^ (remoteLastDownloaded >>> 32));
        result = 31 * result + (remoteLastDownloadedBy != null ? remoteLastDownloadedBy.hashCode() : 0);

        result = 31 * result + (repoPath != null ? repoPath.hashCode() : 0);

        return result;
    }

    @Override
    public long getRemoteDownloadCount() {
        return remoteDownloadCount;
    }

    @Override
    public long getRemoteLastDownloaded() {
        return remoteLastDownloaded;
    }

    @Override
    public String getRemoteLastDownloadedBy() {
        return remoteLastDownloadedBy;
    }
}