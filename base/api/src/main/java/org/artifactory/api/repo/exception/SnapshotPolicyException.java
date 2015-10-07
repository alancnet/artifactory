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

package org.artifactory.api.repo.exception;

import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.repo.RepoPath;

/**
 * A rejection exception thrown when the snapshot policy of the artifact clashes with the snapshot policy of the target
 * repository
 *
 * @author Noam Y. Tenne
 */
public class SnapshotPolicyException extends RejectedArtifactException {

    /**
     * Main constructor
     *
     * @param rejectingRepo    The descriptor of the rejecting repo
     * @param rejectedArtifact The repo path of the rejected artifact
     */
    public SnapshotPolicyException(RepoDescriptor rejectingRepo, RepoPath rejectedArtifact) {
        super(rejectingRepo, rejectedArtifact);
    }

    @Override
    public String getMessage() {
        return "The repository '" + rejectingRepo.getKey() + "' rejected the artifact '" + rejectedArtifact +
                "' due to its snapshot/release handling policy.";
    }

    /**
     * {@inheritDoc}
     *
     * @return In case of snapshot policy rejection, should return 409 - Conflict.
     */
    @Override
    public int getErrorCode() {
        return 409;
    }
}