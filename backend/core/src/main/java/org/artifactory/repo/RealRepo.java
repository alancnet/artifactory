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

package org.artifactory.repo;

import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.common.StatusHolder;
import org.artifactory.descriptor.repo.RealRepoDescriptor;

/**
 * @author Yoav Landman
 */
public interface RealRepo<T extends RealRepoDescriptor> extends Repo<T> {

    boolean isHandleReleases();

    boolean isHandleSnapshots();

    boolean isBlackedOut();

    StatusHolder checkDownloadIsAllowed(RepoPath repoPath);

    boolean handlesReleaseSnapshot(String path);

    int getMaxUniqueSnapshots();

    /**
     * Checks that the actionable path is valid in the current context
     *
     * @param path            Path to test
     * @param downloadRequest True if the originating request is for download, false for upload
     * @return Test result
     */
    BasicStatusHolder assertValidPath(RepoPath path, boolean downloadRequest);

    boolean accepts(RepoPath repoPath);
}
