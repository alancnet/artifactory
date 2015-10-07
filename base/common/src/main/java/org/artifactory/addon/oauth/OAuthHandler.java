package org.artifactory.addon.oauth;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Travis Foster
 */
public interface OAuthHandler {

    /**
     * Handle identityProvider login response.
     */
    Object handleLoginResponse(HttpServletRequest request);

    /**
     * Handle login event from Artifactory login link.
     * Create login request and redirect to the OAuth login page.
     */
    Object handleLoginRequest(HttpServletRequest request);

    /**
     * Handle login from external command line tool.
     * Use basic auth to log in and return an access token.
     */
    Object handleLogin(String method, String name, String path, HttpServletRequest request);

    /**
     * Get the name of the provider specified for NPM logins (if exists)
     */
    String getNpmLoginHandler();
}
