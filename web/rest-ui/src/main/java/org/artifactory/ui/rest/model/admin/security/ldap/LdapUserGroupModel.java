package org.artifactory.ui.rest.model.admin.security.ldap;

import org.artifactory.addon.ldapgroup.LdapUserGroup;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.util.JsonUtil;

/**
 * @author Chen Keinan
 */
public class LdapUserGroupModel extends LdapUserGroup implements RestModel {

    public LdapUserGroupModel() {
        super();
    }

    public LdapUserGroupModel(String groupName, String description, String groupDn) {
        super(groupName, description, groupDn);
    }


    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
