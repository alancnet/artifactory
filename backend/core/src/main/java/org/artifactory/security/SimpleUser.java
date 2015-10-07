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

import com.google.common.collect.Sets;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;

/**
 * Simple user - comparison is done only by user name. This class is immutable and will return new instances when
 * getters are called.
 */
public class SimpleUser implements UserDetails, Comparable {

    public static final Set<GrantedAuthority> USER_GAS =
            Sets.<GrantedAuthority>newHashSet(new SimpleGrantedAuthority(SecurityServiceImpl.ROLE_USER));
    public static final Set<GrantedAuthority> ADMIN_GAS =
            Sets.<GrantedAuthority>newHashSet(new SimpleGrantedAuthority(InternalSecurityService.ROLE_ADMIN),
                    new SimpleGrantedAuthority(SecurityServiceImpl.ROLE_USER));

    private UserInfo userInfo;

    Set<GrantedAuthority> authorities;

    public SimpleUser(UserInfo userInfo) {
        this.userInfo = userInfo;
        authorities = isAdmin() ? ADMIN_GAS : USER_GAS;
    }

    /**
     * @return A new instance of the underlying UserInfo.
     */
    public UserInfo getDescriptor() {
        return userInfo;
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return userInfo.getPassword();
    }

    @Override
    public String getUsername() {
        return userInfo.getUsername();
    }

    public String getSalt() {
        return userInfo.getSalt();
    }

    @Override
    public boolean isAccountNonExpired() {
        return userInfo.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return userInfo.isAccountNonLocked();
    }

    public boolean isTransientUser() {
        return userInfo.isTransientUser();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return userInfo.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return userInfo.isEnabled();
    }

    public boolean isExternal() {
        return userInfo.isExternal();
    }

    public String getEmail() {
        return userInfo.getEmail();
    }

    public boolean isAdmin() {
        return userInfo.isAdmin();
    }

    public boolean isAnonymous() {
        return UserInfo.ANONYMOUS.equals(userInfo.getUsername());
    }

    public boolean isUpdatableProfile() {
        return userInfo.isUpdatableProfile();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SimpleUser user = (SimpleUser) o;
        return getUsername().equals(user.getUsername());

    }

    @Override
    public int hashCode() {
        return getUsername().hashCode();
    }

    @Override
    public String toString() {
        return getUsername();
    }

    @Override
    public int compareTo(Object o) {
        if (o == null || !getClass().isAssignableFrom(o.getClass())) {
            throw new IllegalArgumentException();
        }
        return getUsername().compareTo(((SimpleUser) o).getUsername());
    }
}
