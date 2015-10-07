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
package org.artifactory.io.checksum;

import java.io.Serializable;

/**
 * @author Yoav Landman
 */
public class ChecksumPathInfo implements Serializable {
    private final String path;
    private final String checksum;
    private final long size;
    private final String binaryNodeId;
    private final long timestamp;

    public ChecksumPathInfo(String path, String checksum, long size, String binaryNodeId) {
        this(path, checksum, size, binaryNodeId, -1);
    }

    public ChecksumPathInfo(String path, String checksum, long size, String binaryNodeId, long timestamp) {
        this.path = path;
        this.checksum = checksum;
        this.size = size;
        this.binaryNodeId = binaryNodeId;
        this.timestamp = timestamp;
    }

    public String getPath() {
        return path;
    }

    public String getChecksum() {
        return checksum;
    }

    public long getSize() {
        return size;
    }

    public String getBinaryNodeId() {
        return binaryNodeId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "ChecksumPathInfo{" +
                "path='" + path + '\'' +
                ", checksum='" + checksum + '\'' +
                ", size=" + size +
                ", binaryNodeId='" + binaryNodeId + '\'' +
                ", created=" + timestamp +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ChecksumPathInfo info = (ChecksumPathInfo) o;

        if (timestamp != info.timestamp) {
            return false;
        }
        if (!binaryNodeId.equals(info.binaryNodeId)) {
            return false;
        }
        if (checksum != null ? !checksum.equals(info.checksum) : info.checksum != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = checksum != null ? checksum.hashCode() : 0;
        result = 31 * result + binaryNodeId.hashCode();
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        return result;
    }
}
