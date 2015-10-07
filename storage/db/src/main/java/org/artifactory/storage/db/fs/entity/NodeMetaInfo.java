/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2013 JFrog Ltd.
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
 * Represents a metadata information on a node.
 *
 * @author Yossi Shaul
 */
public class NodeMetaInfo {

    // node id
    private final long nodeId;
    // last modification date (millis)
    private final long propsModified;
    // username of the user last modified the properties
    private final String propsModifiedBy;

    public NodeMetaInfo(long nodeId, long propsModified, String propsModifiedBy) {
        this.nodeId = nodeId;
        this.propsModified = propsModified;
        this.propsModifiedBy = propsModifiedBy;
    }

    public long getNodeId() {
        return nodeId;
    }

    public long getPropsModified() {
        return propsModified;
    }

    public String getPropsModifiedBy() {
        return propsModifiedBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NodeMetaInfo that = (NodeMetaInfo) o;

        if (nodeId != that.nodeId) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (nodeId ^ (nodeId >>> 32));
    }
}
