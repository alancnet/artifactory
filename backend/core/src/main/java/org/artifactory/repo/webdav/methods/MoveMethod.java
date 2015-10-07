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

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.artifactory.api.common.MoveMultiStatusHolder;
import org.artifactory.api.request.ArtifactoryResponse;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.repo.webdav.WebdavMethod;
import org.artifactory.request.ArtifactoryRequest;
import org.artifactory.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLDecoder;

/**
 * @author Yoav Luft
 */
@Component
public class MoveMethod implements WebdavMethod {

    private static final Logger log = LoggerFactory.getLogger(MoveMethod.class);
    private static final String METHOD_NAME = "move";

    @Autowired
    private AuthorizationService authService;

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
        if (StringUtils.isEmpty(repoPath.getPath())) {
            response.sendError(HttpStatus.SC_BAD_REQUEST, "Cannot perform MOVE action on a repository. " +
                    "Please specify a valid path", log);
            return;
        }

        String destination = URLDecoder.decode(request.getHeader("Destination"), "UTF-8");
        if (StringUtils.isEmpty(destination)) {
            response.sendError(HttpStatus.SC_BAD_REQUEST, "Header 'Destination' is required.", log);
            return;
        }

        String targetPathWithoutContextUrl = StringUtils.remove(destination, request.getServletContextUrl());
        String targetPathParent = PathUtils.getParent(targetPathWithoutContextUrl);
        RepoPath targetPath = InternalRepoPathFactory.create(targetPathParent);
        if (!authService.canDelete(repoPath) || !authService.canDeploy(targetPath)) {
            response.sendError(HttpStatus.SC_FORBIDDEN, "Insufficient permissions.", log);
            return;
        }

        MoveMultiStatusHolder status = repoService.move(repoPath, targetPath, false, true, true);
        if (!status.hasWarnings() && !status.hasErrors()) {
            response.sendSuccess();
        } else {
            response.sendError(status);
        }
    }

    @Override
    public String getName() {
        return METHOD_NAME;
    }
}
