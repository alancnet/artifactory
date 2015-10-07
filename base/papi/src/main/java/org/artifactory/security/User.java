/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2013 JFrog Ltd.
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

package org.artifactory.security;

/**
 * Available fields from the user in Artifactory DB,
 * or object provided by authenticate realms override to set user info data.
 */
public interface User {
    String getUsername();

    String getEmail();

    boolean isAdmin();

    boolean isEnabled();

    boolean isUpdatableProfile();

    String getRealm();

    String getPrivateKey();

    String getPublicKey();

    boolean isTransientUser();

    String[] getGroups();

    String getBintrayAuth();

    long getLastLoginTimeMillis();

    String getLastLoginClientIp();

    long getLastAccessTimeMillis();

    String getLastAccessClientIp();

    boolean isAnonymous();

    void setEmail(String email);

    void setAdmin(Boolean admin);

    void setEnabled(Boolean enabled);

    void setUpdatableProfile(Boolean updatableProfile);

    void setPrivateKey(String privateKey);

    void setPublicKey(String publicKey);

    void setBintrayAuth(String bintrayAuth);

    void setGroups(String[] groups);
}
