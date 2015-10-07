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
import org.artifactory.common.wicket.util.ListPropertySorter;
import org.artifactory.sapi.common.RepositoryRuntimeException;
import org.artifactory.webapp.wicket.actionable.column.ActionsColumn;
import org.artifactory.webapp.wicket.page.build.BuildBrowserConstants;
import org.artifactory.webapp.wicket.page.build.actionable.LatestBuildByNameActionableItem;
import org.artifactory.webapp.wicket.page.build.page.BuildBrowserRootPage;
import org.jfrog.build.api.Build;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Locates and displays all the builds in the system
 *
 * @author Noam Y. Tenne
 */
public class AllBuildsPanel extends TitledPanel {

    private static final Logger log = LoggerFactory.getLogger(AllBuildsPanel.class);

    @SpringBean
    private SearchService searchService;

    @SpringBean
    private CentralConfigService centralConfigService;

    /**
     * Main constructor
     *
     * @param id ID to assign to the panel
     */
    public AllBuildsPanel(String id) {
        super(id);
        setOutputMarkupId(true);

        try {
            Set<BuildRun> latestBuildsByName = searchService.getLatestBuilds();
            addTable(latestBuildsByName);
        } catch (RepositoryRuntimeException e) {
            String errorMessage = "An error occurred while loading all existing builds";
            log.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }

    @Override
    public String getTitle() {
        return "All Builds";
    }

    /**
     * Adds the build table to the panel
     *
     * @param latestBuildsByName Latest builds by name to display
     */
    private void addTable(Set<BuildRun> latestBuildsByName) {
        List<IColumn<LatestBuildByNameActionableItem>> columns = Lists.newArrayList();
        columns.add(new ActionsColumn<LatestBuildByNameActionableItem>(""));
        columns.add(new BuildNameColumn());
        columns.add(new BuildDateColumn());
        BuildsDataProvider dataProvider = new BuildsDataProvider(latestBuildsByName);
        add(new SortableTable<>("builds", columns, dataProvider, 200));
    }

    /**
     * The build table data provider
     */
    private static class BuildsDataProvider extends SortableDataProvider<LatestBuildByNameActionableItem> {

        List<BuildRun> buildList;

        /**
         * Main constructor
         *
         * @param latestBuildsByName Latest build by name to display
         */
        public BuildsDataProvider(Set<BuildRun> latestBuildsByName) {
            setSort("startedDate", SortOrder.DESCENDING);
            this.buildList = Lists.newArrayList(latestBuildsByName);
        }

        @Override
        public Iterator<LatestBuildByNameActionableItem> iterator(int first, int count) {
            ListPropertySorter.sort(buildList, getSort());
            List<LatestBuildByNameActionableItem> listToReturn =
                    getActionableItems(buildList.subList(first, first + count));
            return listToReturn.iterator();
        }

        @Override
        public int size() {
            return buildList.size();
        }

        @Override
        public IModel<LatestBuildByNameActionableItem> model(LatestBuildByNameActionableItem object) {
            return new Model<>(object);
        }

        /**
         * Returns actionable items for the given list of builds
         *
         * @param builds Builds to return as actionable
         * @return Actionable builds
         */
        private List<LatestBuildByNameActionableItem> getActionableItems(List<BuildRun> builds) {
            List<LatestBuildByNameActionableItem> items = Lists.newArrayList();
            for (BuildRun build : builds) {
                items.add(new LatestBuildByNameActionableItem(build));
            }

            return items;
        }
    }

    private void drillDown(String buildName) {
        PageParameters pageParameters = new PageParameters();
        pageParameters.set(BuildBrowserConstants.BUILD_NAME, buildName);
        setResponsePage(BuildBrowserRootPage.class, pageParameters);
    }

    private class BuildNameColumn extends AbstractColumn<LatestBuildByNameActionableItem> {
        public BuildNameColumn() {
            super(Model.of("Build Name"), "name");
        }

        @Override
        public void populateItem(Item cellItem, String componentId, IModel rowModel) {
            LatestBuildByNameActionableItem info =
                    (LatestBuildByNameActionableItem) cellItem.getParent().getParent().getDefaultModelObject();
            final String buildName = info.getName();
            Component link = new Label(componentId, buildName);
            link.add(new CssClass("item-link"));
            cellItem.add(link);
            cellItem.add(new AjaxEventBehavior("onclick") {
                @Override
                protected void onEvent(AjaxRequestTarget target) {
                    drillDown(buildName);
                }
            });
        }
    }

    private class BuildDateColumn extends FormattedDateColumn<LatestBuildByNameActionableItem> {
        public BuildDateColumn() {
            super(Model.of("Last Built"), "startedDate", "started", centralConfigService, Build.STARTED_FORMAT);
        }

        @Override
        public void populateItem(final Item<ICellPopulator<LatestBuildByNameActionableItem>> item,
                String componentId, IModel<LatestBuildByNameActionableItem> rowModel) {
            super.populateItem(item, componentId, rowModel);
            item.add(new AjaxEventBehavior("onclick") {
                @Override
                protected void onEvent(AjaxRequestTarget target) {
                    LatestBuildByNameActionableItem info = (LatestBuildByNameActionableItem)
                            item.getParent().getParent().getDefaultModelObject();
                    String buildName = info.getName();
                    drillDown(buildName);
                }
            });
        }
    }
}