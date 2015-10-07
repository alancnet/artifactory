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
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.artifactory.api.build.BuildService;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.build.BuildRun;
import org.artifactory.common.StatusEntry;
import org.artifactory.common.wicket.component.confirm.AjaxConfirm;
import org.artifactory.common.wicket.component.confirm.ConfirmDialog;
import org.artifactory.common.wicket.util.AjaxUtils;
import org.artifactory.webapp.actionable.action.DeleteAction;
import org.artifactory.webapp.actionable.action.ItemAction;
import org.artifactory.webapp.actionable.event.ItemEvent;
import org.artifactory.webapp.wicket.page.build.BuildBrowserConstants;
import org.artifactory.webapp.wicket.page.build.page.BuildBrowserRootPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

/**
 * Deletes the selected build
 *
 * @author Noam Y. Tenne
 */
public class DeleteBuildAction extends ItemAction {
    private static final Logger log = LoggerFactory.getLogger(DeleteBuildAction.class);

    private static final String ACTION_NAME = "Delete";
    private BuildRun buildRun;

    /**
     * Main constructor
     *
     * @param buildRun Basic build info of build to delete
     */
    public DeleteBuildAction(BuildRun buildRun) {
        super(ACTION_NAME);
        this.buildRun = buildRun;
    }

    @Override
    public void onAction(final ItemEvent e) {
        AjaxConfirm.get().confirm(new ConfirmDialog() {
            @Override
            public String getMessage() {
                return String.format("Are you sure you wish to delete the build '%s' #%s?",
                        buildRun.getName(), buildRun.getNumber());
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
     * Deletes the build
     *
     * @param e Item event
     */
    private void delete(ItemEvent e) {
        AjaxRequestTarget target = e.getTarget();
        BuildService buildService = ContextHelper.get().beanForType(BuildService.class);
        String buildName = buildRun.getName();
        String buildNumber = buildRun.getNumber();

        BasicStatusHolder multiStatusHolder = new BasicStatusHolder();
        try {
            buildService.deleteBuild(buildRun, false, multiStatusHolder);
            multiStatusHolder.status(String.format("Successfully deleted build '%s' #%s.", buildName, buildNumber),
                    log);
        } catch (Exception exception) {
            String error = String.format("Exception occurred while deleting build '%s' #%s", buildName, buildNumber);
            multiStatusHolder.error(error, exception, log);
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

        Set<BuildRun> remainingBuilds = buildService.searchBuildsByName(buildName);
        PageParameters pageParameters = new PageParameters();

        if (!remainingBuilds.isEmpty()) {
            pageParameters.set(BuildBrowserConstants.BUILD_NAME, buildName);
        }

        RequestCycle.get().setResponsePage(BuildBrowserRootPage.class, pageParameters);
    }
}
