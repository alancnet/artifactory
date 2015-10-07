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
 * An exception thrown when is rejected for deployment
 *
 * @author Noam Y. Tenne
 */
public abstract class RejectedArtifactException extends RepoRejectException {

    protected RepoDescriptor rejectingRepo;
    protected RepoPath rejectedArtifact;

    /**
     * Main constructor
     *
     * @param rejectingRepo    The descriptor of the rejecting repo
     * @param rejectedArtifact The repo path of the rejected artifact
     */
    public RejectedArtifactException(RepoDescriptor rejectingRepo, RepoPath rejectedArtifact) {
        this.rejectingRepo = rejectingRepo;
        this.rejectedArtifact = rejectedArtifact;
    }

    protected RejectedArtifactException(int status, RepoDescriptor rejectingRepo, RepoPath rejectedArtifact) {
        super(status);
        this.rejectingRepo = rejectingRepo;
        this.rejectedArtifact = rejectedArtifact;
    }
}