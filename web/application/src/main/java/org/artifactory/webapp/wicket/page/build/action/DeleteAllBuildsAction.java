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

package org.artifactory.webapp.wicket.page.build.action;

import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.request.cycle.RequestCycle;
import org.artifactory.api.build.BuildService;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.StatusEntry;
import org.artifactory.common.wicket.component.confirm.AjaxConfirm;
import org.artifactory.common.wicket.component.confirm.ConfirmDialog;
import org.artifactory.common.wicket.util.AjaxUtils;
import org.artifactory.webapp.actionable.action.DeleteAction;
import org.artifactory.webapp.actionable.action.ItemAction;
import org.artifactory.webapp.actionable.event.ItemEvent;
import org.artifactory.webapp.wicket.page.build.page.BuildBrowserRootPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Deletes all the builds of the selected name
 *
 * @author Noam Y. Tenne
 */
public class DeleteAllBuildsAction extends ItemAction {
    private static final Logger log = LoggerFactory.getLogger(DeleteAllBuildsAction.class);

    private static final String ACTION_NAME = "Delete All Builds";
    private String buildName;

    /**
     * Main constructor
     *
     * @param buildName Name of builds to delete
     */
    public DeleteAllBuildsAction(String buildName) {
        super(ACTION_NAME);
        this.buildName = buildName;
    }

    @Override
    public void onAction(final ItemEvent e) {
        AjaxConfirm.get().confirm(new ConfirmDialog() {
            @Override
            public String getMessage() {
                return String.format("Are you sure you wish to delete all the builds of '%s'?", buildName);
            }

            @Override
            public void onConfirm(boolean approved, AjaxRequestTarget target) {
                if (approved) {
                    delete(e);
                }
            }
        });
    }

    @Override
    public String getCssClass() {
        return DeleteAction.class.getSimpleName();
    }

    /**
     * Deletes the builds
     *
     * @param e Item event
     */
    private void delete(ItemEvent e) {
        AjaxRequestTarget target = e.getTarget();
        BuildService buildService = ContextHelper.get().beanForType(BuildService.class);

        BasicStatusHolder multiStatusHolder = new BasicStatusHolder();
        try {
            buildService.deleteBuild(buildName, false, multiStatusHolder);
            multiStatusHolder.status(String.format("Successfully deleted '%s'.", buildName), log);
        } catch (Exception exception) {
            multiStatusHolder.error(String.format("Exception occurred while deleting '%s'", buildName), exception,
                    log);
        }

        if (multiStatusHolder.hasErrors()) {
            Session.get().error(multiStatusHolder.getLastError().getMessage());
            AjaxUtils.refreshFeedback(target);
            return;
        } else if (multiStatusHolder.hasWarnings()) {
            List<StatusEntry> warnings = multiStatusHolder.getWarnings();
            Session.get().warn(warnings.get(warnings.size() - 1).getMessage());
            AjaxUtils.refreshFeedback(target);
            return;
        } else {
            Session.get().info(multiStatusHolder.getStatusMsg());
            AjaxUtils.refreshFeedback(target);
        }

        RequestCycle.get().setResponsePage(BuildBrowserRootPage.class);
    }
}
