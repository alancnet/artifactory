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

package org.artifactory.descriptor.external;

import org.artifactory.descriptor.Descriptor;
import org.artifactory.descriptor.repo.ProxyDescriptor;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "BlackDuckSettingsType",
        namespace = Descriptor.NS,
        propOrder = {"enableIntegration", "serverUri", "username", "password", "connectionTimeoutMillis", "proxy"})
public class BlackDuckSettingsDescriptor implements Descriptor {

    @XmlElement(defaultValue = "false")
    private boolean enableIntegration = false;

    @XmlElement(required = true)
    private String serverUri;

    @XmlElement(required = true)
    private String username;

    @XmlElement(required = true)
    private String password;

    @XmlElement
    private Long connectionTimeoutMillis;

    @XmlIDREF
    @XmlElement(name = "proxyRef")
    @JsonProperty("proxyRef")
    private ProxyDescriptor proxy;

    public boolean isEnableIntegration() {
        return enableIntegration;
    }

    public void setEnableIntegration(boolean enableIntegration) {
        this.enableIntegration = enableIntegration;
    }

    public String getServerUri() {
        return serverUri;
    }

    public void setServerUri(String serverUri) {
        this.serverUri = serverUri;
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

    public Long getConnectionTimeoutMillis() {
        return connectionTimeoutMillis;
    }

    public void setConnectionTimeoutMillis(Long connectionTimeoutMillis) {
        this.connectionTimeoutMillis = connectionTimeoutMillis;
    }

    public ProxyDescriptor getProxy() {
        return proxy;
    }

    public void setProxy(ProxyDescriptor proxy) {
        this.proxy = proxy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BlackDuckSettingsDescriptor that = (BlackDuckSettingsDescriptor) o;

        if (enableIntegration != that.enableIntegration) {
            return false;
        }
        if (connectionTimeoutMillis != null ? !connectionTimeoutMillis.equals(that.connectionTimeoutMillis) :
                that.connectionTimeoutMillis != null) {
            return false;
        }
        if (password != null ? !password.equals(that.password) : that.password != null) {
            return false;
        }
        if (serverUri != null ? !serverUri.equals(that.serverUri) : that.serverUri != null) {
            return false;
        }
        if (username != null ? !username.equals(that.username) : that.username != null) {
            return false;
        }
        if (proxy != null ? !proxy.equals(that.proxy) : that.proxy != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = (enableIntegration ? 1 : 0);
        result = 31 * result + (serverUri != null ? serverUri.hashCode() : 0);
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (connectionTimeoutMillis != null ? connectionTimeoutMillis.hashCode() : 0);
        result = 31 * result + (proxy != null ? proxy.hashCode() : 0);
        return result;
    }
}
