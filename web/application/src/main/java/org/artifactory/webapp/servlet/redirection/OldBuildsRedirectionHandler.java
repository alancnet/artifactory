package org.artifactory.webapp.servlet.redirection;

import org.apache.commons.lang.StringUtils;
import org.artifactory.util.PathUtils;
import org.artifactory.webapp.servlet.RequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Gidi Shabat
 */
public class OldBuildsRedirectionHandler implements RedirectionHandler {
    private static final Logger log = LoggerFactory.getLogger(OldHomeRedirectionHandler.class);

    @Override
    public boolean shouldRedirect(ServletRequest request) {
        String servletPath = RequestUtils.getServletPathFromRequest((HttpServletRequest) request);
        servletPath = servletPath.trim();
        servletPath = servletPath.toLowerCase();
        servletPath = PathUtils.trimLeadingSlashes(servletPath);
        servletPath = PathUtils.trimTrailingSlashes(servletPath);
        return servletPath.startsWith("webapp/builds");
    }

    @Override
    public void redirect(ServletRequest request, ServletResponse response) {
        try {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            String sourceUrl = httpRequest.getRequestURI();
            String queryString = httpRequest.getQueryString()!=null?"?"+httpRequest.getQueryString():"";
            String targetUrl = StringUtils.replace(sourceUrl,"/webapp/builds","/webapp/#/builds")+queryString;
            httpResponse.sendRedirect(targetUrl);
        } catch (Exception e) {
            log.error("Failed to redirect Old generation builds page to new generation builds page request.",e);
        }
    }
}
