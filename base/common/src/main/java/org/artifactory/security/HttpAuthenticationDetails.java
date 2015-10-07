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

package org.artifactory.security;

import org.artifactory.util.HttpUtils;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

import javax.servlet.http.HttpServletRequest;

/**
 * Support getting the client's ip address in reverse-proxied environments
 *
 * @author yoavl
 */
public class HttpAuthenticationDetails extends WebAuthenticationDetails {
    private String remoteAddress;

    /**
     * Records the remote address and will also set the session Id if a session already exists (it won't create one).
     *
     * @param request that the authentication request was received from
     */
    public HttpAuthenticationDetails(HttpServletRequest request) {
        super(request);
        this.remoteAddress = HttpUtils.getRemoteClientAddress(request);
    }

    @Override
    public String getRemoteAddress() {
        return remoteAddress;
    }
}