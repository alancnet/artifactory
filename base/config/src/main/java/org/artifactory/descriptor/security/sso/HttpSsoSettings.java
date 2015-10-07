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

package org.artifactory.descriptor.security.sso;

import org.artifactory.descriptor.Descriptor;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * The HTTP SSO related settings
 *
 * @author Noam Y. Tenne
 */
@XmlType(name = "HttpSsoSettingsType",
        propOrder = {"httpSsoProxied", "noAutoUserCreation", "remoteUserRequestVariable"}, namespace = Descriptor.NS)
public class HttpSsoSettings implements Descriptor {

    @XmlElement(defaultValue = "false")
    private boolean httpSsoProxied = false;

    @XmlElement(defaultValue = "false")
    private boolean noAutoUserCreation = false;

    @XmlElement(defaultValue = "REMOTE_USER")
    private String remoteUserRequestVariable = "REMOTE_USER";

    public boolean isHttpSsoProxied() {
        return httpSsoProxied;
    }

    public void setHttpSsoProxied(boolean httpSsoProxied) {
        this.httpSsoProxied = httpSsoProxied;
    }

    public boolean isNoAutoUserCreation() {
        return noAutoUserCreation;
    }

    public void setNoAutoUserCreation(boolean noAutoUserCreation) {
        this.noAutoUserCreation = noAutoUserCreation;
    }

    public String getRemoteUserRequestVariable() {
        return remoteUserRequestVariable;
    }

    public void setRemoteUserRequestVariable(String remoteUserRequestVariable) {
        this.remoteUserRequestVariable = remoteUserRequestVariable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HttpSsoSettings)) {
            return false;
        }

        HttpSsoSettings settings = (HttpSsoSettings) o;

        if (noAutoUserCreation != settings.noAutoUserCreation) {
            return false;
        }
        if (httpSsoProxied != settings.httpSsoProxied) {
            return false;
        }
        if (remoteUserRequestVariable != null ? !remoteUserRequestVariable.equals(settings.remoteUserRequestVariable) :
                settings.remoteUserRequestVariable != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = (httpSsoProxied ? 1 : 0);
        result = 31 * result + (noAutoUserCreation ? 1 : 0);
        result = 31 * result + (remoteUserRequestVariable != null ? remoteUserRequestVariable.hashCode() : 0);
        return result;
    }
}