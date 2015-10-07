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

package org.artifactory.security.ldap;

import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.LdapGroupAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.api.security.ldap.LdapService;
import org.artifactory.api.security.ldap.LdapUser;
import org.artifactory.descriptor.security.ldap.LdapSetting;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.security.MutableUserInfo;
import org.artifactory.security.RealmAwareAuthenticationProvider;
import org.artifactory.security.SimpleUser;
import org.artifactory.security.UserGroupInfo;
import org.artifactory.security.UserInfo;
import org.artifactory.spring.InternalContextHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.ldap.AuthenticationException;
import org.springframework.ldap.CommunicationException;
import org.springframework.ldap.NamingException;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.ldap.LdapUtils;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Custom LDAP authentication provider just for creating local users for newly ldap authenticated users.
 *
 * @author Yossi Shaul
 */
public class ArtifactoryLdapAuthenticationProvider implements RealmAwareAuthenticationProvider, MessageSourceAware {
    private static final Logger log = LoggerFactory.getLogger(ArtifactoryLdapAuthenticationProvider.class);

    @Autowired
    private UserGroupService userGroupService;

    @Autowired
    private CentralConfigService centralConfig;

    /**
     * Keep the message source to in initialize LdapAuthenticationProvider when created
     */
    private MessageSource messageSource;

    private Map<String, LdapAuthenticationProvider> ldapAuthenticationProviders = null;

    @Autowired
    private InternalLdapAuthenticator authenticator;

    @Autowired
    private LdapService ldapService;

    @Autowired
    private AddonsManager addonsManager;

    /**
     * Get the LDAP authentication providers, by iterating over all the bind authenticators and putting them in a map of
     * the settings key.
     *
     * @return The LDAP authentication provers
     */
    public Map<String, LdapAuthenticationProvider> getLdapAuthenticationProviders() {
        if (ldapAuthenticationProviders == null) {
            ldapAuthenticationProviders = new HashMap<>();
            Map<String, BindAuthenticator> authMap = authenticator.getAuthenticators();
            for (Map.Entry<String, BindAuthenticator> entry : authMap.entrySet()) {
                LdapAuthenticationProvider ldapAuthenticationProvider =
                        new LdapAuthenticationProvider(entry.getValue());
                if (messageSource != null) {
                    ldapAuthenticationProvider.setMessageSource(messageSource);
                }
                ldapAuthenticationProviders.put(entry.getKey(), ldapAuthenticationProvider);
            }
        }
        return ldapAuthenticationProviders;
    }

