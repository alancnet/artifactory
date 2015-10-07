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

import org.artifactory.api.config.ImportableExportable;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.SnapshotVersionBehavior;
import org.artifactory.repo.snapshot.MavenSnapshotVersionAdapter;
import org.artifactory.resource.ResourceStreamHandle;

public interface LocalRepo<T extends LocalRepoDescriptor> extends RealRepo<T>, StoringRepo<T>, ImportableExportable {

    SnapshotVersionBehavior getMavenSnapshotVersionBehavior();

    MavenSnapshotVersionAdapter getMavenSnapshotVersionAdapter();

    /**
     * Internal - get the raw content directly
     *
     * @param repoPath
     * @return
     */
    String getTextFileContent(RepoPath repoPath);

    /**
     * Internal - get the raw content directly
     *
     * @param repoPath
     * @return The ResourceStreamHandle for an existing file or a NullResourceStreamHandle for a non-exiting file Note:
     *         Handle is to be closed by clients to avoid stream leaks!
     */
    ResourceStreamHandle getFileContent(RepoPath repoPath);
}