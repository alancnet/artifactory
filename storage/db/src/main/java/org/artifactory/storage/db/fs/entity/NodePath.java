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

package org.artifactory.storage.db.fs.entity;

import org.apache.commons.lang.StringUtils;
import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.repo.RepoPath;
import org.artifactory.storage.db.fs.util.NodeUtils;
import org.artifactory.util.PathUtils;

import java.io.Serializable;

/**
 * Represents a unique path of a node. The unique path is composed of (repo, path, name).
 * <p/>
 * The repo can never be empty while path and name might be. Name is empty only if the node path represents repo root
 * path.
 *
 * @author Yossi Shaul
 */
public class NodePath implements Serializable {
    private final String repo;
    private final String path;
    private final String name;
    private final boolean file;

    public NodePath(String repo, String path, String name, boolean file) {
        if (StringUtils.isBlank(repo)) {
            throw new IllegalArgumentException("repo cannot be empty");
        }

        if (StringUtils.isBlank(name) && StringUtils.isNotBlank(path)) {
            throw new IllegalArgumentException("name can only be empty for root repo path");
        }

        this.repo = repo;
        this.path = PathUtils.trimSlashes(StringUtils.trimToEmpty(path)).toString();
        this.name = StringUtils.trimToEmpty(name);
        this.file = file;
    }

    public String getRepo() {
        return repo;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public boolean isFile() {
        return file;
    }

    public boolean isFolder() {
        return !file;
    }

    /**
     * @return The depth of this node path. Repository root path has a depth of 0.
     */
    public short getDepth() {
        int depth = isRoot() ? 0 : NodeUtils.getDepth(path) + 1;   // the +1 is for the name
        if (depth > Short.MAX_VALUE) {
            throw new IllegalArgumentException("Depth cannot be bigger than " + Short.MAX_VALUE);
        }
        return (short) depth;
    }

    /**
     * Returns relative path to the node (the path + name)
     * <p><pre>
     * path = "path", name = "name" returns "path/name"
     * path = "", name = "name" returns "name"
     * path = "", name = "" returns ""
     * </pre>
     *
     * @return Returns relative path to the node (the path + name)
     */
    public String getPathName() {
        if (StringUtils.isBlank(path)) {
            return name;
        } else {
            return path + "/" + name;
        }
    }

    /**
     * @return True if this path represents a repository roo path (ie, path and name are empty, depth is 0)
     */
    public boolean isRoot() {
        return StringUtils.isBlank(path) && StringUtils.isBlank(name);
    }


    public RepoPath toRepoPath() {
        String pathAndName = StringUtils.isBlank(path) ? name : path + "/" + name;
        return new RepoPathImpl(repo, pathAndName, isFolder());
    }

    public static NodePath fromRepoPath(RepoPath repoPath) {
        String path = PathUtils.getParent(repoPath.getPath());
        String name = repoPath.getName();
        return new NodePath(repoPath.getRepoKey(), path, name, repoPath.isFile());
    }

    @Override
    public String toString() {
        return repo + ":" + path + "/" + name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NodePath nodePath = (NodePath) o;

        if (!name.equals(nodePath.name)) {
            return false;
        }
        if (!path.equals(nodePath.path)) {
            return false;
        }
        if (!repo.equals(nodePath.repo)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = repo.hashCode();
        result = 31 * result + path.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }
}
