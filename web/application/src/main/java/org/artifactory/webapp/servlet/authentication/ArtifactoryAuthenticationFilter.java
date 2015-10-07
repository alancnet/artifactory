/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2012 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.artifactory.webapp.servlet.authentication;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.Filter;
import javax.servlet.ServletRequest;

/**
 * @author freds
 * @date Mar 10, 2009
 */
public interface ArtifactoryAuthenticationFilter extends Filter, AuthenticationEntryPoint {
    /**
     * Authentications might get expired for various reasons, depending on the authenticator. This method is called
     * whenever the {@link AccessFiler} detects a valid authentication. The implementer should return true if it is
     * the one "responsible" for this request or was the one authenticated this session and it determines that the
     * authentication is not valid anymore (for example an expired cookie).
     *
     * @param request        The http request
     * @param authentication A valid authenticated authentication
     * @return True if the client should be re-authenticated
     */
    boolean requiresReAuthentication(ServletRequest request, Authentication authentication);

    boolean acceptFilter(ServletRequest request);

    boolean acceptEntry(ServletRequest request);

    String getCacheKey(ServletRequest request);
}
