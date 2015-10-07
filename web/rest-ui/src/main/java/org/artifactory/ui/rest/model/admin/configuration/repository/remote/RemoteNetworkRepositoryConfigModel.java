package org.artifactory.ui.rest.model.admin.configuration.repository.remote;

import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.ui.rest.model.admin.configuration.repository.RepositoryNetworkConfigModel;

import static org.artifactory.ui.rest.model.admin.configuration.repository.RepoConfigDefaultValues.DEFAULT_COOKIE_MANAGEMENT;
import static org.artifactory.ui.rest.model.admin.configuration.repository.RepoConfigDefaultValues.DEFAULT_LENIENENT_HOST_AUTH;

/**
 * @author Aviad Shikloshi
 * @author Dan Feldman
 */
public class RemoteNetworkRepositoryConfigModel extends RepositoryNetworkConfigModel {

    protected String localAddress;
    protected Boolean lenientHostAuth = DEFAULT_LENIENENT_HOST_AUTH;
    protected Boolean cookieManagement = DEFAULT_COOKIE_MANAGEMENT;

    public String getLocalAddress() {
        return localAddress;
    }

    public void setLocalAddress(String localAddress) {
        this.localAddress = localAddress;
    }

    public Boolean getLenientHostAuth() {
        return lenientHostAuth;
    }

    public void setLenientHostAuth(Boolean lenientHostAuth) {
        this.lenientHostAuth = lenientHostAuth;
    }

    public Boolean getCookieManagement() {
        return cookieManagement;
    }

    public void setCookieManagement(Boolean cookieManagement) {
        this.cookieManagement = cookieManagement;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
