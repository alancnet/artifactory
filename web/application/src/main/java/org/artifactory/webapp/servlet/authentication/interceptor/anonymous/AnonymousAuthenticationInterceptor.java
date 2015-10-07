package org.artifactory.webapp.servlet.authentication.interceptor.anonymous;

import org.artifactory.interceptor.Interceptor;
import javax.servlet.http.HttpServletRequest;

/**
 * Interceptors that are called by {@link org.artifactory.webapp.servlet.AccessFilter#useAnonymousIfPossible}
 * and signify if a request should be allowed to authenticate as Anonymous by the
 * {@link org.artifactory.webapp.servlet.authentication.ArtifactoryAuthenticationFilterChain}
 *
 * @see org.artifactory.webapp.servlet.AccessFilter
 * @author Dan Feldman
 */
public interface AnonymousAuthenticationInterceptor extends Interceptor {

    boolean accept(HttpServletRequest request);
}
