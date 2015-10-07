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

import org.apache.commons.lang.StringUtils;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.security.PermissionTargetInfo;
import org.artifactory.util.PathUtils;

/**
 * An internal factory for creating RepoPath objects.
 * <p/>
 *
 * @author Yoav Landman
 */
public abstract class InternalRepoPathFactory extends RepoPathFactory {

    public static RepoPath create(String repoKey, String path, boolean folder) {
        return InfoFactoryHolder.get().createRepoPath(repoKey, path, folder);
    }

    public static RepoPath create(String repoKey, String path) {
        return InfoFactoryHolder.get().createRepoPath(repoKey, path);
    }

    public static RepoPath create(RepoPath parent, String relPath) {
        return InfoFactoryHolder.get().createRepoPath(parent, relPath);
    }

    /**
     * Constructs a RepoPath from a RepoPath identifier in the format of repoKey:path
     *
     * @param repoPathId
     * @return
     */
    public static RepoPath fromId(String repoPathId) {
        return InfoFactoryHolder.get().createRepoPathFromId(repoPathId);
    }

    /**
     * @param repoKey The repository key
     * @return Return the root repo path of the given repository
     */
    public static RepoPath repoRootPath(String repoKey) {
        return create(repoKey, StringUtils.EMPTY);
    }

    /**
     * Builds a repository path to a resource inside an archive file. <p/> The format is
     * <code>archiveRepoPath!/resourcePath</code>
     *
     * @param archiveRepoPath Repo path to an archive file (zip, jar etc.)
     * @param resourcePath    Path to a resource (file or folder) inside the archive
     * @return Repo path to a resource inside an archive file
     */
    public static RepoPath archiveResourceRepoPath(RepoPath archiveRepoPath, String resourcePath) {
        if (!resourcePath.startsWith("/")) {
            resourcePath = "/" + resourcePath;
        }
        return create(archiveRepoPath.getRepoKey(),
                archiveRepoPath.getPath() + RepoPath.ARCHIVE_SEP + resourcePath);
    }

    public static RepoPath childRepoPath(RepoPath repoPath, String childRelPath) {
        if (!childRelPath.startsWith("/")) {
            childRelPath = "/" + childRelPath;
        }
        return create(repoPath.getRepoKey(), repoPath.getPath() + childRelPath);
    }

    public static RepoPath secureRepoPathForRepo(String repoKey) {
        return create(repoKey, PermissionTargetInfo.ANY_PATH);
    }

    public static RepoPath cacheRepoPath(RepoPath remoteRepoPath) {
        return create(remoteRepoPath.getRepoKey() + RepoPath.REMOTE_CACHE_SUFFIX, remoteRepoPath.getPath(),
                remoteRepoPath.isFolder());
    }


    public static RepoPath createRepoPath(String id) {
        if (id == null || id.length() == 0) {
            throw new IllegalArgumentException(
                    "RepoAndPathIdIdentity cannot have a null id");
        }
        int idx = id.indexOf(RepoPath.REPO_PATH_SEP);
        if (idx <= 0) {
            throw new IllegalArgumentException(
                    "Could not determine both repository key and path from '" +
                            id + "'.");
        }
        String repoKey = id.substring(0, idx);
        String path = PathUtils.formatRelativePath(id.substring(idx + 1));
        return create(repoKey, path);
    }

}