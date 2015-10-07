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

package org.artifactory.repo.service.mover;

import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.repo.RepoPath;

import java.util.Iterator;

/**
 * An iterator of {@link org.artifactory.repo.RepoPath}. Allows iteration from the root repo path up to the input repo
 * path.
 * This iterator doesn't support java.util.Iterator#remove()
 *
 * @author Yossi Shaul
 */
class RepoPathIterator implements Iterator<RepoPath> {
    private final String[] pathElements;

    private RepoPath current;
    private int index;

    public RepoPathIterator(RepoPath repoPath) {
        pathElements = repoPath.toPath().split("/");
    }

    @Override
    public boolean hasNext() {
        return index < pathElements.length;
    }

    @Override
    public RepoPath next() {
        if (current == null) {
            current = new RepoPathImpl(pathElements[0], "");
        } else {
            current = new RepoPathImpl(current, pathElements[index]);
        }
        index++;
        return current;
    }

    /**
     * Repo path removal is not supported
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("Remove is not implemented");
    }
}
