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

package org.artifactory.webapp.wicket.page.search;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.wicket.SearchAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.search.ItemSearchResult;
import org.artifactory.api.search.ItemSearchResults;
import org.artifactory.api.search.SearchControlsBase;
import org.artifactory.api.search.SearchService;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.wicket.ajax.ImmediateAjaxIndicatorDecorator;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.behavior.defaultbutton.DefaultButtonBehavior;
import org.artifactory.common.wicket.component.form.SecureForm;
import org.artifactory.common.wicket.component.links.TitledAjaxSubmitLink;
import org.artifactory.common.wicket.component.modal.ModalHandler;
import org.artifactory.common.wicket.component.navigation.NavigationToolbarWithDropDown;
import org.artifactory.common.wicket.component.panel.feedback.UnescapedFeedbackMessage;
import org.artifactory.common.wicket.component.table.groupable.GroupableTable;
import org.artifactory.common.wicket.component.table.groupable.column.GroupableColumn;
import org.artifactory.common.wicket.component.table.groupable.provider.GroupableDataProvider;
import org.artifactory.common.wicket.util.AjaxUtils;
import org.artifactory.common.wicket.util.ComponentPersister;
import org.artifactory.webapp.actionable.event.ItemEventTargetComponents;
import org.artifactory.webapp.servlet.RequestUtils;
import org.artifactory.webapp.wicket.page.search.actionable.ActionableSearchResult;
import org.artifactory.webapp.wicket.panel.advanced.AdvancedSearchPanel;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

/**
 * Basic search panel for the different search types to extend
 *
 * @author Noam Tenne
 */
public abstract class BaseSearchPanel<T extends ItemSearchResult> extends Panel implements LimitlessCapableSearcher<T> {

    @SpringBean
    protected SearchService searchService;

    @SpringBean
    protected RepositoryService repoService;

    @SpringBean
    protected CentralConfigService centralConfig;

    @SpringBean
    private AddonsManager addons;

    private ItemSearchResults<T> searchResults;
    private GroupableDataProvider<ActionableSearchResult<T>> dataProvider;
    private WebMarkupContainer searchBorder;

    private static final String RESULT_NAME_PROPERTY = "searchResult.name";

