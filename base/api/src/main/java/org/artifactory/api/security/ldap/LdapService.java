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

package org.artifactory.api.security.ldap;

import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.descriptor.security.ldap.LdapSetting;

/**
 * LDAP service, which performs basic procedures (e.g. testing LDAP connection) on an LDAP server.
 *
 * @author Tomer Cohen
 */
public interface LdapService {
    /**
     * The realm of LDAP groups.
     */
    String REALM = "ldap";

    /**
     * Tries to connect to an ldap server and returns the result in the status holder. The message in the status holder
     * is meant to be displayed to the user.
     *
     * @param ldapSetting The information for the ldap connection
     * @param username    The username that will be used to test the connection
     * @param password    The password that will be used to test the connection
     * @return StatusHolder with the connection attempt results.
     */
    BasicStatusHolder testLdapConnection(LdapSetting ldapSetting, String username, String password);

    /**
     * Given the username, bring back the LDAP user with the DN.
     *
     * @param ldapSetting The ldap Settings.
     * @param userName    The user name
     * @return The LDAP user with the DN.
     */
    LdapUser getDnFromUserName(LdapSetting ldapSetting, String userName);
}