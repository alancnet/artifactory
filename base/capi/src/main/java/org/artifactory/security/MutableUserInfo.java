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

package org.artifactory.security;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * Date: 8/2/11
 * Time: 10:49 AM
 *
 * @author Fred Simon
 */
public interface MutableUserInfo extends UserInfo {
    /**
     * Users with invalid password can only authenticate externally
     */
    String INVALID_PASSWORD = "";

    void setUsername(String username);

    void setPassword(SaltedPassword password);

    void setEmail(String email);

    void setPrivateKey(String privateKey);

    void setPublicKey(String publicKey);

    void setGenPasswordKey(@Nullable String genPasswordKey);

    void setAdmin(boolean admin);

    void setEnabled(boolean enabled);

    void setUpdatableProfile(boolean updatableProfile);

    void setAccountNonExpired(boolean accountNonExpired);

    void setAccountNonLocked(boolean accountNonLocked);

    void setTransientUser(boolean transientUser);

    void setRealm(String realm);

    void setCredentialsNonExpired(boolean credentialsNonExpired);

    void addGroup(String groupName);

    void addGroup(String groupName, String realm);

    void removeGroup(String groupName);

    void setGroups(Set<UserGroupInfo> groups);

    void setInternalGroups(Set<String> groups);

    void setLastLoginTimeMillis(long lastLoginTimeMillis);

    void setLastLoginClientIp(String lastLoginClientIp);

    void setLastAccessTimeMillis(long lastAccessTimeMillis);

    void setLastAccessClientIp(String lastAccessClientIp);

    void setBintrayAuth(String bintrayAuth);
}
