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
 */
package org.artifactory.webapp.servlet.authentication;

import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.security.SecurityService;
import org.artifactory.security.SaltedPassword;
import org.artifactory.security.UserInfo;
import org.artifactory.security.mission.control.MissionControlAuthenticationProvider;
import org.artifactory.security.mission.control.MissionControlProperties;
import org.artifactory.util.PathUtils;
import org.artifactory.webapp.servlet.RequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.artifactory.security.mission.control.MissionControlAuthenticationProvider.HEADER_NAME;

/**
 * @author Gidi Shabat
 */
public class MissionControlAuthenticationFilter implements ArtifactoryAuthenticationFilter {
    private static final Logger log = LoggerFactory.getLogger(ArtifactoryBasicAuthenticationFilter.class);
    private MissionControlProperties missionControlProperties;


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.trace("Initializing filter");
        ServletContext servletContext = filterConfig.getServletContext();
        ArtifactoryContext context = RequestUtils.getArtifactoryContext(servletContext);
        missionControlProperties = context.beanForType(MissionControlProperties.class);
        log.trace("Successfully Initialized filter");
    }

    public boolean requiresReAuthentication(ServletRequest request, Authentication authentication) {
        return acceptFilter(request);
    }

    @Override
    public boolean acceptFilter(ServletRequest request) {
        String header = ((HttpServletRequest) request).getHeader(HEADER_NAME);
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String path = httpRequest.getRequestURI();
        String contextPrefix = RequestUtils.getContextPrefix((HttpServletRequest) request);
        path = PathUtils.trimLeadingSlashes(path);
        String token = missionControlProperties.getToken();
        String url = missionControlProperties.getUrl();
        return token != null && header != null && url != null && path.startsWith(contextPrefix + "/mc");
    }

    @Override
    public boolean acceptEntry(ServletRequest request) {
        return false;
    }

    @Override
    public String getCacheKey(ServletRequest request) {
        return ((HttpServletRequest) request).getHeader(HEADER_NAME);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        log.trace("Starting to validate authentication");
        String missionControlToken = ((HttpServletRequest) request).getHeader(HEADER_NAME);
        SecurityService securityService = ContextHelper.get().beanForType(SecurityService.class);
        int index = missionControlToken.indexOf('@');
        String userName= UserInfo.MISSION_CONTROLL;
        if(index>0){
            userName=missionControlToken.substring(0, index)+"-from-MC";
            missionControlToken=missionControlToken.substring(index+1,missionControlToken.length());
        }
        SaltedPassword saltedPassword = securityService.generateSaltedPassword(missionControlToken);
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String path = httpRequest.getRequestURI();
        String contextPrefix = RequestUtils.getContextPrefix((HttpServletRequest) request);
        path = PathUtils.trimLeadingSlashes(path);
        String token = missionControlProperties.getToken();

        if (token.equals(saltedPassword.getPassword()) && path.startsWith(contextPrefix + "/mc")) {
            MissionControlAuthenticationProvider provider = ContextHelper.get()
                    .beanForType(MissionControlAuthenticationProvider.class);
            Authentication auth = provider.getFullAuthentication(userName);
            RequestUtils.setAuthentication((HttpServletRequest) request, auth, true);
            SecurityContextHolder.getContext().setAuthentication(auth);
            log.trace("Successful Mission Control Authentication ");
        } else {
            MissionControlAuthenticationProvider provider = ContextHelper.get()
                    .beanForType(MissionControlAuthenticationProvider.class);
            Authentication auth = provider.getAnonymousAuthentication();
            RequestUtils.setAuthentication((HttpServletRequest) request, auth, true);
            SecurityContextHolder.getContext().setAuthentication(auth);
            log.trace("Mission Control failed to authenticate request.");
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
    }
}
