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

package org.artifactory.api.storage;

import java.io.Serializable;

/**
 * Simple value object to store stored binaries info.
 *
 * @author Yossi Shaul
 */
public class BinariesInfo implements Serializable {
    private final long binariesCount;
    private final long binariesSize;

    public BinariesInfo(long binariesCount, long binariesSize) {
        this.binariesCount = binariesCount;
        this.binariesSize = binariesSize;
    }

    /**
     * @return Number of binaries
     */
    public long getBinariesCount() {
        return binariesCount;
    }

    /**
     * @return Total size, in bytes, of the binaries
     */
    public long getBinariesSize() {
        return binariesSize;
    }
}
