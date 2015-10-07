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

package org.artifactory.repo.interceptor.storage;

import org.artifactory.common.MutableStatusHolder;
import org.artifactory.md.Properties;
import org.artifactory.repo.RepoPath;
import org.artifactory.sapi.fs.VfsItem;
import org.artifactory.sapi.interceptor.StorageInterceptor;

/**
 * @author yoav
 */
public abstract class StorageInterceptorAdapter implements StorageInterceptor {

    @Override
    public void beforeCreate(VfsItem fsItem, MutableStatusHolder statusHolder) {
    }

    @Override
    public void afterCreate(VfsItem fsItem, MutableStatusHolder statusHolder) {
    }

    @Override
    public void beforeDelete(VfsItem fsItem, MutableStatusHolder statusHolder) {
    }

    @Override
    public void afterDelete(VfsItem fsItem, MutableStatusHolder statusHolder) {
    }

    @Override
    public void afterMove(VfsItem sourceItem, VfsItem targetItem, MutableStatusHolder statusHolder,
            Properties properties) {
    }

    @Override
    public void beforeCopy(VfsItem sourceItem, RepoPath targetRepoPath, MutableStatusHolder statusHolder,
            Properties properties) {
    }

    @Override
    public void afterCopy(VfsItem sourceItem, VfsItem targetItem, MutableStatusHolder statusHolder,
            Properties properties) {
    }

    @Override
    public void beforeMove(VfsItem sourceItem, RepoPath targetRepoPath, MutableStatusHolder statusHolder,
            Properties properties) {
    }

    @Override
    public void beforePropertyCreate(VfsItem fsItem, MutableStatusHolder statusHolder, String name, String... values) {
    }

    @Override
    public void afterPropertyCreate(VfsItem fsItem, MutableStatusHolder statusHolder, String name,
            String... values) {
    }

    @Override
    public void beforePropertyDelete(VfsItem fsItem, MutableStatusHolder statusHolder, String name) {
    }

    @Override
    public void afterPropertyDelete(VfsItem fsItem, MutableStatusHolder statusHolder, String name) {
    }
}