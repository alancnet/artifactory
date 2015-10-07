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

import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.NuGetAddon;
import org.artifactory.api.common.BasicStatusHolder;
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

import javax.inject.Inject;

import static org.artifactory.addon.nuget.NuGetProperties.Id;
import static org.artifactory.addon.nuget.NuGetProperties.Version;

/**
 * Triggers NuGet package related storage events
 *
 * @author Noam Y. Tenne
 */
public class NuGetCalculationInterceptor extends StorageInterceptorAdapter implements ImportInterceptor {

    @Inject
    AddonsManager addonsManager;

    @Inject
    RepositoryService repositoryService;

    @Override
    public void afterDelete(VfsItem fsItem, MutableStatusHolder statusHolder) {
        removeNuPkgFromRepositoryIfNeeded(fsItem);
    }

    @Override
    public void afterCreate(VfsItem fsItem, MutableStatusHolder statusHolder) {
        extractNuPkgInfoIfNeeded(fsItem, new BasicStatusHolder());
    }

    @Override
    public void afterMove(VfsItem sourceItem, VfsItem targetItem, MutableStatusHolder statusHolder,
            Properties properties) {
        handleOnAfterMoveOrCopy(sourceItem, targetItem, statusHolder);
    }

    @Override
    public void afterCopy(VfsItem sourceItem, VfsItem targetItem, MutableStatusHolder statusHolder,
            Properties properties) {
        handleOnAfterMoveOrCopy(null, targetItem, statusHolder);
    }

    @Override
    public void afterImport(VfsItem fsItem, MutableStatusHolder statusHolder) {
        extractNuPkgInfoIfNeeded(fsItem, statusHolder);
    }

    /**
     * Extracts the info of the given item if it's a nupkg and is stored within a nupkg enabled repo
     */
    private void extractNuPkgInfoIfNeeded(VfsItem createdItem, MutableStatusHolder statusHolder) {
        if (shouldTakeAction(createdItem)) {
            addonsManager.addonByType(NuGetAddon.class).extractNuPkgInfo(((FileInfo) createdItem.getInfo()),
                    statusHolder, true);
        }
    }

    /**
     * If it's a nupkg recalculate the IDs latest version state for the repository
     */
    private void handleOnAfterMoveOrCopy(VfsItem sourceItem, VfsItem targetItem, MutableStatusHolder statusHolder) {
        if ((sourceItem != null) && shouldTakeAction(sourceItem)) {
            removeNuPkgFromRepositoryIfNeeded(sourceItem);
        }

        if (shouldTakeAction(targetItem)) {
            Properties nuPkgProperties = targetItem.getProperties();
            String nuPkgId = Id.extract(nuPkgProperties);
            if (StringUtils.isBlank(nuPkgId)) {
                /**
                 * Package might have come from a repo with no NuGet support, so extract the info and the request
                 * calculation
                 */
                addonsManager.addonByType(NuGetAddon.class).extractNuPkgInfo((FileInfo) targetItem.getInfo(),
                        statusHolder, true);
            } else {
                addonsManager.addonByType(NuGetAddon.class).addNuPkgToRepoCacheAsync(targetItem.getRepoPath(),
                        nuPkgProperties);
            }
        }
    }

    private void removeNuPkgFromRepositoryIfNeeded(VfsItem affectedItem) {
        if (shouldTakeAction(affectedItem)) {
            Properties nuPkgProperties = affectedItem.getProperties();
            String nuPkgId = Id.extract(nuPkgProperties);
            String nuPkgVersion = Version.extract(nuPkgProperties);
            if (StringUtils.isNotBlank(nuPkgId) && StringUtils.isNotBlank(nuPkgVersion)) {
                addonsManager.addonByType(NuGetAddon.class).removeNuPkgFromRepoCache(affectedItem.getRepoKey(),
                        nuPkgId, nuPkgVersion);
            }
        }
    }

    private boolean shouldTakeAction(VfsItem item) {
        if (item.isFile()) {
            RepoPath repoPath = item.getRepoPath();
            if (repoPath.getPath().endsWith(".nupkg")) {
                String repoKey = repoPath.getRepoKey();
                RepoDescriptor repoDescriptor = repositoryService.repoDescriptorByKey(repoKey);
                return ((repoDescriptor != null) && repoDescriptor.getType().equals(RepoType.NuGet));
            }
        }

        return false;
    }
}