    @Override
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
        if (ldapAuthenticationProviders != null) {
            for (LdapAuthenticationProvider ldapAuthenticationProvider : ldapAuthenticationProviders.values()) {
                ldapAuthenticationProvider.setMessageSource(messageSource);
            }
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        if (centralConfig.getDescriptor().getSecurity().isLdapEnabled()) {
            for (LdapAuthenticationProvider ldapAuthenticationProvider : getLdapAuthenticationProviders().values()) {
                if (ldapAuthenticationProvider.supports(authentication)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        String userName = authentication.getName();
        // If it's an anonymous user, don't bother searching for the user.
        if (UserInfo.ANONYMOUS.equals(userName)) {
            return null;
        }

        log.debug("Trying to authenticate user '{}' via ldap.", userName);
        LdapSetting usedLdapSetting = null;
        DirContextOperations user = null;
        AddonsManager addonsManager = InternalContextHelper.get().beanForType(AddonsManager.class);
        LdapGroupAddon ldapGroupAddon = addonsManager.addonByType(LdapGroupAddon.class);
        try {
            RuntimeException authenticationException = null;
            for (Map.Entry<String, BindAuthenticator> entry : authenticator.getAuthenticators().entrySet()) {
                LdapSetting currentLdapSetting =
                        centralConfig.getDescriptor().getSecurity().getLdapSettings(entry.getKey());
                BindAuthenticator bindAuthenticator = entry.getValue();
                try {
                    user = bindAuthenticator.authenticate(authentication);
                    if (user != null) {
                        usedLdapSetting = currentLdapSetting;
                        break;
                    }
                } catch (AuthenticationException e) {
                    authenticationException = e;
                    checkIfBindAndSearchActive(currentLdapSetting, userName);
                } catch (org.springframework.security.core.AuthenticationException e) {
                    authenticationException = e;
                    checkIfBindAndSearchActive(currentLdapSetting, userName);
                } catch (RuntimeException e) {
                    authenticationException = e;
                }
            }
            if (user == null) {
                if (authenticationException != null) {
                    UserInfo userInfo = userGroupService.findUser(userName);
                    if (userInfo != null) {
                        removeUserLdapRelatedGroups(userInfo);
                    }
                    throw authenticationException;
                }
                throw new AuthenticationServiceException(ArtifactoryLdapAuthenticator.LDAP_SERVICE_MISCONFIGURED);
            }

            // user authenticated via ldap
            log.debug("'{}' authenticated successfully by ldap server.", userName);

            //Collect internal groups, and if using external groups add them to the user info
            MutableUserInfo userInfo = InfoFactoryHolder.get()
                    .copyUser(userGroupService
                            .findOrCreateExternalAuthUser(userName, !usedLdapSetting.isAutoCreateUser()));
            userInfo.setRealm(LdapService.REALM);
            String emailAttribute = usedLdapSetting.getEmailAttribute();
            if (StringUtils.isNotBlank(emailAttribute)) {
                String email = user.getStringAttribute(emailAttribute);
                if (StringUtils.isNotBlank(email)) {
                    log.debug("User '{}' has email address '{}'", userName, email);
                    if (StringUtils.isBlank(userInfo.getEmail())) {
                        userInfo.setEmail(email);
                    }
                }
            }

            log.debug("Loading LDAP groups");
            ldapGroupAddon.populateGroups(user, userInfo);
            log.debug("Finished Loading LDAP groups");
            SimpleUser simpleUser = new SimpleUser(userInfo);
            // create new authentication response containing the user and it's authorities
            return new LdapRealmAwareAuthentication(simpleUser, authentication.getCredentials(),
                    simpleUser.getAuthorities());
        } catch (AuthenticationException e) {
            String message = String.format("Failed to authenticate user '%s' via LDAP: %s", userName, e.getMessage());
            log.debug(message);
            throw new AuthenticationServiceException(message, e);
        } catch (CommunicationException ce) {
            String message = String.format("Failed to authenticate user '%s' via LDAP: communication error", userName);
            log.warn(message);
            log.debug(message, ce);
            throw new AuthenticationServiceException(message, ce);
        } catch (org.springframework.security.core.AuthenticationException e) {
            String message = String.format("Failed to authenticate user '%s' via LDAP: %s", userName, e.getMessage());
            log.debug(message);
            throw e;
        } catch (NamingException e) {
            String message = String.format("Failed to locate directory entry for authenticated user: %s",
                    e.getMostSpecificCause().getMessage());
            log.debug(message);
            throw new AuthenticationServiceException(message, e);
        } catch (Exception e) {
            String message = "Unexpected exception in LDAP authentication:";
            log.error(message, e);
            throw new AuthenticationServiceException(message, e);
        } finally {
            LdapUtils.closeContext(user);
        }
    }

    /**
     * remove user ldap related group as user no longer exist in ldap
     *
     * @param userInfo Artifactory User Data
     * @param userName user name
     */
    private void removeUserLdapRelatedGroups(UserInfo userInfo) {
        MutableUserInfo mutableUserInfo = InfoFactoryHolder.get()
                .copyUser(userInfo);
        Set<UserGroupInfo> updateUserGroup = new HashSet<>();
        Set<UserGroupInfo> userGroupInfos = new HashSet<>(mutableUserInfo.getGroups());
        for (Iterator<UserGroupInfo> userGroupInfoIterator = userGroupInfos.iterator(); userGroupInfoIterator.hasNext(); ) {
            UserGroupInfo userGroupInfo = userGroupInfoIterator.next();
            if (!LdapService.REALM.equals(userGroupInfo.getRealm())) {
                updateUserGroup.add(userGroupInfo);
            }
        }
        mutableUserInfo.setGroups(updateUserGroup);
        if (!userInfo.isTransientUser()) {
            userGroupService.updateUser(mutableUserInfo, false);
        }
    }

    private void checkIfBindAndSearchActive(LdapSetting ldapSetting, String userName) {
        if (StringUtils.isNotBlank(ldapSetting.getUserDnPattern()) &&
                ldapSetting.getSearch() != null) {
            log.warn("LDAP authentication failed for '{}'. Note: you have configured direct user binding and " +
                    "manager-based search, which are usually mutually exclusive. For AD leave the User DN Pattern " +
                    "field empty.", userName);
        }
    }

    @Override
    public String getRealm() {
        return LdapService.REALM;
    }

    @Override
    public void addExternalGroups(String username, Set<UserGroupInfo> groups) {
        addonsManager.addonByType(LdapGroupAddon.class).addExternalGroups(username, groups);
    }

    @Override
    public boolean userExists(String username) {
        List<LdapSetting> settings = centralConfig.getMutableDescriptor().getSecurity().getLdapSettings();
        if (settings == null || settings.isEmpty()) {
            log.debug("No LDAP settings defined");
            return false;
        }
        for (LdapSetting setting : settings) {
            if (setting.isEnabled()) {
                log.debug("Trying to find user '{}' with LDAP settings '{}'", username, setting);
                LdapUser ldapUser = ldapService.getDnFromUserName(setting, username);
                if (ldapUser != null) {
                    log.debug("Found user '{}' with LDAP settings '{}'", username, setting);
                    return true;
                }
            }
        }
        return false;
    }
}