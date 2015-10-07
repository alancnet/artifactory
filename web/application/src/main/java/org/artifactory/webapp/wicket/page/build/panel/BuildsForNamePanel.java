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

package org.artifactory.webapp.wicket.page.build.panel;

import com.google.common.collect.Lists;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.search.SearchService;
import org.artifactory.build.BuildRun;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.component.panel.titled.TitledPanel;
import org.artifactory.common.wicket.component.table.SortableTable;
import org.artifactory.common.wicket.component.table.columns.FormattedDateColumn;
import org.artifactory.webapp.wicket.actionable.column.ActionsColumn;
import org.artifactory.webapp.wicket.page.build.actionable.BuildActionableItem;
import org.artifactory.webapp.wicket.page.build.page.BuildBrowserRootPage;
import org.artifactory.webapp.wicket.page.build.panel.compare.BuildForNameListSorter;
import org.jfrog.build.api.Build;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.artifactory.webapp.wicket.page.build.BuildBrowserConstants.*;

/**
 * Displays all the builds of a given name
 *
 * @author Noam Y. Tenne
 */
public class BuildsForNamePanel extends TitledPanel {

    private static final Logger log = LoggerFactory.getLogger(BuildsForNamePanel.class);

    @SpringBean
    private SearchService searchService;

    @SpringBean
    private CentralConfigService centralConfigService;

    private String buildName;

    /**
     * Main constructor
     *
     * @param id           ID to assign to the panel
     * @param buildName    The name of the builds to display
     * @param buildsByName Set of builds to display
     */
    public BuildsForNamePanel(String id, String buildName, Set<BuildRun> buildsByName) {
        super(id);
        setOutputMarkupId(true);
        this.buildName = buildName;

        try {
            addTable(buildsByName);
        } catch (Exception e) {
            String errorMessage = "An error occurred while loading the builds with the name '" + buildName + "'";
            log.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }

    @Override
    public String getTitle() {
        return "History for Build '" + buildName + "'";
    }

    /**
     * Adds the build table to the panel
     *
     * @param buildsByName Builds to display
     */
    private void addTable(Set<BuildRun> buildsByName) {
        List<IColumn<BuildActionableItem>> columns = Lists.newArrayList();

        columns.add(new ActionsColumn<BuildActionableItem>(""));
        columns.add(new BuildNumberColumn());
        columns.add(new BuildDateColumn());
        columns.add(new LastReleaseStatusColumn());

        BuildsDataProvider dataProvider = new BuildsDataProvider(buildsByName);

        add(new SortableTable<>("builds", columns, dataProvider, 200));
    }

    private void drillDown(BuildActionableItem build) {
        PageParameters pageParameters = new PageParameters();
        pageParameters.set(BUILD_NAME, buildName);
        pageParameters.set(BUILD_NUMBER, build.getBuildNumber());
        pageParameters.set(BUILD_STARTED, build.getStarted());
        setResponsePage(BuildBrowserRootPage.class, pageParameters);
    }

    /**
     * The build table data provider
     */
    private static class BuildsDataProvider extends SortableDataProvider<BuildActionableItem> {

        List<BuildRun> buildList;

        /**
         * @param buildsByName Builds to display
         */
        public BuildsDataProvider(Set<BuildRun> buildsByName) {
            setSort("startedDate", SortOrder.DESCENDING);
            this.buildList = Lists.newArrayList(buildsByName);
        }

        @Override
        public Iterator<BuildActionableItem> iterator(int first, int count) {
            BuildForNameListSorter.sort(buildList, getSort());
            List<BuildActionableItem> listToReturn = getActionableItems(buildList.subList(first, first + count));
            return listToReturn.iterator();
        }

        @Override
        public int size() {
            return buildList.size();
        }

        @Override
        public IModel<BuildActionableItem> model(BuildActionableItem object) {
            return new Model<>(object);
        }

        /**
         * Returns a list of actionable items for the given builds
         *
         * @param builds Builds to display
         * @return Actionable item list
         */
        private List<BuildActionableItem> getActionableItems(List<BuildRun> builds) {
            List<BuildActionableItem> actionableItems = Lists.newArrayList();

            for (BuildRun build : builds) {
                actionableItems.add(new BuildActionableItem(build));
            }

            return actionableItems;
        }
    }

    private class BuildNumberColumn extends UnStyledLinkColumn {

        public BuildNumberColumn() {
            super(Model.of("Build Number"), "number");
        }

        @Override
        protected void addStylesToLink(Component unStyledLink) {
            unStyledLink.add(new CssClass("item-link"));
        }

        @Override
        protected String getDisplayValue(BuildActionableItem buildActionableItem) {
            return buildActionableItem.getBuildNumber();
        }
    }

    private class BuildDateColumn extends FormattedDateColumn<BuildActionableItem> {

        public BuildDateColumn() {
            super(Model.of("Time Built"), "startedDate", "started", centralConfigService, Build.STARTED_FORMAT);
        }

        @Override
        public void populateItem(final Item<ICellPopulator<BuildActionableItem>> item, String componentId,
                IModel<BuildActionableItem> rowModel) {
            super.populateItem(item, componentId, rowModel);
            item.add(new AjaxEventBehavior("onclick") {
                @Override
                protected void onEvent(AjaxRequestTarget target) {
                    final BuildActionableItem build =
                            (BuildActionableItem) item.getParent().getParent().getDefaultModelObject();
                    drillDown(build);
                }
            });
        }
    }

    private class LastReleaseStatusColumn extends UnStyledLinkColumn {

        public LastReleaseStatusColumn() {
            super(Model.of("Release Status"), "lastReleaseStatus");
        }

        @Override
        protected String getDisplayValue(BuildActionableItem buildActionableItem) {
            return buildActionableItem.getLastReleaseStatus();
        }
    }

    private abstract class UnStyledLinkColumn extends AbstractColumn<BuildActionableItem> {

        public UnStyledLinkColumn(IModel<String> displayModel, String sortProperty) {
            super(displayModel, sortProperty);
        }

        @Override
        public void populateItem(final Item cellItem, String componentId, IModel rowModel) {
            final BuildActionableItem build =
                    (BuildActionableItem) cellItem.getParent().getParent().getDefaultModelObject();
            String value = getDisplayValue(build);
            Component link = new Label(componentId, value);
            addStylesToLink(link);
            cellItem.add(link);
            cellItem.add(new AjaxEventBehavior("onclick") {
                @Override
                protected void onEvent(AjaxRequestTarget target) {
                    drillDown(build);
                }
            });
        }

        protected void addStylesToLink(Component unStyledLink) {
        }

        protected abstract String getDisplayValue(BuildActionableItem buildActionableItem);
    }
}