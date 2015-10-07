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

package org.artifactory.webapp.servlet;

import com.google.common.cache.CacheBuilder;
import org.apache.commons.codec.digest.DigestUtils;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.security.SecurityListener;
import org.artifactory.api.security.SecurityService;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.property.ArtifactorySystemProperties;
import org.artifactory.security.HttpAuthenticationDetailsSource;
import org.artifactory.security.UserInfo;
import org.artifactory.util.HttpUtils;
import org.artifactory.webapp.servlet.authentication.ArtifactoryAuthenticationFilter;
import org.artifactory.webapp.servlet.authentication.ArtifactoryAuthenticationFilterChain;
import org.artifactory.webapp.servlet.authentication.interceptor.anonymous.AnonymousAuthenticationInterceptor;
import org.artifactory.webapp.servlet.authentication.interceptor.anonymous.AnonymousAuthenticationInterceptors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public class AccessFilter extends DelayedFilterBase implements SecurityListener {
    private static final Logger log = LoggerFactory.getLogger(AccessFilter.class);

    private ArtifactoryContext context;
    private ArtifactoryAuthenticationFilter authFilter;
    private AnonymousAuthenticationInterceptors authInterceptors;

    /**
     * holds cached Authentication instances for the non ui requests based on the Authorization header and client ip
     */
    private ConcurrentMap<AuthCacheKey, Authentication> nonUiAuthCache;
    private ConcurrentMap<String, AuthenticationCache> userChangedCache;

    @Override
    public void initLater(FilterConfig filterConfig) throws ServletException {
        ServletContext servletContext = filterConfig.getServletContext();
        this.context = RequestUtils.getArtifactoryContext(servletContext);
        ArtifactoryAuthenticationFilterChain filterChain = new ArtifactoryAuthenticationFilterChain();
        //Add all the authentication filters
        //TODO: [by yl] Support ordering...
        filterChain.addFilters(context.beansForType(ArtifactoryAuthenticationFilter.class).values());
        authFilter = filterChain;
        initCaches(filterConfig);
        authFilter.init(filterConfig);
        authInterceptors = new AnonymousAuthenticationInterceptors();
        authInterceptors.addInterceptors(context.beansForType(AnonymousAuthenticationInterceptor.class).values());
    }

    private void initCaches(FilterConfig filterConfig) {
        ArtifactorySystemProperties properties =
                ((ArtifactoryHome) filterConfig.getServletContext().getAttribute(ArtifactoryHome.SERVLET_CTX_ATTR))
                        .getArtifactoryProperties();
        ConstantValues idleTimeSecsProp = ConstantValues.securityAuthenticationCacheIdleTimeSecs;
        long cacheIdleSecs = properties.getLongProperty(idleTimeSecsProp);
        ConstantValues initSizeProp = ConstantValues.securityAuthenticationCacheInitSize;
        long initSize = properties.getLongProperty(initSizeProp);
        nonUiAuthCache = CacheBuilder.newBuilder().softValues()
                .initialCapacity((int) initSize)
                .expireAfterWrite(cacheIdleSecs, TimeUnit.SECONDS)
                .<AuthCacheKey, Authentication>build().asMap();
        userChangedCache = CacheBuilder.newBuilder().softValues()
                .initialCapacity((int) initSize)
                .expireAfterWrite(cacheIdleSecs, TimeUnit.SECONDS)
                .<String, AuthenticationCache>build().asMap();
        SecurityService securityService = context.beanForType(SecurityService.class);
        securityService.addListener(this);
    }

    @Override
    public void onClearSecurity() {
        nonUiAuthCache.clear();
        userChangedCache.clear();
    }

    @Override
    public void onUserUpdate(String username) {
        invalidateUserAuthCache(username);
    }

    @Override
    public void onUserDelete(String username) {
        invalidateUserAuthCache(username);
    }

    private void invalidateUserAuthCache(String username) {
        // Flag change to force re-login
        AuthenticationCache authenticationCache = userChangedCache.get(username);
        if (authenticationCache != null) {
            authenticationCache.changed();
        }
    }

    @Override
    public void destroy() {
        //May not be inited yet
        if (authFilter != null) {
            authFilter.destroy();
        }
        if (nonUiAuthCache != null) {
            nonUiAuthCache.clear();
            nonUiAuthCache = null;
        }
        if (userChangedCache != null) {
            userChangedCache.clear();
            userChangedCache = null;
        }
    }

    @Override
    public void doFilter(final ServletRequest req, final ServletResponse resp, final FilterChain chain)
            throws IOException, ServletException {
        doFilterInternal((HttpServletRequest) req, ((HttpServletResponse) resp), chain);
    }

    private void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        final String servletPath = RequestUtils.getServletPathFromRequest(request);
        // add no cache header to web app request
        RequestUtils.addNoCacheToWebAppRequest(servletPath, response);
        String method = request.getMethod();
        if ((servletPath == null || "/".equals(servletPath) || servletPath.length() == 0) &&
                "get".equalsIgnoreCase(method)) {
            //We were called with an empty path - redirect to the app main page
            response.sendRedirect(HttpUtils.ANGULAR_WEBAPP + "/");
            return;
        }
        //Reuse the authentication if it exists
        Authentication authentication = RequestUtils.getAuthentication(request);
        boolean isAuthenticated = authentication != null && authentication.isAuthenticated();
        // Make sure this is called only once
        boolean reAuthRequired = reAuthenticationRequired(request, authentication);
        if (reAuthRequired) {
            /**
             * A re-authentication is required but we might still have data that needs to be invalidated (like the
             * Wicket session)
             */
            Map<String, LogoutHandler> logoutHandlers = ContextHelper.get().beansForType(LogoutHandler.class);
            for (LogoutHandler logoutHandler : logoutHandlers.values()) {
                logoutHandler.logout(request, response, authentication);
            }
        }
        boolean authenticationRequired = !isAuthenticated || reAuthRequired;
        SecurityContext securityContext = SecurityContextHolder.getContext();
        if (authenticationRequired) {
            if (authFilter.acceptFilter(request)) {
                authenticateAndExecute(request, response, chain, securityContext);
            } else {
                useAnonymousIfPossible(request, response, chain, securityContext);
            }
        } else {
            log.debug("Using authentication {} from Http session.", authentication);
            useAuthentication(request, response, chain, authentication, securityContext);
        }
    }

    /**
     * check if angular routing pattern match
     * @param servletPath - servlet path
     * @param method - method type
     * @return if true match angular pattern
     */
    private boolean isAngularRoutingPatternMatch(String servletPath, String method) {
        return servletPath.startsWith("/web/app/") && "get".equalsIgnoreCase(method);
    }

    private boolean reAuthenticationRequired(HttpServletRequest request, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            // Not authenticated so not required to redo ;-)
            return false;
        }
        // If user changed force re-auth
        String username = authentication.getName();
        AuthenticationCache authenticationCache = userChangedCache.get(username);
        if (authenticationCache != null && authenticationCache.isChanged(authentication)) {
            authenticationCache.loggedOut(authentication);
            return true;
        }
        return authFilter.requiresReAuthentication(request, authentication);
    }


    private void authenticateAndExecute(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain, SecurityContext securityContext)
    throws IOException, ServletException {
        // Try to see if authentication in cache based on the hashed header and client ip
        Authentication authentication = getNonUiCachedAuthentication(request);
        if (authentication != null && authentication.isAuthenticated()
                && !reAuthenticationRequired(request, authentication)) {
            log.debug("Header authentication {} found in cache.", authentication);
            useAuthentication(request, response, chain, authentication, securityContext);
            // Add to user change cache the login state
            addToUserChange(authentication);
            return;
        }
        try {
            authFilter.doFilter(request, response, chain);
        } finally {
            Authentication newAuthentication = securityContext.getAuthentication();
            if (newAuthentication != null && newAuthentication.isAuthenticated()) {
                // Add to user change cache the login state
                addToUserChange(newAuthentication);
                // Save authentication like in Wicket Session (if session exists)
                if (RequestUtils.setAuthentication(request, newAuthentication, false)) {
                    log.debug("Added authentication {} in Http session.", newAuthentication);
                } else {
                    // If it did not work use the header cache
                    // An authorization cache key with no header can only be used for Anonymous authentication
                    AuthCacheKey authCacheKey = new AuthCacheKey(
                            authFilter.getCacheKey(request), request.getRemoteAddr());
                    String username = newAuthentication.getName();
                    if ((UserInfo.ANONYMOUS.equals(username) && authCacheKey.hasEmptyHeader()) ||
                            (!UserInfo.ANONYMOUS.equals(username) && !authCacheKey.hasEmptyHeader())) {
                        nonUiAuthCache.put(authCacheKey, newAuthentication);
                        userChangedCache.get(username).addAuthCacheKey(authCacheKey);
                        log.debug("Added authentication {} in cache.", newAuthentication);
                    }
                }
            }
            securityContext.setAuthentication(null);
        }
    }

    private void addToUserChange(Authentication authentication) {
        String username = authentication.getName();
        if (!UserInfo.ANONYMOUS.equals(username)) {
            AuthenticationCache existingCache = userChangedCache.putIfAbsent(username,
                    new AuthenticationCache(authentication));
            if (existingCache != null) {
                existingCache.loggedIn(authentication);
            }
        }
    }

    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    private void useAnonymousIfPossible(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain, SecurityContext securityContext) throws IOException, ServletException {
        boolean anonAccessEnabled = context.getAuthorizationService().isAnonAccessEnabled();
        if (anonAccessEnabled || authInterceptors.accept(request)) {
            log.debug("Using anonymous");
            Authentication authentication = getNonUiCachedAuthentication(request);
            if (authentication == null) {
                log.debug("Creating the Anonymous token");
                final UsernamePasswordAuthenticationToken authRequest =
                        new UsernamePasswordAuthenticationToken(UserInfo.ANONYMOUS, "");
                AuthenticationDetailsSource ads = new HttpAuthenticationDetailsSource();
                //noinspection unchecked
                authRequest.setDetails(ads.buildDetails(request));
                // explicitly ask for the default spring authentication manager by name (we have another one which
                // is only used by the basic authentication filter)
                AuthenticationManager authenticationManager =
                        context.beanForType("authenticationManager", AuthenticationManager.class);
                authentication = authenticationManager.authenticate(authRequest);
                if (authentication != null && authentication.isAuthenticated() && !RequestUtils.isUiRequest(request)) {
                    AuthCacheKey authCacheKey = new AuthCacheKey(authFilter.getCacheKey(request),
                            request.getRemoteAddr());
                    nonUiAuthCache.put(authCacheKey, authentication);
                    log.debug("Added anonymous authentication {} to cache", authentication);
                }
            } else {
                log.debug("Using cached anonymous authentication");
            }
            useAuthentication(request, response, chain, authentication, securityContext);
        } else {
            if (authFilter.acceptEntry(request)) {
                log.debug("Sending request requiring authentication");
                authFilter.commence(request, response,
                        new InsufficientAuthenticationException("Authentication is required"));
            } else {
                log.debug("No filter or entry just chain");
                chain.doFilter(request, response);
            }
        }
    }

    private Authentication getNonUiCachedAuthentication(HttpServletRequest request) {
        // return cached authentication only if this is a non ui request (this guards the case when user accessed
        // Artifactory both from external tool and from the ui)
        AuthCacheKey authCacheKey = new AuthCacheKey(authFilter.getCacheKey(request), request.getRemoteAddr());
        return RequestUtils.isUiRequest(request) ? null : nonUiAuthCache.get(authCacheKey);
    }

    private void useAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
            Authentication authentication, SecurityContext securityContext) throws IOException, ServletException {
        try {
            securityContext.setAuthentication(authentication);
            chain.doFilter(request, response);
            addToUserChange(authentication);
        } finally {
            securityContext.setAuthentication(null);
        }
    }

    @Override
    public int compareTo(SecurityListener o) {
        return 0;
    }

    private static class AuthCacheKey {
        private static final String EMPTY_HEADER = DigestUtils.shaHex("");

        private final String hashedHeader;
        private final String ip;

        private AuthCacheKey(String header, String ip) {
            if (header == null) {
                this.hashedHeader = EMPTY_HEADER;
            } else {
                this.hashedHeader = DigestUtils.shaHex(header);
            }
            this.ip = ip;
        }

        public boolean hasEmptyHeader() {
            return this.hashedHeader.equals(EMPTY_HEADER);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            AuthCacheKey key = (AuthCacheKey) o;
            return hashedHeader.equals(key.hashedHeader) && ip.equals(key.ip);
        }

        @Override
        public int hashCode() {
            int result = hashedHeader.hashCode();
            result = 31 * result + ip.hashCode();
            return result;
        }
    }

    class AuthenticationCache {
        Set<AuthCacheKey> authCacheKeys;
        Map<Integer, Integer> authState = new HashMap<>(3);

        AuthenticationCache(Authentication first) {
            authState.put(first.hashCode(), 0);
        }

        synchronized void addAuthCacheKey(AuthCacheKey authCacheKey) {
            if (authCacheKeys == null) {
                authCacheKeys = new HashSet<>();
            }
            authCacheKeys.add(authCacheKey);
        }

        synchronized void changed() {
            if (authCacheKeys != null) {
                for (AuthCacheKey authCacheKey : authCacheKeys) {
                    Authentication removed = nonUiAuthCache.remove(authCacheKey);
                    if (removed != null) {
                        Integer key = removed.hashCode();
                        log.debug("Removed {}:{} from the non-ui authentication cache", removed.getName(), key);
                        authState.put(key, 1);
                    }
                }
                authCacheKeys.clear();
            }
            Set<Integer> keys = new HashSet<>(authState.keySet());
            for (Integer key : keys) {
                authState.put(key, 1);
            }
        }

        boolean isChanged(Authentication auth) {
            int key = auth.hashCode();
            Integer state = authState.get(key);
            if (state != null) {
                return state == 1;
            }
            return false;
        }

        void loggedOut(Authentication auth) {
            authState.put(auth.hashCode(), 2);
        }

        void loggedIn(Authentication auth) {
            authState.put(auth.hashCode(), 0);
        }
    }
}