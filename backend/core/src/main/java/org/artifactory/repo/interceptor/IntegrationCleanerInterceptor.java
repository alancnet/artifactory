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

import org.artifactory.api.module.ModuleInfo;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.common.MutableStatusHolder;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.cleanup.InternalIntegrationCleanupService;
import org.artifactory.repo.interceptor.storage.StorageInterceptorAdapter;
import org.artifactory.sapi.fs.VfsItem;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yoav
 */
public class IntegrationCleanerInterceptor extends StorageInterceptorAdapter {

    @Autowired
    private InternalIntegrationCleanupService cleanupService;

    @Autowired
    private RepositoryService repositoryService;

    /**
     * Cleanup old snapshots etc.
     *
     * @param fsItem
     * @param statusHolder
     */
    @Override
    public void afterCreate(VfsItem fsItem, MutableStatusHolder statusHolder) {
        if (fsItem.isFolder()) {
            return;
        }

        LocalRepoDescriptor repoDescriptor = repositoryService.localRepoDescriptorByKey(fsItem.getRepoKey());
        if (repoDescriptor == null) {
            return;
        }

        int maxUniqueSnapshots = repoDescriptor.getMaxUniqueSnapshots();
        if (maxUniqueSnapshots > 0) {
            RepoPath fsItemRepoPath = fsItem.getRepoPath();
            ModuleInfo itemModuleInfo = repositoryService.getItemModuleInfo(fsItemRepoPath);
            if (!itemModuleInfo.isValid() || !itemModuleInfo.isIntegration()) {
                return;
            }

            cleanupService.addItemToCache(fsItemRepoPath);
        }
    }
}