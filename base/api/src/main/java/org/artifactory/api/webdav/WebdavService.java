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

package org.artifactory.api.webdav;

import com.google.common.collect.Sets;
import org.artifactory.api.request.ArtifactoryResponse;
import org.artifactory.request.ArtifactoryRequest;

import java.io.IOException;
import java.util.Set;

/**
 * User: freds Date: Jul 27, 2008 Time: 9:26:56 PM
 */
public interface WebdavService {

    /**
     * This is used by request utils and should go
     * Supported web dav methods. (post method is not supported)
     * TODO [yluft]: Remove this.
     */
    Set<String> WEBDAV_METHODS = Sets.newHashSet(
            "propfind", "mkcol", "move", "delete", "options", "proppatch", "lock", "unlock");

    /**
     * Attempt to handle the request.
     * @param request
     * @param response
     * @return true if request was handled, false if no method handler was found.
     * @throws IOException
     */
    boolean handleRequest(String methodName, ArtifactoryRequest request, ArtifactoryResponse response) throws IOException;

    /**
     * Supported web dav methods. (post method is not supported)
     */
    Set<String> supportedMethods();
}
