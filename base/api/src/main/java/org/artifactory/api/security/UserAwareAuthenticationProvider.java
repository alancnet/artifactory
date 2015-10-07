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

import org.artifactory.security.UserGroupInfo;

import java.util.List;
import java.util.Set;

/**
 * A an authentication provider that expands on external users, checking, and populating user's groups from an external
 * realm (e.g. LDAP or Crowd)
 *
 * @author Tomer Cohen
 */
public interface UserAwareAuthenticationProvider {

    /**
     * Check if the user exists in a specific realm.
     *
     * @param userName The username to check
     * @param realm    The realm that the user should belong to
     * @return True if the user exists in the realm.
     */
    boolean userExists(String userName, String realm);

    /**
     * Add external groups from an external realm (e.g. LDAP or Crowd) to a user.
     *
     * @param userName The username.
     * @param realm    The realm that the username belongs to.
     * @param groups   The initial set of groups that the user belongs to and that should be populated with the external
     *                 groups.
     */
    void addExternalGroups(String userName, String realm, Set<UserGroupInfo> groups);

    /**
     * @return The list of authentication providers.
     */
    public List getProviders();
}
