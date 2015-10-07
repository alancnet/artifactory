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

import org.apache.commons.lang.StringUtils;
import org.artifactory.checksum.ChecksumInfo;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.descriptor.repo.LocalRepoChecksumPolicyType;
import org.artifactory.mime.MavenNaming;
import org.artifactory.repo.RepoPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Set;

/**
 * This is the checksum policy used by local non-cache repositories. This class is not supposed to be used by the cache
 * repositories.
 *
 * @author Yossi Shaul
 */
public class LocalRepoChecksumPolicy implements ChecksumPolicy, Serializable {
    private static final Logger log = LoggerFactory.getLogger(LocalRepoChecksumPolicy.class);

    private LocalRepoChecksumPolicyType policyType = LocalRepoChecksumPolicyType.CLIENT;

    /**
     * Verify client (original) checksums vs. calculated checksums when the original is not null. This method usually
     * called while saving file to the storage (just after the actual checksums are calculated). This check is only
     * applied if there are original checksums (ie, the client sends the checksum info with the deployed file).
     *
     * @param checksums The resource checksums
     * @return True if it the checksums are ok according to the policy.
     */
    @Override
    public boolean verify(Set<ChecksumInfo> checksums) {
        boolean checksumsMatch = true;
        for (ChecksumInfo checksum : checksums) {
            // check only if the client checksum info exist
            if (StringUtils.isNotBlank(checksum.getOriginal())) {
                checksumsMatch &= checksum.checksumsMatch();
            }
        }

        // fail only if there's real mismatch and the policy is client checksums
        if (!checksumsMatch) {
            if (LocalRepoChecksumPolicyType.CLIENT == policyType) {
                return false;
            } else {
                log.debug("Checksum mismatch: %s", checksums);
            }
        }
        return true;
    }


    @Override
    public String getChecksum(ChecksumType checksumType, Set<ChecksumInfo> checksums) {
        return getChecksum(checksumType, checksums, null);
    }

    @Override
    public String getChecksum(ChecksumType checksumType, Set<ChecksumInfo> checksums, RepoPath repoPath) {
        ChecksumInfo checksumInfo = getByType(checksumType, checksums);
        if (checksumInfo == null) {
            return null;
        }

        if (repoPath != null && MavenNaming.isMavenMetadata(repoPath.getPath())) {
            return checksumInfo.getActual();  // maven metadata checksums are always the "server" checksum
        }

        if (LocalRepoChecksumPolicyType.CLIENT == policyType) {
            return checksumInfo.getOriginal();  // the "client" checksum
        } else {
            return checksumInfo.getActual();    // the "server" (actual file) checksum
        }
    }

    public void setPolicyType(LocalRepoChecksumPolicyType policyType) {
        if (policyType != null) {
            this.policyType = policyType;
        }
    }

    private ChecksumInfo getByType(ChecksumType checksumType, Set<ChecksumInfo> checksums) {
        for (ChecksumInfo info : checksums) {
            if (checksumType.equals(info.getType())) {
                return info;
            }
        }
        return null;
    }

    public LocalRepoChecksumPolicyType getPolicyType() {
        return policyType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LocalRepoChecksumPolicy that = (LocalRepoChecksumPolicy) o;

        if (policyType != that.policyType) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return policyType.hashCode();
    }

    @Override
    public String toString() {
        return "LocalRepoChecksumPolicy: " + policyType;
    }
}
