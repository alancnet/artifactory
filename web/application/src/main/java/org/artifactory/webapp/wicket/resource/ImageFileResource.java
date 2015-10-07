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

import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.DynamicImageResource;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.util.file.File;
import org.apache.wicket.util.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Resource to get the uploaded user logo
 *
 * @author Tomer Cohen
 */
public class ImageFileResource extends DynamicImageResource {
    private File file;

    public ImageFileResource(java.io.File file) {
        this.file = new File(file);
    }

    @Override
    protected void configureResponse(ResourceResponse response, Attributes attributes) {
        response.setContentType("image/" + file.getExtension());
    }

    @Override
    protected byte[] getImageData(Attributes attributes) {
        InputStream inputStream = null;
        try {
            inputStream = file.inputStream();
            return IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Can't read image file", e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    public void onResourceRequested() {
        IResource.Attributes attributes = new IResource.Attributes(RequestCycle.get().getRequest(),
                RequestCycle.get().getResponse());
        respond(attributes);
    }
}