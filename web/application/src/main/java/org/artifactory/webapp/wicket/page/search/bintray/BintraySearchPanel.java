/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2013 JFrog Ltd.
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

package org.artifactory.webapp.wicket.page.search.bintray;

import com.google.common.collect.Lists;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.api.bintray.BintrayItemInfo;
import org.artifactory.api.bintray.BintrayService;
import org.artifactory.api.bintray.exception.BintrayException;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.search.BintrayItemSearchResults;
import org.artifactory.api.search.SearchControlsBase;
import org.artifactory.api.search.artifact.ArtifactSearchControls;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.wicket.ajax.ImmediateAjaxIndicatorDecorator;
import org.artifactory.common.wicket.behavior.defaultbutton.DefaultButtonBehavior;
import org.artifactory.common.wicket.component.form.SecureForm;
import org.artifactory.common.wicket.component.help.HelpBubble;
import org.artifactory.common.wicket.component.links.TitledAjaxSubmitLink;
import org.artifactory.common.wicket.component.modal.ModalHandler;
import org.artifactory.common.wicket.component.navigation.NavigationToolbarWithDropDown;
import org.artifactory.common.wicket.component.table.SortableTable;
import org.artifactory.common.wicket.component.table.columns.BooleanColumn;
import org.artifactory.common.wicket.component.table.columns.FormattedDateColumn;
import org.artifactory.common.wicket.util.AjaxUtils;
import org.artifactory.common.wicket.util.ComponentPersister;
import org.artifactory.common.wicket.util.ListPropertySorter;
import org.artifactory.common.wicket.util.WicketUtils;
import org.artifactory.descriptor.repo.HttpRepoDescriptor;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.util.RepoLayoutUtils;
import org.artifactory.webapp.wicket.actionable.column.ActionsColumn;
import org.artifactory.webapp.wicket.page.config.repos.CachingDescriptorHelper;
import org.artifactory.webapp.wicket.page.config.repos.remote.HttpRepoPanel;
import org.artifactory.webapp.wicket.page.search.actionable.ActionableExternalItemSearchResult;
import org.artifactory.webapp.wicket.panel.advanced.AdvancedSearchPanel;
import org.jfrog.build.api.Build;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.artifactory.common.wicket.component.CreateUpdateAction.CREATE;

/**
 * Bintray search panel
 *
 * @author Gidi Shabat
 */
public class BintraySearchPanel extends Panel {
    private static final Logger log = LoggerFactory.getLogger(BintraySearchPanel.class);
    private static final int MINIMAL_QUERY_LENGTH = 3;
    private final SearchDataProvider<ActionableExternalItemSearchResult<BintrayItemInfo>> dataProvider;
    private final ArtifactSearchControls searchControls;
    @SpringBean
    protected BintrayService bintrayService;
    @SpringBean
    protected RepositoryService repositoryService;
    @SpringBean
    private CentralConfigService centralConfigService;
    @SpringBean
    private AuthorizationService authorizationService;
    private long rangeLimitTotal;


    public BintraySearchPanel(String id) {
        super(id);
        // Initialize fields
        searchControls = new ArtifactSearchControls();
        dataProvider = new SearchDataProvider<>();
        dataProvider.setSort(new SortParam("searchResult.name", true));
        setOutputMarkupId(true);
        final WebMarkupContainer searchBorder = addSearchBorder();
        Form form = addForm(searchBorder);
        // Add table
        AddTable(searchBorder);
        // Add search button
        addSearchButton(searchBorder, form);
        // Add search control.
        addSearchControl(form);
        // Add JCenter configuration  link
        addJCenterConfigurationLink(form);
        // Add the  search in bintray link
        addSearchInBintrayLabel(form);
    }

    @Override
    public boolean isEnabled() {
        return isPanelActive();
    }

    private WebMarkupContainer addSearchBorder() {
        final WebMarkupContainer searchBorder = new WebMarkupContainer("searchBorder");
        searchBorder.setOutputMarkupId(true);
        add(searchBorder);
        return searchBorder;
    }

