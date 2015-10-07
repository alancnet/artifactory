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

import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.artifactory.common.wicket.component.modal.panel.BaseModalPanel;
import org.artifactory.common.wicket.component.modal.panel.bordered.nesting.PanelNestingBorderedModal;
import org.artifactory.repo.RepoPath;
import org.artifactory.webapp.actionable.event.ItemEventTargetComponents;
import org.artifactory.webapp.actionable.event.RepoAwareItemEvent;
import org.artifactory.webapp.wicket.page.browse.treebrowser.action.CopyPathPanel;

/**
 * Enables the user to copy the currently selected location of the tree to another repository
 *
 * @author Noam Y. Tenne
 */
public class CopyAction extends RepoAwareItemAction {

    public static final String ACTION_NAME = "Copy";

    public CopyAction() {
        super(ACTION_NAME);
    }

    @Override
    public void onAction(RepoAwareItemEvent event) {
        RepoPath repoPath = event.getRepoPath();

        //Create a modal window and add the move path panel to it
        ItemEventTargetComponents eventTargetComponents = event.getTargetComponents();

        ModalWindow modalWindow = eventTargetComponents.getModalWindow();
        CopyPathPanel panel = new CopyPathPanel(modalWindow.getContentId(), repoPath);

        BaseModalPanel modalPanel = new PanelNestingBorderedModal(panel);
        modalPanel.setWidth(500);
        modalPanel.setTitle(String.format("Copy '%s'", repoPath));
        modalWindow.setContent(modalPanel);
        modalWindow.show(event.getTarget());
    }
}
