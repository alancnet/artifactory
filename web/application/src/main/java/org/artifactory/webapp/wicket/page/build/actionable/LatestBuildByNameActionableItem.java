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

package org.artifactory.webapp.wicket.page.build.actionable;

import org.apache.wicket.markup.html.panel.Panel;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.build.BuildRun;
import org.artifactory.webapp.actionable.ActionableItemBase;
import org.artifactory.webapp.wicket.page.build.action.DeleteAllBuildsAction;
import org.artifactory.webapp.wicket.util.ItemCssClass;

import java.util.Date;

/**
 * Actionable item for the All Builds list
 *
 * @author Noam Y. Tenne
 */
public class LatestBuildByNameActionableItem extends ActionableItemBase {

    private BuildRun buildRun;

    /**
     * Main constructor
     *
     * @param buildRun Basic info of selected builds
     */
    public LatestBuildByNameActionableItem(BuildRun buildRun) {
        this.buildRun = buildRun;
    }

    @Override
    public Panel newItemDetailsPanel(String id) {
        return null;
    }

    @Override
    public String getDisplayName() {
        return buildRun.getName();
    }

    @Override
    public String getCssClass() {
        return ItemCssClass.doc.getCssClass();
    }

    @Override
    public void filterActions(AuthorizationService authService) {
        if (authService.isAdmin()) {
            getActions().add(new DeleteAllBuildsAction(buildRun.getName()));
        }
    }

    /**
     * Returns the name of the build
     *
     * @return Selected build name
     */
    public String getName() {
        return buildRun.getName();
    }

    /**
     * Returns the started time of the build
     *
     * @return Selected build start time
     */
    public String getStarted() {
        return buildRun.getStarted();
    }

    /**
     * Returns the started time of the build as a date
     *
     * @return Selected build start time as date
     */
    public Date getStartedDate() {
        return buildRun.getStartedDate();
    }
}