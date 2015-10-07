/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2014 JFrog Ltd.
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

package org.artifactory.storage.fs.tree;

import com.google.common.collect.Lists;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.fs.ItemInfo;
import org.artifactory.repo.RepoPath;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.List;

/**
 * Criteria builder for tree browsing.
 *
 * @author Yossi Shaul
 */
public class TreeBrowsingCriteriaBuilder {

    private boolean cacheChildren = true;
    private List<ItemNodeFilter> filters;
    private Comparator<ItemInfo> comparator;

    public TreeBrowsingCriteria build() {
        return new TreeBrowsingCriteria(cacheChildren, filters, comparator);
    }

    public TreeBrowsingCriteriaBuilder cacheChildren(boolean cache) {
        cacheChildren = cache;
        return this;
    }

    public TreeBrowsingCriteriaBuilder addFilter(ItemNodeFilter filter) {
        if (filter == null) {
            return this;
        }
        if (filters == null) {
            filters = Lists.newArrayList();
        }
        filters.add(filter);
        return this;
    }

    public TreeBrowsingCriteriaBuilder sortAlphabetically() {
        comparator = new Comparator<ItemInfo>() {
            @Override
            public int compare(ItemInfo a, ItemInfo b) {
                if (a.getName().equals(b.getName())) {
                    return 0;
                }
                if (a.isFolder() && !b.isFolder()) {
                    return -1;
                }
                if (!a.isFolder() && b.isFolder()) {
                    return 1;
                }
                return a.getName().compareTo(b.getName());
            }
        };
        return this;
    }

    public TreeBrowsingCriteriaBuilder sortBy(Comparator<ItemInfo> comparator) {
        this.comparator = comparator;
        return this;
    }

    public TreeBrowsingCriteriaBuilder applySecurity() {
        addFilter(new ItemNodeSecurityFilter(ContextHelper.get().getAuthorizationService()));
        return this;
    }

    public TreeBrowsingCriteriaBuilder applyRepoIncludeExclude() {
        addFilter(new ItemNodeRepoIncludeExcludeFilter(ContextHelper.get().getRepositoryService()));
        return this;
    }

    private static class ItemNodeSecurityFilter implements ItemNodeFilter,FilterAccepted {
        private final AuthorizationService authService;
        private boolean canRead = true;

        public ItemNodeSecurityFilter(AuthorizationService authService) {
            this.authService = authService;
        }

        @Override
        public boolean accepts(@Nonnull ItemInfo itemInfo) {
            canRead =  authService.canRead(itemInfo.getRepoPath());
            return canRead;
        }

        @Override
        public boolean isNodeAcceptCanRead() {
            return canRead;
        }
    }

    private static class ItemNodeRepoIncludeExcludeFilter implements ItemNodeFilter,FilterAccepted {
        private final RepositoryService repoService;

        public ItemNodeRepoIncludeExcludeFilter(RepositoryService repoService) {
            this.repoService = repoService;
        }

        @Override
        public boolean accepts(@Nonnull ItemInfo itemInfo) {
            RepoPath repoPath = itemInfo.getRepoPath();
            return repoService.isRepoPathAccepted(repoPath);
        }

        @Override
        public boolean isNodeAcceptCanRead() {
            return true;
        }
    }
}

