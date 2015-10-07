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
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.artifactory.api.repo.exception.RepoRejectException;
import org.artifactory.api.request.ArtifactoryResponse;
import org.artifactory.repo.LocalRepo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.local.ValidDeployPathContext;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.repo.webdav.LockableWebdavMethod;
import org.artifactory.request.ArtifactoryRequest;
import org.artifactory.sapi.fs.VfsFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author Yoav Luft
 */
@Component
public class MkcolMethod implements LockableWebdavMethod {

    private static final Logger log = LoggerFactory.getLogger(MkcolMethod.class);

    @Autowired
    private InternalRepositoryService repoService;

    @Override
    public boolean canHandle(String method) {
        return getName().equals(method);
    }

    @Override
    public void handle(ArtifactoryRequest request, ArtifactoryResponse response) throws IOException {
        log.debug("Handling {}", getName());
        RepoPath repoPath = request.getRepoPath();
        String repoKey = request.getRepoKey();
        LocalRepo repo = repoService.localOrCachedRepositoryByKey(repoKey);
        if (repo == null) {
            response.sendError(HttpStatus.SC_NOT_FOUND, "Could not find repo '" + repoKey + "'.", log);
            return;
        }

        //Return 405 if called on root or the folder already exists
        String path = repoPath.getPath();
        if (StringUtils.isBlank(path) || repo.itemExists(path)) {
            response.sendError(HttpStatus.SC_METHOD_NOT_ALLOWED,
                    "MKCOL can only be executed on non-existent resource: " + repoPath, log);
            return;
        }
        //Check that we are allowed to write
        try {
            // Servlet container doesn't support long values so we take it manually from the header
            String contentLengthHeader = request.getHeader(HttpHeaders.CONTENT_LENGTH);
            long contentLength = StringUtils.isBlank(contentLengthHeader) ? -1 : Long.parseLong(contentLengthHeader);
            repoService.assertValidDeployPath(
                    new ValidDeployPathContext.Builder(repo, repoPath).contentLength(contentLength).build());
        } catch (RepoRejectException rre) {
            response.sendError(rre.getErrorCode(), rre.getMessage(), log);
            return;
        }

        // make sure the parent exists
        VfsFolder parentFolder = repo.getMutableFolder(repoPath.getParent());
        if (parentFolder == null) {
            response.sendError(HttpStatus.SC_CONFLICT,
                    "Directory cannot be created: parent doesn't exist: " + repoPath.getParent(), log);
            return;
        }

        repo.createOrGetFolder(repoPath);
        response.setStatus(HttpStatus.SC_CREATED);
    }

    @Override
    public String getName() {
        return "mkcol";
    }
}
