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
import org.artifactory.sapi.common.RepositoryRuntimeException;

/**
 * @author freds
 * @date Oct 24, 2008
 */
public class ItemNotFoundRuntimeException extends RepositoryRuntimeException {
    public ItemNotFoundRuntimeException(RepoPath repoPath) {
        super("Item " + repoPath + " does not exist");
    }

    public ItemNotFoundRuntimeException(String message) {
        super(message);
    }

    public ItemNotFoundRuntimeException(Throwable cause) {
        super(cause);
    }

    public ItemNotFoundRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
