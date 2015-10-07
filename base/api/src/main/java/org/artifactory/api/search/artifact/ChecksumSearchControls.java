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

package org.artifactory.api.search.artifact;

import org.artifactory.api.search.SearchControlsBase;
import org.artifactory.checksum.ChecksumType;

import java.util.EnumMap;

/**
 * Search controls to be used for checksum searches
 *
 * @author Noam Y. Tenne
 */
public class ChecksumSearchControls extends SearchControlsBase {

    private EnumMap<ChecksumType, String> checksums;

    public ChecksumSearchControls() {
    }

    @Override
    public boolean isEmpty() {
        return checksums == null || checksums.isEmpty();
    }

    @Override
    public boolean isWildcardsOnly() {
        if (isEmpty()) {
            return true;
        }
        if (checksums != null) {
            for (String checksum : checksums.values()) {
                if (isWildcardsOnly(checksum)) {
                    return true;
                }
            }
        }
        return false;
    }

    public EnumMap<ChecksumType, String> getChecksums() {
        return checksums;
    }

    public void addChecksum(ChecksumType checksumType, String checksumValue) {
        if (checksums == null) {
            checksums = new EnumMap<>(ChecksumType.class);
        }
        checksums.put(checksumType, checksumValue);
    }

    public void clearChecksums() {
        if (checksums != null) {
            checksums.clear();
        }
    }
}
