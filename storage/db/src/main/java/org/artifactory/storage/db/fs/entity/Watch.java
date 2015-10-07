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

package org.artifactory.storage.db.fs.entity;

/**
 * Represents a record in the watches table.
 *
 * @author Yossi Shaul
 */
public class Watch {

    private final long watchId;
    private final long nodeId;
    private final String username;
    private final long since;

    public Watch(long watchId, long nodeId, String username, long since) {
        this.watchId = watchId;
        this.nodeId = nodeId;
        this.username = username;
        this.since = since;
    }

    public long getWatchId() {
        return watchId;
    }

    public long getNodeId() {
        return nodeId;
    }

    public String getUsername() {
        return username;
    }

    public long getSince() {
        return since;
    }
}
