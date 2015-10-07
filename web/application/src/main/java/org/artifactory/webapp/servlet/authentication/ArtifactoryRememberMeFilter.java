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

package org.artifactory.webapp.servlet.authentication;

import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.property.ArtifactorySystemProperties;
import org.artifactory.webapp.servlet.RequestUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Eli Givoni
 */
public class ArtifactoryRememberMeFilter implements ArtifactoryAuthenticationFilter {

    private RememberMeAuthenticationFilter rememberMeDelegateFilter;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        ArtifactorySystemProperties properties =
                ((ArtifactoryHome) filterConfig.getServletContext().getAttribute(ArtifactoryHome.SERVLET_CTX_ATTR))
                        .getArtifactoryProperties();
        ConstantValues disableRememberMeProp = ConstantValues.securityDisableRememberMe;
        Boolean disableRememberMe = properties.getBooleanProperty(disableRememberMeProp);
        if (!disableRememberMe) {
            ServletContext servletContext = filterConfig.getServletContext();
            ArtifactoryContext context = RequestUtils.getArtifactoryContext(servletContext);
            rememberMeDelegateFilter = context.beanForType(RememberMeAuthenticationFilter.class);
            rememberMeDelegateFilter.init(filterConfig);
        }
    }

    public boolean requiresReAuthentication(ServletRequest request, Authentication authentication) {
        return false;
    }

    @Override
    public boolean acceptFilter(ServletRequest request) {
        if (rememberMeDelegateFilter == null) {
            return false;
        }

        Cookie[] cookies = ((HttpServletRequest) request).getCookies();

        if ((cookies == null) || (cookies.length == 0)) {
            return false;
        }

        for (Cookie cookie : cookies) {
            if (AbstractRememberMeServices.SPRING_SECURITY_REMEMBER_ME_COOKIE_KEY.equals(cookie.getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean acceptEntry(ServletRequest request) {
        return false;
    }

    @Override
    public String getCacheKey(ServletRequest request) {
        return ((HttpServletRequest) request).getHeader("Authorization");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (rememberMeDelegateFilter != null) {
            rememberMeDelegateFilter.doFilter(request, response, chain);
        }
    }

    @Override
    public void destroy() {
        if (rememberMeDelegateFilter != null) {
            rememberMeDelegateFilter.destroy();
        }
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException)
            throws IOException, ServletException {
        throw new UnsupportedOperationException("This filter doesn't support commencing");
    }
}
