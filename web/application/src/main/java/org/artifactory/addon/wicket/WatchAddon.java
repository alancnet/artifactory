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

package org.artifactory.addon.wicket;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.artifactory.addon.Addon;
import org.artifactory.repo.RepoPath;
import org.artifactory.webapp.actionable.action.ItemAction;

/**
 * Addon for artifact watching capabilities
 *
 * @author Noam Tenne
 */
public interface WatchAddon extends Addon {

    /**
     * Returns the watch action object
     *
     * @param itemRepoPath RepoPath object of the select item
     * @return ItemAction
     */
    ItemAction getWatchAction(RepoPath itemRepoPath);

    /**
     * Returns the tab item watchers tab
     *
     * @param tabTitle The tab title
     * @param repoPath RepoPath object of the select item
     * @return Item watchers tab
     */
    ITab getWatchersTab(String tabTitle, RepoPath repoPath);

    /**Wat
     * Returns the Watching Since label
     *
     * @param labelId      ID to give to constructed label
     * @param itemRepoPath RepoPath object of the select item
     * @return LabeledValue
     */
    MarkupContainer getWatchingSinceLabel(String labelId, RepoPath itemRepoPath);

    /**
     * Returns the Directly Watched Path panel
     *
     * @param panelId      ID to give to constructed panel
     * @param itemRepoPath RepoPath object of the select item
     * @return Watched path panel
     */
    MarkupContainer getDirectlyWatchedPathPanel(String panelId, RepoPath itemRepoPath);
}