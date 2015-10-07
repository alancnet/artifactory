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

package org.artifactory.repo.snapshot;

import org.artifactory.api.module.ModuleInfo;
import org.artifactory.api.module.regex.NamedPattern;
import org.artifactory.fs.ItemInfo;
import org.artifactory.repo.StoringRepo;
import org.artifactory.storage.fs.tree.ItemNode;
import org.artifactory.storage.fs.tree.ItemNodeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Calendar;
import java.util.List;

/**
 * Collects release versions items under a given root node.
 *
 * @author Shay Yaakov
 */
public class ReleaseVersionsRetriever extends VersionsRetriever {
    private static final Logger log = LoggerFactory.getLogger(ReleaseVersionsRetriever.class);

    public ReleaseVersionsRetriever(boolean reverseOrderResults) {
        super(reverseOrderResults);
    }

    @Override
    protected void internalCollectVersionsItems(StoringRepo repo, ItemNode node) {
        if (node.isFolder()) {
            List<ItemNode> children = node.getChildren();
            for (ItemNode child : children) {
                internalCollectVersionsItems(repo, child);
            }
        } else {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(node.getItemInfo().getCreated());
            versionsItems.put(cal, node.getItemInfo());
        }
    }

    @Override
    public ItemNodeFilter getFileFilter(StoringRepo repo, NamedPattern pattern) {
        return new ReleaseFileFilter(repo, pattern);
    }

    private static class ReleaseFileFilter implements ItemNodeFilter {
        private final StoringRepo repo;

        private final NamedPattern pattern;

        public ReleaseFileFilter(StoringRepo repo, NamedPattern pattern) {
            this.repo = repo;
            this.pattern = pattern;
        }

        @Override
        public boolean accepts(@Nonnull ItemInfo itemInfo) {
            if (itemInfo.isFolder()) {
                return true;
            }
            String path = itemInfo.getRelPath();
            if (!pattern.matcher(path).matches()) {
                return false;
            }

            //Make sure this file's module info is valid and is actually a release version
            ModuleInfo itemModuleInfo = repo.getItemModuleInfo(path);
            return itemModuleInfo.isValid() && !itemModuleInfo.isIntegration();
        }
    }
}
