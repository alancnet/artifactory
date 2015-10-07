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

import com.google.common.collect.Sets;
import org.artifactory.api.request.ArtifactoryResponse;

import java.io.IOException;
import java.util.Set;

/**
 * @author Noam Y. Tenne
 */
public abstract class RepoRequests {

    private static ThreadLocal<RepoRequestContext> context = new ThreadLocal<>();

    private RepoRequests() {
    }

    public static void set(String methodName, String username, ArtifactoryRequest artifactoryRequest,
            ArtifactoryResponse artifactoryResponse) {
        context.set(RepoRequestContext.create(methodName, username, artifactoryRequest, artifactoryResponse));
    }

    public static void destroy() throws IOException {
        try {
            RepoRequestContext repoRequestContext = context.get();
            if (repoRequestContext != null) {
                repoRequestContext.destroy();
            }
        } finally {
            context.remove();
        }
    }

    public static void logToContext(String format, Object... params) {
        RepoRequestContext repoRequestContext = context.get();
        if (repoRequestContext != null) {
            String formattedMessage = String.format(format, params);
            repoRequestContext.log(formattedMessage);
        }
    }

    public static Set<String> getOriginatedHeaders() {
        Set<String> originatedHeaders = Sets.newHashSet();
        RepoRequestContext repoRequestContext = context.get();
        if (repoRequestContext != null) {
            repoRequestContext.appendOriginatedHeaders(originatedHeaders);
        }
        return originatedHeaders;
    }

    /**
     * Use with caution, This might return an empty string when calling internally from a different thread!
     *
     * @return The servlet context url if the thread local is bounded, otherwise returns an empty string
     */
    public static String getServletContextUrl() {
        RepoRequestContext repoRequestContext = context.get();
        if (repoRequestContext != null) {
            return repoRequestContext.getArtifactoryRequest().getServletContextUrl();
        }

        return "";
    }
}
