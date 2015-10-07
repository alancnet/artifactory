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

package org.artifactory.webapp.wicket.page.security.user;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.security.UserAwareAuthenticationProvider;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.behavior.JavascriptEvent;
import org.artifactory.common.wicket.behavior.tooltip.TooltipBehavior;
import org.artifactory.common.wicket.component.CreateUpdateAction;
import org.artifactory.common.wicket.component.modal.links.ModalShowLink;
import org.artifactory.common.wicket.component.modal.panel.BaseModalPanel;
import org.artifactory.common.wicket.component.panel.list.ModalListPanel;
import org.artifactory.common.wicket.component.table.columns.BooleanColumn;
import org.artifactory.common.wicket.component.table.columns.TooltipLabelColumn;
import org.artifactory.common.wicket.component.table.columns.checkbox.SelectAllCheckboxColumn;
import org.artifactory.security.AccessLogger;
import org.artifactory.security.UserGroupInfo;
import org.artifactory.webapp.wicket.page.security.user.column.UserColumn;
import org.artifactory.webapp.wicket.page.security.user.permission.UserPermissionsPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The users table is special in that it is sensitive to a filter and also contains a checkbox column.
 *
 * @author Yossi Shaul
 */
public class UsersTable extends ModalListPanel<UserModel> {
    private static final Logger log = LoggerFactory.getLogger(UsersTable.class);

    @SpringBean
    private UserGroupService userGroupService;

    @SpringBean
    private AuthorizationService authorizationService;

    @SpringBean
    private UserAwareAuthenticationProvider provider;

    private UsersTableDataProvider dataProvider;

    public UsersTable(String id, UsersTableDataProvider dataProvider) {
        super(id, dataProvider);
        this.dataProvider = dataProvider;
    }

    @Override
    public String getTitle() {
        return "Users List";
    }

    @Override
    protected List<UserModel> getList() {
        throw new UnsupportedOperationException("Users table uses it's own data provider");
    }

    @Override
    protected void addColumns(List<? super IColumn<UserModel>> columns) {
        columns.add(createSelectedColumn());
        columns.add(new UserColumn("User Name"));
        columns.add(createRealmColumn());
        columns.add(new BooleanColumn<UserModel>("Admin", "admin", "admin"));
        columns.add(new PropertyColumn<UserModel>(Model.of("Last Login"), "lastLoginTimeMillis", "lastLoginString"));
        columns.add(createExternalStatusColumn());
    }

    private SelectAllCheckboxColumn<UserModel> createSelectedColumn() {
        return new SelectAllCheckboxColumn<>("", "selected", null);
    }

    private PropertyColumn<UserModel> createRealmColumn() {
        return new PropertyColumn<UserModel>(Model.of("Realm"), "realm", "realm") {
            @Override
            public void populateItem(Item<ICellPopulator<UserModel>> item, String componentId,
                    final IModel<UserModel> model) {
                String realm;
                UserModel userModel = model.getObject();
                if (userModel.isAnonymous()) {
                    realm = "";
                } else if (StringUtils.isBlank(userModel.getRealm())) {
                    realm = "Will be updated on next login";
                    item.add(new CssClass("gray-listed-label"));
                } else {
                    realm = StringUtils.capitalize(userModel.getRealm());
                }
                item.add(new Label(componentId, Model.of(realm)));
            }
        };
    }

    @Override
    protected boolean canAddRowItemDoubleClickBehavior(IModel<UserModel> model) {
        //Respond to double clicks on the row only if it's not anonymous
        return !model.getObject().isAnonymous();
    }

    private TooltipLabelColumn<UserModel> createExternalStatusColumn() {
        return new TooltipLabelColumn<UserModel>(Model.of("External Realm Status"), "status", "status", 0) {
            @Override
            public void populateItem(Item<ICellPopulator<UserModel>> item, final String componentId,
                    IModel<UserModel> model) {
                final UserModel user = model.getObject();
                if (isExternalUser(user)) {
                    createExternalUserComponent(item, componentId, user);
                } else {
                    createLocalUserLabel(item, componentId);
                }
            }
        };
    }

    private void createLocalUserLabel(Item<ICellPopulator<UserModel>> item, String componentId) {
        // Create empty label ( o need to check the user status in remote servers).
        final Model emptyText = Model.of("");
        final Label constantLabel = new Label(componentId, emptyText);
        constantLabel.add(new CssClass("item-link"));
        item.add(constantLabel);
    }

    private boolean isExternalUser(UserModel user) {
        return !("internal".equals(user.getRealm()) || "system".equals(
                user.getRealm()) || user.getRealm() == null || user.getRealm().isEmpty() || user.isAnonymous());
    }

