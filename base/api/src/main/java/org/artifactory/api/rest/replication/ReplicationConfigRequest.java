package org.artifactory.api.rest.replication;

import org.artifactory.api.rest.restmodel.IModel;

import java.io.Serializable;

/**
 * @author mamo
 */
public class ReplicationConfigRequest implements Serializable, IModel {

    //local/remote
    private Boolean enabled;
    private String cronExp;
    private Boolean syncDeletes;
    private Boolean syncProperties;
    private String pathPrefix;

    //local only
    private String url;
    private Boolean enableEventReplication;
    private String username;
    private String password;
    private String proxy;
    private Integer socketTimeoutMillis;

    public Boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getCronExp() {
        return cronExp;
    }

    public void setCronExp(String cronExp) {
        this.cronExp = cronExp;
    }

    public Boolean getSyncDeletes() {
        return syncDeletes;
    }

    public void setSyncDeletes(Boolean syncDeletes) {
        this.syncDeletes = syncDeletes;
    }

    public Boolean getSyncProperties() {
        return syncProperties;
    }

    public void setSyncProperties(Boolean syncProperties) {
        this.syncProperties = syncProperties;
    }

    public String getPathPrefix() {
        return pathPrefix;
    }

    public void setPathPrefix(String pathPrefix) {
        this.pathPrefix = pathPrefix;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Boolean getEnableEventReplication() {
        return enableEventReplication;
    }

    public void setEnableEventReplication(Boolean enableEventReplication) {
        this.enableEventReplication = enableEventReplication;
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

    public Integer getSocketTimeoutMillis() {
        return socketTimeoutMillis;
    }

    public void setSocketTimeoutMillis(Integer socketTimeoutMillis) {
        this.socketTimeoutMillis = socketTimeoutMillis;
    }
}
