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

import org.apache.commons.io.FilenameUtils;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;
import org.artifactory.api.search.ItemSearchResult;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.webapp.actionable.RepoAwareActionableItemBase;
import org.artifactory.webapp.actionable.action.ItemAction;
import org.artifactory.webapp.wicket.util.ItemCssClass;

import java.util.List;
import java.util.Set;

/**
 * Base class for actionable search results. Gives the different search results the freedom to customize the actions
 * Menu
 *
 * @author Yossi Shaul
 */
public abstract class ActionableSearchResult<T extends ItemSearchResult> extends RepoAwareActionableItemBase {

    private T searchResult;

    /**
     * Default constructor
     *
     * @param searchResult The search result we want to attach actions to
     */
    public ActionableSearchResult(T searchResult) {
        super(searchResult.getItemInfo());
        this.searchResult = searchResult;
        Set<ItemAction> actions = getActions();
        addActions(actions);
    }

    /**
     * Add action items to the menu list
     *
     * @param actions List to collect actions.
     */
    protected abstract void addActions(Set<ItemAction> actions);

    /**
     * Include or exclude different actions from the menu
     *
     * @param authService For auth purposes
     */
    @Override
    public abstract void filterActions(AuthorizationService authService);

    public T getSearchResult() {
        return searchResult;
    }

    @Override
    public Panel newItemDetailsPanel(String id) {
        throw new UnsupportedOperationException("method not allowed on search result");
    }

    @Override
    public void addTabs(List<ITab> tabs) {
        throw new UnsupportedOperationException("method not allowed on search result");
    }

    @Override
    public String getDisplayName() {
        return getItemInfo().getName();
    }

    @Override
    public String getCssClass() {
        return ItemCssClass.getFileCssClass(getItemInfo().getRelPath()).getCssClass();
    }

    public String getBaseName() {
        return FilenameUtils.getBaseName(searchResult.getName());
    }
}