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

package org.artifactory.engine;

import org.artifactory.api.repo.Request;
import org.artifactory.api.repo.exception.RepoRejectException;
import org.artifactory.api.request.ArtifactoryResponse;
import org.artifactory.api.request.UploadService;
import org.artifactory.repo.LocalRepo;
import org.artifactory.request.ArtifactoryRequest;
import org.artifactory.sapi.common.Lock;

import java.io.IOException;

/**
 * The internal implementation of the upload service
 *
 * @author Noam Y. Tenne
 */
public interface InternalUploadService extends UploadService {

    /**
     * Performs the upload within a new transaction.
     *
     * @param request  Originating request
     * @param response Response to send
     * @param repo     Target local non-cache repo
     */
    @Lock
    @Request(aggregateEventsByTimeWindow = true)
    void uploadWithinTransaction(ArtifactoryRequest request, ArtifactoryResponse response, LocalRepo repo)
            throws IOException,
            RepoRejectException;
}
