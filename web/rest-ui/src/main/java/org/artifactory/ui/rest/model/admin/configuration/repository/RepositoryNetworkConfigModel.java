package org.artifactory.ui.rest.model.admin.configuration.repository;

import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.util.JsonUtil;

import static org.artifactory.ui.rest.model.admin.configuration.repository.RepoConfigDefaultValues.DEFAULT_SOCKET_TIMEOUT;
import static org.artifactory.ui.rest.model.admin.configuration.repository.RepoConfigDefaultValues.DEFAULT_SYNC_PROPERTIES;

/**
 * @author Aviad Shikloshi
 * @author Dan Feldman
 */
public class RepositoryNetworkConfigModel implements RestModel {

    protected String url;
    protected String username;
    protected String password;
    protected String proxy;
    protected Integer socketTimeout = DEFAULT_SOCKET_TIMEOUT;
    protected Boolean syncProperties = DEFAULT_SYNC_PROPERTIES;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProxy() {
        return proxy;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    public Integer getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(Integer socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public Boolean isSyncProperties() {
        return syncProperties;
    }

    public void setSyncProperties(Boolean syncProperties) {
        this.syncProperties = syncProperties;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
