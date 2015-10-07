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

package org.artifactory.api.security;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.util.Builder;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.security.MutableUserInfo;
import org.artifactory.security.SaltedPassword;
import org.artifactory.security.UserGroupInfo;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Builder for user info with sensible defaults.
 *
 * @author Yossi Shaul
 */
public class UserInfoBuilder implements Builder<MutableUserInfo> {

    private final String username;
    private SaltedPassword password = SaltedPassword.INVALID_PASSWORD;
    private String email = "";
    private boolean admin = false;
    private boolean enabled = true;
    private boolean updatableProfile = false;
    private boolean transientUser = false;
    private Set<UserGroupInfo> groups = new HashSet<>();
    private String bintrayAuth;

    public UserInfoBuilder(String username) {
        this.username = username;
    }

    /**
     * @return The user.
     */
    @Override
    public MutableUserInfo build() {
        if (StringUtils.isBlank(username)) {
            throw new IllegalStateException("User must have a username");
        }

        MutableUserInfo user = InfoFactoryHolder.get().createUser();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.setAdmin(admin);
        user.setEnabled(enabled);
        user.setUpdatableProfile(updatableProfile);
        user.setAccountNonExpired(true);
        user.setCredentialsNonExpired(true);
        user.setAccountNonLocked(true);
        user.setTransientUser(transientUser);
        user.setGroups(groups);
        user.setBintrayAuth(bintrayAuth);
        return user;
    }

    public UserInfoBuilder email(String email) {
        this.email = email;
        return this;
    }

    public UserInfoBuilder password(SaltedPassword saltedPassword) {
        this.password = saltedPassword;
        return this;
    }

    public UserInfoBuilder admin(boolean admin) {
        this.admin = admin;
        return this;
    }

    public UserInfoBuilder enabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public UserInfoBuilder updatableProfile(boolean updatableProfile) {
        this.updatableProfile = updatableProfile;
        return this;
    }

    public UserInfoBuilder transientUser() {
        this.transientUser = true;
        return this;
    }

    public UserInfoBuilder internalGroups(Set<String> groupNames) {
        if (groupNames != null) {
            groups(InfoFactoryHolder.get().createGroups(groupNames));
        } else {
            groups(null);
        }
        return this;
    }

    public UserInfoBuilder groups(@Nullable Set<UserGroupInfo> groups) {
        if (groups != null) {
            this.groups = groups;
        } else {
            this.groups = Collections.emptySet();
        }
        return this;
    }

    public UserInfoBuilder bintrayAuth(String bintrayAuth) {
        this.bintrayAuth = bintrayAuth;
        return this;
    }
}
