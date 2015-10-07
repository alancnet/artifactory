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

package org.artifactory.webapp.wicket.page.config.security;

import com.google.common.collect.Lists;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.wicket.LdapGroupWebAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.common.wicket.component.CreateUpdateAction;
import org.artifactory.common.wicket.component.CreateUpdatePanel;
import org.artifactory.common.wicket.component.modal.panel.BaseModalPanel;
import org.artifactory.common.wicket.component.panel.list.ModalListPanel;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.security.SecurityDescriptor;
import org.artifactory.descriptor.security.ldap.group.LdapGroupSetting;

import java.util.List;

/**
 * @author Tomer Cohen
 */
public class LdapGroupListPanel extends ModalListPanel<LdapGroupSetting> {

    @SpringBean
    private CentralConfigService centralConfigService;

    @SpringBean
    private AddonsManager addonsManager;

    private MutableCentralConfigDescriptor mutableDescriptor;

    public LdapGroupListPanel(String id) {
        super(id);
        setOutputMarkupId(true);
        mutableDescriptor = centralConfigService.getMutableDescriptor();
    }


    @Override
    protected BaseModalPanel newCreateItemPanel() {
        LdapGroupWebAddon ldapAddon = addonsManager.addonByType(LdapGroupWebAddon.class);
        CreateUpdatePanel<LdapGroupSetting> ldapGroupPanel =
                ldapAddon.getLdapGroupPanel(CreateUpdateAction.CREATE, new LdapGroupSetting(), this);
        return ldapGroupPanel;
    }


    @Override
    protected BaseModalPanel newUpdateItemPanel(LdapGroupSetting itemObject) {
        LdapGroupWebAddon ldapAddon = addonsManager.addonByType(LdapGroupWebAddon.class);
        CreateUpdatePanel<LdapGroupSetting> ldapGroupPanel =
                ldapAddon.getLdapGroupPanel(CreateUpdateAction.UPDATE, itemObject, this);
        return ldapGroupPanel;
    }

    @Override
    public String getTitle() {
        return "LDAP Groups";
    }

    @Override
    protected List<LdapGroupSetting> getList() {
        SecurityDescriptor security = mutableDescriptor.getSecurity();
        List<LdapGroupSetting> ldapGroupSettings = security.getLdapGroupSettings();
        if (ldapGroupSettings != null) {
            return ldapGroupSettings;
        }
        return Lists.newArrayList();
    }

    @Override
    protected void addColumns(List<? super IColumn<LdapGroupSetting>> columns) {
        columns.add(new PropertyColumn<LdapGroupSetting>(Model.of("Name"), "name", "name"));
        columns.add(new PropertyColumn<LdapGroupSetting>(Model.of("Strategy"), "strategy", "strategy"));
        columns.add(new PropertyColumn<LdapGroupSetting>(Model.of("Attached LDAP"), "enabledLdap", "enabledLdap"));
    }

    @Override
    protected String getDeleteConfirmationText(LdapGroupSetting itemObject) {
        return "Are you sure you wish to delete the LDAP group '" + itemObject.getName() + "'?";
    }

    @Override
    protected void deleteItem(LdapGroupSetting itemObject, AjaxRequestTarget target) {
        SecurityDescriptor securityDescriptor = mutableDescriptor.getSecurity();
        securityDescriptor.removeLdapGroup(itemObject.getName());
        centralConfigService.saveEditedDescriptorAndReload(mutableDescriptor);
    }

    public MutableCentralConfigDescriptor getMutableDescriptor() {
        return mutableDescriptor;
    }

    public void setMutableDescriptor(MutableCentralConfigDescriptor mutableDescriptor) {
        this.mutableDescriptor = mutableDescriptor;
    }
}
