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

import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.config.ImportableExportable;
import org.artifactory.api.repo.Async;
import org.artifactory.descriptor.security.ldap.LdapSetting;
import org.artifactory.sapi.common.Lock;
import org.artifactory.security.SaltedPassword;
import org.artifactory.security.SecurityInfo;
import org.artifactory.util.SerializablePair;

import javax.annotation.Nullable;
import java.util.Date;

/**
 * User: freds Date: Aug 13, 2008 Time: 5:17:47 PM
 */
public interface SecurityService extends ImportableExportable {
    String FILE_NAME = "security.xml";

    String DEFAULT_ADMIN_USER = "admin";

    String DEFAULT_ADMIN_PASSWORD = "password";

    String USER_SYSTEM = "_system_";

    SecurityInfo getSecurityData();

    @Lock
    void importSecurityData(String securityXml);

    @Lock
    void importSecurityData(SecurityInfo descriptor);

    /**
     * @see org.artifactory.security.ldap.LdapConnectionTester#testLdapConnection
     */
    BasicStatusHolder testLdapConnection(LdapSetting ldapSetting, String username, String password);

    /**
     * @return True if password encryption is enabled (supported or required).
     */
    public boolean isPasswordEncryptionEnabled();

    /**
     * @return True if the password matches to the password of the currently logged-in user.
     */
    public boolean userPasswordMatches(String passwordToCheck);

    /**
     * Generates a password recovery key for the specified user and send it by mail
     *
     * @param username     User to rest his password
     * @param clientIp     The IP of the client that sent the request
     * @param resetPageUrl The URL of the reset page we refer to
     */
    @Async(transactional = true)
    void generatePasswordResetKey(String username, String clientIp, String resetPageUrl) throws Exception;

    /**
     * Returns a pair object with the given users password reset key info. If user doesn't exist, a
     * UsernameNotFoundException will be thrown. When the user is not associated with a key, return a null object. In a
     * case where the key is invalid (has less than 3 parts), an IllegalArgumentException is thrown
     *
     * @param username User to retrieve password reset info about
     * @return Pair<Date, String> - Pair containing key generation time and client ip (respectively)
     */
    SerializablePair<Date, String> getPasswordResetKeyInfo(String username);

    /**
     * Returns the given user's last login information
     *
     * @param username Logged in user's name
     * @return Pair<String, Long> - Containing the client IP and last logged in time millis
     */
    SerializablePair<String, Long> getUserLastLoginInfo(String username);

    /**
     * Updates the user last login information
     *
     * @param username        Logged in user's name
     * @param clientIp        The IP of the client that was logged in from
     * @param loginTimeMillis The time of login
     */
    @Async(transactional = true)
    void updateUserLastLogin(String username, String clientIp, long loginTimeMillis);

    /**
     * Returns the given user's last access information
     *
     * @param username Name of user that performed an action
     * @return Pair<String, Long> - Containing the client IP and last access in time millis
     */
    SerializablePair<String, Long> getUserLastAccessInfo(String username);

    /**
     * Updates the user last access information
     *
     * @param username                     Name of user that performed an action
     * @param clientIp                     The IP of the client that has accessed
     * @param accessTimeMillis             The time of access
     * @param acessUpdatesResolutionMillis The ferquency in which to update access times
     */
    @Async(transactional = true)
    void updateUserLastAccess(String username, String clientIp, long accessTimeMillis,
            long acessUpdatesResolutionMillis);

    /**
     * Indicates if Artifactory is configured as proxied by Apache
     *
     * @return True if is proxied. False if not
     */
    boolean isHttpSsoProxied();

    /**
     * Returns the HTTP SSO remote user request variable
     *
     * @return Remote user request variable
     */
    String getHttpSsoRemoteUserRequestVariable();

    /**
     * Indicates if artifactory shouldn't automatically create a user object in the DB for an SSO authenticated user
     *
     * @return True if user should be created in memory. False if user should be created in the DB
     */
    boolean isNoHttpSsoAutoUserCreation();

    void addListener(SecurityListener listener);

    void removeListener(SecurityListener listener);

    void authenticateAsSystem();

    void nullifyContext();

    SaltedPassword generateSaltedPassword(String rawPassword);

    SaltedPassword generateSaltedPassword(String rawPassword, @Nullable String salt);

    String getDefaultSalt();

}
