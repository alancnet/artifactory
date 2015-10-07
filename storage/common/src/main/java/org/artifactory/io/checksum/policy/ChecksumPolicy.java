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
import org.artifactory.repo.RepoPath;

import java.util.Set;

/**
 * A checksum policy is responsible to handle any problem related to mismatches between the original checksum and the
 * one calculated by Artifactory.
 *
 * @author Yossi Shaul
 */
public interface ChecksumPolicy {
    /**
     * Processes the checksums info and possibly changes the checksum info.
     *
     * @param checksumInfos The checksums to process and update.
     * @return True if the checksums are valid according to this policy.
     */
    boolean verify(Set<ChecksumInfo> checksumInfos);

    /**
     * Returns the checksum value by type. Actual implementation will decide if to return the original, calculated or
     * something else.
     *
     * @param checksumType The checksum type
     * @return Checksum value for the checksum type
     */
    String getChecksum(ChecksumType checksumType, Set<ChecksumInfo> checksumInfos);

    /**
     * Returns the checksum value by type. Actual implementation will decide if to return the original, calculated or
     * something else. The repo path might also be used by the policy.
     *
     * @param checksumType The checksum type
     * @param repoPath     The repo path of the resource the checksum is requested upon
     * @return Checksum value for the checksum type
     */
    String getChecksum(ChecksumType checksumType, Set<ChecksumInfo> checksumInfos, RepoPath repoPath);
}
