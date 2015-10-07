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

package org.artifactory.request;

import org.artifactory.api.request.ArtifactoryResponse;
import org.artifactory.webapp.servlet.TraceLoggingResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Set;
import java.util.UUID;

/**
 * A context maintained as a thread local object for all download/upload requests arriving to Artifactory.
 *
 * @author Noam Y. Tenne
 */
public class RepoRequestContext {

    private static final Logger log = LoggerFactory.getLogger(RepoRequestContext.class);

    private final String id;
    private final String methodName;
    private final String username;
    private final ArtifactoryRequest artifactoryRequest;
    private final ArtifactoryResponse artifactoryResponse;
    private final String logSig;

    public static RepoRequestContext create(String methodName, String username, ArtifactoryRequest artifactoryRequest,
            ArtifactoryResponse artifactoryResponse) {
        if (artifactoryResponse instanceof TraceLoggingResponse) {
            return new TraceLoggingRepoRequestContext(methodName, username, artifactoryRequest, artifactoryResponse);
        }
        return new RepoRequestContext(methodName, username, artifactoryRequest, artifactoryResponse);
    }

    public void destroy() throws IOException {
    }

    protected RepoRequestContext(String methodName, String username, ArtifactoryRequest artifactoryRequest,
            ArtifactoryResponse artifactoryResponse) {
        id = UUID.randomUUID().toString().substring(0, 8);
        this.methodName = methodName.toUpperCase();
        this.username = username;
        this.artifactoryRequest = artifactoryRequest;
        this.artifactoryResponse = artifactoryResponse;
        logSig = id + " " + methodName + " " + username + " " + artifactoryRequest.getRepoPath().getId();
    }

    public void log(String message) {
        if (log.isDebugEnabled()) {
            log.debug(logSig + " " + message);
        }
    }

    protected String getId() {
        return id;
    }

    protected String getMethodName() {
        return methodName;
    }

    protected String getUsername() {
        return username;
    }

    protected ArtifactoryRequest getArtifactoryRequest() {
        return artifactoryRequest;
    }

    protected ArtifactoryResponse getArtifactoryResponse() {
        return artifactoryResponse;
    }

    public void appendOriginatedHeaders(@Nonnull Set<String> originatedHeaders) {
        ArtifactoryRequest request = getArtifactoryRequest();
        Enumeration requestOriginated = request.getHeaders(ArtifactoryRequest.ARTIFACTORY_ORIGINATED);
        if (requestOriginated != null) {
            while (requestOriginated.hasMoreElements()) {
                originatedHeaders.add(((String) requestOriginated.nextElement()));
            }
        }
    }
}
