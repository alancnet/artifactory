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

/**
 * This checksum policy ignores missing original checksums, but fails if original exist but not equals to the actual.
 *
 * @author Yossi Shaul
 */
public class ChecksumPolicyGenerateIfAbsent extends ChecksumPolicyBase {

    @Override
    boolean verifyChecksum(ChecksumInfo checksumInfo) {
        if (checksumInfo.getOriginal() == null) {
            return true;
        } else if (!checksumInfo.checksumsMatch()) {
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
        return ChecksumPolicyType.GEN_IF_ABSENT;
    }
}