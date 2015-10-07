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

import org.artifactory.api.repo.exception.RepoRejectException;
import org.artifactory.checksum.ChecksumsInfo;
import org.artifactory.repo.RepoPath;

/**
 * Thrown when checksum policy doesn't allow saving a certain file.
 *
 * @author Yossi Shaul
 */
public class ChecksumPolicyException extends RepoRejectException {
    public ChecksumPolicyException(ChecksumPolicy policy, ChecksumsInfo checksums, RepoPath repoPath) {
        super("Checksum policy '" + policy + "' rejected the artifact '" + repoPath + "'. " +
                "Checksums info: " + checksums);
    }

    @Override
    public int getErrorCode() {
        return 409;
    }
}