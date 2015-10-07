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
import org.artifactory.repo.RepoPath;
import org.artifactory.webapp.actionable.action.ItemAction;
import org.artifactory.webapp.actionable.action.ShowInTreeAction;
import org.artifactory.webapp.actionable.event.ItemEvent;
import org.artifactory.webapp.wicket.page.browse.treebrowser.BrowseRepoPage;

/**
 * Displays the given repo path of a build-produced item in the tree
 *
 * @author Noam Y. Tenne
 */
public class ShowBuildItemInTreeAction extends ItemAction {

    private static final String ACTION_NAME = "Show Build Item In Tree";
    private RepoPath repoPath;

    /**
     * Main constructor
     *
     * @param repoPath Repo path to show
     */
    public ShowBuildItemInTreeAction(RepoPath repoPath) {
        super(ACTION_NAME);
        this.repoPath = repoPath;
    }

    @Override
    public void onAction(ItemEvent e) {
        RequestCycle.get().setResponsePage(new BrowseRepoPage(repoPath));
    }

    @Override
    public String getCssClass() {
        return ShowInTreeAction.class.getSimpleName();
    }
}