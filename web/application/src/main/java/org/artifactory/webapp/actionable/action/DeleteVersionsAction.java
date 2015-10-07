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

package org.artifactory.webapp.actionable.action;

import com.google.common.collect.Lists;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.artifactory.api.module.VersionUnit;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.search.ItemSearchResults;
import org.artifactory.api.search.deployable.VersionUnitSearchResult;
import org.artifactory.common.wicket.component.help.HelpBubble;
import org.artifactory.common.wicket.component.modal.panel.BaseModalPanel;
import org.artifactory.common.wicket.component.modal.panel.bordered.nesting.PanelNestingBorderedModal;
import org.artifactory.repo.RepoPath;
import org.artifactory.webapp.actionable.RepoAwareActionableItem;
import org.artifactory.webapp.actionable.event.ItemEventTargetComponents;
import org.artifactory.webapp.actionable.event.RepoAwareItemEvent;
import org.artifactory.webapp.wicket.page.browse.treebrowser.TreeBrowsePanel;
import org.artifactory.webapp.wicket.page.browse.treebrowser.action.DeleteVersionsPanel;

import java.util.List;

/**
 * @author Yossi Shaul
 */
public class DeleteVersionsAction extends RepoAwareItemAction {
    public static final String ACTION_NAME = "Delete Versions...";

    public DeleteVersionsAction() {
        super(ACTION_NAME);
    }

    @Override
    public void onAction(RepoAwareItemEvent event) {
        RepoAwareActionableItem source = event.getSource();
        org.artifactory.fs.ItemInfo info = source.getItemInfo();
        RepoPath itemPath = info.getRepoPath();
        if(!info.isFolder() && itemPath.getParent() != null) {
            itemPath = itemPath.getParent();
        }
        RepositoryService repositoryService = getRepoService();
        ItemSearchResults<VersionUnitSearchResult> results = repositoryService.getVersionUnitsUnder(itemPath);

        List<VersionUnit> versionUnits = Lists.newArrayList();
        for (VersionUnitSearchResult result : results.getResults()) {
            versionUnits.add(result.getVersionUnit());
        }

        ItemEventTargetComponents eventTargetComponents = event.getTargetComponents();
        ModalWindow modalWindow = eventTargetComponents.getModalWindow();
        WebMarkupContainer nodePanelContainer = eventTargetComponents.getNodePanelContainer();
        TreeBrowsePanel browseRepoPanel = (TreeBrowsePanel) nodePanelContainer.getParent();

        DeleteVersionsPanel panel = new DeleteVersionsPanel(modalWindow.getContentId(), versionUnits,
                browseRepoPanel, event.getSource());
        BaseModalPanel modalPanel = new PanelNestingBorderedModal(panel);
        modalPanel.setTitle("Delete Versions");
        modalWindow.setContent(modalPanel);
        AjaxRequestTarget target = event.getTarget();

        //VfsQueryDb returns -1 if userQueryLimit was exceeded.
        boolean exceededQueryLimit = results.getFullResultsCount() == -1;
        String warnMessage = "";
        String bubbleMessage = "";

        if (exceededQueryLimit) {
            modalWindow.setInitialWidth(700); //So that the warning text and the bubble are inline

            warnMessage = "The results shown are limited. Try executing Delete Versions from a node lower in the " +
                    "hierarchy.";

            bubbleMessage = "The results shown are limited by the artifactory.search.userQueryLimit system property."
                    + "\n You can modify it to get more results, but this can dramatically impact performance on large "
                    + "repositories.";
        }
        Label label = new Label("largeQueryWarn", warnMessage);
        HelpBubble bubble = new HelpBubble("largeQueryWarn.help", bubbleMessage);
        panel.add(label);
        panel.add(bubble);
        label.setVisible(exceededQueryLimit);
        bubble.setVisible(exceededQueryLimit);
        modalWindow.show(target);
    }
}