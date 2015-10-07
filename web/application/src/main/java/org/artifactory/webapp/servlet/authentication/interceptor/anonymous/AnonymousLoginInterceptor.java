package org.artifactory.webapp.servlet.authentication.interceptor.anonymous;

import javax.servlet.http.HttpServletRequest;

/**
 * Allows anonymous to login if anonymous access is disabled (RTFACT-7073)
 *
 * @author Dan Feldman
 */
public class AnonymousLoginInterceptor implements AnonymousAuthenticationInterceptor {

    @Override
    public boolean accept(HttpServletRequest request) {
        return request.getRequestURI().contains("auth");
    }
}
