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

package org.artifactory.security.db;

import org.artifactory.security.SimpleUser;
import org.artifactory.security.UserInfo;
import org.artifactory.storage.security.service.UserGroupStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.dao.SaltSource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;

/**
 * This class provides both the user details and the password salt. Configured in the security.xml.
 *
 * @author freds
 */
@Repository("dbUserDetailsService")
public class DbUserDetailsService implements UserDetailsService, SaltSource {

    @Autowired
    private UserGroupStoreService userGroupStore;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserInfo user = userGroupStore.findUser(username);
        if (user == null) {
            throw new UsernameNotFoundException("User with name '" + username + "' does not exists!");
        }
        return new SimpleUser(user);
    }

    @Override
    public Object getSalt(UserDetails user) {
        return ((SimpleUser) user).getSalt();
    }
}
