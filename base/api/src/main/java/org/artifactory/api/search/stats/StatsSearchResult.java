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

package org.artifactory.api.search.stats;

import org.artifactory.api.search.artifact.ArtifactSearchResult;
import org.artifactory.fs.ItemInfo;
import org.artifactory.fs.StatsInfo;

/**
 * Date: 12/19/12
 * Time: 11:22 AM
 *
 * @author freds
 */
public class StatsSearchResult extends ArtifactSearchResult {
    private final StatsInfo statsInfo;

    public StatsSearchResult(ItemInfo itemInfo, StatsInfo statsInfo) {
        super(itemInfo);
        this.statsInfo = statsInfo;
    }

    public long getDownloadCount() {
        if (statsInfo == null) {
            return 0L;
        }
        return statsInfo.getDownloadCount();
    }

    public String getLastDownloadedBy() {
        if (statsInfo == null) {
            return getItemInfo().getModifiedBy();
        }
        return statsInfo.getLastDownloadedBy();
    }

    public long getLastDownloaded() {
        if (statsInfo == null) {
            return getItemInfo().getCreated();
        }
        return statsInfo.getLastDownloaded();
    }
}
