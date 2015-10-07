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

package org.artifactory.webapp.wicket.page.security.user.column;

import com.google.common.collect.Lists;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.basic.SmartLinkLabel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.security.UserAwareAuthenticationProvider;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.common.wicket.component.deletable.listview.DeletableLabelGroup;
import org.artifactory.security.UserGroupInfo;
import org.artifactory.security.UserInfo;
import org.artifactory.webapp.wicket.page.config.security.general.SecurityGeneralConfigPage;
import org.artifactory.webapp.wicket.page.security.user.UserModel;

import java.util.Set;

/**
 * @author Yoav Aharoni
 */
public class UsernamePanel extends Panel {

    @SpringBean
    private AuthorizationService authorizationService;

    @SpringBean
    private UserGroupService userGroupService;

    @SpringBean
    private UserAwareAuthenticationProvider provider;

    public UsernamePanel(String id, IModel<UserModel> model) {
        super(id);
        add(new AttributeModifier("class", "UserColumn"));
        final UserModel userModel = model.getObject();
        final String username = userModel.getUsername();

        if (UserInfo.ANONYMOUS.equals(username) && !authorizationService.isAnonAccessEnabled()) {
            CharSequence pageUrl = getRequestCycle().urlFor(SecurityGeneralConfigPage.class, new PageParameters());
            add(new SmartLinkLabel("username", username + " (<a href=\"" + pageUrl + "\">disabled</a>)").
                    setEscapeModelStrings(false));
        } else {

            add(new Label("username", username));
        }

        Set<UserGroupInfo> userGroups = userModel.getGroups();

        DeletableLabelGroup<UserGroupInfo> groups =
                new DeletableLabelGroup<UserGroupInfo>("groups", userGroups) {
                    @Override
                    public void onDelete(UserGroupInfo value, AjaxRequestTarget target) {
                        super.onDelete(value, target);
                        //Save the group changes on each delete
                        userGroupService.removeUsersFromGroup(value.getGroupName(), Lists.newArrayList(username));
                        userModel.removeGroup(value);
                    }
                };
        // set the final merged set of groups to the user model.
        userModel.addGroups(userGroups);
        groups.setItemsPerPage(3);
        groups.setLabelClickable(false);
        groups.setLabelDeletable(false);
        add(groups);
    }
}
