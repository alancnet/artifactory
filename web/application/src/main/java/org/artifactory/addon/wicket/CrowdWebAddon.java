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

package org.artifactory.addon.wicket;

import org.artifactory.addon.Addon;
import org.artifactory.common.wicket.model.sitemap.MenuNode;
import org.artifactory.descriptor.security.sso.CrowdSettings;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.rmi.RemoteException;
import java.util.Set;

/**
 * Web interface for the Crowd add-on.
 *
 * @author Yossi Shaul
 */
public interface CrowdWebAddon extends Addon {

    /**
     * Returns the Crowd SSO site map builder menu node
     *
     * @param nodeName Name to give to menu node
     * @return Real menu node if addon is enabled. Disabled if not
     */
    MenuNode getCrowdAddonMenuNode(String nodeName);

    /**
     * Calls for a Crowd connection testing
     *
     * @param crowdSettings Settings to test
     * @throws Exception Thrown if the test failed in any way
     */
    void testCrowdConnection(CrowdSettings crowdSettings) throws Exception;

    /**
     * Finds Crowd groups for a certain username, if blank retrieves all groups that are configured in Crowd.
     *
     * @param username             The username for which to get the groups for.
     * @param currentCrowdSettings
     * @return A set of Crowd groups that the user belongs to, if the username is blank, all groups configured in Crowd
     *         will be returned.
     */
    Set findCrowdGroups(String username, CrowdSettings currentCrowdSettings);

    /**
     * Logoff the crowd authenticated user from the crowd server.
     *
     * @param request  The http request
     * @param response The hyyp response
     */
    void logOffCrowd(HttpServletRequest request, HttpServletResponse response) throws RemoteException;

}
