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

package org.artifactory.webapp.actionable.model;

import org.artifactory.api.repo.RepositoryService;
import org.artifactory.fs.FileInfo;
import org.artifactory.fs.FolderInfo;
import org.artifactory.fs.ItemInfo;
import org.artifactory.mime.MimeType;
import org.artifactory.mime.NamingUtils;
import org.artifactory.repo.RepoPath;
import org.artifactory.webapp.actionable.ActionableItem;
import org.artifactory.webapp.actionable.RepoAwareActionableItem;
import org.artifactory.webapp.actionable.RepoAwareActionableItemBase;

import javax.annotation.Nullable;
import java.util.List;

/**
 * An actionable item that supports caching for it's children
 *
 * @author Shay Yaakov
 */
public abstract class CachedItemActionableItem extends RepoAwareActionableItemBase implements HierarchicActionableItem {

    @Nullable
    protected List<ActionableItem> children;

    protected CachedItemActionableItem(RepoPath repoPath) {
        super(repoPath);
    }

    //Child items can potentially be removed externally. If a child node does no longer
    //exists we need to recalculate the children.
    // We don't simply remove the deleted item since it might be a compacted folder and we don't
    // want to repeat the same logic.
    protected boolean childrenCacheUpToDate() {
        if (children == null) {
            return false;
        }
        for (ActionableItem item : children) {
            if (item instanceof RepoAwareActionableItem) {
                RepoAwareActionableItem repoAwareItem = (RepoAwareActionableItem) item;
                RepoPath repoPath = repoAwareItem.getRepoPath();
                if (repoAwareItem instanceof FolderActionableItem) {
                    repoPath = ((FolderActionableItem) repoAwareItem).getCanonicalPath();
                }
                RepositoryService repoService = getRepoService();
                if (!repoService.exists(repoPath) || !repoService.isRepoPathVisible(repoPath)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns a new child item
     *
     * @param pathItem       The path to the child content
     * @param relativePath   The relative path to the child itself
     * @param compactAllowed Determines if the child can be compacted
     * @return
     */
    protected RepoAwareActionableItem getChildItem(ItemInfo pathItem, String relativePath, boolean compactAllowed) {
        RepoAwareActionableItem child;
        if (pathItem.isFolder()) {
            child = new FolderActionableItem(((FolderInfo) pathItem), compactAllowed);
        } else {
            MimeType mimeType = NamingUtils.getMimeType(relativePath);
            if (mimeType.isArchive()) {
                child = new ZipFileActionableItem((FileInfo) pathItem, compactAllowed);
            } else {
                child = new FileActionableItem((FileInfo) pathItem);
            }
        }
        return child;
    }
}
