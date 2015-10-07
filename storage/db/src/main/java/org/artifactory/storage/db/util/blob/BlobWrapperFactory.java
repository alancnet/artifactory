/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2013 JFrog Ltd.
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

package org.artifactory.storage.db.util.blob;

import org.artifactory.storage.StorageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * A factory for building {@link BlobWrapper} matching the configured database.
 *
 * @author Yossi Shaul
 */
@Component
public class BlobWrapperFactory {

    @Autowired
    private StorageProperties storageProps;

    /**
     * @param in     The input stream to wrap
     * @param length The length of the input stream (in bytes)
     * @return A blob wrapper around an input stream with known length
     */
    public BlobWrapper create(InputStream in, long length) {
        return new BlobWrapper(in, length);
    }

    /**
     * @param data The data to use as the blob input stream
     * @return A blob wrapper around a string
     */
    public BlobWrapper create(String data) {
        return new BlobWrapper(data);
    }

    /**
     * Creates a blob wrapper with unknown length.
     * This factory method will create a wrapper that fits the currently configured database.
     *
     * @param in The input stream to wrap
     * @return A blob wrapper
     */
    public BlobWrapper create(InputStream in) {
        if (!storageProps.isPostgres()) {
            return new BlobWrapper(in);
        } else {
            return new PostgresBlobWrapper(in);
        }
    }

    /**
     * @param jsonObject The object to serialize and offer as an input stream to the blob wrapper
     * @return A blob wrapper around json object
     */
    public BlobWrapper createJsonObjectWrapper(Object jsonObject) {
        JsonBlobWrapper jsonBlobWrapper = new JsonBlobWrapper(jsonObject);
        if (!storageProps.isPostgres()) {
            return jsonBlobWrapper;
        } else {
            return new PostgresBlobWrapper(jsonBlobWrapper.getInputStream());
        }
    }

}
