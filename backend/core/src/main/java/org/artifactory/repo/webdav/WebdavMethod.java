/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2014 JFrog Ltd.
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

package org.artifactory.repo.webdav;

import org.artifactory.api.request.ArtifactoryResponse;
import org.artifactory.request.ArtifactoryRequest;

import java.io.IOException;

/**
 * @author Yoav Luft
 */
public interface WebdavMethod {
    /**
     * Checks whether this method can handle the given request, returns true if it does.
     * @param method the name of the requested method
     * @return
     */
    boolean canHandle(String method);

    /**
     * Handle the request. Should only be called if @{link canHandle} returned true.
     * @param request
     * @param response
     */
    void handle(ArtifactoryRequest request, ArtifactoryResponse response) throws IOException;

    /**
     * Return this methods name.
     */
    String getName();
}
