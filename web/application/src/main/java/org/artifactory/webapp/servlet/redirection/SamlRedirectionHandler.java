package org.artifactory.webapp.servlet.redirection;

import org.apache.commons.lang.StringUtils;
import org.artifactory.util.PathUtils;
import org.artifactory.webapp.servlet.RequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Gidi Shabat
 */
public class SamlRedirectionHandler implements RedirectionHandler {
    private static final Logger log = LoggerFactory.getLogger(SamlRedirectionHandler.class);
    @Override
    public boolean shouldRedirect(ServletRequest request) {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String path = httpRequest.getRequestURI();
        path = PathUtils.trimLeadingSlashes(path);
        path = path.toLowerCase();
        return path.endsWith("/webapp/saml/loginrequest") ||
                path.endsWith("/webapp/saml/loginresponse") ||
                path.endsWith("/webapp/saml/logoutrequest");
    }

    @Override
    public void redirect(ServletRequest request, ServletResponse response) {
        try {
            String path = RequestUtils.getServletPathFromRequest((HttpServletRequest) request);
            String targetUrl = StringUtils.replace(path, "webapp", "ui");
            RequestDispatcher dispatcher = request.getRequestDispatcher(targetUrl);
            dispatcher.forward(request, response);
        } catch (Exception e) {
            log.error("Failed to redirect SAML request.",e);
        }
    }
}