    public BaseSearchPanel(final Page parent, String id) {
        super(id);
        AjaxUtils.refreshFeedback();
        searchBorder = new WebMarkupContainer("searchBorder");
        searchBorder.setOutputMarkupId(true);
        add(searchBorder);

        // Results table
        dataProvider = new SearchDataProvider();

        dataProvider.setSort(new SortParam(RESULT_NAME_PROPERTY, true));

        List<IColumn<ActionableSearchResult<T>>> columns = Lists.newArrayList();
        addColumns(columns);

        final GroupableTable table = new GroupableTable<ActionableSearchResult<T>>("results", columns, dataProvider,
                ConstantValues.uiSearchMaxRowsPerPage.getInt()) {
            public String getSearchExpression() {
                return BaseSearchPanel.this.getSearchExpression();
            }

            public String getSearchCount() {
                int maxResults = ConstantValues.searchMaxResults.getInt();
                long fullResultsCount = searchResults.getFullResultsCount();
                int queryLimit = ConstantValues.searchUserQueryLimit.getInt();

                String searchExpression = BaseSearchPanel.this.getSearchExpression();

                StringBuilder msg = new StringBuilder();
                //Return this only if we limit the search results and don't return the full number of results found
                int rowCount = getRowCount();
                if (isLimitSearchResults() && fullResultsCount > maxResults) {
                    msg.append(rowCount).append(" out of ").
                            append(fullResultsCount == queryLimit ? "more than " : "").append(fullResultsCount).
                            append(" matches found for '").append(searchExpression).append("'");
                } else if (isLimitSearchResults() && fullResultsCount == -1 && rowCount >= maxResults) {
                    msg.append("Showing first ").append(rowCount).append(" matches found for '").
                            append(searchExpression).append("'");
                } else if (searchExpression == null) {
                    msg.append(rowCount).append(" matches found");
                } else {
                    msg.append(rowCount).append(" matches found for '").append(searchExpression).append("'");
                }
                String timeStr = NumberFormat.getNumberInstance().format(searchResults.getTime());
                msg.append(" (").append(timeStr).append(" ms)");
                return msg.toString();
            }

            @Override
            protected NavigationToolbarWithDropDown getDropDownNavToolbar() {
                return new NavigationToolbarWithDropDown(this, 0);
            }
        };
        searchBorder.add(table);

        //Form
        Form form = new SecureForm("form");
        form.setOutputMarkupId(true);
        searchBorder.add(form);

        addSearchComponents(form);

        //selected repo for search
        CompoundPropertyModel advancedModel = new CompoundPropertyModel<>(getSearchControls());
        final AdvancedSearchPanel advancedPanel = new AdvancedSearchPanel("advancedPanel", advancedModel);
        form.add(advancedPanel);

        SearchAddon searchAddon = addons.addonByType(SearchAddon.class);
        SaveSearchResultsPanel saveSearchResultsPanel = searchAddon.getSaveSearchResultsPanel("saveResultsPanel",
                new PropertyModel(this, "searchResults.results"), this);
        saveSearchResultsPanel.init();
        searchBorder.add(saveSearchResultsPanel);

        TitledAjaxSubmitLink searchButton = new TitledAjaxSubmitLink("submit", "Search", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form form) {
                try {
                    ComponentPersister.saveChildren(BaseSearchPanel.this);
                    validateSearchControls();
                    onSearch();
                    fetchResults(parent);
                    table.setCurrentPage(0);    // scroll back to the first page
                    target.add(searchBorder);
                    advancedPanel.expandCollapseReposList();
                } catch (IllegalArgumentException iae) {
                    error(iae.getMessage());
                }
                AjaxUtils.refreshFeedback(target);
            }

            @Override
            protected IAjaxCallDecorator getAjaxCallDecorator() {
                return new ImmediateAjaxIndicatorDecorator();
            }
        };
        addSearchButton(form, searchButton);

