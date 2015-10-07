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

package org.artifactory.webapp.wicket.page.security.group.permission;

import com.google.common.collect.Lists;
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
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.common.wicket.component.border.titled.TitledBorder;
import org.artifactory.common.wicket.component.modal.panel.BaseModalPanel;
import org.artifactory.common.wicket.component.table.SortableTable;
import org.artifactory.common.wicket.component.table.columns.BooleanColumn;
import org.artifactory.common.wicket.util.ListPropertySorter;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.security.AceInfo;
import org.artifactory.security.AclInfo;
import org.artifactory.security.GroupInfo;
import org.artifactory.security.PermissionTargetInfo;
import org.artifactory.webapp.wicket.page.security.acl.AclsPage;
import org.artifactory.webapp.wicket.page.security.acl.PermissionsRow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author Tomer Cohen
 */
public class GroupPermissionsPanel extends BaseModalPanel {

    @SpringBean
    private UserGroupService userGroupService;

    @SpringBean
    private AclService aclService;

    @SpringBean
    private AuthorizationService authorizationService;

    private GroupInfo groupInfo;

    public GroupPermissionsPanel(GroupInfo groupInfo) {
        this.groupInfo = groupInfo;
        setWidth(500);
        setTitle(String.format("%s's Permission Targets", groupInfo.getGroupName()));

        TitledBorder border = new TitledBorder("border");
        add(border);
        border.add(addTable());
    }

    private SortableTable addTable() {
        List<IColumn<PermissionsRow>> columns = Lists.newArrayList();

        columns.add(new AbstractColumn<PermissionsRow>(
                Model.of("Permission Target"), "permissionTarget.name") {
            @Override
            public void populateItem(Item cellItem, String componentId, IModel rowModel) {
                cellItem.add(new LinkPanel(componentId, rowModel));
            }
        });

        columns.add(new BooleanColumn<PermissionsRow>("Manage", "manage", "manage"));
        columns.add(new BooleanColumn<PermissionsRow>("Delete", "delete", "delete"));
        columns.add(new BooleanColumn<PermissionsRow>("Deploy", "deploy", "deploy"));
        columns.add(new BooleanColumn<PermissionsRow>("Annotate", "annotate", "annotate"));
        columns.add(new BooleanColumn<PermissionsRow>("Read", "read", "read"));
        PermissionsTabTableDataProvider dataProvider = new PermissionsTabTableDataProvider(groupInfo);
        return new SortableTable<>("userPermissionsTable", columns, dataProvider, 10);
    }

    class PermissionsTabTableDataProvider extends SortableDataProvider<PermissionsRow> {
        private final GroupInfo groupInfo;
        private List<PermissionsRow> groupPermissions;

        PermissionsTabTableDataProvider(GroupInfo groupInfo) {
            setSort("permissionTarget.name", SortOrder.ASCENDING);
            this.groupInfo = groupInfo;
            loadData();
        }

        @Override
        public Iterator<PermissionsRow> iterator(int first, int count) {
            ListPropertySorter.sort(groupPermissions, getSort());
            List<PermissionsRow> list = groupPermissions.subList(first, first + count);
            return list.iterator();
        }

        @Override
        public int size() {
            return groupPermissions.size();
        }

        @Override
        public IModel<PermissionsRow> model(PermissionsRow object) {
            return new Model<>(object);
        }

        private void loadData() {
            groupPermissions = new ArrayList<>();
            List<AclInfo> acls = aclService.getAllAcls();
            for (AclInfo acl : acls) {
                PermissionsRow permissionRow = createPermissionRow(acl);
                addIfHasPermissions(permissionRow, groupPermissions);
            }
        }

        private PermissionsRow createPermissionRow(AclInfo acl) {
            PermissionTargetInfo target = acl.getPermissionTarget();
            Set<AceInfo> infos = acl.getAces();
            AceInfo targetAce = getTargetAce(infos);
            PermissionsRow permissionsRow = new PermissionsRow(target);
            if (targetAce != null) {
                permissionsRow.setRead(targetAce.canRead());
                permissionsRow.setAnnotate(targetAce.canAnnotate());
                permissionsRow.setDeploy(targetAce.canDeploy());
                permissionsRow.setDelete(targetAce.canDelete());
                permissionsRow.setManage(targetAce.canManage());
            }
            return permissionsRow;
        }

        private AceInfo getTargetAce(Set<AceInfo> infos) {
            for (AceInfo info : infos) {
                if (info.getPrincipal().equals(this.groupInfo.getGroupName())) {
                    return info;
                }
            }
            return null;
        }

        private void addIfHasPermissions(PermissionsRow permissionRow, List<PermissionsRow> userPermissions) {
            if (permissionRow.hasPermissions()) {
                // only add users/groups who have some permission
                userPermissions.add(permissionRow);
            }
        }

    }

    private class LinkPanel extends Panel {
        private LinkPanel(String id, IModel model) {
            super(id, model);
            PermissionsRow permRow = (PermissionsRow) model.getObject();
            final PermissionTargetInfo permissionTarget = permRow.getPermissionTarget();
            Link link = new Link("link") {
                @Override
                public void onClick() {
                    setResponsePage(new AclsPage(InfoFactoryHolder.get().copyPermissionTarget(permissionTarget)));
                }
            };
            add(link);
            link.add(new Label("label", permissionTarget.getName()));
        }
    }
}

