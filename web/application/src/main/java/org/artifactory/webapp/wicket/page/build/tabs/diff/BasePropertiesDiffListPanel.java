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

package org.artifactory.webapp.wicket.page.build.tabs.diff;

import com.google.common.collect.Lists;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.artifactory.common.wicket.WicketProperty;
import org.artifactory.common.wicket.component.panel.titled.TitledPanel;
import org.artifactory.common.wicket.component.table.SortableTable;
import org.artifactory.common.wicket.component.table.groupable.GroupableTable;
import org.artifactory.common.wicket.component.table.groupable.provider.GroupableDataProvider;
import org.artifactory.webapp.wicket.page.build.actionable.BuildsDiffActionableItem;
import org.jfrog.build.api.Build;

import java.util.List;

/**
 * @author Shay Yaakov
 */
public abstract class BasePropertiesDiffListPanel extends TitledPanel {

    protected Build build;

    @WicketProperty
    private String secondItemName = "Key:Value";

    public BasePropertiesDiffListPanel(String id, Build build) {
        super(id, new Model<Build>());
        this.build = build;
        setOutputMarkupId(true);
    }

    @Override
    public String getTitle() {
        return "Environment Variables";
    }

    protected abstract List<BuildsDiffActionableItem> getProperties(Build selectedBuild);

    protected void addTable() {
        List<IColumn<BuildsDiffActionableItem>> columns = Lists.newArrayList();
        columns.add(
                new BuildDiffPropertyColumn(Model.of("Key:Value (Current Build)"), "model.compoundKeyValue",
                        "model.compoundKeyValue"));
        columns.add(new BuildDiffPropertyColumn(new PropertyModel<String>(this, "secondItemName"),
                "model.compoundDiffKeyValue", "model.compoundDiffKeyValue"));
        columns.add(new BuildDiffGroupableColumn(Model.of("Status"), "model.status", "model.status"));
        add(new GroupableTable<>("envDiff", columns, new PropertiesDiffDataProvider(), 10));
    }

    public PropertiesDiffDataProvider getTableDataProvider() {
        return ((PropertiesDiffDataProvider) getTable().getSortableDataProvider());
    }

    private SortableTable<BuildsDiffActionableItem> getTable() {
        return (SortableTable<BuildsDiffActionableItem>) get("envDiff");
    }

    public class PropertiesDiffDataProvider extends GroupableDataProvider<BuildsDiffActionableItem> {

        public PropertiesDiffDataProvider() {
            super(getProperties(null));
            setSort("model.status", SortOrder.ASCENDING);
        }
    }

    public void setSecondItemName(String secondItemName) {
        this.secondItemName = "Key:Value (Build #" + secondItemName + ")";
    }
}
