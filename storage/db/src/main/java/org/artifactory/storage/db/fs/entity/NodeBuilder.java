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

import org.artifactory.api.util.Builder;
import org.artifactory.repo.RepoPath;

import java.util.Date;

/**
 * Builder for {@link org.artifactory.storage.db.fs.entity.Node}.
 *
 * @author Yossi Shaul
 */
public class NodeBuilder implements Builder<Node> {

    private long nodeId;
    private boolean file;
    private String repo;
    private String path;
    private String name;
    private long created;
    private String createdBy;
    private long modified;
    private String modifiedBy;
    private long updated;

    private NodePath nodePath;

    // file only attributes
    private long length;
    private String sha1Actual;
    private String sha1Original;
    private String md5Actual;
    private String md5Original;

    public NodeBuilder() {
        created = modified = updated = System.currentTimeMillis();
    }

    @Override
    public Node build() {
        if (nodeId <= 0) {
            throw new IllegalArgumentException("nodeId = " + nodeId);
        }

        // only build if node path is not set
        if (nodePath == null) {
            // node path will validate and normalize the paths
            nodePath = new NodePath(repo, path, name, file);
        }

        return new Node(nodeId, file, nodePath.getRepo(), nodePath.getPath(), nodePath.getName(), nodePath.getDepth(),
                created, createdBy, modified, modifiedBy, updated, length, sha1Actual, sha1Original, md5Actual,
                md5Original);
    }

    public NodeBuilder nodeId(long nodeId) {
        this.nodeId = nodeId;
        return this;
    }

    public NodeBuilder file(boolean file) {
        this.file = file;
        return this;
    }

    public NodeBuilder length(long length) {
        this.length = length;
        return this;
    }

    public NodeBuilder repo(String repoName) {
        this.repo = repoName;
        return this;
    }

    public NodeBuilder path(String path) {
        this.path = path;
        return this;
    }

    public NodeBuilder name(String fileName) {
        this.name = fileName;
        return this;
    }

    public NodeBuilder created(long created) {
        this.created = created;
        return this;
    }

    public NodeBuilder created(Date created) {
        this.created = created.getTime();
        return this;
    }

    public NodeBuilder createdBy(String createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public NodeBuilder modified(long modified) {
        this.modified = modified;
        return this;
    }

    public NodeBuilder modified(Date modified) {
        this.modified = modified.getTime();
        return this;
    }

    public NodeBuilder modifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
        return this;
    }

    public NodeBuilder updated(long updated) {
        this.updated = updated;
        return this;
    }

    public NodeBuilder updated(Date updated) {
        this.updated = updated.getTime();
        return this;
    }

    public NodeBuilder sha1Actual(String sha1Actual) {
        this.sha1Actual = sha1Actual;
        return this;
    }

    public NodeBuilder sha1Original(String sha1Original) {
        this.sha1Original = sha1Original;
        return this;
    }

    public NodeBuilder md5Actual(String md5Actual) {
        this.md5Actual = md5Actual;
        return this;
    }

    public NodeBuilder md5Original(String md5Original) {
        this.md5Original = md5Original;
        return this;
    }

    /**
     * Set the repo, path and name from the input repo path. Throws exception if the repo path is a root repo path.
     * Needn't call other path methods if calling this one.
     *
     * @param repoPath The repo path. Shouldn't be a root repo path.
     */
    public NodeBuilder nodePath(RepoPath repoPath) {
        nodePath = NodePath.fromRepoPath(repoPath);
        return this;
    }
}
