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

import org.springframework.security.authentication.AuthenticationProvider;

import java.util.Set;

/**
 * A realm-aware auth providers.
 *
 * @author Tomer Cohen
 */
public interface RealmAwareAuthenticationProvider extends AuthenticationProvider {
    /**
     * @return The realm of the authentication provider.
     */
    String getRealm();

    /**
     * Add the external groups to the user by the specific realm that the user came from. If the user came from LDAP
     * then it will be populated by LDAP groups, same for Crowd.
     *
     * @param username The username for which to populate in the realm
     * @param groups   The initial groups that belongs to the username, and that the external groups should be added
     *                 to.
     */
    public void addExternalGroups(String username, Set<UserGroupInfo> groups);

    /**
     * @param userName The username to check that it exists.
     * @return True if the user exists in the realm.
     */
    boolean userExists(String userName);
}
