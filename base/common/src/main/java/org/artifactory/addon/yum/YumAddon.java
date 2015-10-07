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

package org.artifactory.addon.yum;

import org.artifactory.addon.Addon;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.fs.FileInfo;
import org.artifactory.repo.RepoPath;

/**
 * @author Noam Y. Tenne
 */
public interface YumAddon extends Addon {

    String REPO_DATA_DIR = "repodata/";

    /**
     * Activate the YUM metadata calculation for all needed paths under the local repository.
     * The paths are the parent of all repodata needed based on the yum depth parameter.
     * Each calculation will be queued and processed asynchronously.
     *
     * @param repo the local repository to activate YUM calculation on.
     */
    void requestAsyncRepositoryYumMetadataCalculation(LocalRepoDescriptor repo);

    /**
     * Activate the YUM metadata calculation for the specific list of paths.
     * Each paths should be a parent of a repodata folder that need recalculation.
     * Each calculation will be queued and processed asynchronously.
     *
     * @param repoPaths
     */
    void requestAsyncRepositoryYumMetadataCalculation(RepoPath... repoPaths);

    /**
     * Activate the YUM metadata calculation for all needed paths under the local repository.
     * The paths are the parent of all repodata needed based on the yum depth parameter.
     * Each calculation will be processed in a separate transaction but synchronously to this method.
     *
     * @param repo the local repository to activate YUM calculation on.
     */
    void requestYumMetadataCalculation(LocalRepoDescriptor repo);

    /**
     *  get Rpm file Meta data
     * @param fileInfo
     * @return
     */
    ArtifactRpmMetadata getRpmMetadata(FileInfo fileInfo) ;
}
