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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.common.wicket.WicketProperty;
import org.artifactory.common.wicket.behavior.defaultbutton.DefaultButtonBehavior;
import org.artifactory.common.wicket.behavior.filteringselect.FilteringSelectBehavior;
import org.artifactory.common.wicket.component.form.SecureForm;
import org.artifactory.common.wicket.component.links.TitledAjaxSubmitLink;
import org.artifactory.common.wicket.component.panel.fieldset.FieldSetPanel;
import org.artifactory.common.wicket.util.ListPropertySorter;
import org.artifactory.security.GroupInfo;

import java.util.List;

/**
 * This panel controls the users filtering by username and/or group.
 *
 * @author Yossi Shaul
 */
public class UsersFilterPanel extends FieldSetPanel {
    @SpringBean
    private UserGroupService userGroupService;

    @WicketProperty
    private String usernameFilter;

    @WicketProperty
    private String groupFilter;

    public UsersFilterPanel(String id, final UsersPanel usersListPanel) {
        super(id);

        Form form = new SecureForm("usersFilterForm");
        add(form);

        form.add(new TextField<>("usernameFilter", new PropertyModel<String>(this, "usernameFilter")));

        TitledAjaxSubmitLink filterButton = new TitledAjaxSubmitLink("filter", "Filter", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form form) {
                // refresh the users list and table
                usersListPanel.refreshUsersList(target);
            }
        };
        form.add(filterButton);
        form.add(new DefaultButtonBehavior(filterButton));
        final List<GroupInfo> groupInfos = userGroupService.getAllGroups();
        SortParam sortParam = new SortParam("groupName", true);
        ListPropertySorter.sort(groupInfos, sortParam);

        // Drop-down choice of groups to filter by
        DropDownChoice groupDdc = new UsersPanel.FilterGroupDropDownChoice("groupFilter",
                new PropertyModel<GroupInfo>(this, "groupFilter"), groupInfos);
        groupDdc.add(new FilteringSelectBehavior());
        form.add(groupDdc);
    }

    @Override
    public String getTitle() {
        return "Filter Users";
    }

    public String getUsernameFilter() {
        return usernameFilter;
    }

    public String getGroupFilter() {
        return groupFilter;
    }

    public void setUsernameFilter(String usernameFilter) {
        this.usernameFilter = usernameFilter;
    }

    public void setGroupFilter(String groupFilter) {
        this.groupFilter = groupFilter;
    }
}
