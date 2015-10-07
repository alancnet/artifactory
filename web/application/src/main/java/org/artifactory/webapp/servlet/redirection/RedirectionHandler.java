package org.artifactory.webapp.servlet.redirection;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Gidi Shabat
 */
public interface RedirectionHandler {
    boolean shouldRedirect(ServletRequest request);
    void redirect(ServletRequest request,ServletResponse response);
}
