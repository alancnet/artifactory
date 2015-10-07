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
import javax.servlet.http.HttpServletResponse;

/**
 * @author Gidi Shabat
 */
public class OldLoginRedirectionHandler implements RedirectionHandler {
    private static final Logger log = LoggerFactory.getLogger(OldLoginRedirectionHandler.class);
    @Override
    public boolean shouldRedirect(ServletRequest request) {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String path = httpRequest.getRequestURI();
        path = PathUtils.trimLeadingSlashes(path);
        path = path.toLowerCase();
        return path.endsWith("/webapp/login.html");
    }

    @Override
    public void redirect(ServletRequest request, ServletResponse response) {
        try {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            String queryString = httpRequest.getQueryString()!=null?"?"+httpRequest.getQueryString():"";
            String sourceUrl = httpRequest.getRequestURI();
            String targetUrl = StringUtils.replace(sourceUrl,"/webapp/login.html","/webapp/#/login")+queryString;
            httpResponse.sendRedirect(targetUrl);
        } catch (Exception e) {
            log.error("Failed to redirect Old generation login page to new generation login page request.",e);
        }
    }
}
