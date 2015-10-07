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
 * Configuration object for the SAML settings.
 *
 * @author Gidi Shabat
 */
@XmlType(name = "SamlSettingsType",
        propOrder = {"enableIntegration", "loginUrl", "logoutUrl", "certificate", "serviceProviderName", "noAutoUserCreation"},
        namespace = Descriptor.NS)
public class SamlSettings implements Descriptor {

    @XmlElement(defaultValue = "false")
    private boolean enableIntegration = false;

    private String loginUrl;

    private String logoutUrl;

    private String certificate;

    private String serviceProviderName;

    @XmlElement(defaultValue = "true")
    private Boolean noAutoUserCreation = true;


    public boolean isEnableIntegration() {
        return enableIntegration;
    }

    public void setEnableIntegration(boolean enableIntegration) {
        this.enableIntegration = enableIntegration;
    }

    public String getLoginUrl() {
        return loginUrl;
    }

    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    public String getLogoutUrl() {
        return logoutUrl;
    }

    public void setLogoutUrl(String logoutUrl) {
        this.logoutUrl = logoutUrl;
    }

    public String getServiceProviderName() {
        return serviceProviderName;
    }

    public void setServiceProviderName(String serviceProviderName) {
        this.serviceProviderName = serviceProviderName;
    }

    public Boolean getNoAutoUserCreation() {
        return noAutoUserCreation;
    }

    public void setNoAutoUserCreation(Boolean noAutoUserCreation) {
        this.noAutoUserCreation = noAutoUserCreation;
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SamlSettings that = (SamlSettings) o;

        if (enableIntegration != that.enableIntegration) {
            return false;
        }
        if (loginUrl != null ? !loginUrl.equals(that.loginUrl) : that.loginUrl != null) {
            return false;
        }
        if (logoutUrl != null ? !logoutUrl.equals(that.logoutUrl) : that.logoutUrl != null) {
            return false;
        }
        if (certificate != null ? !certificate.equals(that.certificate) : that.certificate != null) {
            return false;
        }
        if (serviceProviderName != null ? !serviceProviderName.equals(that.serviceProviderName) :
                that.serviceProviderName != null) {
            return false;
        }
        if (noAutoUserCreation != null ? !noAutoUserCreation.equals(that.noAutoUserCreation) :
                that.noAutoUserCreation != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = (enableIntegration ? 1 : 0);
        result = 31 * result + (loginUrl != null ? loginUrl.hashCode() : 0);
        result = 31 * result + (logoutUrl != null ? logoutUrl.hashCode() : 0);
        result = 31 * result + (certificate != null ? certificate.hashCode() : 0);
        result = 31 * result + (serviceProviderName != null ? serviceProviderName.hashCode() : 0);
        result = 31 * result + (noAutoUserCreation != null ? noAutoUserCreation.hashCode() : 0);
        return result;
    }


}
