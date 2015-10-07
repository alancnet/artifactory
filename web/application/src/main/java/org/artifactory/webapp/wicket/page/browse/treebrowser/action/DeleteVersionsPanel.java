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

package org.artifactory.webapp.wicket.page.browse.treebrowser.action;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.wicket.BuildAddon;
import org.artifactory.api.module.ModuleInfo;
import org.artifactory.api.module.VersionUnit;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.common.wicket.component.confirm.AjaxConfirm;
import org.artifactory.common.wicket.component.confirm.ConfirmDialog;
import org.artifactory.common.wicket.component.form.SecureForm;
import org.artifactory.common.wicket.component.links.TitledAjaxSubmitLink;
import org.artifactory.common.wicket.component.modal.ModalHandler;
import org.artifactory.common.wicket.component.modal.links.ModalCloseLink;
import org.artifactory.common.wicket.component.table.SortableTable;
import org.artifactory.common.wicket.component.table.columns.checkbox.SelectAllCheckboxColumn;
import org.artifactory.common.wicket.util.AjaxUtils;
import org.artifactory.common.wicket.util.ListPropertySorter;
import org.artifactory.mime.MavenNaming;
import org.artifactory.repo.RepoPath;
import org.artifactory.webapp.actionable.RepoAwareActionableItem;
import org.artifactory.webapp.actionable.model.FolderActionableItem;
import org.artifactory.webapp.wicket.actionable.tree.ActionableItemTreeNode;
import org.artifactory.webapp.wicket.actionable.tree.ActionableItemsTree;
import org.artifactory.webapp.wicket.page.browse.treebrowser.TreeBrowsePanel;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * This panel allows the user to select a group of deployable artifacts to delete.
 *
 * @author Yossi Shaul
 */
public class DeleteVersionsPanel extends Panel {
    private VersionUnitsDataProvider dataProvider;

    @SpringBean
    private AddonsManager addonsManager;

    @SpringBean
    private RepositoryService repoService;

    public DeleteVersionsPanel(String id, List<VersionUnit> versionUnits, TreeBrowsePanel browseRepoPanel,
            RepoAwareActionableItem source) {
        super(id);

        Form form = new SecureForm("form");
        add(form);

        Multimap<String, VersionUnit> vuGroupAndVersion = aggregateByGroupAndVersion(versionUnits);

        dataProvider = new VersionUnitsDataProvider(vuGroupAndVersion);

        List<IColumn<VersionUnitModel>> columns = Lists.newArrayList();
        columns.add(new SelectAllCheckboxColumn<VersionUnitModel>("", "selected", null));
        columns.add(new PropertyColumn<VersionUnitModel>(Model.of("Group Id"), "groupId", "groupId"));
        columns.add(new PropertyColumn<VersionUnitModel>(Model.of("Version"), "version", "version"));
        columns.add(new PropertyColumn<VersionUnitModel>(Model.of("Directories Count"), "count"));

        SortableTable table = new SortableTable<>("deployableUnits", columns, dataProvider, 20);
        form.add(table);

        form.add(new ModalCloseLink("cancel"));
        form.add(createSubmitButton(form, browseRepoPanel, source));
    }

    private Multimap<String, VersionUnit> aggregateByGroupAndVersion(List<VersionUnit> units) {
        Multimap<String, VersionUnit> multiMap = HashMultimap.create();
        for (VersionUnit unit : units) {
            ModuleInfo moduleInfo = unit.getModuleInfo();
            String unitKey = toGroupVersionKey(moduleInfo);
            multiMap.put(unitKey, unit);
        }
        return multiMap;
    }

    private String toGroupVersionKey(ModuleInfo info) {
        StringBuilder groupVersionKeyBuilder =
                new StringBuilder(info.getOrganization()).append(":").append(info.getBaseRevision());
        if (info.isIntegration()) {

            groupVersionKeyBuilder.append("-");
            if (MavenNaming.SNAPSHOT.equals(info.getFolderIntegrationRevision())) {
                groupVersionKeyBuilder.append(MavenNaming.SNAPSHOT);
            } else {
                groupVersionKeyBuilder.append("INTEGRATION");
            }
        }
        return groupVersionKeyBuilder.toString();
    }

    private String groupFromGroupVersionKey(String groupVersionKey) {
        return groupVersionKey.split(":")[0];
    }

    private String versionFromGroupVersionKey(String groupVersionKey) {
        return groupVersionKey.split(":")[1];
    }

