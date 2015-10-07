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

package org.artifactory.webapp.wicket.page.search.actionable;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.wicket.FilteredResourcesWebAddon;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.search.archive.ArchiveSearchResult;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.mime.NamingUtils;
import org.artifactory.repo.RepoPath;
import org.artifactory.util.PathUtils;
import org.artifactory.webapp.actionable.action.DownloadAction;
import org.artifactory.webapp.actionable.action.ItemAction;
import org.artifactory.webapp.actionable.action.ShowInTreeAction;
import org.artifactory.webapp.actionable.action.ViewSourceAction;

import java.util.Set;

/**
 * Actionable search result for the archive content search
 *
 * @author Noam Tenne
 */
public class ActionableArchiveSearchResult extends ActionableSearchResult<ArchiveSearchResult> {
    private ViewSourceAction viewSourceAction;

    public ActionableArchiveSearchResult(ArchiveSearchResult searchResult) {
        super(searchResult);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addActions(Set<ItemAction> actions) {
        FilteredResourcesWebAddon filteredAddon = ContextHelper.get().beanForType(
                AddonsManager.class).addonByType(FilteredResourcesWebAddon.class);
        if (!filteredAddon.isDefault()) {
            actions.add(new DownloadAction());
        }
        actions.add(new ShowInTreeAction());
        viewSourceAction = new ViewSourceAction();
        actions.add(viewSourceAction);
    }

    /**
     * @return The repo path of the archive containing this entry
     */
    public RepoPath getArchiveRepoPath() {
        return super.getRepoPath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void filterActions(AuthorizationService authService) {
        String entryPath = getSearchResult().getEntryPath();
        if (!NamingUtils.isViewable(entryPath) && !"class".equalsIgnoreCase(PathUtils.getExtension(entryPath))) {
            viewSourceAction.setEnabled(false);
        }
    }
}