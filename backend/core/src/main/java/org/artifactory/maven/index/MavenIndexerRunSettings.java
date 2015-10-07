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

package org.artifactory.maven.index;

import java.util.Date;
import java.util.List;

/**
 * Settings param for indexer task
 *
 * @author Shay Yaakov
 */
public class MavenIndexerRunSettings {

    private Date fireTime;
    private final boolean manualRun;
    private final boolean forceRemoteDownload;
    /**
     * If not null will index each repo inside the list.
     */
    private List<String> repoKeys;

    public MavenIndexerRunSettings(boolean manualRun, boolean forceRemoteDownload, List<String> repoKeys) {
        this.manualRun = manualRun;
        this.forceRemoteDownload = forceRemoteDownload;
        this.repoKeys = repoKeys;
    }

    public Date getFireTime() {
        return fireTime;
    }

    public boolean isManualRun() {
        return manualRun;
    }

    public boolean isForceRemoteDownload() {
        return forceRemoteDownload;
    }

    public List<String> getRepoKeys() {
        return repoKeys;
    }

    public void setFireTime(Date fireTime) {
        this.fireTime = fireTime;
    }
}
