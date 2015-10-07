/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2012 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.artifactory.descriptor.replication;

import org.artifactory.descriptor.Descriptor;
import org.artifactory.descriptor.repo.ProxyDescriptor;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Noam Y. Tenne
 */
@XmlType(name = "LocalReplicationType", propOrder = {"url", "proxy", "socketTimeoutMillis", "username", "password",
        "enableEventReplication"}, namespace = Descriptor.NS)
public class LocalReplicationDescriptor extends ReplicationBaseDescriptor {

    @XmlElement(required = false)
    private String url;

    @XmlIDREF
    @XmlElement(name = "proxyRef", required = false)
    private ProxyDescriptor proxy;

    @XmlElement(defaultValue = "15000", required = false)
    private int socketTimeoutMillis = 15000;//Default socket timeout

    @XmlElement(required = false)
    private String username;

    @XmlElement(required = false)
    private String password;

    @XmlElement(defaultValue = "true")
    private boolean enableEventReplication = false;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ProxyDescriptor getProxy() {
        return proxy;
    }

    public void setProxy(ProxyDescriptor proxy) {
        this.proxy = proxy;
    }

    public int getSocketTimeoutMillis() {
        return socketTimeoutMillis;
    }

    public void setSocketTimeoutMillis(int socketTimeoutMillis) {
        this.socketTimeoutMillis = socketTimeoutMillis;
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

    public boolean isEnableEventReplication() {
        return enableEventReplication;
    }

    public void setEnableEventReplication(boolean enableEventReplication) {
        this.enableEventReplication = enableEventReplication;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        LocalReplicationDescriptor that = (LocalReplicationDescriptor) o;

        if (enableEventReplication != that.enableEventReplication) {
            return false;
        }
        if (socketTimeoutMillis != that.socketTimeoutMillis) {
            return false;
        }
        if (password != null ? !password.equals(that.password) : that.password != null) {
            return false;
        }
        if (proxy != null ? !proxy.equals(that.proxy) : that.proxy != null) {
            return false;
        }
        if (!url.equals(that.url)) {
            return false;
        }
        if (username != null ? !username.equals(that.username) : that.username != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + url.hashCode();
        result = 31 * result + (proxy != null ? proxy.hashCode() : 0);
        result = 31 * result + socketTimeoutMillis;
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (enableEventReplication ? 1 : 0);
        return result;
    }
}
