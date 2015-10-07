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

package org.artifactory.webapp.wicket.page.security.group;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.wicket.LdapGroupWebAddon;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.component.CreateUpdateAction;
import org.artifactory.common.wicket.component.modal.links.ModalShowLink;
import org.artifactory.common.wicket.component.modal.panel.BaseModalPanel;
import org.artifactory.common.wicket.component.panel.list.ModalListPanel;
import org.artifactory.common.wicket.component.table.columns.BooleanColumn;
import org.artifactory.common.wicket.component.table.columns.TitlePropertyColumn;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.sapi.security.SecurityConstants;
import org.artifactory.security.AccessLogger;
import org.artifactory.security.GroupInfo;
import org.artifactory.security.MutableGroupInfo;
import org.artifactory.webapp.wicket.page.security.group.permission.GroupPermissionsPanel;

import java.util.List;

/**
 * @author Yossi Shaul
 */
public class GroupsListPanel extends ModalListPanel<GroupInfo> {

    @SpringBean
    private UserGroupService userGroupService;

    @SpringBean
    private AddonsManager addonsManager;

    public GroupsListPanel(String id) {
        super(id);
        getDataProvider().setSort("groupName", SortOrder.ASCENDING);
    }

    @Override
    public String getTitle() {
        return "";
    }

    @Override
    protected List<GroupInfo> getList() {
        return userGroupService.getAllGroups();
    }

    @Override
    protected void addColumns(List<? super IColumn<GroupInfo>> columns) {
        columns.add(new TitlePropertyColumn<GroupInfo>("Group Name", "groupName", "groupName") {
            @Override
            public void populateItem(Item<ICellPopulator<GroupInfo>> item, String componentId,
                    IModel<GroupInfo> model) {
                super.populateItem(item, componentId, model);
                item.add(new CssClass("group nowrap"));
            }
        });

        columns.add(new TitlePropertyColumn<GroupInfo>("Description", "description", "description") {
            @Override
            public void populateItem(Item<ICellPopulator<GroupInfo>> item, String componentId,
                    IModel<GroupInfo> model) {
                super.populateItem(item, componentId, model);
                GroupInfo groupInfo = model.getObject();
                String description = groupInfo.getDescription();
                item.add(new AttributeModifier("title", description != null ? description : ""));
                item.add(new CssClass("one-line"));
            }
        });
        LdapGroupWebAddon ldapGroupWebAddon = addonsManager.addonByType(LdapGroupWebAddon.class);
        BooleanColumn<GroupInfo> externalGroupColumn = ldapGroupWebAddon.addExternalGroupIndicator(
                new BasicStatusHolder());
        if (externalGroupColumn != null) {
            columns.add(externalGroupColumn);
        }
        columns.add(new BooleanColumn<GroupInfo>("Auto Join", "newUserDefault", "newUserDefault"));
    }

    @Override
    protected BaseModalPanel newCreateItemPanel() {
        MutableGroupInfo group = InfoFactoryHolder.get().createGroup();
        group.setRealm(SecurityConstants.DEFAULT_REALM);
        return new GroupCreateUpdatePanel(CreateUpdateAction.CREATE, group, this);
    }

    @Override
    protected BaseModalPanel newUpdateItemPanel(GroupInfo group) {
        return new GroupCreateUpdatePanel(CreateUpdateAction.UPDATE, group, this);
    }

    @Override
    protected String getDeleteConfirmationText(GroupInfo group) {
        return "Are you sure you wish to delete the group '" + group.getGroupName() + "'?";
    }

    @Override
    protected void deleteItem(GroupInfo group, AjaxRequestTarget target) {
        userGroupService.deleteGroup(group.getGroupName());
        AccessLogger.deleted("Group " + group.getGroupName() + " was deleted successfully");
    }

    protected BaseModalPanel newViewUserPermissionsPanel(GroupInfo groupInfo) {
        return new GroupPermissionsPanel(groupInfo);
    }

    @Override
    protected void addLinks(List<AbstractLink> links, final GroupInfo groupInfo, String linkId) {
        super.addLinks(links, groupInfo, linkId);
        ModalShowLink viewPermissionsLink = new ModalShowLink(linkId, "Permissions") {
            @Override
            protected BaseModalPanel getModelPanel() {
                return newViewUserPermissionsPanel(groupInfo);
            }
        };
        viewPermissionsLink.add(new CssClass("icon-link"));
        viewPermissionsLink.add(new CssClass("ViewUserPermissionsAction"));
        links.add(viewPermissionsLink);
    }
}