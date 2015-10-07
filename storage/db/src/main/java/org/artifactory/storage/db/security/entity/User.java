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

package org.artifactory.storage.db.security.entity;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang.StringUtils;

import java.util.Set;

/**
 * Case class for User in DB security entity
 *
 * @author freds
 */
public class User {
    private final long userId;
    private final String username;
    private final String password;
    private final String salt;
    private final String email;
    private final String genPasswordKey;
    private final String bintrayAuth;
    private final boolean admin;
    private final boolean enabled;
    private final boolean updatableProfile;

    private final String realm;
    private final String privateKey;
    private final String publicKey;

    private final long lastLoginTimeMillis;
    private final String lastLoginClientIp;

    private final long lastAccessTimeMillis;
    private final String lastAccessClientIp;

    /**
     * Initialized as null, and can (and should) be set only once
     */
    private ImmutableSet<UserGroup> groups = null;


    public User(long userId, String username, String password, String salt, String email, boolean admin,
            boolean enabled, boolean updatableProfile, String bintrayAuth) {
        this(userId, username, password, salt, email, null, admin, enabled, updatableProfile,
                null, null, null, 0L, null, 0L, null, bintrayAuth);
    }

    public User(long userId, String username,
            String password, String salt, String email, String genPasswordKey,
            boolean admin, boolean enabled, boolean updatableProfile,
            String realm, String privateKey, String publicKey,
            long lastLoginTimeMillis, String lastLoginClientIp, long lastAccessTimeMillis, String lastAccessClientIp,
            String bintrayAuth) {
        if (userId <= 0L) {
            throw new IllegalArgumentException("User id cannot be zero or negative!");
        }
        if (StringUtils.isBlank(username)) {
            throw new IllegalArgumentException("User name cannot be null!");
        }
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.salt = salt;
        this.email = email;
        this.genPasswordKey = genPasswordKey;
        this.bintrayAuth = bintrayAuth;
        this.admin = admin;
        this.enabled = enabled;
        this.updatableProfile = updatableProfile;
        this.realm = realm;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.lastLoginTimeMillis = lastLoginTimeMillis;
        this.lastLoginClientIp = lastLoginClientIp;
        this.lastAccessTimeMillis = lastAccessTimeMillis;
        this.lastAccessClientIp = lastAccessClientIp;
    }

    public long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getSalt() {
        return salt;
    }

    public String getEmail() {
        return email;
    }

    public String getGenPasswordKey() {
        return genPasswordKey;
    }

    public String getBintrayAuth() {
        return bintrayAuth;
    }

    public boolean isAdmin() {
        return admin;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isUpdatableProfile() {
        return updatableProfile;
    }

    public String getRealm() {
        return realm;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public long getLastLoginTimeMillis() {
        return lastLoginTimeMillis;
    }

    public String getLastLoginClientIp() {
        return lastLoginClientIp;
    }

    public long getLastAccessTimeMillis() {
        return lastAccessTimeMillis;
    }

    public String getLastAccessClientIp() {
        return lastAccessClientIp;
    }

    public ImmutableSet<UserGroup> getGroups() {
        if (groups == null) {
            throw new IllegalStateException("User object was not initialized correctly! Groups missing.");
        }
        return groups;
    }

    public void setGroups(Set<UserGroup> groups) {
        if (this.groups != null) {
            throw new IllegalStateException("Cannot set groups already set!");
        }
        if (groups == null) {
            throw new IllegalArgumentException("Cannot set groups to null");
        }
        for (UserGroup group : groups) {
            if (group.getUserId() != userId) {
                throw new IllegalArgumentException("Cannot add group link " + group
                        + " to user id=" + userId + " name=" + username + "!\n"
                        + "User IDs do not match");
            }
        }
        this.groups = ImmutableSet.copyOf(groups);
    }

    public boolean isIdentical(User user) {
        if (this == user) {
            return true;
        }
        if (user == null || getClass() != user.getClass()) {
            return false;
        }

        if (!username.equals(user.username)) {
            return false;
        }
        if (admin != user.admin || enabled != user.enabled
                || lastAccessTimeMillis != user.lastAccessTimeMillis
                || lastLoginTimeMillis != user.lastLoginTimeMillis
                || updatableProfile != user.updatableProfile) {
            return false;
        }
        if (email != null ? !email.equals(user.email) : user.email != null) {
            return false;
        }
        if (genPasswordKey != null ? !genPasswordKey.equals(user.genPasswordKey) : user.genPasswordKey != null) {
            return false;
        }
        if (groups != null ? !groups.equals(user.groups) : user.groups != null) {
            return false;
        }
        if (lastAccessClientIp != null ? !lastAccessClientIp.equals(user.lastAccessClientIp) :
                user.lastAccessClientIp != null) {
            return false;
        }
        if (lastLoginClientIp != null ? !lastLoginClientIp.equals(user.lastLoginClientIp) :
                user.lastLoginClientIp != null) {
            return false;
        }
        if (password != null ? !password.equals(user.password) : user.password != null) {
            return false;
        }
        if (privateKey != null ? !privateKey.equals(user.privateKey) : user.privateKey != null) {
            return false;
        }
        if (publicKey != null ? !publicKey.equals(user.publicKey) : user.publicKey != null) {
            return false;
        }
        if (realm != null ? !realm.equals(user.realm) : user.realm != null) {
            return false;
        }
        if (salt != null ? !salt.equals(user.salt) : user.salt != null) {
            return false;
        }
        if (bintrayAuth != null ? !bintrayAuth.equals(user.bintrayAuth) : user.bintrayAuth != null) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", salt='" + salt + '\'' +
                ", email='" + email + '\'' +
                ", genPasswordKey='" + genPasswordKey + '\'' +
                ", admin=" + admin +
                ", enabled=" + enabled +
                ", updatableProfile=" + updatableProfile +
                ", realm='" + realm + '\'' +
                ", privateKey='" + privateKey + '\'' +
                ", publicKey='" + publicKey + '\'' +
                ", lastLoginTimeMillis=" + lastLoginTimeMillis +
                ", lastLoginClientIp='" + lastLoginClientIp + '\'' +
                ", lastAccessTimeMillis=" + lastAccessTimeMillis +
                ", lastAccessClientIp='" + lastAccessClientIp + '\'' +
                ", bintrayAuth='" + bintrayAuth + '\'' +
                ", groups=" + groups +
                '}';
    }
}
