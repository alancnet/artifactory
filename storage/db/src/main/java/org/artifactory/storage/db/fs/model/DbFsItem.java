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

package org.artifactory.storage.db.fs.model;

import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.fs.ItemInfo;
import org.artifactory.md.Properties;
import org.artifactory.repo.RepoPath;
import org.artifactory.sapi.fs.VfsItem;
import org.artifactory.storage.binstore.service.BinaryStore;
import org.artifactory.storage.db.DbService;
import org.artifactory.storage.fs.repo.StoringRepo;
import org.artifactory.storage.fs.service.FileService;
import org.artifactory.storage.fs.service.PropertiesService;
import org.artifactory.storage.fs.service.WatchesService;

/**
 * Represents a virtual file/folder backed by database entry.
 *
 * @author Yossi Shaul
 */
public abstract class DbFsItem<T extends ItemInfo> implements VfsItem<T> {

    /**
     * The repository that contains this item.
     */
    private final StoringRepo repo;

    /**
     * Unique database identifier. {@link org.artifactory.storage.db.DbService#NO_DB_ID} if not persisted yet.
     */
    protected long id = DbService.NO_DB_ID;

    protected final T info;

    protected DbFsItem(StoringRepo repo, long id, T info) {
        this.repo = repo;
        this.id = id;
        this.info = info;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public T getInfo() {
        return info;
    }

    public StoringRepo getRepo() {
        return repo;
    }

    @Override
    public String getName() {
        return info.getName();
    }

    @Override
    public long getCreated() {
        return info.getCreated();
    }

    @Override
    public RepoPath getRepoPath() {
        return info.getRepoPath();
    }

    @Override
    public String getRepoKey() {
        return info.getRepoKey();
    }

    @Override
    public String getPath() {
        return info.getRelPath();
    }

    @Override
    public Properties getProperties() {
        return getPropertiesService().getProperties(getRepoPath());
    }

    @Override
    public String toString() {
        return getRepoPath().toString();
    }

    protected FileService getFileService() {
        return ContextHelper.get().beanForType(FileService.class);
    }

    protected RepositoryService getRepositoryService() {
        return ContextHelper.get().beanForType(RepositoryService.class);
    }

    protected BinaryStore getBinariesService() {
        return ContextHelper.get().beanForType(BinaryStore.class);
    }

    protected PropertiesService getPropertiesService() {
        return ContextHelper.get().beanForType(PropertiesService.class);
    }

    protected WatchesService getWatchesService() {
        return ContextHelper.get().beanForType(WatchesService.class);
    }

}
