package org.artifactory.ui.rest.model.admin.security.ldap;

import java.util.List;

import org.artifactory.rest.common.model.BaseModel;

/**
 * @author Chen Keinan
 */
public class LdapImportModel extends BaseModel {

    private LdapGroupModel ldapGroupSettings;
    private List<LdapUserGroupModel> importGroups;

    public LdapGroupModel getLdapGroupSettings() {
        return ldapGroupSettings;
    }

    public void setLdapGroupSettings(LdapGroupModel ldapGroupSettings) {
        this.ldapGroupSettings = ldapGroupSettings;
    }

    public List<LdapUserGroupModel> getImportGroups() {
        return importGroups;
    }

    public void setImportGroups(
            List<LdapUserGroupModel> importGroups) {
        this.importGroups = importGroups;
    }
}
