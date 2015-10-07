package org.artifactory.ui.rest.model.admin.security.crowdsso;

import org.artifactory.descriptor.security.sso.CrowdSettings;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.util.JsonUtil;

/**
 * @author Chen Keinan
 */
public class CrowdIntegration extends CrowdSettings implements RestModel {

    public CrowdIntegration() {
    }

    public CrowdIntegration(CrowdSettings crowdSettings) {
        if (crowdSettings != null) {
            setApplicationName(crowdSettings.getApplicationName());
            setDirectAuthentication(crowdSettings.isDirectAuthentication());
            setEnableIntegration(crowdSettings.isEnableIntegration());
            setNoAutoUserCreation(crowdSettings.isNoAutoUserCreation());
            setPassword(crowdSettings.getPassword());
            setServerUrl(crowdSettings.getServerUrl());
            setUseDefaultProxy(crowdSettings.isUseDefaultProxy());
            setSessionValidationInterval(crowdSettings.getSessionValidationInterval());
        }
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
