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

package org.artifactory.webapp.wicket.page.browse.treebrowser.tabs.build.action;

import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.artifactory.build.BuildRun;
import org.artifactory.webapp.actionable.action.ItemAction;
import org.artifactory.webapp.actionable.event.ItemEvent;
import org.artifactory.webapp.wicket.page.build.BuildBrowserConstants;
import org.artifactory.webapp.wicket.page.build.page.BuildBrowserRootPage;

/**
 * Redirects to the build history view of the given build name
 *
 * @author Noam Y. Tenne
 */
public class GoToBuildAction extends ItemAction {

    private static final String ACTION_NAME = "Go To Build";
    private BuildRun buildRun;
    private String moduleId;

    /**
     * Main constructor
     *
     * @param buildRun Basic build info to act upon
     * @param moduleId ID of module to point to
     */
    public GoToBuildAction(BuildRun buildRun, String moduleId) {
        super(ACTION_NAME);
        this.buildRun = buildRun;
        this.moduleId = moduleId;
    }

    @Override
    public void onAction(ItemEvent e) {
        PageParameters pageParameters = new PageParameters();
        pageParameters.set(BuildBrowserConstants.BUILD_NAME, buildRun.getName());
        pageParameters.set(BuildBrowserConstants.BUILD_NUMBER, buildRun.getNumber());
        pageParameters.set(BuildBrowserConstants.BUILD_STARTED, buildRun.getStarted());
        pageParameters.set(BuildBrowserConstants.MODULE_ID, moduleId);
        RequestCycle.get().setResponsePage(BuildBrowserRootPage.class, pageParameters);
    }
}