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
import org.artifactory.checksum.ChecksumType;
import org.artifactory.checksum.ChecksumsInfo;
import org.artifactory.descriptor.repo.ChecksumPolicyType;
import org.artifactory.repo.RepoPath;

import java.io.Serializable;
import java.util.Set;

/**
 * Base abstract implementation of the ChecksumPolicy.
 *
 * @author Yossi Shaul
 */
public abstract class ChecksumPolicyBase implements ChecksumPolicy, Serializable {
    abstract boolean verifyChecksum(ChecksumInfo checksumInfo);

    abstract String getChecksum(ChecksumInfo checksumInfo);

    @Override
    public boolean verify(Set<ChecksumInfo> checksumInfos) {
        for (ChecksumInfo checksumInfo : checksumInfos) {
            // call the actual implementation to do its job
            if (verifyChecksum(checksumInfo)) {
                // by default if one checksum passes the file is ok
                return true;
            }
        }
        return false;
    }

    @Override
    public String getChecksum(ChecksumType checksumType, Set<ChecksumInfo> checksumInfos) {
        ChecksumInfo info = getChecksumInfo(checksumType, checksumInfos);
        if (info != null) {
            return getChecksum(info);

        }
        return null;
    }

    // TODO: Need to change the API to receive the container class not a Set
    public String getGoodChecksum(ChecksumType checksumType, ChecksumsInfo checksumInfos, RepoPath repoPath) {
        // remote checksum policies don't care about the repo path
        return getChecksum(checksumInfos.getChecksumInfo(checksumType));
    }

    @Override
    public String getChecksum(ChecksumType checksumType, Set<ChecksumInfo> checksumInfos, RepoPath repoPath) {
        // remote checksum policies don't care about the repo path
        return getChecksum(checksumType, checksumInfos);
    }

    private ChecksumInfo getChecksumInfo(ChecksumType type, Set<ChecksumInfo> infos) {
        for (ChecksumInfo info : infos) {
            if (type.equals(info.getType())) {
                return info;
            }
        }
        return null;
    }

    public static ChecksumPolicy getByType(ChecksumPolicyType type) {
        switch (type) {
            case GEN_IF_ABSENT:
                return new ChecksumPolicyGenerateIfAbsent();
            case FAIL:
                return new ChecksumPolicyFail();
            case IGNORE_AND_GEN:
                return new ChecksumPolicyIgnoreAndGenerate();
            case PASS_THRU:
                return new ChecksumPolicyPassThru();
            default:
                throw new IllegalArgumentException("No checksum policy found for type " + type);
        }
    }

    /**
     * @return The checksum policy type this checksum policy implements.
     */
    abstract ChecksumPolicyType getChecksumPolicyType();

    @Override
    public String toString() {
        return getChecksumPolicyType().toString();
    }

}
