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

package org.artifactory.webapp.wicket.page.build.tabs.list;

import com.google.common.collect.Lists;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.component.panel.titled.TitledPanel;
import org.artifactory.common.wicket.component.table.SortableTable;
import org.artifactory.common.wicket.component.table.groupable.GroupableTable;
import org.artifactory.common.wicket.component.table.groupable.provider.GroupableDataProvider;
import org.artifactory.common.wicket.util.ListPropertySorter;
import org.artifactory.webapp.wicket.actionable.column.ActionsColumn;
import org.artifactory.webapp.wicket.page.build.actionable.ModuleDependencyActionableItem;

import java.util.Iterator;
import java.util.List;

/**
 * The base modules dependencies list panel
 *
 * @author Noam Y. Tenne
 */
public abstract class BaseModuleDependenciesListPanel extends TitledPanel {

    /**
     * Main constructor
     *
     * @param id ID to assign to the panel
     */
    public BaseModuleDependenciesListPanel(String id) {
        super(id);
        add(new CssClass("dependencies-panel"));
    }

    @Override
    public String getTitle() {
        return "Dependencies";
    }

    /**
     * Returns a list of unpopulated dependency actionable items
     *
     * @return Unpopulated actionable items
     */
    public abstract List<ModuleDependencyActionableItem> getDependencies();

    /**
     * Populates dependency actionable items with their corresponding repo paths (if exist)
     *
     * @param dependencies Unpopulated actionable items
     * @return Dependency actionable item list
     */
    protected abstract List<ModuleDependencyActionableItem> populateModuleDependencyActionableItem(
            List<ModuleDependencyActionableItem> dependencies);

    /**
     * Adds the dependencies table
     */
    protected void addTable() {
        List<IColumn<ModuleDependencyActionableItem>> columns = Lists.newArrayList();
        columns.add(new ActionsColumn<ModuleDependencyActionableItem>(""));
        columns.add(new ModuleDependencyPropertyColumn(Model.of("ID"), "dependency.id", "dependency.id"));
        columns.add(new ModuleDependencyGroupableColumn(Model.of("Scopes"), "dependencyScope", "dependencyScope"));
        columns.add(new ModuleDependencyPropertyColumn(Model.of("Type"), "dependency.type", "dependency.type"));
        columns.add(new ModuleDependencyPropertyColumn(Model.of("Repo Path"), null, "repoPathOrMissingMessage"));

        add(new GroupableTable<>(
                "dependencies", columns, new ModuleDependenciesDataProvider(), 10));
    }

    public ModuleDependenciesDataProvider getTableDataProvider() {
        return ((ModuleDependenciesDataProvider) getTable().getSortableDataProvider());
    }

    private SortableTable<ModuleDependencyActionableItem> getTable() {
        return (SortableTable<ModuleDependencyActionableItem>) get("dependencies");
    }

    /**
     * The published module's dependencies table data provider
     */
    public class ModuleDependenciesDataProvider extends GroupableDataProvider<ModuleDependencyActionableItem> {

        public ModuleDependenciesDataProvider() {
            super(getDependencies());
            setSort("dependencyScope", SortOrder.ASCENDING);
            setGroupParam(new SortParam("dependencyScope", true));
            setGroupRenderer("dependencyScope", new ChoiceRenderer<ModuleDependencyActionableItem>(
                    "dependencyScope", "dependencyScope"));
        }

        @Override
        public Iterator<ModuleDependencyActionableItem> iterator(int first, int count) {
            List<ModuleDependencyActionableItem> data = getData();
            ListPropertySorter.sort(data, getGroupParam(), getSort());
            List<ModuleDependencyActionableItem> listToReturn =
                    populateModuleDependencyActionableItem(data.subList(first, first + count));
            return listToReturn.iterator();
        }

        @Override
        public IModel<ModuleDependencyActionableItem> model(ModuleDependencyActionableItem item) {
            item = new ModuleDependencyActionableItem(item.getRepoPath(), item.getDependency(), item.getStatus()) {
                public Object getRepoPathOrMissingMessage() {
                    if (super.getRepoPath() == null) {
                        return "No path found (externally resolved or deleted/overwritten)";
                    } else {
                        return super.getRepoPath();
                    }
                }
            };
            return new Model<>(item);
        }
    }
}