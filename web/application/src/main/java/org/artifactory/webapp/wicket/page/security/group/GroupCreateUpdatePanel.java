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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.StringValidator;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.common.wicket.behavior.defaultbutton.DefaultButtonBehavior;
import org.artifactory.common.wicket.component.CreateUpdateAction;
import org.artifactory.common.wicket.component.CreateUpdatePanel;
import org.artifactory.common.wicket.component.border.titled.TitledBorder;
import org.artifactory.common.wicket.component.checkbox.styled.StyledCheckbox;
import org.artifactory.common.wicket.component.links.TitledAjaxSubmitLink;
import org.artifactory.common.wicket.component.modal.ModalHandler;
import org.artifactory.common.wicket.component.modal.links.ModalCloseLink;
import org.artifactory.common.wicket.util.AjaxUtils;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.security.AccessLogger;
import org.artifactory.security.GroupInfo;
import org.artifactory.security.MutableGroupInfo;
import org.artifactory.webapp.wicket.util.validation.NameValidator;

/**
 * @author Yossi Shaul
 */
public class GroupCreateUpdatePanel extends CreateUpdatePanel<GroupInfo> {

    @SpringBean
    private UserGroupService groupService;

    public GroupCreateUpdatePanel(CreateUpdateAction action, GroupInfo groupInfo,
            GroupsListPanel groupsListPanel) {
        super(action, groupInfo);
        setWidth(440);

        add(form);

        TitledBorder border = new TitledBorder("border");
        add(border);
        form.add(border);

        // Group name
        RequiredTextField<String> groupNameTf = new RequiredTextField<>("groupName");
        setDefaultFocusField(groupNameTf);
        groupNameTf.add(StringValidator.maximumLength(100));
        groupNameTf.setEnabled(isCreate());// don't allow groupname update
        groupNameTf.add(new NameValidator("Invalid group name '%s'"));
        border.add(groupNameTf);

        // Group description
        TextArea groupDescriptionTextArea = new TextArea("description");
        border.add(groupDescriptionTextArea);

        // If default for newly created users
        border.add(new StyledCheckbox("newUserDefault"));

        // Cancel button
        form.add(new ModalCloseLink("cancel"));

        // Submit button
        TitledAjaxSubmitLink submit = createSubmitButton(groupsListPanel);
        form.add(submit);
        form.add(new DefaultButtonBehavior(submit));
    }

    private TitledAjaxSubmitLink createSubmitButton(final GroupsListPanel groupsListPanel) {
        String submitCaption = isCreate() ? "Create" : "Save";
        TitledAjaxSubmitLink submit = new TitledAjaxSubmitLink("submit", submitCaption, form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form form) {
                boolean hasError = false;
                if (isCreate()) {
                    hasError = onCreate();
                    AccessLogger.created("Successfully created group '" + getGroupInfo().getGroupName() + "'");
                } else {
                    onUpdate();
                    AccessLogger.updated("Successfully updated group '" + getGroupInfo().getGroupName() + "'");
                }
                AjaxUtils.refreshFeedback(target);
                if (!hasError) {
                    // close panel and refresh
                    target.add(groupsListPanel);
                    ModalHandler.closeCurrent(target);
                }
            }
        };
        return submit;
    }

    private boolean onCreate() {
        MutableGroupInfo group = getGroupInfo();
        boolean created = groupService.createGroup(group);
        if (!created) {
            error("Group '" + group.getGroupName() + "' already exists.");
            return true;    // has error
        } else {
            getPage().info("Successfully created group'" + group.getGroupName() + "'");
            return false;   // no error
        }
    }

    private void onUpdate() {
        groupService.updateGroup(getGroupInfo());
        getPage().info("Successfully updated group '" + getGroupInfo().getGroupName()  + "'");
    }

    private MutableGroupInfo getGroupInfo() {
        MutableGroupInfo groupInfo = InfoFactoryHolder.get().copyGroup((GroupInfo) form.getDefaultModelObject());
        groupInfo.setGroupName(groupInfo.getGroupName().toLowerCase());
        groupInfo.setRealm(groupInfo.getRealm().toLowerCase());
        return groupInfo;
    }
}