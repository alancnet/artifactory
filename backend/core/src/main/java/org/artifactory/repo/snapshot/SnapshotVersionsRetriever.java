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

import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.module.ModuleInfo;
import org.artifactory.api.module.regex.NamedPattern;
import org.artifactory.descriptor.repo.RepoLayout;
import org.artifactory.fs.ItemInfo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.StoringRepo;
import org.artifactory.storage.fs.tree.ItemNode;
import org.artifactory.storage.fs.tree.ItemNodeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Collects integration versions items under a given root node.
 *
 * @author Shay Yaakov
 */
public class SnapshotVersionsRetriever extends VersionsRetriever {
    private static final Logger log = LoggerFactory.getLogger(SnapshotVersionsRetriever.class);

    private Map<String, Calendar> integrationCreationMap = Maps.newHashMap();

    public SnapshotVersionsRetriever(boolean reverseOrderResults) {
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
            RepoPath itemRepoPath = node.getRepoPath();
            ModuleInfo itemModuleInfo = repo.getItemModuleInfo(itemRepoPath.getPath());

            Calendar itemCreated = Calendar.getInstance();
            ItemInfo itemInfo = node.getItemInfo();
            itemCreated.setTimeInMillis(itemInfo.getCreated());

            String uniqueRevision = itemModuleInfo.getFileIntegrationRevision();

            //If we already keep a creation date for this child's unique revision
            if (integrationCreationMap.containsKey(uniqueRevision)) {

                //If the current child's creation date precedes the existing one
                Calendar existingIntegrationCreation = integrationCreationMap.get(uniqueRevision);
                if (itemCreated.before(existingIntegrationCreation)) {

                    //Update the reference of all the children with the same unique integration
                    integrationCreationMap.put(uniqueRevision, itemCreated);
                    Collection<ItemInfo> itemsToRelocate = versionsItems.removeAll(existingIntegrationCreation);
                    versionsItems.putAll(itemCreated, itemsToRelocate);
                    versionsItems.put(itemCreated, itemInfo);
                } else {

                    //Child's creation date isn't newer, just add it
                    versionsItems.put(existingIntegrationCreation, itemInfo);
                }
            } else {
                //No reference exists yet, create one
                integrationCreationMap.put(uniqueRevision, itemCreated);
                versionsItems.put(itemCreated, itemInfo);
            }
        }
    }

    @Override
    public ItemNodeFilter getFileFilter(StoringRepo repo, NamedPattern pattern) {
        return new IntegrationFileFilter(repo, pattern);
    }

    private static class IntegrationFileFilter implements ItemNodeFilter {
        private final StoringRepo repo;

        private final NamedPattern pattern;

        public IntegrationFileFilter(StoringRepo repo, NamedPattern pattern) {
            this.repo = repo;
            this.pattern = pattern;
        }

        @Override
        public boolean accepts(ItemInfo itemInfo) {
            if (itemInfo.isFolder()) {
                return true;
            }
            String path = itemInfo.getRelPath();
            if (!pattern.matcher(path).matches()) {
                return false;
            }
            ModuleInfo itemModuleInfo = repo.getItemModuleInfo(path);
            RepoLayout repoLayout = repo.getDescriptor().getRepoLayout();

            boolean integrationCondition = itemModuleInfo.isIntegration() &&
                    //Checks to make sure it is not a non-unique integration mixed with unique integrations
                    (StringUtils.equals(repoLayout.getFolderIntegrationRevisionRegExp(),
                            repoLayout.getFileIntegrationRevisionRegExp()) ||
                            !StringUtils.equals(itemModuleInfo.getFolderIntegrationRevision(),
                                    itemModuleInfo.getFileIntegrationRevision()));

            //Make sure this file's module info is valid and is actually an integration version
            return itemModuleInfo.isValid() && integrationCondition;
        }
    }
}
