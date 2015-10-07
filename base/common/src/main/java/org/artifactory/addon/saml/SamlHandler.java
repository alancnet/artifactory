package org.artifactory.addon.saml;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Gidi Shabat
 */
public interface SamlHandler {

    /**
     * Check if the authentication is SAML authentication.
     */
    public boolean isSamlAuthentication(HttpServletRequest request, HttpServletResponse response) throws SamlException;

    /**
     * Handle identityProvider login response.
     */
    public void handleLoginResponse(HttpServletRequest request, HttpServletResponse response) throws SamlException;
    /**
     * Handle login event from Artifactory login link.
     * Create login request and redirect to the  identity provider.
     */
    void handleLoginRequest(HttpServletRequest request, HttpServletResponse response) throws SamlException;

    /**
     * Return the logout redirect url
     */
    public String generateSamlLogoutRedirectUrl(HttpServletRequest request, HttpServletResponse response)throws SamlException;
}
