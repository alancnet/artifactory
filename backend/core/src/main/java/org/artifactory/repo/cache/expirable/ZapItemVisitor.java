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

package org.artifactory.repo.cache.expirable;

import org.artifactory.api.context.ContextHelper;
import org.artifactory.descriptor.repo.LocalCacheRepoDescriptor;
import org.artifactory.repo.db.DbCacheRepo;
import org.artifactory.sapi.fs.MutableVfsFile;
import org.artifactory.sapi.fs.MutableVfsFolder;
import org.artifactory.sapi.fs.VfsFile;
import org.artifactory.sapi.fs.VfsFolder;
import org.artifactory.sapi.fs.VfsItem;
import org.artifactory.sapi.fs.VfsItemVisitor;

import java.util.List;

/**
 * Zap expired cache items. Sets the last updated value of expirable files and folders to a value that will make them
 * expired.
 *
 * @author Yossi Shaul
 */
public class ZapItemVisitor implements VfsItemVisitor {

    private final DbCacheRepo cacheRepo;
    private long expiredLastUpdated;
    private int updatedItemsCount;

    /**
     * @param cacheRepo The cache repo containing the files to visit
     */
    public ZapItemVisitor(DbCacheRepo cacheRepo) {
        this.cacheRepo = cacheRepo;
        LocalCacheRepoDescriptor descriptor = cacheRepo.getDescriptor();
        long retrievalCachePeriodMillis = descriptor.getRetrievalCachePeriodMillis();
        expiredLastUpdated = System.currentTimeMillis() - retrievalCachePeriodMillis;
    }

    /**
     * @param file File to visit. Must reside inside the cache repo.
     */
    @Override
    public void visit(VfsFile file) {
        CacheExpiry cacheExpiry = ContextHelper.get().beanForType(CacheExpiry.class);
        if (cacheExpiry.isExpirable(cacheRepo, file.getPath())) {
            // zap has a meaning only on non unique snapshot files
            MutableVfsFile mutableFile = cacheRepo.getMutableFile(file.getRepoPath());
            mutableFile.setUpdated(expiredLastUpdated);
            updatedItemsCount++;
        }
    }

    /**
     * @param folder Folder to visit. Must reside inside the cache repo.
     */
    @Override
    public void visit(VfsFolder folder) {
        // folders are always expirable
        MutableVfsFolder mutableFolder = cacheRepo.getMutableFolder(folder.getRepoPath());
        mutableFolder.setUpdated(expiredLastUpdated);
        updatedItemsCount++;

        // zap children
        List<VfsItem> children = folder.getImmutableChildren();
        for (VfsItem child : children) {
            visit(child);
        }
    }

    /**
     * @return Number of files and folders affected
     */
    public int getUpdatedItemsCount() {
        return updatedItemsCount;
    }

    @Override
    public void visit(VfsItem visitable) {
        if (visitable.isFile()) {
            visit((VfsFile) visitable);
        } else {
            visit((VfsFolder) visitable);
        }
    }

}
