/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2013 JFrog Ltd.
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

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.artifactory.common.ConstantValues;
import org.artifactory.util.HttpUtils;
import org.artifactory.util.UiRequestUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

/**
 * Intercept spring security exceptions and transforms the response into a JSON object.
 *
 * @author Shay Yaakov
 */
public class ArtifactoryBasicAuthenticationEntryPoint extends BasicAuthenticationEntryPoint {

    public static final String REALM = "Artifactory Realm";

    @Override
    public void afterPropertiesSet() throws Exception {
        // the inherited afterPropertiesSet checks that realmName is set
        setRealmName(REALM);
        super.afterPropertiesSet();
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {
        sendErrorResponseToClient(request, response, authException);
    }

    /**
     * if ivy request send error response with 403 error code else send 401
     * @param request http request
     * @param response http response
     * @param authException - authentication exception
     * @throws IOException
     */
    private void sendErrorResponseToClient(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException {
        if (isIvyRequest(request,authException) || isAlreadyAuthedNuGetRequest(request, authException)){
            sendErrorResponse(request, response, authException, SC_FORBIDDEN);
        }
        else {
            sendErrorResponse(request, response, authException, SC_UNAUTHORIZED);
        }
    }

    /**
     * send error response to client
     * @param response - http response
     * @param authException authentication exception
     * @param responseStatusCode - http response status code
     * @throws IOException
     */
    private void sendErrorResponse(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException, int responseStatusCode)
            throws IOException {
        if (!UiRequestUtils.isUiRestRequest(request)) {
            response.addHeader("WWW-Authenticate", "Basic realm=\"" + getRealmName() + "\"");
        }
        HttpUtils.sendErrorResponse(response, responseStatusCode, authException.getMessage());
    }

    @Override
    public String getRealmName() {
        return REALM;
    }

    /**
     * @param request http request
     * @param authException auth exception
     * @return true if httpForceForbiddenResponse system properties is configure and ivy request
     */
    private boolean isIvyRequest(HttpServletRequest request,AuthenticationException authException){
        return ConstantValues.httpForceForbiddenResponse.getBoolean() && authException instanceof BadCredentialsException &&
                request.getHeader("User-Agent").toLowerCase().indexOf("Ivy".toLowerCase()) != -1;
    }

    /**
     * Return 403 to nuget client if bad credentials error, if not the client keeps asking for credentials
     * indefinitely on 401 responses.
     */
    private boolean isAlreadyAuthedNuGetRequest(HttpServletRequest request, AuthenticationException authException) {
        return authException instanceof BadCredentialsException
                && request.getHeader(HttpHeaders.USER_AGENT).toLowerCase().contains("nuget")
                && (StringUtils.isNotBlank(request.getHeader(HttpHeaders.AUTHORIZATION))
                    || StringUtils.isNotBlank(request.getHeader("X-NuGet-ApiKey")));
    }


}
