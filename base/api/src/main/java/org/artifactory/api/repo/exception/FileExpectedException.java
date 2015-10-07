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

import org.artifactory.repo.RepoPath;

/**
 * This exception is thrown whenever a folder exists in the repository path but a file is expected.
 */
public class FileExpectedException extends RuntimeException {
    private final RepoPath repoPath;
    private static final String MESSAGE_PREFIX = "Expected a file but found a folder, at: ";

    public FileExpectedException(RepoPath repoPath) {
        super(MESSAGE_PREFIX + repoPath);
        this.repoPath = repoPath;
    }

    public RepoPath getRepoPath() {
        return repoPath;
    }
}