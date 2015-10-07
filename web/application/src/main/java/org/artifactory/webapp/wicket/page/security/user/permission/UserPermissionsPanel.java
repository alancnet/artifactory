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

package org.artifactory.webapp.wicket.page.security.user.permission;

import com.google.common.collect.Lists;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.api.security.AclService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.common.wicket.component.border.titled.TitledBorder;
import org.artifactory.common.wicket.component.modal.panel.BaseModalPanel;
import org.artifactory.common.wicket.component.table.SortableTable;
import org.artifactory.common.wicket.component.table.columns.BooleanColumn;
import org.artifactory.common.wicket.util.ListPropertySorter;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.security.AclInfo;
import org.artifactory.security.MutablePermissionTargetInfo;
import org.artifactory.security.MutableUserInfo;
import org.artifactory.security.PermissionTargetInfo;
import org.artifactory.security.UserGroupInfo;
import org.artifactory.webapp.wicket.page.security.acl.AclsPage;
import org.artifactory.webapp.wicket.page.security.acl.PermissionsRow;
import org.artifactory.webapp.wicket.page.security.user.UserModel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author Yossi Shaul
 */
public class UserPermissionsPanel extends BaseModalPanel {

    @SpringBean
    private UserGroupService userGroupService;

    @SpringBean
    private AclService aclService;

    private MutableUserInfo userInfo;

    public UserPermissionsPanel(UserModel user) {
        setWidth(500);
        setTitle(String.format("%s's Permission Targets", user.getUsername()));
        userInfo = InfoFactoryHolder.get().copyUser(userGroupService.findUser(user.getUsername()));
        Set<UserGroupInfo> groups = user.getGroups();
        //Add groups that may have been added from an external source.
        for (UserGroupInfo userGroupInfo : groups) {
            userInfo.addGroup(userGroupInfo.getGroupName(), userGroupInfo.getRealm());
        }
        TitledBorder border = new TitledBorder("border");
        add(border);
        border.add(addTable());
    }

    private SortableTable addTable() {
        List<IColumn<PermissionsRow>> columns = Lists.newArrayList();
        columns.add(
                new AbstractColumn<PermissionsRow>(Model.of("Permission Target"), "permissionTarget.name") {
                    @Override
                    public void populateItem(Item<ICellPopulator<PermissionsRow>> cellItem, String componentId,
                            IModel<PermissionsRow> rowModel) {
                        cellItem.add(new LinkPanel(componentId, rowModel));
                    }
                });

        columns.add(new BooleanColumn<PermissionsRow>("Manage", "manage", "manage"));
        columns.add(new BooleanColumn<PermissionsRow>("Delete", "delete", "delete"));
        columns.add(new BooleanColumn<PermissionsRow>("Deploy", "deploy", "deploy"));
        columns.add(new BooleanColumn<PermissionsRow>("Annotate", "annotate", "annotate"));
        columns.add(new BooleanColumn<PermissionsRow>("Read", "read", "read"));

        PermissionsTabTableDataProvider dataProvider = new PermissionsTabTableDataProvider(userInfo);
        return new SortableTable<>("userPermissionsTable", columns, dataProvider, 10);
    }

    class PermissionsTabTableDataProvider extends SortableDataProvider<PermissionsRow> {
        private final MutableUserInfo userInfo;
        private List<PermissionsRow> userPermissions;

        public PermissionsTabTableDataProvider(MutableUserInfo userInfo) {
            setSort("permissionTarget.name", SortOrder.ASCENDING);
            this.userInfo = userInfo;
            loadData();
        }

        @Override
        public Iterator<PermissionsRow> iterator(int first, int count) {
            ListPropertySorter.sort(userPermissions, getSort());
            List<PermissionsRow> list = userPermissions.subList(first, first + count);
            return list.iterator();
        }

        @Override
        public int size() {
            return userPermissions.size();
        }

        @Override
        public IModel<PermissionsRow> model(PermissionsRow object) {
            return new Model<>(object);
        }

        private void loadData() {
            userPermissions = new ArrayList<>();
            List<AclInfo> acls = aclService.getAllAcls();
            for (AclInfo acl : acls) {
                PermissionsRow permissionRow = createPermissionRow(acl);
                addIfHasPermissions(permissionRow, userPermissions);
            }
        }

        private PermissionsRow createPermissionRow(AclInfo acl) {
            PermissionTargetInfo target = acl.getPermissionTarget();
            PermissionsRow permissionsRow = new PermissionsRow(target);
            permissionsRow.setRead(aclService.canRead(userInfo, target));
            permissionsRow.setAnnotate(aclService.canAnnotate(userInfo, target));
            permissionsRow.setDeploy(aclService.canDeploy(userInfo, target));
            permissionsRow.setDelete(aclService.canDelete(userInfo, target));
            permissionsRow.setManage(aclService.canManage(userInfo, target));
            return permissionsRow;
        }

        private void addIfHasPermissions(PermissionsRow permissionRow, List<PermissionsRow> userPermissions) {
            if (permissionRow.hasPermissions()) {
                // only add users/groups who have some permission
                userPermissions.add(permissionRow);
            }
        }
    }

    private static class LinkPanel extends Panel {
        private LinkPanel(String id, IModel<PermissionsRow> model) {
            super(id, model);
            PermissionsRow permRow = model.getObject();
            final MutablePermissionTargetInfo permissionTarget = InfoFactoryHolder.get()
                    .copyPermissionTarget(permRow.getPermissionTarget());
            Link link = new Link("link") {
                @Override
                public void onClick() {
                    setResponsePage(new AclsPage(permissionTarget));
                }
            };
            add(link);
            link.add(new Label("label", permissionTarget.getName()));
        }
    }
}
