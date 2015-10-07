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

package org.artifactory.webapp.wicket.page.browse.treebrowser.tabs.build;

import com.google.common.collect.Lists;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.addon.AddonsManager;
import org.artifactory.api.build.BuildService;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.search.SearchService;
import org.artifactory.build.BuildRun;
import org.artifactory.common.wicket.component.border.fieldset.FieldSetBorder;
import org.artifactory.common.wicket.component.modal.ModalHandler;
import org.artifactory.common.wicket.component.table.columns.FormattedDateColumn;
import org.artifactory.common.wicket.component.table.masterdetail.MasterDetailEntry;
import org.artifactory.common.wicket.component.table.masterdetail.MasterDetailTable;
import org.artifactory.fs.FileInfo;
import org.artifactory.sapi.common.RepositoryRuntimeException;
import org.artifactory.webapp.actionable.ActionableItem;
import org.artifactory.webapp.actionable.RepoAwareActionableItem;
import org.artifactory.webapp.wicket.actionable.column.ActionsColumn;
import org.artifactory.webapp.wicket.page.browse.treebrowser.tabs.build.actionable.BuildDependencyActionableItem;
import org.artifactory.webapp.wicket.page.browse.treebrowser.tabs.build.actionable.BuildTabActionableItem;
import org.jfrog.build.api.Build;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * The repo item build association tab base panel
 *
 * @author Noam Y. Tenne
 */
public abstract class BaseBuildsTabPanel extends Panel {

    private static final Logger log = LoggerFactory.getLogger(BaseBuildsTabPanel.class);

    @SpringBean
    protected AddonsManager addonsManager;

    @SpringBean
    private CentralConfigService centralConfigService;

    @SpringBean
    protected BuildService buildService;

    @SpringBean
    protected SearchService searchService;

    protected String sha1;
    protected String md5;
    protected ModalHandler textContentViewer;

    /**
     * Main constructor
     *
     * @param id   ID to assign to the panel
     * @param item Selected repo item
     */
    public BaseBuildsTabPanel(String id, RepoAwareActionableItem item) {
        super(id);
        sha1 = ((FileInfo) item.getItemInfo()).getSha1();
        md5 = ((FileInfo) item.getItemInfo()).getMd5();
        textContentViewer = new ModalHandler("contentDialog");
        add(textContentViewer);

        try {
            addBuildTables();
        } catch (RepositoryRuntimeException rre) {
            String errorMessage = "An error occurred while loading the build associations of '" +
                    item.getRepoPath().getId() + "'";
            log.error(errorMessage, rre);
            error(errorMessage);
        }
    }

    /**
     * Adds the build tables
     */
    private void addBuildTables() {
        // add artifacts border
        FieldSetBorder artifactsBorder = new FieldSetBorder("artifactBorder");
        add(artifactsBorder);

        List<IColumn> artifactColumns = Lists.newArrayList();
        artifactColumns.add(new ItemActionsColumn());
        artifactColumns.add(new PropertyColumn(Model.of("Build Name"), "master.name", "master.name"));
        artifactColumns.add(new PropertyColumn(Model.of("Build Number"), "master.number", "master.number"));
        artifactColumns.add(new FormattedDateColumn(Model.of("Build Started"), "master.startedDate",
                "master.started", centralConfigService, Build.STARTED_FORMAT));
        artifactColumns.add(new PropertyColumn(Model.of("Module ID"), "detail.moduleId", "detail.moduleId"));

        artifactsBorder.add(new ProducedByTable("artifactBuilds", artifactColumns));

        // add dependencies border
        FieldSetBorder dependenciesBorder = new FieldSetBorder("dependencyBorder");
        add(dependenciesBorder);

        List<IColumn> dependencyColumns = Lists.newArrayList();
        dependencyColumns.add(new ItemActionsColumn());
        dependencyColumns.add(new PropertyColumn(Model.of("Build Name"), "master.name", "master.name"));
        dependencyColumns.add(new PropertyColumn(Model.of("Build Number"), "master.number", "master.number"));
        dependencyColumns.add(new PropertyColumn(Model.of("Module ID"), "detail.moduleId", "detail.moduleId"));
        dependencyColumns.add(new PropertyColumn(Model.of("Scope"), "detail.scope", "detail.scope"));

        dependenciesBorder.add(new UsedByTable("dependencyBuilds", dependencyColumns));
    }

    /**
     * Returns the list of artifact basic build info items to display
     *
     * @return Artifact basic build info list
     */
    protected abstract List<BuildRun> getArtifactBuilds();

    /**
     * Returns the list of dependency basic build info items to display
     *
     * @return Dependency basic build info item list
     */
    protected abstract List<BuildRun> getDependencyBuilds();

    /**
     * Returns the list of artifact build actionable items to display
     *
     * @param run Basic build info to create actionable items from
     * @return Artifact build actionable item list
     */
    protected abstract List<BuildTabActionableItem> getArtifactActionableItems(BuildRun run);

    /**
     * Returns the list of dependency build actionable items to display
     *
     * @param run Basic build info to create actionable items from
     * @return Dependency build actionable item list
     */
    protected abstract List<BuildDependencyActionableItem> getDependencyActionableItems(BuildRun run);

    private class ProducedByTable extends MasterDetailTable<BuildRun, BuildTabActionableItem> {
        public ProducedByTable(String id, List<IColumn> columns) {
            super(id, columns, BaseBuildsTabPanel.this.getArtifactBuilds(), "master.startedDate", 10);
        }

        @Override
        protected String getMasterLabel(BuildRun masterObject) {
            return String.format("%s, Build #%s", masterObject.getName(), masterObject.getNumber());
        }

        @Override
        protected List<BuildTabActionableItem> getDetails(BuildRun masterObject) {
            return getArtifactActionableItems(masterObject);
        }
    }

    private class UsedByTable extends MasterDetailTable<BuildRun, BuildDependencyActionableItem> {
        public UsedByTable(String id, List<IColumn> columns) {
            super(id, columns, BaseBuildsTabPanel.this.getDependencyBuilds(), "master.name", 10);

        }

        @Override
        protected String getMasterLabel(BuildRun masterObject) {
            return String.format("%s, Build #%s", masterObject.getName(), masterObject.getNumber());
        }

        @Override
        protected List<BuildDependencyActionableItem> getDetails(BuildRun masterObject) {
            return getDependencyActionableItems(masterObject);
        }
    }

    private static class ItemActionsColumn extends ActionsColumn {
        public ItemActionsColumn() {
            super("");
        }

        @SuppressWarnings({"unchecked"})
        @Override
        protected ActionableItem getRowObject(IModel rowModel) {
            return ((MasterDetailEntry<BuildRun, BuildTabActionableItem>) rowModel.getObject()).getDetail();
        }
    }
}