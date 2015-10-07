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

package org.artifactory.repo.webdav.methods;

import org.apache.http.HttpStatus;
import org.artifactory.api.request.ArtifactoryResponse;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.repo.webdav.WebdavMethod;
import org.artifactory.request.ArtifactoryRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Empty handler for supporting windows client
 *
 * @author Yoav Luft
 */
@Component
public class ProppatchMethod implements WebdavMethod {

    private static final Logger log = LoggerFactory.getLogger(ProppatchMethod.class);
    public static final String METHOD_NAME = "proppatch";

    @Autowired
    private InternalRepositoryService repoService;

    @Override
    public boolean canHandle(String method) {
        return getName().equalsIgnoreCase(method);
    }

    @Override
    public void handle(ArtifactoryRequest request, ArtifactoryResponse response) throws IOException {
        log.debug("Handling {}", getName());
        RepoPath repoPath = request.getRepoPath();
        if (repoService.exists(repoPath)) {
            // TODO [yluft]: actually check if we can handle the request.
            response.sendSuccess();
        } else {
            response.sendError(HttpStatus.SC_NOT_FOUND, "Resource " + repoPath.toPath() + " does not exists.", log);
        }
    }

    @Override
    public String getName() {
        return METHOD_NAME;
    }
}
