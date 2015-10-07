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


import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.CoreAddonsImpl;
import org.artifactory.addon.LdapGroupAddon;
import org.artifactory.descriptor.security.ldap.LdapSetting;
import org.artifactory.descriptor.security.ldap.SearchPattern;
import org.artifactory.spring.InternalArtifactoryContext;
import org.artifactory.spring.InternalContextHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.ldap.NamingException;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.ppolicy.PasswordPolicyControl;
import org.springframework.security.ldap.ppolicy.PasswordPolicyControlExtractor;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import java.util.List;

/**
 * @author freds
 * @date Sep 12, 2008
 */
public class ArtifactoryBindAuthenticator extends BindAuthenticator {
    private static final Logger log = LoggerFactory.getLogger(ArtifactoryBindAuthenticator.class);

    /**
     * The spring context connected to LDAP server
     */
    private LdapContextSource contextSource;

    /**
     * A list of user search that can be used to search and authenticate the user. Used with AD.
     */
    private List<FilterBasedLdapUserSearch> userSearches;

    public ArtifactoryBindAuthenticator(LdapContextSource contextSource, LdapSetting ldapSetting) {
        super(contextSource);
        init(contextSource, ldapSetting);
    }

    public void init(LdapContextSource contextSource, LdapSetting ldapSetting) {
        Assert.notNull(contextSource, "contextSource must not be null.");
        this.contextSource = contextSource;
        boolean hasDnPattern = StringUtils.hasText(ldapSetting.getUserDnPattern());
        SearchPattern search = ldapSetting.getSearch();
        boolean hasSearch = search != null && StringUtils.hasText(search.getSearchFilter());
        Assert.isTrue(hasDnPattern || hasSearch,
                "An Authentication pattern should provide a userDnPattern or a searchFilter (or both)");

        if (hasDnPattern) {
            setUserDnPatterns(new String[]{ldapSetting.getUserDnPattern()});
        }

        if (hasSearch) {
            this.userSearches = getLdapGroupAddon().getLdapUserSearches(contextSource, ldapSetting);
        }
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
    protected LdapContextSource getContextSource() {
        return contextSource;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // Nothing to do, check done at constructor time
    }

    @Override
    public DirContextOperations authenticate(Authentication authentication) {
        DirContextOperations user = null;
        Assert.isInstanceOf(UsernamePasswordAuthenticationToken.class, authentication,
                "Can only process UsernamePasswordAuthenticationToken objects");

        String username = authentication.getName();
        String password = (String) authentication.getCredentials();
        // may have LDAPs that have no password for the users, see RTFACT-3103, RTFACT-3378
        if (!StringUtils.hasText(password)) {
            throw new BadCredentialsException("Empty password used.");
        }

        // If DN patterns are configured, try authenticating with them directly
        for (String dn : getUserDns(username)) {
            user = bindWithDn(dn, username, password);

            if (user != null) {
                break;
            }
        }

        if (user == null && (userSearches != null && !userSearches.isEmpty())) {
            for (FilterBasedLdapUserSearch userSearch : userSearches) {
                try {
                    DirContextOperations userFromSearch = userSearch.searchForUser(username);
                    user = bindWithDn(userFromSearch.getDn().toString(), username, password);
                    if (user != null) {
                        break;
                    }
                } catch (UsernameNotFoundException e) {
                    log.debug("Searching for user {} failed for {}: {}",
                            userSearch, username, e.getMessage());
                } catch (IncorrectResultSizeDataAccessException irsae) {
                    log.error("User: {} found {} times in LDAP server", username, irsae.getActualSize());
                }
            }
        }

        if (user == null) {
            throw new BadCredentialsException(
                    messages.getMessage("BindAuthenticator.badCredentials", "Bad credentials"));
        }

        return user;
    }

    private DirContextOperations bindWithDn(String userDnStr, String username, String password) {
        BaseLdapPathContextSource ctxSource = getContextSource();
        DistinguishedName userDn = new DistinguishedName(userDnStr);
        DistinguishedName fullDn = new DistinguishedName(userDn);
        fullDn.prepend(ctxSource.getBaseLdapPath());

        log.debug("Attempting to bind as " + fullDn);

        DirContext ctx = null;
        try {
            ctx = getContextSource().getContext(fullDn.toString(), password);
            // Check for password policy control
            PasswordPolicyControl ppolicy = PasswordPolicyControlExtractor.extractControl(ctx);

            log.debug("Retrieving attributes...");

            Attributes attrs = ctx.getAttributes(userDn, getUserAttributes());

            DirContextAdapter result = new DirContextAdapter(attrs, userDn, ctxSource.getBaseLdapPath());

            if (ppolicy != null) {
                result.setAttributeValue(ppolicy.getID(), ppolicy);
            }

            return result;
        } catch (NamingException e) {
            // This will be thrown if an invalid user name is used and the method may
            // be called multiple times to try different names, so we trap the exception
            // unless a subclass wishes to implement more specialized behaviour.
            if ((e instanceof org.springframework.ldap.AuthenticationException)
                    || (e instanceof org.springframework.ldap.OperationNotSupportedException)) {
                handleBindException(userDnStr, username, e);
            } else {
                throw e;
            }
        } catch (javax.naming.NamingException e) {
            throw LdapUtils.convertLdapException(e);
        } finally {
            LdapUtils.closeContext(ctx);
        }

        return null;
    }
}