    private void createExternalUserComponent(final Item<ICellPopulator<UserModel>> item, final String componentId,
            final UserModel user) {
        // Create "action label" which allows to check the user status in remote server, by clicking the "action label".
        log.debug("User '{}' is from realm '{}'", user.getUsername(), user.getRealm());
        if (user.getStatus() == null) {
            final Model<String> m = Model.of("Check external status");
            final Label actionLabel = new Label(componentId, m);
            actionLabel.add(new CssClass("item-link"));
            item.add(actionLabel);
            item.add(new AjaxEventBehavior("onClick") {
                @Override
                protected void onEvent(final AjaxRequestTarget target) {

                    log.debug("User '{}' is from realm '{}'", user.getUsername(), user.getRealm());
                    Label statusLabel = createStatusComponent(user, componentId);
                    actionLabel.replaceWith(statusLabel);
                    target.add(item);
                    Set<UserGroupInfo> userGroups = user.getGroups();
                    provider.addExternalGroups(user.getUsername(), user.getRealm(), userGroups);
                    user.addGroups(userGroups);
                    target.add(UsersTable.this);

                }
            });
            // TODO find better way to implement te following code.
            // The following code (LinksColumn.current.hide()) hides the row's link panel (edit delete permissions panel).
            // Note: refreshing the table without hiding the link will cause the link panel to stay stuck on the screen
            item.add(new JavascriptEvent("onmousedown", "LinksColumn.current.hide();"));
        } else {
            Label statusLabel = createStatusComponent(user, componentId);
            item.add(statusLabel);
        }
    }

    private Label createStatusComponent(UserModel user, String componentId) {
        Label statusLabel;
        if (provider.userExists(user.getUsername(), user.getRealm())) {
            user.setStatus(UserModel.Status.ACTIVE_USER);
            final Model status = Model.of("Active user");
            statusLabel = new Label(componentId, status);
        } else {
            user.setStatus(UserModel.Status.INACTIVE_USER);
            final Model status = Model.of("Inactive user");
            statusLabel = new Label(componentId, status);
            statusLabel.add(new CssClass("black-listed-label"));
        }
        TooltipBehavior tooltipBehavior = new TooltipBehavior(new PropertyModel(user, "status.description"));
        statusLabel.add(tooltipBehavior);
        return statusLabel;
    }

    @Override
    protected BaseModalPanel newCreateItemPanel() {
        Set<String> defaultGroupsNames = userGroupService.getNewUserDefaultGroupsNames();
        return new UserCreateUpdatePanel(CreateUpdateAction.CREATE, new UserModel(defaultGroupsNames), this);
    }

    @Override
    protected BaseModalPanel newUpdateItemPanel(UserModel user) {
        return new UserCreateUpdatePanel(CreateUpdateAction.UPDATE, user, this);
    }

    protected BaseModalPanel newViewUserPermissionsPanel(UserModel user) {
        return new UserPermissionsPanel(user);
    }

    @Override
    protected String getDeleteConfirmationText(UserModel user) {
        return "Are you sure you wish to delete the user '" + user.getUsername() + "'?";
    }

    @Override
    protected void deleteItem(UserModel user, AjaxRequestTarget target) {
        String currentUsername = authorizationService.currentUsername();
        String selectedUsername = user.getUsername();
        if (currentUsername.equals(selectedUsername)) {
            error("Action cancelled. You are logged-in as the user you have selected for removal.");
            return;
        }
        userGroupService.deleteUser(selectedUsername);
        AccessLogger.deleted("User " + selectedUsername + " was deleted successfully");
        refreshUsersList(target);
    }

    @Override
    protected void addLinks(List<AbstractLink> links, final UserModel userModel, String linkId) {
        //Do not add the delete and edit links to the anonymous user
        if (!userModel.isAnonymous()) {
            super.addLinks(links, userModel, linkId);
        }
        // add view user permissions link
        ModalShowLink viewPermissionsLink = new ModalShowLink(linkId, "Permissions") {
            @Override
            protected BaseModalPanel getModelPanel() {
                return newViewUserPermissionsPanel(userModel);
            }
        };
        viewPermissionsLink.add(new CssClass("icon-link"));
        viewPermissionsLink.add(new CssClass("ViewUserPermissionsAction"));
        links.add(viewPermissionsLink);
    }

    public void refreshUsersList(AjaxRequestTarget target) {
        dataProvider.recalcUsersList();
        target.add(this);
    }

    List<String> getSelectedUsernames() {
        List<String> selectedUsernames = new ArrayList<>();
        for (UserModel userModel : dataProvider.getUsers()) {
            if (userModel.isSelected()) {
                selectedUsernames.add(userModel.getUsername());
            }
        }
        return selectedUsernames;
    }
}
