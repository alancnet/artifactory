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

package org.artifactory.api.version;

import org.artifactory.api.repo.Async;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Main interface for the Version Info Service
 *
 * @author Noam Tenne
 */
public interface VersionInfoService {

    /**
     * Indicates that the remote versioning service is unavailable It might be down, blocked or not connected yet
     */
    static final String SERVICE_UNAVAILABLE = "NA";

    /**
     * Points to the wiki home page, in case the service is unavailable
     */
    static final String WIKI_DEFAULT = "http://wiki.jfrog.org/confluence/display/RTF";

    /**
     * Get latest version information. If not yet retrieved from the remote server return SERVICE_UNAVAILABLE and
     * retrieve the versioning in a background task.
     *
     * @param headersMap a map of the original http headers
     * @param release    True to get the latest stable version, False to get the latest version of any kind @return
     *                   String Latest version number
     */
    @Nonnull
    public VersionHolder getLatestVersion(Map<String, String> headersMap, boolean release);

    /**
     * Get latest version number from the cache. If doesn't exist will return NA.
     *
     * @param release True to get the latest stable version, False to get the latest version of any kind
     * @return String Latest version number
     */
    @Nonnull
    public VersionHolder getLatestVersionFromCache(boolean release);

    /**
     * @param headersMap Client http header params
     * @return Artifactory versioning info from the remove jfrog service.
     */
    @Async
    Future<ArtifactoryVersioning> getRemoteVersioningAsync(Map<String, String> headersMap);
}
