package org.artifactory.addon.saml;

import org.artifactory.addon.Addon;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Chen Keinan
 */
public interface SamlSsoAddon extends Addon {

    String getSamlLoginIdentityProviderUrl(HttpServletRequest request);

    void createCertificate(String certificate) throws Exception;

    Boolean isSamlAuthentication(HttpServletRequest servletRequest, HttpServletResponse servletResponse)
            throws SamlException;
}
