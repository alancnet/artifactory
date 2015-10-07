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

import org.artifactory.webapp.actionable.ActionableItem;
import org.artifactory.webapp.actionable.action.ItemAction;
import org.artifactory.webapp.actionable.event.ItemEvent;

/**
 * Provides the URL to the builds CI server
 *
 * @author Noam Y. Tenne
 */
public class ShowInCiServerAction extends ItemAction {

    private static final String ACTION_NAME = "Show in CI Server";
    private String url;

    /**
     * Main constructor
     *
     * @param url URL to build in CI server
     */
    public ShowInCiServerAction(String url) {
        super(ACTION_NAME);
        this.url = url;
    }

    @Override
    public String getActionLinkURL(ActionableItem actionableItem) {
        return url;
    }

    @Override
    public void onAction(ItemEvent e) {
        // this method should not be called for this action
    }
}