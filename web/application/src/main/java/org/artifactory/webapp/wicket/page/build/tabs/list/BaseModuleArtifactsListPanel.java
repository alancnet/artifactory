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
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.artifactory.common.wicket.component.modal.ModalHandler;
import org.artifactory.common.wicket.component.panel.titled.TitledPanel;
import org.artifactory.common.wicket.component.table.SortableTable;
import org.artifactory.common.wicket.util.ListPropertySorter;
import org.artifactory.webapp.actionable.event.ItemEventTargetComponents;
import org.artifactory.webapp.wicket.actionable.column.ActionsColumn;
import org.artifactory.webapp.wicket.page.build.actionable.ModuleArtifactActionableItem;

import java.util.Iterator;
import java.util.List;

/**
 * The base module artifacts list panel
 *
 * @author Noam Y. Tenne
 */
public abstract class BaseModuleArtifactsListPanel extends TitledPanel {

    /**
     * Main constructor
     *
     * @param id ID to assign to the panel
     */
    public BaseModuleArtifactsListPanel(String id) {
        super(id);
    }

    @Override
    public String getTitle() {
        return "Artifacts";
    }

    /**
     * Returns the list of artifacts to be displayed
     *
     * @return Artifact list to display
     */
    public abstract List<ModuleArtifactActionableItem> getArtifacts();

    /**
     * Adds the artifacts table
     */
    protected void addTable() {
        List<IColumn<ModuleArtifactActionableItem>> columns = Lists.newArrayList();
        columns.add(new ActionsColumn<ModuleArtifactActionableItem>(""));
        columns.add(new ModuleArtifactPropertyColumn(Model.of("Name"), "name", "artifact.name"));
        columns.add(new ModuleArtifactPropertyColumn(Model.of("Type"), "type", "artifact.type"));
        columns.add(new ModuleArtifactPropertyColumn(Model.of("Repo Path"), null, "repoPath"));
        add(new SortableTable<>(
                "artifacts", columns, new ModuleArtifactsDataProvider(), 10));
    }

    public ModuleArtifactsDataProvider getTableDataProvider() {
        return ((ModuleArtifactsDataProvider) getTable().getSortableDataProvider());
    }

    private SortableTable<ModuleArtifactActionableItem> getTable() {
        return (SortableTable<ModuleArtifactActionableItem>) get("artifacts");
    }

    /**
     * The published module's artifacts table data provider
     */
    public class ModuleArtifactsDataProvider extends SortableDataProvider<ModuleArtifactActionableItem> {

        private List<ModuleArtifactActionableItem> artifactsList;

        /**
         * Default constructor
         */
        public ModuleArtifactsDataProvider() {
            setSort("artifact.artifactName", SortOrder.ASCENDING);
            this.artifactsList = getArtifacts();
        }

        @Override
        public Iterator<ModuleArtifactActionableItem> iterator(int first, int count) {
            ListPropertySorter.sort(artifactsList, getSort());
            List<ModuleArtifactActionableItem> listToReturn = artifactsList.subList(first, first + count);
            setEventTargetComponents(listToReturn);
            return listToReturn.iterator();
        }

        private void setEventTargetComponents(List<ModuleArtifactActionableItem> items) {
            ModalWindow contentDialog = ModalHandler.getInstanceFor(getPage());
            ItemEventTargetComponents targets = new ItemEventTargetComponents(BaseModuleArtifactsListPanel.this, null,
                    contentDialog);
            for (ModuleArtifactActionableItem item : items) {
                item.setEventTargetComponents(targets);
            }
        }

        @Override
        public int size() {
            return artifactsList.size();
        }

        @Override
        public IModel<ModuleArtifactActionableItem> model(ModuleArtifactActionableItem object) {
            return new Model<>(object);
        }

        public void setArtifactsList(List<ModuleArtifactActionableItem> artifactsList) {
            this.artifactsList = artifactsList;
        }
    }
}