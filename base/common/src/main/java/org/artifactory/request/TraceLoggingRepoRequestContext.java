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
import org.joda.time.format.ISODateTimeFormat;

import java.io.IOException;

/**
 * @author Noam Y. Tenne
 */
public class TraceLoggingRepoRequestContext extends RepoRequestContext {
    public TraceLoggingRepoRequestContext(String methodName, String username, ArtifactoryRequest artifactoryRequest,
            ArtifactoryResponse artifactoryResponse) {
        super(methodName, username, artifactoryRequest, artifactoryResponse);
    }

    @Override
    public void destroy() throws IOException {
        super.destroy();
        ((TraceLoggingResponse) getArtifactoryResponse()).sendResponse(getId(), getMethodName(), getUsername(),
                getArtifactoryRequest().getRepoPath().getId());
    }

    @Override
    public void log(String message) {
        super.log(message);
        ((TraceLoggingResponse) getArtifactoryResponse()).log(
                ISODateTimeFormat.dateTime().print(System.currentTimeMillis()) + " " + message);
    }
}
