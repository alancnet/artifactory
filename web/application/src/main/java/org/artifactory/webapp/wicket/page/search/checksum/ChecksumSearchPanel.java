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

package org.artifactory.webapp.wicket.page.search.checksum;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.artifactory.api.search.ItemSearchResults;
import org.artifactory.api.search.artifact.ArtifactSearchResult;
import org.artifactory.api.search.artifact.ChecksumSearchControls;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.common.wicket.WicketProperty;
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
 * Displays the checksum searcher
 *
 * @author Shay Yaakov
 */
public class ChecksumSearchPanel extends BaseSearchPanel<ArtifactSearchResult> {

    private ChecksumSearchControls searchControls;

    @WicketProperty
    private String query;

    public ChecksumSearchPanel(Page parent, String id) {
        super(parent, id);
    }

    @Override
    protected void addSearchComponents(Form form) {
        searchControls = new ChecksumSearchControls();

        TextField searchControl = new TextField<>("query", new PropertyModel<String>(this, "query"));
        searchControl.setOutputMarkupId(true);
        form.add(searchControl);
        form.add(new HelpBubble("searchHelp", "Artifact SHA-1 or MD5 checksum."));
    }

    @Override
    protected ChecksumSearchControls getSearchControls() {
        return searchControls;
    }

    @Override
    protected void validateSearchControls() {
        // RTFACT-6211 - clearing old search query before running new query
        searchControls.clearChecksums();
        if (StringUtils.isNotBlank(query)) {
            if (StringUtils.length(query) == ChecksumType.md5.length()) {
                searchControls.addChecksum(ChecksumType.md5, query);
            } else if (StringUtils.length(query) == ChecksumType.sha1.length()) {
                searchControls.addChecksum(ChecksumType.sha1, query);
            }
        }
        if (searchControls.isEmpty()) {
            throw new IllegalArgumentException("Please enter a valid checksum to search for");
        }
        if (searchControls.isWildcardsOnly()) {
            throw new IllegalArgumentException("Search term containing only wildcards is not permitted");
        }
    }

    @Override
    protected Class<? extends BaseSearchPage> getMenuPageClass() {
        return ChecksumSearchPage.class;
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
    public String getSearchExpression() {
        return query != null ? query.trim() : "";
    }

    @Override
    protected ItemSearchResults<ArtifactSearchResult> searchArtifacts() {
        return search(true);
    }

    @Override
    protected ItemSearchResults<ArtifactSearchResult> performLimitlessArtifactSearch() {
        return search(false);
    }

    @Override
    protected void onNoResults() {
        Session.get().warn(String.format("No matches found for '%s'.",
                StringEscapeUtils.escapeHtml(query)));
    }

    @Override
    protected ActionableSearchResult<ArtifactSearchResult> getActionableResult(ArtifactSearchResult searchResult) {
        return new ActionableArtifactSearchResult<>(searchResult);
    }

    @Override
    protected boolean isLimitSearchResults() {
        return searchControls.isLimitSearchResults();
    }

    /**
     * Performs the search
     *
     * @param limitResults True if the search results should be limited
     * @return List of search results
     */
    private ItemSearchResults<ArtifactSearchResult> search(boolean limitResults) {
        searchControls.setLimitSearchResults(limitResults);
        return searchService.getArtifactsByChecksumResults(searchControls);
    }
}