    private void addJCenterConfigurationLink(Form form) {
        String linkMessage = "The JCenter remote repository is currently not configured, ";
        if (authorizationService.isAdmin()) {
            linkMessage += "press the link to create it";
        } else {
            linkMessage += "please contact the administrator";
        }
        WebMarkupContainer container = new WebMarkupContainer("test") {
            @Override
            public boolean isVisible() {
                return !isJCenterExists() && isPanelActive();
            }

            @Override
            public boolean isEnabled() {
                return authorizationService.isAdmin();
            }
        };
        TitledAjaxSubmitLink configureJCenterLink = new
                TitledAjaxSubmitLink("jCenterLink",
                        linkMessage, null) {
                    @Override
                    protected void onSubmit(AjaxRequestTarget target, Form form) {
                        String url = ConstantValues.jCenterUrl.getString();
                        HttpRepoDescriptor repoDescriptor = new HttpRepoDescriptor();
                        repoDescriptor.setRepoLayout(RepoLayoutUtils.MAVEN_2_DEFAULT);
                        repoDescriptor.setUrl(url);
                        repoDescriptor.setDescription("Bintray's JCenter remote repository");
                        repoDescriptor.setKey("jcenter");
                        repoDescriptor.setHandleReleases(true);
                        repoDescriptor.setHandleSnapshots(false);
                        CachingDescriptorHelper cachingDescriptorHelper = new CachingDescriptorHelper(
                                centralConfigService.getMutableDescriptor());
                        HttpRepoPanel panel = new HttpRepoPanel(CREATE, repoDescriptor, cachingDescriptorHelper);
                        ModalHandler modalHandler = ModalHandler.getInstanceFor(this);
                        modalHandler.setModalPanel(panel);
                        modalHandler.show(target);
                    }

                };
        container.add(configureJCenterLink);
        form.add(container);
    }

    private void addSearchInBintrayLabel(Form form) {
        form.add(new HelpBubble("searchHelp", "Artifact name. * and ? are accepted."));
    }

    private boolean isJCenterExists() {
        return bintrayService.getJCenterRepo() != null;
    }

    private void addSearchControl(Form form) {
        TextField<String> searchControl = new TextField<>("query",
                new PropertyModel<String>(searchControls, "query"));
        searchControl.setOutputMarkupId(true);
        form.add(searchControl);

    }

