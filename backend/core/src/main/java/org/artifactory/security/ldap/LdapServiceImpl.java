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
import org.artifactory.addon.CoreAddonsImpl;
import org.artifactory.addon.LdapGroupAddon;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.security.ldap.LdapService;
import org.artifactory.api.security.ldap.LdapUser;
import org.artifactory.descriptor.security.ldap.LdapSetting;
import org.artifactory.descriptor.security.ldap.SearchPattern;
import org.artifactory.spring.InternalArtifactoryContext;
import org.artifactory.spring.InternalContextHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.CommunicationException;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.apache.commons.lang.StringUtils.isBlank;

/**
 * This class tests an ldap connection given a ldap settings.
 *
 * @author Yossi Shaul
 * @author Tomer Cohen
 */
@Service
public class LdapServiceImpl extends AbstractLdapService implements LdapService {
    private static final Logger log = LoggerFactory.getLogger(LdapServiceImpl.class);

    @Override
    public BasicStatusHolder testLdapConnection(LdapSetting ldapSetting, String username, String password) {
        BasicStatusHolder status = new BasicStatusHolder();
        try {
            LdapContextSource securityContext =
                    ArtifactoryLdapAuthenticator.createSecurityContext(ldapSetting);
            ArtifactoryBindAuthenticator authenticator = new ArtifactoryBindAuthenticator(
                    securityContext, ldapSetting);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, password);
            authenticator.authenticate(authentication);
            status.status("Successfully connected and authenticated the test user.", log);
            LdapTemplate ldapTemplate = createLdapTemplate(ldapSetting);
            LdapUser ldapUser = getUserFromLdapSearch(ldapTemplate, username, ldapSetting);
            if (ldapUser == null) {
                status.warn(
                        "LDAP user search failed, LDAP queries concerning users and groups may not be available.",
                        log);
            }
        } catch (Exception e) {
            log.debug("Error while testing LDAP authentication: " + e.getMessage(), e);
            SearchPattern pattern = ldapSetting.getSearch();
            if ((pattern != null && StringUtils.isNotBlank(pattern.getSearchFilter())) &&
                    StringUtils.isNotBlank(ldapSetting.getUserDnPattern())) {
                handleException(e, status, username, true);
            } else {
                handleException(e, status, username, false);
            }
        }
        return status;
    }

    private LdapGroupAddon getLdapGroupAddon() {
        InternalArtifactoryContext context = InternalContextHelper.get();
        if (context != null) {
            AddonsManager addonsManager = context.beanForType(AddonsManager.class);
            return addonsManager.addonByType(LdapGroupAddon.class);
        } else {
            return new CoreAddonsImpl();
        }
    }


    @Override
    @SuppressWarnings({"unchecked"})
    public LdapUser getDnFromUserName(LdapSetting ldapSetting, String userName) {
        if (ldapSetting == null) {
            log.warn("Cannot find user '{}' in LDAP: No LDAP settings defined.", userName);
            return null;
        }
        if (!ldapSetting.isEnabled()) {
            log.warn("Cannot find user '{}' in LDAP: LDAP settings not enabled.", userName);
            return null;
        }
        if (ldapSetting.getSearch() == null || isBlank(ldapSetting.getSearch().getSearchFilter())) {
            log.warn("Cannot find user '{}' in LDAP: No search filter defined.", userName);
            return null;
        }
        LdapTemplate ldapTemplate = createLdapTemplate(ldapSetting);
        return getUserFromLdapSearch(ldapTemplate, userName, ldapSetting);
    }

    public DirContextOperations searchUserInLdap(LdapTemplate ldapTemplate, String userName, LdapSetting settings) {
        if (settings.getSearch() == null) {
            return null;
        }
        DirContextOperations contextOperations = null;
        try {
            log.debug("Searching for user {}", userName);
            List<FilterBasedLdapUserSearch> ldapUserSearches = getLdapGroupAddon().getLdapUserSearches(
                    ldapTemplate.getContextSource(), settings);
            for (FilterBasedLdapUserSearch ldapUserSearch : ldapUserSearches) {
                try {
                    contextOperations = ldapUserSearch.searchForUser(userName);
                } catch (org.springframework.security.core.AuthenticationException e) {
                    log.debug("Failed to retrieve groups user '{}' via LDAP: {}", userName, e.getMessage());
                }
                if (contextOperations != null) {
                    break;
                }
            }
            if (contextOperations != null) {
                // Only DirContextAdapter can be used since the LDAP connection need to be released and we still need
                // read access to this LDAP context.
                if (!(contextOperations instanceof DirContextAdapter)) {
                    throw new ClassCastException(
                            "Cannot use LDAP DirContext class " + contextOperations.getClass().getName() +
                                    " it should be " + DirContextAdapter.class.getName());
                }
                log.debug("Found user {}, has DN: {}", userName, contextOperations.getNameInNamespace());
            }
        } catch (CommunicationException ce) {
            String message =
                    String.format("Failed to retrieve groups for user '%s' via LDAP: communication error.", userName);
            log.warn(message);
        } catch (Exception e) {
            String message = "Unexpected exception in LDAP query:";
            log.debug(message, e);
            log.warn(message + "for user {} vid LDAP: {}", userName, e.getMessage());
        }
        return contextOperations;
    }


    private LdapUser getUserFromLdapSearch(LdapTemplate ldapTemplate, String userName, LdapSetting settings) {
        DirContextOperations contextOperations = searchUserInLdap(ldapTemplate, userName, settings);
        if (contextOperations == null) {
            return null;
        }
        return new LdapUser(userName, contextOperations.getNameInNamespace());
    }
}
