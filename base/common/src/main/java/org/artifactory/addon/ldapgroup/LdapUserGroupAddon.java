package org.artifactory.addon.ldapgroup;

import org.artifactory.addon.Addon;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.descriptor.security.ldap.group.LdapGroupPopulatorStrategies;
import org.artifactory.descriptor.security.ldap.group.LdapGroupSetting;

import java.util.List;
import java.util.Set;

/**
 * @author Chen Keinan
 */
public interface LdapUserGroupAddon extends Addon {

    /**
     * refresh ldap groups
     *
     * @param userName         - refresh by user name
     * @param ldapGroupSetting - ldap group settings
     * @param statusHolder     -  import status holder
     * @return List of Groups found following refresh
     */
    Set refreshLdapGroups(String userName, LdapGroupSetting ldapGroupSetting, BasicStatusHolder statusHolder);

    /**
     * import ldap groups into artifactory
     *
     * @param ldapGroups - ldap groups to be imported
     * @param strategy   - ldap group strategy
     * @return number group imported
     */
    int importLdapGroupsToArtifactory(List ldapGroups, LdapGroupPopulatorStrategies strategy);

    String[] retrieveUserLdapGroups(String userName, LdapGroupSetting ldapGroupSetting);
}
