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

import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.wicket.BuildAddon;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.StatusHolder;
import org.artifactory.common.wicket.component.confirm.AjaxConfirm;
import org.artifactory.common.wicket.component.confirm.ConfirmDialog;
import org.artifactory.common.wicket.util.AjaxUtils;
import org.artifactory.repo.RepoPath;
import org.artifactory.webapp.actionable.event.ItemEvent;
import org.artifactory.webapp.actionable.event.RepoAwareItemEvent;
import org.artifactory.webapp.wicket.page.browse.treebrowser.TreeBrowsePanel;

/**
 * @author yoavl
 */
public class DeleteAction extends RepoAwareItemAction {
    public static final String ACTION_NAME = "Delete";

    public DeleteAction() {
        super(ACTION_NAME);
    }

    @Override
    public void onAction(final RepoAwareItemEvent e) {
        AjaxConfirm.get().confirm(new ConfirmDialog() {
            @Override
            public String getMessage() {
                return getDeleteConfirmMessage(e);
            }

            @Override
            public void onConfirm(boolean approved, AjaxRequestTarget target) {
                if (approved) {
                    deleteItem(e);
                }
            }
        });
    }

    protected void deleteItem(RepoAwareItemEvent e) {
        RepoPath repoPath = e.getRepoPath();
        StatusHolder status = callUndeploy(repoPath);

        if (status.isError()) {
            String message = status.getStatusMsg();
            Session.get().error("Delete failed with error: " + message);
        } else {
            Session.get().info(getDeleteSuccessMessage(repoPath));
            removeNodePanel(e);
            notifyListeners(e);
        }
        AjaxUtils.refreshFeedback(e.getTarget());
    }

    protected StatusHolder callUndeploy(RepoPath repoPath) {
        return getRepoService().undeploy(repoPath, true, true);
    }

    protected String getDeleteSuccessMessage(RepoPath repoPath) {
        return "Successfully deleted '" + repoPath + "'.";
    }

    @Override
    public boolean isNotifyingListeners() {
        return false;
    }

    protected String getDeleteConfirmMessage(RepoAwareItemEvent e) {
        String defaultMessage = "Are you sure you wish to delete '" + e.getSource().getDisplayName() + "'?";
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        BuildAddon buildAddon = addonsManager.addonByType(BuildAddon.class);
        return buildAddon.getDeleteItemWarningMessage(e.getSource().getItemInfo(), defaultMessage);
    }

    private void removeNodePanel(ItemEvent event) {
        WebMarkupContainer nodePanelContainer = event.getTargetComponents().getNodePanelContainer();
        TreeBrowsePanel browseRepoPanel = (TreeBrowsePanel) nodePanelContainer.getParent();
        browseRepoPanel.removeNodePanel(event.getTarget());
    }
}