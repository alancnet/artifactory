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

import org.apache.wicket.Component;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.artifactory.common.wicket.component.modal.panel.BaseModalPanel;
import org.artifactory.common.wicket.component.modal.panel.bordered.nesting.PanelNestingBorderedModal;
import org.artifactory.repo.RepoPath;
import org.artifactory.webapp.actionable.event.ItemEventTargetComponents;
import org.artifactory.webapp.actionable.event.RepoAwareItemEvent;
import org.artifactory.webapp.wicket.page.browse.treebrowser.TreeBrowsePanel;
import org.artifactory.webapp.wicket.page.browse.treebrowser.action.MovePathPanel;

/**
 * @author Yossi Shaul
 */
public class MoveAction extends RepoAwareItemAction {
    public static final String ACTION_NAME = "Move";

    public MoveAction() {
        super(ACTION_NAME);
    }

    @Override
    public void onAction(RepoAwareItemEvent event) {
        RepoPath repoPath = event.getRepoPath();

        // create a modal window and add the move path panel to it
        ItemEventTargetComponents eventTargetComponents = event.getTargetComponents();
        // should be the tree
        Component tree = eventTargetComponents.getRefreshableComponent();

        WebMarkupContainer nodaPanelContainer = eventTargetComponents.getNodePanelContainer();
        TreeBrowsePanel browseRepoPanel = (TreeBrowsePanel) nodaPanelContainer.getParent();

        ModalWindow modalWindow = eventTargetComponents.getModalWindow();
        MovePathPanel panel = new MovePathPanel(modalWindow.getContentId(), repoPath, tree, browseRepoPanel);

        BaseModalPanel modalPanel = new PanelNestingBorderedModal(panel);
        modalPanel.setWidth(500);
        modalPanel.setTitle(String.format("Move '%s'", repoPath));
        modalWindow.setContent(modalPanel);
        modalWindow.show(event.getTarget());
    }
}