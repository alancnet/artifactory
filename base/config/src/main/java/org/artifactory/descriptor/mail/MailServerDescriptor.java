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

package org.artifactory.descriptor.mail;

import org.artifactory.descriptor.Descriptor;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * The descriptor of the mail server configuration
 */
@XmlType(name = "MailServerType", propOrder = {"enabled", "host", "port", "username", "password", "from",
        "subjectPrefix", "tls", "ssl", "artifactoryUrl"}, namespace = Descriptor.NS)
public class MailServerDescriptor implements Descriptor {

    @XmlElement(defaultValue = "true")
    private boolean enabled = true;
    private String host;
    private int port = 25;
    private String username;
    private String password;
    private String from;
    private String subjectPrefix = "[Artifactory]";
    private String artifactoryUrl;
    private boolean tls;
    private boolean ssl;
    public static final String MAIL_BASE_URL_NOT_CONFIGURED_LINK = "/artifactory_url_not_set_in_mail_config";
    public static final String MAIL_BASE_URL_NOT_CONFIGURED_TITLE = "\"Artifactory Url not set in mail config\"";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getSubjectPrefix() {
        return subjectPrefix;
    }

    public void setSubjectPrefix(String subjectPrefix) {
        this.subjectPrefix = subjectPrefix;
    }

    public boolean isTls() {
        return tls;
    }

    public void setTls(boolean tls) {
        this.tls = tls;
    }

    public boolean isSsl() {
        return ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    /**
     * Artifactory URL that will be used <b>exclusively<b/> in <b>EMAILS ONLY!<b/>
     *
     * @return The Artifactory URL.
     */
    public String getArtifactoryUrl() {
        return artifactoryUrl;
    }

    public void setArtifactoryUrl(String artifactoryUrl) {
        this.artifactoryUrl = artifactoryUrl;
    }
}
