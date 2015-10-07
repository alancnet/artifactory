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

import org.artifactory.md.Properties;
import org.artifactory.mime.NamingUtils;
import org.artifactory.util.PathUtils;

import javax.annotation.Nonnull;

/**
 * Implementation based on a download http request.
 *
 * @author Yossi Shaul
 */
public class DownloadRequestContext extends BaseRequestContext {

    private boolean forceExpiryCheck;

    public DownloadRequestContext(@Nonnull ArtifactoryRequest artifactoryRequest) {
        super(artifactoryRequest);
    }

    public DownloadRequestContext(@Nonnull ArtifactoryRequest artifactoryRequest, boolean forceExpiryCheck) {
        super(artifactoryRequest);
        this.forceExpiryCheck = forceExpiryCheck;
    }

    @Override
    public boolean isFromAnotherArtifactory() {
        return request.isFromAnotherArtifactory();
    }

    @Override
    public String getResourcePath() {
        String path = request.getPath();
        return NamingUtils.isChecksum(path) ? PathUtils.stripExtension(path) : path;
    }

    @Override
    public Properties getProperties() {
        return request.getProperties();
    }

    @Override
    public boolean isForceExpiryCheck() {
        return forceExpiryCheck;
    }
}
