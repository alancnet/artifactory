package org.artifactory.ui.rest.model.admin.security.ldap;

import org.artifactory.descriptor.security.ldap.LdapSetting;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.model.RestSpecialFields;
import org.artifactory.rest.common.service.IgnoreSpecialFields;
import org.artifactory.rest.common.util.JsonUtil;
import org.codehaus.jackson.map.annotate.JsonFilter;

/**
 * @author chen Keinan
 */
@JsonFilter("exclude fields")
@IgnoreSpecialFields({"autoCreateUser", "enabled", "emailAttribute"})
public class LdapSettingModel extends LdapSetting implements RestModel, RestSpecialFields {

    private boolean isView;
    private String testUsername;
    private String testPassword;

    LdapSettingModel() {
    }

    public LdapSettingModel(LdapSetting ldapSetting, boolean isView) {
        if (ldapSetting != null) {
            super.setKey(ldapSetting.getKey());
            super.setLdapUrl(ldapSetting.getLdapUrl());
            if (isView) {
                this.isView = true;
            } else {
                super.setEnabled(ldapSetting.isEnabled());
                super.setAutoCreateUser(ldapSetting.isAutoCreateUser());
                super.setEmailAttribute(ldapSetting.getEmailAttribute());
                super.setSearch(ldapSetting.getSearch());
                super.setUserDnPattern(ldapSetting.getUserDnPattern());
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


    public String getTestUsername() {
        return testUsername;
    }

    public void setTestUsername(String testUsername) {
        this.testUsername = testUsername;
    }

    public String getTestPassword() {
        return testPassword;
    }

    public void setTestPassword(String testPassword) {
        this.testPassword = testPassword;
    }

}
