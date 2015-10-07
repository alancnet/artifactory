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

package org.artifactory.repo.interceptor;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.plugin.PluginsAddon;
import org.artifactory.addon.plugin.storage.*;
import org.artifactory.common.MutableStatusHolder;
import org.artifactory.md.Properties;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.interceptor.storage.StorageInterceptorAdapter;
import org.artifactory.sapi.fs.VfsItem;

import javax.inject.Inject;

/**
 * An interceptor that executes plugin scripts
 *
 * @author Yoav Landman
 */
public class PluginsInterceptor extends StorageInterceptorAdapter {

    @Inject
    AddonsManager addonsManager;

    @Override
    public void beforeCreate(VfsItem fsItem, MutableStatusHolder statusHolder) {
        getPluginsAddon().execPluginActions(BeforeCreateAction.class, null, fsItem.getInfo());
    }

    @Override
    public void afterCreate(VfsItem fsItem, MutableStatusHolder statusHolder) {
        getPluginsAddon().execPluginActions(AfterCreateAction.class, null, fsItem.getInfo());
    }

    @Override
    public void beforeDelete(VfsItem fsItem, MutableStatusHolder statusHolder) {
        getPluginsAddon().execPluginActions(BeforeDeleteAction.class, null, fsItem.getInfo());
    }

    @Override
    public void afterDelete(VfsItem fsItem, MutableStatusHolder statusHolder) {
        getPluginsAddon().execPluginActions(AfterDeleteAction.class, null, fsItem.getInfo());
    }

    @Override
    public void beforeMove(VfsItem sourceItem, RepoPath targetRepoPath, MutableStatusHolder statusHolder,
            Properties properties) {
        getPluginsAddon().execPluginActions(BeforeMoveAction.class, null, sourceItem.getInfo(), targetRepoPath,
                properties);
    }


    @Override
    public void afterMove(VfsItem sourceItem, VfsItem targetItem, MutableStatusHolder statusHolder,
            Properties properties) {
        getPluginsAddon().execPluginActions(AfterMoveAction.class, null, sourceItem.getInfo(),
                nullOrRepoPath(targetItem), properties);
    }

    @Override
    public void beforeCopy(VfsItem sourceItem, RepoPath targetRepoPath, MutableStatusHolder statusHolder,
            Properties properties) {
        getPluginsAddon().execPluginActions(BeforeCopyAction.class, null, sourceItem.getInfo(), targetRepoPath,
                properties);
    }

    @Override
    public void afterCopy(VfsItem sourceItem, VfsItem targetItem, MutableStatusHolder statusHolder,
            Properties properties) {
        getPluginsAddon().execPluginActions(AfterCopyAction.class, null, sourceItem.getInfo(),
                nullOrRepoPath(targetItem), properties);
    }

    @Override
    public void beforePropertyCreate(VfsItem fsItem, MutableStatusHolder statusHolder, String name, String... values) {
        getPluginsAddon().execPluginActions(BeforePropertyCreateAction.class, null, fsItem.getInfo(), name, values);
    }

    @Override
    public void afterPropertyCreate(VfsItem fsItem, MutableStatusHolder statusHolder, String name,
            String... values) {
        getPluginsAddon().execPluginActions(AfterPropertyCreateAction.class, null, fsItem.getInfo(), name, values);
    }

    @Override
    public void beforePropertyDelete(VfsItem fsItem, MutableStatusHolder statusHolder, String name) {
        getPluginsAddon().execPluginActions(BeforePropertyDeleteAction.class, null, fsItem.getInfo(), name);
    }

    @Override
    public void afterPropertyDelete(VfsItem fsItem, MutableStatusHolder statusHolder, String name) {
        getPluginsAddon().execPluginActions(AfterPropertyDeleteAction.class, null, fsItem.getInfo(), name);
    }

    private RepoPath nullOrRepoPath(VfsItem targetItem) {
        return targetItem != null ? targetItem.getRepoPath() : null;
    }

    private PluginsAddon getPluginsAddon() {
        return addonsManager.addonByType(PluginsAddon.class);
    }
}
