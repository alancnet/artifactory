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

package org.artifactory.storage.db.search.model;

import org.artifactory.api.context.ContextHelper;
import org.artifactory.fs.ItemInfo;
import org.artifactory.repo.RepoPath;
import org.artifactory.storage.db.fs.entity.NodePath;
import org.artifactory.storage.fs.service.FileService;

/**
 * Date: 11/29/12
 * Time: 2:48 PM
 *
 * @author freds
 */
public abstract class DbItemInfoProxy implements ItemInfo {
    private final long nodeId;
    private final NodePath nodePath;
    private ItemInfo itemInfo;
    private RepoPath repoPath;

    public DbItemInfoProxy(long nodeId, NodePath nodePath) {
        if (nodePath == null) {
            throw new IllegalArgumentException("Cannot create Item proxy without a node path. Got ID: " + nodeId);
        }
        this.nodeId = nodeId;
        this.nodePath = nodePath;
        this.itemInfo = null;
    }

    public ItemInfo getMaterialized() {
        if (itemInfo == null) {
            FileService fileService = ContextHelper.get().beanForType(FileService.class);
            itemInfo = fileService.loadItem(nodeId);
        }
        return itemInfo;
    }

    @Override
    public RepoPath getRepoPath() {
        if (repoPath == null) {
            repoPath = nodePath.toRepoPath();
        }
        return repoPath;
    }

    @Override
    public boolean isFolder() {
        return nodePath.isFolder();
    }

    @Override
    public String getName() {
        return nodePath.getName();
    }

    @Override
    public String getRepoKey() {
        return nodePath.getRepo();
    }

    @Override
    public String getRelPath() {
        return getRepoPath().getPath();
    }

    @Override
    public long getCreated() {
        return getMaterialized().getCreated();
    }

    @Override
    public long getLastModified() {
        return getMaterialized().getLastModified();
    }

    @Override
    public String getModifiedBy() {
        return getMaterialized().getModifiedBy();
    }

    @Override
    public String getCreatedBy() {
        return getMaterialized().getCreatedBy();
    }

    @Override
    public long getLastUpdated() {
        return getMaterialized().getLastUpdated();
    }

    @Override
    public boolean isIdentical(ItemInfo info) {
        return getMaterialized().isIdentical(info);
    }

    @Override
    public int compareTo(ItemInfo o) {
        return getMaterialized().compareTo(o);
    }

    public boolean isMaterialized() {
        return itemInfo != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DbItemInfoProxy)) {
            return false;
        }

        DbItemInfoProxy that = (DbItemInfoProxy) o;

        if (!nodePath.equals(that.nodePath)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return nodePath.hashCode();
    }
}
