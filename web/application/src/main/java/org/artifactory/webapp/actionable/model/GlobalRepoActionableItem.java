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

import com.google.common.collect.Lists;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.descriptor.repo.LocalRepoAlphaComparator;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.webapp.actionable.ActionableItem;
import org.artifactory.webapp.actionable.ActionableItemBase;
import org.artifactory.webapp.wicket.util.ItemCssClass;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * This is the root node of the tree browser. It contains all the repository nodes.
 *
 * @author Yoav Landman
 */
public class GlobalRepoActionableItem extends ActionableItemBase implements HierarchicActionableItem {

    private boolean compactAllowed;

    @Override
    public boolean isCompactAllowed() {
        return compactAllowed;
    }

    @Override
    public void setCompactAllowed(boolean compactAllowed) {
        this.compactAllowed = compactAllowed;
    }

    @Override
    public String getDisplayName() {
        return "";
    }

    @Override
    public String getCssClass() {
        return ItemCssClass.root.getCssClass();
    }

    @Override
    public Panel newItemDetailsPanel(String id) {
        return new EmptyPanel(id);
    }

    @Override
    public List<ActionableItem> getChildren(AuthorizationService authService) {
        //Add a tree node for each file repository and local cache repository
        RepositoryService repositoryService = ContextHelper.get().getRepositoryService();
        List<LocalRepoDescriptor> repos = repositoryService.getLocalAndCachedRepoDescriptors();
        removeNonPermissionRepositories(repos);
        Collections.sort(repos, new LocalRepoAlphaComparator());
        List<ActionableItem> items = Lists.newArrayListWithCapacity(repos.size());
        for (LocalRepoDescriptor repo : repos) {
            LocalRepoActionableItem repoActionableItem = new LocalRepoActionableItem(repo);
            repoActionableItem.setCompactAllowed(isCompactAllowed());
            items.add(repoActionableItem);
        }
        return items;
    }

    @Override
    public boolean hasChildren(AuthorizationService authService) {
        return true;
    }

    @Override
    public void filterActions(AuthorizationService authService) {
    }

    private void removeNonPermissionRepositories(List<? extends RepoDescriptor> repositories) {
        AuthorizationService authorizationService = ContextHelper.get().getAuthorizationService();
        Iterator<? extends RepoDescriptor> repoDescriptors = repositories.iterator();
        while (repoDescriptors.hasNext()) {
            RepoDescriptor repoDescriptor = repoDescriptors.next();
            if (!authorizationService.userHasPermissionsOnRepositoryRoot(repoDescriptor.getKey())) {
                repoDescriptors.remove();
            }
        }
    }
}