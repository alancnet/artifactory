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

package org.artifactory.addon.crowd;

import org.artifactory.addon.Addon;
import org.artifactory.descriptor.security.sso.CrowdSettings;
import org.artifactory.security.UserGroupInfo;
import org.springframework.security.core.Authentication;

import java.util.Set;

/**
 * Allows the SSO addon to behave as a proxy for Crowd authentication.<br> These methods cannot appear in the normal
 * addon interface since they need to be used by components in the core
 *
 * @author Noam Y. Tenne
 */
public interface CrowdAddon extends Addon {

    String REALM = "crowd";

    /**
     * Indicates whether crowd authentication is supported\enabled
     *
     * @param authentication Authentication object for the provider
     * @return True if crowd authentication is supported\enabled
     */
    boolean isCrowdAuthenticationSupported(Class<?> authentication);

    /**
     * Authenticates the request via Crowd
     *
     * @param authentication Authentication to use
     * @return New token with local user details and credentials
     */
    Authentication authenticateCrowd(Authentication authentication);

    /**
     * Find user in Crowd.
     *
     * @param userName The username to find in Crowd
     * @return True if the user exists, false otherwise.
     */
    boolean findUser(String userName);

    /**
     * Add external groups that were brought over from Crowd into the set of groups that the user belongs to. Only
     * groups that were imported into Artifactory from crowd will be added
     *
     * @param userName The username to find the groups for.
     * @param groups   The set of groups that the user belongs to.
     */
    public void addExternalGroups(String userName, Set<UserGroupInfo> groups);

    /**
     * Finds Crowd groups for a certain username, if blank retrieves all groups that are configured in Crowd.
     *
     * @param username             The username for which to get the groups for.
     * @param currentCrowdSettings
     * @return A set of Crowd groups that the user belongs to, if the username is blank, all groups configured in Crowd
     *         will be returned.
     */
    Set<CrowdExtGroup> findCrowdExtGroups(String username, CrowdSettings currentCrowdSettings);

    void testCrowdConnection(CrowdSettings crowdSettings) throws Exception;
}
