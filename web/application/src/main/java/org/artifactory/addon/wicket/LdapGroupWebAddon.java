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

package org.artifactory.addon.wicket;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.artifactory.addon.Addon;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.common.wicket.component.CreateUpdateAction;
import org.artifactory.common.wicket.component.CreateUpdatePanel;
import org.artifactory.common.wicket.component.panel.titled.TitledPanel;
import org.artifactory.common.wicket.component.table.columns.BooleanColumn;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.security.ldap.LdapSetting;
import org.artifactory.descriptor.security.ldap.group.LdapGroupPopulatorStrategies;
import org.artifactory.descriptor.security.ldap.group.LdapGroupSetting;
import org.artifactory.security.GroupInfo;
import org.artifactory.webapp.wicket.page.config.security.LdapGroupListPanel;

import java.util.List;
import java.util.Set;

/**
 * LDAP integration with Artifactory.
 *
 * @author Tomer Cohen
 */
public interface LdapGroupWebAddon extends Addon {

    /**
     * Get the LDAP group create/update panel for the LDAP group addons.
     *
     * @param createUpdateAction The action (create/update).
     * @param ldapGroupSetting   The initial (for creation) or the existing (for update) ldapGroupSetting.
     * @param parent             The parent panel which contains the current one.
     * @return The create/update panel for LDAP group settings.
     */
    CreateUpdatePanel<LdapGroupSetting> getLdapGroupPanel(CreateUpdateAction createUpdateAction,
            LdapGroupSetting ldapGroupSetting, LdapGroupListPanel parent);

    /**
     * Get indicator whether a group is external (e.g. LDAP) or was created in Artifactory only.
     *
     * @param statusHolder Status holder
     * @return Indicator whether a group is external.
     */
    BooleanColumn<GroupInfo> addExternalGroupIndicator(BasicStatusHolder statusHolder);

    /**
     * Get the Ldap Group configuration Panel when addon is activated.
     *
     * @param id The wicket Id
     * @return Ldap Group configuration Panel when addon is activated.
     */
    TitledPanel getLdapGroupConfigurationPanel(String id);

    /**
     * Import external groups (e.g. LDAP) into Artifactory
     *
     * @param ldapGroups The ldap groups to import into Artifactory
     * @param strategy   The strategy to use for the import.
     * @see LdapGroupPopulatorStrategies
     */
    int importExternalGroupsToArtifactory(List ldapGroups, LdapGroupPopulatorStrategies strategy);

    /**
     * Show the ldap groups, either all in the system or for a single user.
     *
     * @param userName         The username for which to show the LDAP groups for, null for all groups.
     * @param ldapGroupSetting The LDAP group settings to use for the refresh.
     * @param statusHolder     status holder
     * @return A list of LDAP groups.
     */
    Set refreshLdapGroupList(String userName, LdapGroupSetting ldapGroupSetting, BasicStatusHolder
            statusHolder);

    /**
     * Get a warning label if there is at least one LDAP group synchronization active indicating that some permissions
     * which are implied by the LDAP group synchronization addon is not displayed.
     *
     * @param wicketId The wicket ID for the warning label.
     * @return The warning label
     */
    Label getLdapActiveWarning(String wicketId);

    /**
     * Get the LDAP lists defined in the system, in the pro version of Artifactory the panel is re-orderable such
     * that a certain order can be defined as to which LDAP will be queried first, moreover, the pro version
     * allows for <b>multiple enabled</b> LDAP definitions.
     *
     * @param wicketId The wicket ID of the panel.
     * @return The current defined LDAP settings panel.
     */
    WebMarkupContainer getLdapListPanel(String wicketId);

    /**
     * Save the LDAP configurations, in the pro version of Artifactory several <b>multiple enabled</b> LDAP
     * definitions will be saved.
     *
     * @param configDescriptor The config descriptor to be saved.
     * @param ldapSetting      The LDAP setting that was added/modified.
     */
    void saveLdapSetting(MutableCentralConfigDescriptor configDescriptor, LdapSetting ldapSetting);
}
