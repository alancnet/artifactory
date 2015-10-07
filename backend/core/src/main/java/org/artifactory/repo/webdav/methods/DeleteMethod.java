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
import org.artifactory.common.StatusHolder;
import org.artifactory.mime.NamingUtils;
import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.repo.LocalRepo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.repo.webdav.LockableWebdavMethod;
import org.artifactory.request.ArtifactoryRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author Yoav Luft
 */
@Component
public class DeleteMethod implements LockableWebdavMethod {

    private static final Logger log = LoggerFactory.getLogger(DeleteMethod.class);

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
        String repoKey = repoPath.getRepoKey();
        LocalRepo localRepository = repoService.localOrCachedRepositoryByKey(repoKey);
        if (localRepository == null) {
            response.setStatus(HttpStatus.SC_NOT_FOUND);
            return;
        }

        if (!NamingUtils.isProperties(repoPath.getPath())) {
            deleteItem(response, repoPath);
        } else {
            deleteProperties(response, repoPath);
        }
    }

    @Override
    public String getName() {
        return "delete";
    }

    private void deleteItem(ArtifactoryResponse response, RepoPath repoPath) throws IOException {
        StatusHolder statusHolder = repoService.undeploy(repoPath);
        if (statusHolder.isError()) {
            response.sendError(statusHolder);
        } else {
            response.setStatus(HttpStatus.SC_NO_CONTENT);
        }
    }

    private void deleteProperties(ArtifactoryResponse response, RepoPath repoPath) throws IOException {
        RepoPathImpl itemRepoPath = new RepoPathImpl(repoPath.getRepoKey(),
                NamingUtils.stripMetadataFromPath(repoPath.getPath()));
        boolean removed = repoService.removeProperties(itemRepoPath);
        if (removed) {
            response.setStatus(HttpStatus.SC_NO_CONTENT);
        } else {
            response.sendError(HttpStatus.SC_NOT_FOUND, "Failed to remove properties from " + itemRepoPath, log);
        }
    }
}