    private TitledAjaxSubmitLink createSubmitButton(Form form, final TreeBrowsePanel browseRepoPanel, final
    RepoAwareActionableItem source) {
        TitledAjaxSubmitLink submit = new TitledAjaxSubmitLink("submit", "Delete Selected", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form form) {
                final Set<VersionUnit> selectedVersionUnits = dataProvider.getSelectedVersionUnits();
                if (selectedVersionUnits.isEmpty()) {
                    error("No version selected for deletion");
                    return; // keep popup open
                }
                AjaxConfirm.get().confirm(new ConfirmDialog() {
                    @Override
                    public String getMessage() {
                        BuildAddon buildAddon = addonsManager.addonByType(BuildAddon.class);
                        return buildAddon.getDeleteVersionsWarningMessage(getVersionUnitParents(selectedVersionUnits),
                                "Are you sure you wish to delete the selected versions?");
                    }

                    @Override
                    public void onConfirm(boolean approved, AjaxRequestTarget target) {
                        if (approved) {
                            repoService.undeployVersionUnits(selectedVersionUnits);

                            getPage().info("Selected versions deleted successfully");
                            AjaxUtils.refreshFeedback(target);
                            browseRepoPanel.removeNodePanel(target);
                            ActionableItemsTree tree = browseRepoPanel.getTree();

                            ActionableItemTreeNode itemNode = tree.searchForNodeByItem(source);
                            RepoPath path = source.getRepoPath();
                            if (source instanceof FolderActionableItem) {
                                path = ((FolderActionableItem) source).getCanonicalPath();
                            }
                            ActionableItemTreeNode parent = itemNode.getParent();
                            if (repoService.exists(path)) {
                                tree.getTreeState().collapseNode(itemNode);
                            } else {
                                itemNode.removeFromParent();
                                tree.getTreeState().collapseNode(parent);
                            }

                            target.add(tree);
                            tree.adjustLayout(target);
                            ModalHandler.closeCurrent(target);
                        }
                    }
                });
            }
        };
        return submit;
    }

    private List<RepoPath> getVersionUnitParents(Set<VersionUnit> versionUnits) {
        Set<RepoPath> parents = Sets.newHashSet();

        for (VersionUnit versionUnit : versionUnits) {
            parents.addAll(versionUnit.getParents());
        }

        return Lists.newArrayList(parents);
    }

    private class VersionUnitsDataProvider extends SortableDataProvider<VersionUnitModel> {
        private Multimap<String, VersionUnit> vuGroupAndVersion;
        protected List<VersionUnitModel> vuModels;

        private VersionUnitsDataProvider(Multimap<String, VersionUnit> vuGroupAndVersion) {
            this.vuGroupAndVersion = vuGroupAndVersion;
            Set<String> groupVersionKeys = vuGroupAndVersion.keySet();
            vuModels = Lists.newArrayListWithCapacity(groupVersionKeys.size());
            for (String key : groupVersionKeys) {
                vuModels.add(new VersionUnitModel(key, vuGroupAndVersion.get(key).size()));
            }
            setSort("groupId", SortOrder.ASCENDING);
        }

        @Override
        public Iterator<VersionUnitModel> iterator(int first, int count) {
            ListPropertySorter.sort(vuModels, getSort());
            List<VersionUnitModel> vusSubList = vuModels.subList(first, first + count);
            return vusSubList.iterator();
        }

        @Override
        public int size() {
            return vuModels.size();
        }

        @Override
        public IModel<VersionUnitModel> model(VersionUnitModel object) {
            return new Model<>(object);
        }

        public Set<VersionUnit> getSelectedVersionUnits() {
            Set<VersionUnit> selectedVersionUnits = Sets.newHashSet();
            for (VersionUnitModel model : vuModels) {
                if (model.isSelected()) {
                    for (VersionUnit versionUnit : vuGroupAndVersion.get(model.getKey())) {
                        selectedVersionUnits.add(versionUnit);
                    }
                }
            }
            return selectedVersionUnits;
        }
    }

    private class VersionUnitModel implements Serializable {
        private String key;
        private String groupId;
        private String version;
        private int count;
        private boolean selected;

        private VersionUnitModel(String key, int count) {
            this.key = key;
            this.count = count;
            this.groupId = groupFromGroupVersionKey(key);
            this.version = versionFromGroupVersionKey(key);
        }

        public int getCount() {
            return count;
        }

        public String getKey() {
            return key;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public String getGroupId() {
            return groupId;
        }

        public String getVersion() {
            return version;
        }
    }
}
