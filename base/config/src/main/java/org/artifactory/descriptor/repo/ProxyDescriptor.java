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

package org.artifactory.descriptor.repo;

import org.apache.commons.lang.StringUtils;
import org.artifactory.descriptor.Descriptor;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "ProxyType",
        propOrder = {"key", "host", "port", "username", "password", "ntHost", "domain", "defaultProxy", "redirectedToHosts"})
public class ProxyDescriptor implements Descriptor {

    @XmlID
    @XmlElement(required = true)
    private String key;

    @XmlElement(required = true)
    private String host;

    @XmlElement(required = true)
    private int port;

    private String username;
    private String password;
    private String ntHost;
    private String domain;
    private boolean defaultProxy;
    /**
     * New line or comma separated host names that the proxy might redirect requests to.
     */
    private String redirectedToHosts;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
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

    public String getNtHost() {
        return ntHost;
    }

    public void setNtHost(String ntHost) {
        this.ntHost = ntHost;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public boolean isDefaultProxy() {
        return defaultProxy;
    }

    public void setDefaultProxy(boolean defaultProxy) {
        this.defaultProxy = defaultProxy;
    }

    /**
     * Returns new line or comma separated host names that the proxy might redirect requests to.
     * Use {@link ProxyDescriptor#getRedirectedToHostsList()} to get a list of host names.
     */
    @Nullable
    public String getRedirectedToHosts() {
        return redirectedToHosts;
    }

    @Nullable
    public String[] getRedirectedToHostsList() {
        // split by newline, space, comma or semi-colon
        return StringUtils.split(redirectedToHosts, "\n,; ");
    }

    public void setRedirectedToHosts(@Nullable String redirectedToHosts) {
        this.redirectedToHosts = StringUtils.trim(redirectedToHosts);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProxyDescriptor that = (ProxyDescriptor) o;
        return key.equals(that.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }
}
