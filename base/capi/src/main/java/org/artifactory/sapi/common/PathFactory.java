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

package org.artifactory.sapi.common;

import org.artifactory.repo.RepoPath;

import java.io.File;

/**
 * Date: 8/4/11
 * Time: 11:38 AM
 *
 * @author Fred Simon
 */
public interface PathFactory {
    String escape(String pathElement);

    String getAllRepoRootPath();

    File getRepositoriesExportDir(File exportDir);

    String getAbsolutePath(RepoPath repoPath);

    RepoPath getRepoPath(String absPath);
}
