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

package org.artifactory.api.mail;

/**
 * Contains the different parameters of the mail server configuration
 *
 * @author Noam Tenne
 */
public class MailServerConfiguration {

    private boolean enabled;
    private String host;
    private int port;
    private String username;
    private String password;
    private String from;
    private String subjectPrefix;
    private String artifactoryUrl;
    private boolean useTls;
    private boolean useSsl;

    public MailServerConfiguration(boolean enabled, String host, int port, String username, String password,
            String from, String subjectPrefix, boolean useTls, boolean useSsl, String artifactoryUrl) {
        this.enabled = enabled;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.from = from;
        this.subjectPrefix = subjectPrefix;
        this.useTls = useTls;
        this.useSsl = useSsl;
        this.artifactoryUrl = artifactoryUrl;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getFrom() {
        return from;
    }

    public String getSubjectPrefix() {
        return subjectPrefix;
    }

    public boolean isUseTls() {
        return useTls;
    }

    public boolean isUseSsl() {
        return useSsl;
    }

    public String getArtifactoryUrl() {
        return artifactoryUrl;
    }
}