        form.add(new DefaultButtonBehavior(searchButton));
    }

    protected void validateSearchControls() {
    }

    @Override
    protected void onBeforeRender() {
        ComponentPersister.loadChildren(this);
        super.onBeforeRender();
    }

    protected void onSearch() {
    }

    protected void addSearchButton(Form form, TitledAjaxSubmitLink searchButton) {
        form.add(searchButton);
    }

    protected void warnNoArtifacts(String query) {
        String message;
        if (StringUtils.isEmpty(query)) {
            message = "No artifacts found.";
        } else {
            message = String.format("No artifacts found for '%s'.", escapeHtml(query));
        }

        if (query != null && !query.contains("*") && !query.contains("?")) {
            message += " You can broaden your search by using the * and ? wildcards.";
        }

        Session.get().warn(message);
    }

    protected abstract void addSearchComponents(Form form);

    protected abstract SearchControlsBase getSearchControls();

    protected abstract Class<? extends BaseSearchPage> getMenuPageClass();

    protected abstract void addColumns(List<IColumn<ActionableSearchResult<T>>> columns);

    public abstract String getSearchExpression();

    /**
     * Performs the search
     *
     * @return List of results limited by the system spec
     */
    protected abstract ItemSearchResults<T> searchArtifacts();

    /**
     * Performs a limitless result search
     *
     * @return List of results unlimited by the system spec
     */
    protected abstract ItemSearchResults<T> performLimitlessArtifactSearch();

    protected abstract void onNoResults();

    protected abstract ActionableSearchResult<T> getActionableResult(T searchResult);

    /**
     * Indicates if the search results should be limited as in the system spec
     *
     * @return True if the search results should be limited
     */
    protected abstract boolean isLimitSearchResults();

    @SuppressWarnings({"unchecked"})
    protected void fetchResults(Page parent) {
        List<T> searchResultList;
        try {
            searchResults = searchArtifacts();
            searchResultList = searchResults.getResults();
        } catch (Exception e) {
            dataProvider.setData(Collections.<ActionableSearchResult<T>>emptyList());
            Session.get().error("There was an error while searching: " + e.getMessage());
            getSaveSearchResultsPanel().updateState();
            return;
        }

        if (searchResultList.isEmpty()) {
            onNoResults();
        }

        getSaveSearchResultsPanel().updateState();

        int maxResults = ConstantValues.searchMaxResults.getInt();

        //Display this only if we limit the search results and don't return the full number of results found
        if (isLimitSearchResults() && searchResultList.size() > maxResults) {
            long fullResultsCount = searchResults.getFullResultsCount();
            int queryLimit = ConstantValues.searchUserQueryLimit.getInt();
            StringBuilder resultsText = new StringBuilder("Showing first ");
            resultsText.append(maxResults);
            if (fullResultsCount == queryLimit) {
                resultsText.append(" out of more than ").append(fullResultsCount);
            } else if (fullResultsCount != -1) {
                resultsText.append(" out of ").append(fullResultsCount);
            }
            String limitDisclaimer = addons.addonByType(SearchAddon.class).getSearchLimitDisclaimer();
            resultsText.append(" results. Please consider refining your search.").append(limitDisclaimer);
            Session.get().warn(new UnescapedFeedbackMessage(resultsText.toString()));
            searchResultList = searchResultList.subList(0, maxResults);
        }

        List<ActionableSearchResult<T>> actionableSearchResults = new ArrayList<>();
        for (T result : searchResultList) {
            ActionableSearchResult<T> searchResult = getActionableResult(result);
            ModalWindow contentDialog = ModalHandler.getInstanceFor(parent);
            ItemEventTargetComponents targets =
                    new ItemEventTargetComponents(this, null, contentDialog);
            searchResult.setEventTargetComponents(targets);
            actionableSearchResults.add(searchResult);
        }

        //Reset sorting (will default to results order by name)
        dataProvider.setData(actionableSearchResults);
    }

    public SaveSearchResultsPanel getSaveSearchResultsPanel() {
        return (SaveSearchResultsPanel) searchBorder.get("saveResultsPanel");
    }

    public GroupableDataProvider<ActionableSearchResult<T>> getDataProvider() {
        return dataProvider;
    }

    @Override
    public List<T> searchLimitlessArtifacts() {
        ItemSearchResults<T> results = performLimitlessArtifactSearch();
        return results.getResults();
    }

    protected static class ArtifactNameColumn extends GroupableColumn {
        public ArtifactNameColumn() {
            this("Artifact");
        }

        public ArtifactNameColumn(String columnName) {
            super(columnName, RESULT_NAME_PROPERTY, RESULT_NAME_PROPERTY);
        }

        @SuppressWarnings({"unchecked"})
        @Override
        public void populateItem(Item cellItem, String componentId, IModel model) {
            final String hrefPrefix = RequestUtils.getWicketServletContextUrl();
            ActionableSearchResult<ItemSearchResult> result =
                    (ActionableSearchResult<ItemSearchResult>) cellItem.getParent().getParent().getDefaultModelObject();
            String href = hrefPrefix + "/" + result.getSearchResult().getItemInfo().getRepoKey() + "/" +
                    result.getSearchResult().getItemInfo().getRelPath();
            String name = result.getSearchResult().getName();
            ExternalLink link = new ExternalLink(componentId, href, name);
            link.add(new CssClass("item-link"));
            cellItem.add(link);
        }

        @Override
        public String getGroupProperty() {
            return "baseName";
        }
    }

    private class SearchDataProvider extends GroupableDataProvider<ActionableSearchResult<T>> {

        public SearchDataProvider() {
            super(Collections.<ActionableSearchResult<T>>emptyList());
        }
    }
}