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

package org.artifactory.webapp.wicket.page.security.acl;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.api.security.AclService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.common.wicket.component.CreateUpdateAction;
import org.artifactory.common.wicket.component.modal.panel.BaseModalPanel;
import org.artifactory.common.wicket.component.panel.list.ModalListPanel;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.security.AccessLogger;
import org.artifactory.security.ArtifactoryPermission;
import org.artifactory.security.PermissionTargetInfo;

import java.util.List;

/**
 * @author Yoav Aharoni
 */
public class PermissionTargetListPanel extends ModalListPanel<PermissionTargetInfo> {
    @SpringBean
    private AuthorizationService authService;

    @SpringBean
    private AclService security;

    public PermissionTargetListPanel(String id) {
        super(id);

        if (!authService.isAdmin()) {
            disableNewItemLink();
        }

        getDataProvider().setSort("name", SortOrder.ASCENDING);
    }

    @Override
    public String getTitle() {
        return "";
    }

    @Override
    protected List<PermissionTargetInfo> getList() {
        return security.getPermissionTargets(ArtifactoryPermission.MANAGE);
    }

    @Override
    protected void addColumns(List<? super IColumn<PermissionTargetInfo>> columns) {
        columns.add(new PropertyColumn<PermissionTargetInfo>(Model.of("Permission Target Name"), "name", "name"));
        columns.add(new PropertyColumn<PermissionTargetInfo>(Model.of("Repositories"), "repoKeys") {
            // TODO: [by dan] RTFACT-6906 display cached repositories as remote until security is corrected
            @Override
            protected IModel<List<String>> createLabelModel(IModel<PermissionTargetInfo> rowModel) {
                List<String> repoKeys = rowModel.getObject().getRepoKeys();
                ListModel repoKeysModel =
                        new ListModel<>(security.convertCachedRepoKeysToRemote(repoKeys));
                return repoKeysModel;
            }
        });
    }

    @Override
    protected BaseModalPanel newCreateItemPanel() {
        return new PermissionTargetCreateUpdatePanel(
                CreateUpdateAction.CREATE,
                InfoFactoryHolder.get().createPermissionTarget(), this);
    }

    @Override
    protected BaseModalPanel newUpdateItemPanel(PermissionTargetInfo permissionTarget) {
        return new PermissionTargetCreateUpdatePanel(
                CreateUpdateAction.UPDATE,
                InfoFactoryHolder.get().copyPermissionTarget(permissionTarget), this);
    }

    @Override
    protected String getDeleteConfirmationText(PermissionTargetInfo permissionTarget) {
        return "Are you sure you wish to delete the target '" + permissionTarget.getName() + "'?";
    }

    @Override
    protected void deleteItem(PermissionTargetInfo permissionTarget, AjaxRequestTarget target) {
        security.deleteAcl(permissionTarget);
        AccessLogger.deleted("Permission Target " + permissionTarget.getName() + " was deleted successfully");
    }

}
