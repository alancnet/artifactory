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

package org.artifactory.webapp.wicket.resource;

import org.apache.wicket.request.resource.DynamicImageResource;
import org.apache.wicket.util.file.File;
import org.apache.wicket.util.io.IOUtils;
import org.artifactory.common.ArtifactoryHome;

import java.io.IOException;
import java.io.InputStream;

/**
 * Resource to get the uploaded user logo
 *
 * @author Tomer Cohen
 */
public class LogoResource extends DynamicImageResource {

    private ArtifactoryHome artifactoryHome;

    public LogoResource(ArtifactoryHome artifactoryHome) {
        this.artifactoryHome = artifactoryHome;
    }

    @Override
    protected byte[] getImageData(Attributes attributes) {
        InputStream inputStream = null;
        try {
            File file = new File(artifactoryHome.getLogoDir(), "logo");
            inputStream = file.inputStream();
            return IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Can't read image file", e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }
}
