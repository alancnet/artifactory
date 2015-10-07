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

package org.artifactory.webapp.wicket.page.search.archive;

import org.apache.wicket.Page;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.artifactory.api.search.ItemSearchResults;
import org.artifactory.api.search.archive.ArchiveSearchControls;
import org.artifactory.api.search.archive.ArchiveSearchResult;
import org.artifactory.common.wicket.WicketProperty;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.component.checkbox.styled.StyledCheckbox;
import org.artifactory.common.wicket.component.help.HelpBubble;
import org.artifactory.common.wicket.component.table.columns.TitlePropertyColumn;
import org.artifactory.common.wicket.component.table.groupable.column.GroupableColumn;
import org.artifactory.webapp.wicket.actionable.column.ActionsColumn;
import org.artifactory.webapp.wicket.page.browse.treebrowser.BrowseRepoPage;
import org.artifactory.webapp.wicket.page.search.BaseSearchPage;
import org.artifactory.webapp.wicket.page.search.BaseSearchPanel;
import org.artifactory.webapp.wicket.page.search.actionable.ActionableArchiveSearchResult;
import org.artifactory.webapp.wicket.page.search.actionable.ActionableSearchResult;

import java.util.List;

import static org.artifactory.common.wicket.util.ComponentPersister.setPersistent;

/**
 * Displays the archive content searcher
 *
 * @author Noam Tenne
 */
public class ArchiveSearchPanel extends BaseSearchPanel<ArchiveSearchResult> {

    private ArchiveSearchControls searchControls;

    @WicketProperty
    private boolean excludeInnerClasses;

    @WicketProperty
    private boolean searchClassResourcesOnly;

    public ArchiveSearchPanel(Page parent, String id) {
        super(parent, id);
        searchClassResourcesOnly = true; //defaults to true
    }

    @Override
    protected void validateSearchControls() {
        if (searchControls.isEmpty()) {
            throw new IllegalArgumentException("The search term cannot be empty.");
        }
        if (searchControls.isWildcardsOnly()) {
            throw new IllegalArgumentException("Search term containing only wildcards is not permitted");
        }
    }

    @Override
    protected void addSearchComponents(Form form) {
        searchControls = new ArchiveSearchControls();
        searchControls.setPath("*"); //default to any path
        getDataProvider().setGroupParam(new SortParam("searchResult.entryName", true));

        TextField pathText = new TextField("path", new PropertyModel<String>(searchControls, "path"));
        pathText.setOutputMarkupId(true);
        setPersistent(pathText);
        form.add(pathText);
        form.add(new HelpBubble("path.help", new ResourceModel("path.help")));

        TextField nameText = new TextField("name", new PropertyModel<String>(searchControls, "name"));
        nameText.setOutputMarkupId(true);
        setPersistent(nameText);
        form.add(nameText);
        form.add(new HelpBubble("name.help", new ResourceModel("name.help")));

        form.add(new StyledCheckbox("searchClassResourcesOnly",
                new PropertyModel<Boolean>(this, "searchClassResourcesOnly")));
        form.add(new HelpBubble("searchClassResourcesOnly.help", new ResourceModel("searchClassResourcesOnly.help")));

        form.add(new StyledCheckbox("excludeInnerClasses",
                new PropertyModel<Boolean>(this, "excludeInnerClasses")));
        form.add(new HelpBubble("excludeInnerClassesHelp", "Mark to exclude inner classes from the list of results."));

        //Group entry names which are similar but have different character cases
        getDataProvider().setGroupRenderer("searchResult.entry",
                new ChoiceRenderer<ActionableSearchResult<ArchiveSearchResult>>("searchResult.entryName",
                        "searchResult.lowerCaseEntryName"));
    }

    @Override
    protected ArchiveSearchControls getSearchControls() {
        return searchControls;
    }

    @Override
    protected Class<? extends BaseSearchPage> getMenuPageClass() {
        return ArchiveSearchPage.class;
    }

    @Override
    protected void onNoResults() {
        warnNoArtifacts(searchControls.getQueryDisplay());
    }

    @Override
    protected ActionableSearchResult<ArchiveSearchResult> getActionableResult(
            ArchiveSearchResult searchResult) {
        return new ActionableArchiveSearchResult(searchResult);
    }

    @Override
    protected boolean isLimitSearchResults() {
        return searchControls.isLimitSearchResults();
    }

    @Override
    public String getSearchExpression() {
        return searchControls.getQueryDisplay();
    }

    @Override
    protected void addColumns(List<IColumn<ActionableSearchResult<ArchiveSearchResult>>> columns) {
        columns.add(new ActionsColumn<ActionableSearchResult<ArchiveSearchResult>>(""));
        columns.add(new GroupableColumn<ActionableSearchResult<ArchiveSearchResult>>(
                "Entry Name", "searchResult.entryName", "searchResult.entryPath"));
        columns.add(new BaseSearchPanel.ArtifactNameColumn());
        columns.add(new ArtifactPathColumn());
        //columns.add(new TitlePropertyColumn<ActionableSearchResult<ArchiveSearchResult>>(
        //        "Artifact Path", "searchResult.relDirPath", "searchResult.relDirPath"));
        columns.add(new TitlePropertyColumn<ActionableSearchResult<ArchiveSearchResult>>(
                "Repository", "searchResult.repoKey", "searchResult.repoKey"));
    }

    @Override
    protected ItemSearchResults<ArchiveSearchResult> searchArtifacts() {
        return search(false);
    }

    @Override
    protected ItemSearchResults<ArchiveSearchResult> performLimitlessArtifactSearch() {
        return search(true);
    }

    /**
     * Performs the search
     *
     * @param limitlessSearch True if should perform a limitless search
     * @return List of search results
     */
    private ItemSearchResults<ArchiveSearchResult> search(boolean limitlessSearch) {
        ArchiveSearchControls controlsCopy = new ArchiveSearchControls(searchControls);
        if (limitlessSearch) {
            controlsCopy.setLimitSearchResults(false);
            controlsCopy.setShouldCalcEntries(false);
        }
        controlsCopy.setSearchClassResourcesOnly(searchClassResourcesOnly);
        controlsCopy.setExcludeInnerClasses(excludeInnerClasses);
        return searchService.searchArchiveContent(controlsCopy);
    }

    protected static class ArtifactPathColumn extends GroupableColumn {
        public ArtifactPathColumn() {
            super("Artifact Path", "searchResult.relDirPath", "searchResult.relDirPath");
        }

        @SuppressWarnings({"unchecked"})
        @Override
        public void populateItem(final Item cellItem, String componentId, IModel model) {
            final ActionableArchiveSearchResult result =
                    (ActionableArchiveSearchResult) cellItem.getParent().getParent().getDefaultModelObject();
            final String relDirPath = result.getSearchResult().getRelDirPath();
            Link linkToTreeView = new Link<String>(componentId, Model.of(relDirPath)) {
                @Override
                public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
                    replaceComponentTagBody(markupStream, openTag, relDirPath);
                }

                @Override
                public void onClick() {
                    RequestCycle.get().setResponsePage(new BrowseRepoPage(result.getArchiveRepoPath()));
                }
            };
            linkToTreeView.add(new CssClass("item-link"));
            cellItem.add(linkToTreeView);
        }

        @Override
        public String getGroupProperty() {
            return "baseName";
        }
    }


}