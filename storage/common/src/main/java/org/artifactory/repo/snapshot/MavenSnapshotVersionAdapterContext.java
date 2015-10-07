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

package org.artifactory.repo.snapshot;

import org.artifactory.api.module.ModuleInfo;
import org.artifactory.repo.RepoPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A pass to value for the {@link MavenSnapshotVersionAdapter}.
 *
 * @author Yossi Shaul
 */
public class MavenSnapshotVersionAdapterContext {
    private static final Logger log = LoggerFactory.getLogger(MavenSnapshotVersionAdapterContext.class);

    private RepoPath repoPath;
    private ModuleInfo moduleInfo;
    private long timestamp;

    public MavenSnapshotVersionAdapterContext(RepoPath repoPath, ModuleInfo moduleInfo) {
        this.repoPath = repoPath;
        this.moduleInfo = moduleInfo;
    }

    public RepoPath getRepoPath() {
        return repoPath;
    }

    public ModuleInfo getModuleInfo() {
        return moduleInfo;
    }

    /**
     * @return A timestamp attached to the artifact deployed. Can be used by the adapter.
     */
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        try {
            this.timestamp = Long.parseLong(timestamp);
        } catch (NumberFormatException e) {
            log.error("Timestamp is not a valid long: " + timestamp);
        }
    }
}
