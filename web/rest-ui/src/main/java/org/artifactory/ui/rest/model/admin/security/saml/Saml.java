package org.artifactory.ui.rest.model.admin.security.saml;

import org.artifactory.descriptor.security.sso.SamlSettings;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.util.JsonUtil;

/**
 * @author Chen Keinan
 */
public class Saml extends SamlSettings implements RestModel {

    Saml() {
    }

    public Saml(SamlSettings samlSettings) {
        if (samlSettings != null) {
            super.setEnableIntegration(samlSettings.isEnableIntegration());
            super.setCertificate(samlSettings.getCertificate());
            super.setLoginUrl(samlSettings.getLoginUrl());
            super.setLogoutUrl(samlSettings.getLogoutUrl());
            super.setNoAutoUserCreation(samlSettings.getNoAutoUserCreation());
            super.setServiceProviderName(samlSettings.getServiceProviderName());
        }
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
