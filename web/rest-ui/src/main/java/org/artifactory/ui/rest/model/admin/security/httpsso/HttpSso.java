package org.artifactory.ui.rest.model.admin.security.httpsso;

import org.artifactory.descriptor.security.sso.HttpSsoSettings;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.util.JsonUtil;

/**
 * @author Chen Keinan
 */
public class HttpSso extends HttpSsoSettings implements RestModel {

    public HttpSso() {
    }

    public HttpSso(HttpSsoSettings httpSsoSettings) {
        if (httpSsoSettings != null) {
            super.setHttpSsoProxied(httpSsoSettings.isHttpSsoProxied());
            super.setNoAutoUserCreation(!httpSsoSettings.isNoAutoUserCreation());
            super.setRemoteUserRequestVariable(httpSsoSettings.getRemoteUserRequestVariable());
        }
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
