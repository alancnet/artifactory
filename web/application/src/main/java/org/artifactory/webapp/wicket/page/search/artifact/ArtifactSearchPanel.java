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

package org.artifactory.webapp.wicket.page.search.artifact;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Page;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.artifactory.api.search.ItemSearchResults;
import org.artifactory.api.search.artifact.ArtifactSearchControls;
import org.artifactory.api.search.artifact.ArtifactSearchResult;
import org.artifactory.common.wicket.component.help.HelpBubble;
import org.artifactory.common.wicket.component.table.groupable.column.GroupableColumn;
import org.artifactory.webapp.wicket.actionable.column.ActionsColumn;
import org.artifactory.webapp.wicket.page.search.BaseSearchPage;
import org.artifactory.webapp.wicket.page.search.BaseSearchPanel;
import org.artifactory.webapp.wicket.page.search.LastModifiedColumn;
import org.artifactory.webapp.wicket.page.search.actionable.ActionableArtifactSearchResult;
import org.artifactory.webapp.wicket.page.search.actionable.ActionableSearchResult;

import java.util.List;

/**
 * Displays the simple artifact searcher
 *
 * @author Noam Tenne
 */
public class ArtifactSearchPanel extends BaseSearchPanel<ArtifactSearchResult> {

    private ArtifactSearchControls searchControls;

    public ArtifactSearchPanel(Page parent, String id, String query) {
        super(parent, id);

        if (StringUtils.isNotBlank(query)) {
            searchControls.setQuery(query);
            fetchResults(parent);
        }
    }

    @Override
    protected void validateSearchControls() {
        if (searchControls.isEmpty()) {
            throw new IllegalArgumentException("The search term cannot be empty");
        }
        if (searchControls.isWildcardsOnly()) {
            throw new IllegalArgumentException("Search term containing only wildcards is not permitted");
        }
    }

    @Override
    protected void addSearchComponents(Form form) {
        searchControls = new ArtifactSearchControls();

        TextField searchControl = new TextField<>("query", new PropertyModel<String>(searchControls, "query"));
        searchControl.setOutputMarkupId(true);
        form.add(searchControl);

        form.add(new HelpBubble("searchHelp", "Artifact name. * and ? are accepted."));
    }

    @Override
    protected ArtifactSearchControls getSearchControls() {
        return searchControls;
    }

    @Override
    protected Class<? extends BaseSearchPage> getMenuPageClass() {
        return ArtifactSearchPage.class;
    }

    @Override
    protected void onNoResults() {
        warnNoArtifacts(searchControls.getQuery());
    }

    @Override
    @SuppressWarnings({"unchecked"})
    protected ActionableSearchResult<ArtifactSearchResult> getActionableResult(ArtifactSearchResult searchResult) {
        return new ActionableArtifactSearchResult(searchResult);
    }

    @Override
    protected boolean isLimitSearchResults() {
        return searchControls.isLimitSearchResults();
    }

    @Override
    public String getSearchExpression() {
        return searchControls.getQuery();
    }

    @Override
    protected void addColumns(List<IColumn<ActionableSearchResult<ArtifactSearchResult>>> columns) {
        columns.add(new ActionsColumn<ActionableSearchResult<ArtifactSearchResult>>(""));
        columns.add(new BaseSearchPanel.ArtifactNameColumn());
        columns.add(new GroupableColumn<ActionableSearchResult<ArtifactSearchResult>>(
                Model.of("Path"), "searchResult.relDirPath", "searchResult.relDirPath"));
        columns.add(new LastModifiedColumn());
        columns.add(new GroupableColumn<ActionableSearchResult<ArtifactSearchResult>>(
                Model.of("Repository"), "searchResult.repoKey", "searchResult.repoKey"));
    }

    @Override
    protected ItemSearchResults<ArtifactSearchResult> searchArtifacts() {
        return search(true);
    }

    @Override
    protected ItemSearchResults<ArtifactSearchResult> performLimitlessArtifactSearch() {
        return search(false);
    }

    /**
     * Performs the search
     *
     * @param limitResults True if the search results should be limited
     * @return List of search results
     */
    private ItemSearchResults<ArtifactSearchResult> search(boolean limitResults) {
        ArtifactSearchControls controlsCopy = new ArtifactSearchControls(searchControls);

        String query = searchControls.getQuery();
        if (StringUtils.isNotBlank(query)) {
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append(query);

            if (!query.endsWith("*") && !query.endsWith("?")) {
                queryBuilder.append("*");
            }
            controlsCopy.setQuery(queryBuilder.toString());
        }
        controlsCopy.setLimitSearchResults(limitResults);
        return searchService.searchArtifacts(controlsCopy);
    }
}
