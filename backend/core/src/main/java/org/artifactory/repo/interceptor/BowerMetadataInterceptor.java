/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2013 JFrog Ltd.
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
import org.artifactory.addon.bower.BowerAddon;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.common.MutableStatusHolder;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.fs.FileInfo;
import org.artifactory.md.Properties;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.interceptor.storage.StorageInterceptorAdapter;
import org.artifactory.sapi.fs.VfsItem;
import org.artifactory.sapi.interceptor.ImportInterceptor;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Shay Yaakov
 */
public class BowerMetadataInterceptor extends StorageInterceptorAdapter implements ImportInterceptor {

    @Autowired
    private AddonsManager addonsManager;

    @Autowired
    private RepositoryService repositoryService;

    @Override
    public void afterCreate(VfsItem fsItem, MutableStatusHolder statusHolder) {
        if (shouldTakeAction(fsItem)) {
            addonsManager.addonByType(BowerAddon.class).addBowerPackage(((FileInfo) fsItem.getInfo()));
        }
    }

    @Override
    public void afterDelete(VfsItem fsItem, MutableStatusHolder statusHolder) {
        if (shouldTakeAction(fsItem)) {
            addonsManager.addonByType(BowerAddon.class).removeBowerPackage(((FileInfo) fsItem.getInfo()));
        }
    }

    @Override
    public void afterMove(VfsItem sourceItem, VfsItem targetItem, MutableStatusHolder statusHolder,
            Properties properties) {
        if (shouldTakeAction(sourceItem)) {
            addonsManager.addonByType(BowerAddon.class).removeBowerPackage(((FileInfo) sourceItem.getInfo()));
        }

        if (shouldTakeAction(targetItem)) {
            addonsManager.addonByType(BowerAddon.class).handleAddAfterCommit(((FileInfo) targetItem.getInfo()));
        }
    }

    @Override
    public void afterCopy(VfsItem sourceItem, VfsItem targetItem, MutableStatusHolder statusHolder,
            Properties properties) {
        if (shouldTakeAction(targetItem)) {
            addonsManager.addonByType(BowerAddon.class).handleAddAfterCommit(((FileInfo) targetItem.getInfo()));
        }
    }

    @Override
    public void afterImport(VfsItem fsItem, MutableStatusHolder statusHolder) {
        if (shouldTakeAction(fsItem)) {
            addonsManager.addonByType(BowerAddon.class).addBowerPackage(((FileInfo) fsItem.getInfo()));
        }
    }

    private boolean shouldTakeAction(VfsItem item) {
        if (item.isFile()) {
            RepoPath repoPath = item.getRepoPath();
            if (isBowerFile(repoPath.getPath())) {
                String repoKey = repoPath.getRepoKey();
                RepoDescriptor repoDescriptor = repositoryService.localOrCachedRepoDescriptorByKey(repoKey);
                return ((repoDescriptor != null) && repoDescriptor.getType().equals(RepoType.Bower));
            }
        }

        return false;
    }

    private boolean isBowerFile(String filePath) {
        return addonsManager.addonByType(BowerAddon.class).isBowerFile(filePath);
    }
}
