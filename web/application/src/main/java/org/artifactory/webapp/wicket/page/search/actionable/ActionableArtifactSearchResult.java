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

import org.artifactory.api.search.ItemSearchResult;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.mime.NamingUtils;
import org.artifactory.webapp.actionable.action.DownloadAction;
import org.artifactory.webapp.actionable.action.ItemAction;
import org.artifactory.webapp.actionable.action.ShowInTreeAction;
import org.artifactory.webapp.actionable.action.ViewAction;
import org.artifactory.webapp.actionable.action.ViewTextFileAction;

import java.util.Set;

/**
 * Actionable search result for the artifact search
 *
 * @author Noam Tenne
 */
public class ActionableArtifactSearchResult<T extends ItemSearchResult> extends ActionableSearchResult<T> {

    protected T searchResult;
    private ViewAction viewAction;
    protected DownloadAction downloadAction;

    public ActionableArtifactSearchResult(T searchResult) {
        super(searchResult);
        this.searchResult = searchResult;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addActions(Set<ItemAction> actions) {
        viewAction = new ViewTextFileAction();
        actions.add(viewAction);
        downloadAction = new DownloadAction();
        actions.add(downloadAction);
        actions.add(new ShowInTreeAction());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void filterActions(AuthorizationService authService) {
        //If the result is not a pom file or xml file
        if (!NamingUtils.isViewable((getItemInfo().getName()))) {
            viewAction.setEnabled(false);
        }
    }
}
