package org.artifactory.addon.oauth;

import org.artifactory.addon.Addon;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Travis Foster
 */
public interface OAuthSsoAddon extends Addon {
    String getOAuthLoginPageUrl(HttpServletRequest request);
}
