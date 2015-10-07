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

package org.artifactory.io.checksum.policy;

import org.artifactory.checksum.ChecksumInfo;
import org.artifactory.descriptor.repo.ChecksumPolicyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This checksum policy doesn't allow mismatches between the original and the calculated checksums.
 *
 * @author Yossi Shaul
 */
public class ChecksumPolicyFail extends ChecksumPolicyBase {
    private static final Logger log = LoggerFactory.getLogger(ChecksumPolicyFail.class);

    @Override
    boolean verifyChecksum(ChecksumInfo checksumInfo) {
        String original = checksumInfo.getOriginal();
        if (original == null) {
            log.warn("Rejecting original {} null checksum", checksumInfo.getType());
            return false;
        }
        if (!checksumInfo.checksumsMatch()) {
            log.warn("Checksum mismatch: {}", checksumInfo);
            return false;
        }
        return true;
    }

    @Override
    String getChecksum(ChecksumInfo checksumInfo) {
        return checksumInfo.getActual();
    }

    @Override
    ChecksumPolicyType getChecksumPolicyType() {
        return ChecksumPolicyType.FAIL;
    }
}