    private void addSearchButton(final WebMarkupContainer searchBorder, Form form) {
        // Selected repo for search
        CompoundPropertyModel<SearchControlsBase> advancedModel = new CompoundPropertyModel<SearchControlsBase>(
                searchControls);
        final AdvancedSearchPanel advancedPanel = new AdvancedSearchPanel("advancedPanel", advancedModel);
        TitledAjaxSubmitLink searchButton = new TitledAjaxSubmitLink("submit", "Search", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form form) {
                try {
                    ComponentPersister.saveChildren(BintraySearchPanel.this);
                    validateSearchControls();
                    List<ActionableExternalItemSearchResult<BintrayItemInfo>> result = search();
                    dataProvider.setRows(result);
                    target.add(searchBorder);
                    advancedPanel.expandCollapseReposList();
                } catch (IllegalArgumentException iae) {
                    error(iae.getMessage());
                }
                AjaxUtils.refreshFeedback(target);
            }

            @Override
            public boolean isEnabled() {
                return isPanelActive();
            }

            @Override
            protected IAjaxCallDecorator getAjaxCallDecorator() {
                return new ImmediateAjaxIndicatorDecorator();
            }
        };
        form.add(searchButton);
        form.add(new DefaultButtonBehavior(searchButton));
    }

    private boolean isPanelActive() {
        boolean hideRemoteSearch = ConstantValues.bintrayUIHideRemoteSearch.getBoolean();
        boolean offlineMode = centralConfigService.getDescriptor().isOfflineMode();
        return !offlineMode && !hideRemoteSearch;
    }

    private Form addForm(WebMarkupContainer searchBorder) {
        Form form = new SecureForm("form");
        form.setOutputMarkupId(true);
        searchBorder.add(form);
        return form;
    }

    private void AddTable(WebMarkupContainer searchBorder) {
        List<IColumn<ActionableExternalItemSearchResult<BintrayItemInfo>>> columns = Lists.newArrayList();
        addColumns(columns);
        SortableTable<ActionableExternalItemSearchResult<BintrayItemInfo>> table = new SortableTable<ActionableExternalItemSearchResult<BintrayItemInfo>>(
                "results",
                columns, dataProvider, 15) {
            @Override
            protected NavigationToolbarWithDropDown getDropDownNavToolbar() {
                return new NavigationToolbarWithDropDown(this, 0) {
                    @Override
                    protected String getNavigatorText() {
                        int count = dataProvider.getRows().size();
                        long limit = rangeLimitTotal;
                        return String.format("Found %d matches. Showing first  %d.", limit, count);
                    }
                };
            }
        };
        searchBorder.add(table);


    }

    protected void validateSearchControls() {
        if (searchControls.isEmpty()) {
            throw new IllegalArgumentException("The search term cannot be empty");
        }
        if (searchControls.isWildcardsOnly()) {
            throw new IllegalArgumentException("Search term containing only wildcards is not permitted");
        }
    }

    @SuppressWarnings({"unchecked"})
    protected ActionableExternalItemSearchResult<BintrayItemInfo> getActionableResult(
            BintrayItemInfo searchResult, RemoteRepoDescriptor jCenterDescriptor) {
        RepoPath repoPath;
        if (jCenterDescriptor != null) {
            if (searchResult.isCached()) {
                repoPath = searchResult.getLocalRepoPath();
            } else {
                repoPath = InternalRepoPathFactory.create(jCenterDescriptor.getKey(), searchResult.getPath());
            }
        } else {
            repoPath = InternalRepoPathFactory.create("", "");
        }
        return new ActionableExternalItemSearchResult(jCenterDescriptor, searchResult, repoPath);
    }

    /**
     * Create the table columns
     *
     * @param columns
     */
    protected void addColumns(List<IColumn<ActionableExternalItemSearchResult<BintrayItemInfo>>> columns) {
        columns.add(new ActionsColumn<ActionableExternalItemSearchResult<BintrayItemInfo>>(""));
        columns.add(new PropertyColumn<ActionableExternalItemSearchResult<BintrayItemInfo>>(
                Model.of("Name"), "searchResult.name", "searchResult.name"));
        columns.add(new PropertyColumn<ActionableExternalItemSearchResult<BintrayItemInfo>>(
                Model.of("Path"), "searchResult.path", "searchResult.path"));
        columns.add(new PropertyColumn<ActionableExternalItemSearchResult<BintrayItemInfo>>(
                Model.of("Package"), "searchResult.package", "searchResult.package"));

        columns.add(new FormattedDateColumn<ActionableExternalItemSearchResult<BintrayItemInfo>>(Model.of("Released"),
                "searchResult.created", "searchResult.created"
                , centralConfigService, Build.STARTED_FORMAT));
        BooleanColumn<ActionableExternalItemSearchResult<BintrayItemInfo>> cached = new BooleanColumn<ActionableExternalItemSearchResult<BintrayItemInfo>>(
                "Stored", "searchResult.cached", "searchResult.cached") {
        };
        columns.add(cached);
    }

    /**
     * Performs the search
     *
     * @return List of search results
     */
    private List<ActionableExternalItemSearchResult<BintrayItemInfo>> search() {
        List<ActionableExternalItemSearchResult<BintrayItemInfo>> actionableSearchResults = new ArrayList<>();
        try {
            String query = searchControls.getQuery();
            if (query.length() < MINIMAL_QUERY_LENGTH) {
                throw new IllegalArgumentException("The search key must contain at least 3 letters");
            }
            Map<String, String> headersMap = WicketUtils.getHeadersMap();
            BintrayItemSearchResults<BintrayItemInfo> itemSearchResults = bintrayService.searchByName(query,
                    headersMap);
            rangeLimitTotal = itemSearchResults.getRangeLimitTotal();
            final RemoteRepoDescriptor jCenterDescriptor = bintrayService.getJCenterRepo();
            for (BintrayItemInfo result : itemSearchResults.getResults()) {
                actionableSearchResults.add(getActionableResult(result, jCenterDescriptor));
            }
            return actionableSearchResults;
        } catch (IOException e) {
            if (getFeedbackMessages().isEmpty()) {
                getPage().error("Connection failed with exception: " + e.getMessage());
            }
        } catch (BintrayException e) {
            error("Fail to retrieve query results from Bintray." + e.getMessage());
            log.error("Fail to retrieve query results from Bintray.", e);
        }
        return actionableSearchResults;
    }

    public void refresh(AjaxRequestTarget target) {
        target.add(this);
        dataProvider.getRows().clear();
    }

    private static class SearchDataProvider<T extends ActionableExternalItemSearchResult<BintrayItemInfo>>
            extends SortableDataProvider<T> {
        private List<T> rows = new ArrayList<>();

        @Override
        public Iterator<? extends T> iterator(int first, int count) {
            ListPropertySorter.sort(rows, getSort());
            List<T> listToReturn = rows.subList(first, first + count);
            return listToReturn.iterator();
        }

        @Override
        public int size() {
            return rows.size();
        }

        @Override
        public IModel<T> model(T object) {
            return new Model<>(object);
        }

        public List<T> getRows() {
            return rows;
        }

        public void setRows(List<T> rows) {
            this.rows = rows;
        }
    }
}
