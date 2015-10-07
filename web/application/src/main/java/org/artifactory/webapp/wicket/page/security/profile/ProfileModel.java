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

package org.artifactory.webapp.wicket.page.security.profile;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;

/**
 * @author Yoav Hakman
 */
public class ProfileModel implements Serializable {

    private String currentPassword;
    private String newPassword;
    private String retypedPassword;
    private String email;
    private String bintrayUsername;
    private String bintrayApiKey;

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getRetypedPassword() {
        return retypedPassword;
    }

    public void setRetypedPassword(String retypedPassword) {
        this.retypedPassword = retypedPassword;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBintrayUsername() {
        return bintrayUsername;
    }

    public void setBintrayUsername(String bintrayUsername) {
        this.bintrayUsername = bintrayUsername;
    }

    public String getBintrayApiKey() {
        return bintrayApiKey;
    }

    public void setBintrayApiKey(String bintrayApiKey) {
        this.bintrayApiKey = bintrayApiKey;
    }

    public void setBintrayAuth(String bintrayAuth) {
        if (StringUtils.isBlank(bintrayAuth)) {
            return;
        }

        String[] split = StringUtils.split(bintrayAuth, ':');
        if (split.length != 2) {
            throw new IllegalArgumentException("Bintray authentication token '" + bintrayAuth
                    + "' should have both username and api key separated with a colon between them.");
        }

        this.bintrayUsername = split[0];
        this.bintrayApiKey = split[1];
    }

    public String getBintrayAuth() {
        if (StringUtils.isNotBlank(bintrayUsername) && StringUtils.isNotBlank(bintrayApiKey)) {
            return bintrayUsername + ":" + bintrayApiKey;
        }

        return null;
    }
}
