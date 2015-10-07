package org.artifactory.ui.rest.model.admin.security.ldap;

import org.artifactory.descriptor.security.ldap.group.LdapGroupSetting;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.model.RestSpecialFields;
import org.artifactory.rest.common.service.IgnoreSpecialFields;
import org.artifactory.rest.common.util.JsonUtil;
import org.codehaus.jackson.map.annotate.JsonFilter;

/**
 * @author Chen Keinan
 */
@JsonFilter("exclude fields")
@IgnoreSpecialFields({"descriptionAttribute", "subTree", "groupMemberAttribute", "groupNameAttribute", "groupBaseDn", "filter", "enabled"})
public class LdapGroupModel extends LdapGroupSetting implements RestModel, RestSpecialFields {
    private boolean isView;

    public LdapGroupModel() {
    }

    public LdapGroupModel(LdapGroupSetting ldapGroupSetting, boolean isView) {
        if (ldapGroupSetting != null) {
            super.setName(ldapGroupSetting.getName());
            super.setStrategy(ldapGroupSetting.getStrategy());
            super.setEnabledLdap(ldapGroupSetting.getEnabledLdap());
            if (isView) {
                this.isView = isView;
            } else {
                super.setDescriptionAttribute(ldapGroupSetting.getDescriptionAttribute());
                super.setFilter(ldapGroupSetting.getFilter());
                super.setGroupBaseDn(ldapGroupSetting.getGroupBaseDn());
                super.setGroupMemberAttribute(ldapGroupSetting.getGroupMemberAttribute());
                super.setGroupNameAttribute(ldapGroupSetting.getGroupNameAttribute());
                super.setSubTree(ldapGroupSetting.isSubTree());
            }
        }
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToStringIgnoreSpecialFields(this);
    }

    @Override
    public boolean ignoreSpecialFields() {
        return isView;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(this.getClass().getSuperclass().getSimpleName().equals(o.getClass().getSimpleName()))) {
            return false;
        }
        LdapGroupSetting that = (LdapGroupSetting) o;
        return this.getName().equals(that.getName());
    }

    @Override
    public int hashCode() {
        return this.getName().hashCode();
    }
}
