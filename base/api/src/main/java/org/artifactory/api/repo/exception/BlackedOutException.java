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
 * A rejection exception thrown when attempting to import to a blacked out repository
 *
 * @author Noam Y. Tenne
 */
public class BlackedOutException extends RejectedArtifactException {

    /**
     * Main constructor
     *
     * @param rejectingRepo    The descriptor of the rejecting repo
     * @param rejectedArtifact The repo path of the rejected artifact
     */
    public BlackedOutException(RepoDescriptor rejectingRepo, RepoPath rejectedArtifact) {
        super(rejectingRepo, rejectedArtifact);
    }

    @Override
    public String getMessage() {
        return "The repository '" + rejectingRepo.getKey() + "' is blacked out and cannot serve artifact '" +
                rejectedArtifact + "'.";
    }
}