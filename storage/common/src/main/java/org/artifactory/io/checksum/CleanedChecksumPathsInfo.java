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

import com.google.common.collect.ImmutableCollection;

import java.io.Serializable;

/**
 * @author Yoav Landman
 */
public class CleanedChecksumPathsInfo implements Serializable {
    private final ImmutableCollection<String> checksums;
    private final long size;

    public CleanedChecksumPathsInfo(ImmutableCollection<String> checksums, long size) {
        this.checksums = checksums;
        this.size = size;
    }

    public ImmutableCollection<String> getChecksums() {
        return checksums;
    }

    public long getSize() {
        return size;
    }
}
