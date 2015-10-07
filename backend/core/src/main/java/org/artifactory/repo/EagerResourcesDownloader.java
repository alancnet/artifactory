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

package org.artifactory.repo;

import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.Async;
import org.artifactory.api.request.DownloadService;
import org.artifactory.api.request.InternalArtifactoryRequest;
import org.artifactory.request.InternalArtifactoryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * This job creates an internal request asking Artifactory to download certain resource(s).
 *
 * @author Yossi Shaul
 */
@Component
public class EagerResourcesDownloader {
    private static final Logger log = LoggerFactory.getLogger(EagerResourcesDownloader.class);

    @Async
    public void downloadAsync(RepoPath eagerRepoPath) {
        InternalArtifactoryRequest internalRequest = new InternalArtifactoryRequest(eagerRepoPath);
        downloadNow(eagerRepoPath, internalRequest);
    }

    public void downloadNow(RepoPath repoPath, InternalArtifactoryRequest internalRequest) {
        InternalArtifactoryResponse internalResponse = new InternalArtifactoryResponse();
        DownloadService downloadService = ContextHelper.get().beanForType(DownloadService.class);
        log.debug("Eager fetching path {}", repoPath);
        try {
            downloadService.process(internalRequest, internalResponse);
        } catch (IOException e) {
            // ignore - will be logged by the download service
        }
    }
}
