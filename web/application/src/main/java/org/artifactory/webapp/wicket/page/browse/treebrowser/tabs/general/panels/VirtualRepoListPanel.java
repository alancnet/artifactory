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

package org.artifactory.webapp.wicket.page.browse.treebrowser.tabs.general.panels;

import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.component.panel.fieldset.FieldSetPanel;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;
import org.artifactory.util.PathUtils;
import org.artifactory.webapp.actionable.RepoAwareActionableItem;
import org.artifactory.webapp.actionable.model.FileActionableItem;
import org.artifactory.webapp.actionable.model.FolderActionableItem;
import org.artifactory.webapp.servlet.RequestUtils;

import java.util.List;

/**
 * This panel displayes a list of the virtual repositories which are associated with the repo/of the selected item
 *
 * @author Noam Tenne
 */
public class VirtualRepoListPanel extends FieldSetPanel {
    public VirtualRepoListPanel(String id, RepoAwareActionableItem repoItem) {
        super(id);
        add(new CssClass("horizontal-list"));
        addRepoList(repoItem);
    }

    /**
     * Adds a list of virtual repositories, using a RepoAwareActionableItem for information
     *
     * @param item The selected item from the tree
     */
    private void addRepoList(final RepoAwareActionableItem item) {
        RepositoryService repositoryService = ContextHelper.get().getRepositoryService();
        List<VirtualRepoDescriptor> reposToDisplay = repositoryService.getVirtualReposContainingRepo(item.getRepo());

        add(new ListView<VirtualRepoDescriptor>("items", reposToDisplay) {
            @Override
            protected void populateItem(ListItem<VirtualRepoDescriptor> virtualRepo) {
                VirtualRepoDescriptor virtualRepoDescriptor = virtualRepo.getModelObject();
                final String hrefPrefix = RequestUtils.getWicketServletContextUrl();
                String path = getRepoPath(item);
                String href = hrefPrefix + "/" + virtualRepoDescriptor.getKey() + "/" + path;
                if (!href.endsWith("/")) {
                    href += "/";
                }
                AbstractLink link = new ExternalLink("link", href, virtualRepoDescriptor.getKey());
                link.add(new CssClass("repository-virtual"));
                virtualRepo.add(link);
            }
        });
    }

    private String getRepoPath(RepoAwareActionableItem item) {
        if (item instanceof FolderActionableItem) {
            // get the full path of folders in case the folder item is compacted
            FolderActionableItem folderItem = (FolderActionableItem) item;
            return folderItem.getFolderInfo().getRelPath();
        }
        if (item instanceof FileActionableItem) {
            // for files link to the parent folder
            return PathUtils.getParent(item.getRepoPath().getPath());
        }

        return item.getRepoPath().getPath();
    }

    @Override
    public String getTitle() {
        return "Virtual Repository Associations";
    }
}
