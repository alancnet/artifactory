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

import com.google.common.collect.Maps;
import org.apache.commons.lang.ObjectUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.LdapGroupAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.config.InternalCentralConfigService;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.security.ldap.LdapSetting;
import org.artifactory.descriptor.security.ldap.SearchPattern;
import org.artifactory.security.crypto.CryptoHelper;
import org.artifactory.spring.Reloadable;
import org.artifactory.util.PathUtils;
import org.artifactory.version.CompoundVersionDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.stereotype.Component;

import javax.naming.Context;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wrapper for the LDAP bind authenticator. Used to authenticate users against ldap and as a factory for the security
 * context and actual authenticator.
 *
 * @author Yossi Shaul
 */
@Reloadable(beanClass = InternalLdapAuthenticator.class, initAfter = InternalCentralConfigService.class)
@Component
public class ArtifactoryLdapAuthenticator implements InternalLdapAuthenticator {
    private static final Logger log = LoggerFactory.getLogger(ArtifactoryLdapAuthenticator.class);

    private static final String NO_LDAP_SERVICE_CONFIGURED = "No LDAP service configured";
    public static final String LDAP_SERVICE_MISCONFIGURED = "LDAP service misconfigured";

    @Autowired
    private CentralConfigService centralConfig;

    private Map<String, BindAuthenticator> authenticators;

    @Override
    public void init() {
        try {
            authenticators = createBindAuthenticators();
            if (authenticators.isEmpty()) {
                authenticators = null;
            }
        } catch (Exception e) {
            log.error("Failed to create LDAP authenticator. Please verify and fix your LDAP settings.", e);
        }
        if (authenticators == null) {
            log.debug("LDAP service is disabled");
        }
    }

    @Override
    public void reload(CentralConfigDescriptor oldDescriptor) {
        if (!centralConfig.getDescriptor().getSecurity().equals(oldDescriptor.getSecurity())
                || !ObjectUtils.equals(centralConfig.getDescriptor().getDefaultProxy(), oldDescriptor.getDefaultProxy())
                || authenticators == null) {
            authenticators = null;
            init();
        }
    }

    @Override
    public void destroy() {
    }

    @Override
    public void convert(CompoundVersionDetails source, CompoundVersionDetails target) {
    }

    @Override
    public Map<String, BindAuthenticator> getAuthenticators() {
        return authenticators;
    }

    private Map<String, BindAuthenticator> createBindAuthenticators() {
        Map<String, BindAuthenticator> result = Maps.newLinkedHashMap();
        LdapGroupAddon groupAddon = ContextHelper.get().beanForType(AddonsManager.class).addonByType(
                LdapGroupAddon.class);
        List<LdapSetting> ldapSettings = groupAddon.getEnabledLdapSettings();
        for (LdapSetting ldapSetting : ldapSettings) {
            LdapContextSource contextSource = createSecurityContext(ldapSetting);
            ArtifactoryBindAuthenticator bindAuthenticator =
                    new ArtifactoryBindAuthenticator(contextSource, ldapSetting);
            result.put(ldapSetting.getKey(), bindAuthenticator);
        }
        return result;
    }

    static LdapContextSource createSecurityContext(LdapSetting ldapSetting) {
        String url = ldapSetting.getLdapUrl();
        String scheme = getLdapScheme(url);
        String baseUrl = getLdapBaseUrl(scheme, url);
        DefaultSpringSecurityContextSource contextSource = new DefaultSpringSecurityContextSource(scheme + baseUrl);
        contextSource.setBase(adjustBase(url.substring((scheme + baseUrl).length())));

        // set default connection timeout, read timeout and referral strategy.
        HashMap<String, String> env = new HashMap<>();
        String connectTimeout = ArtifactoryHome.get().getArtifactoryProperties().getProperty(
                "artifactory.security.ldap.connect.timeoutMillis", "10000");
        env.put("com.sun.jndi.ldap.connect.timeout",connectTimeout);
        String readTimeout = ArtifactoryHome.get().getArtifactoryProperties().getProperty(
                "artifactory.security.ldap.socket.timeoutMillis", "15000");
            env.put("com.sun.jndi.ldap.read.timeout", readTimeout);
        String referralStrategy = ArtifactoryHome.get().getArtifactoryProperties().getProperty(
                "artifactory.security.ldap.referralStrategy", "follow");
        env.put(Context.REFERRAL, referralStrategy);
        String poolIdleTimeout = ArtifactoryHome.get().getArtifactoryProperties().getProperty(
                "artifactory.security.ldap.pool.timeoutMillis", null);
        if (poolIdleTimeout != null) {
            env.put("com.sun.jndi.ldap.connect.pool.timeout", poolIdleTimeout);
        }

        contextSource.setBaseEnvironmentProperties(env);
        SearchPattern searchPattern = ldapSetting.getSearch();
        if (searchPattern != null) {
            if (PathUtils.hasText(searchPattern.getManagerDn())) {
                contextSource.setUserDn(searchPattern.getManagerDn());
                contextSource.setPassword(CryptoHelper.decryptIfNeeded(searchPattern.getManagerPassword()));
            } else {
                contextSource.setAnonymousReadOnly(true);
            }
        }

        try {
            contextSource.afterPropertiesSet();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return contextSource;
    }

    private static String getLdapBaseUrl(String scheme, String url) {
        url = url.substring(scheme.length());
        int index = url.indexOf('/');
        if (index != -1) {
            return url.substring(0, index);
        } else {
            return url;
        }
    }

    private static String getLdapScheme(String url) {
        String scheme = url.substring(0, url.indexOf("//") + 2);
        return scheme.substring(0, url.indexOf('/')) + "//";
    }

    /**
     * Adjust the base {@link org.springframework.ldap.core.DistinguishedName} of the {@link LdapContextSource} which is
     * space escaped
     *
     * @param url The entire LDAP url
     * @return The escaped base DN
     */
    private static String adjustBase(String url) {
        url = url.replace(" ", "%20");
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Could not parse LDAP URL.", e);
        }
        String base = uri.getPath();
        if (base.indexOf('/') == 0) {
            return base.substring(1);
        } else {
            return base;
        }
    }

    @Override
    public DirContextOperations authenticate(Authentication authentication) {
        //Spring expects an exception on failed authentication
        if (authenticators != null && centralConfig.getDescriptor().getSecurity().isLdapEnabled()) {
            RuntimeException authenticationException = null;
            for (BindAuthenticator authenticator : authenticators.values()) {
                DirContextOperations user = null;
                try {
                    user = authenticator.authenticate(authentication);
                } catch (RuntimeException e) {
                    authenticationException = e;
                }
                if (user != null) {
                    return user;
                }
            }
            if (authenticationException != null) {
                throw authenticationException;
            }
            throw new AuthenticationServiceException(LDAP_SERVICE_MISCONFIGURED);
        } else {
            throw new AuthenticationServiceException(NO_LDAP_SERVICE_CONFIGURED);
        }
    }
}
