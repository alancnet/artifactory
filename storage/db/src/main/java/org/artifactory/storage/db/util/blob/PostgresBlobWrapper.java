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

import org.apache.commons.io.FileUtils;
import org.iostreams.streams.in.DeleteOnCloseFileInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A Blob wrapper that always has the length of the wrapped input stream.
 * This is a requirement of the current PostgreSQL driver which requires the length of inserted blobs.
 *
 * @author Yossi Shaul
 */
public class PostgresBlobWrapper extends BlobWrapper {

    private long length = LENGTH_UNKNOWN;
    private FileInputStream fileInputStream;

    public PostgresBlobWrapper(InputStream in) {
        super(in);
    }

    /**
     * @return The length of the wrapped input stream. Never {@link BlobWrapper#LENGTH_UNKNOWN}.
     */
    @Override
    public long getLength() {
        if (length == LENGTH_UNKNOWN) {
            // lazily create a temp file and save the length the the file input stream
            createTempFileInputStream();
        }
        return length;
    }

    /**
     * @return The input stream read from a temp file.
     */
    @Override
    public InputStream getInputStream() {
        if (length == LENGTH_UNKNOWN) {
            // lazily create a temp file and save the length the the file input stream
            createTempFileInputStream();
        }
        return fileInputStream;
    }

    private void createTempFileInputStream() {
        try {
            File tempFile = File.createTempFile("dbRecord", null, null);
            tempFile.deleteOnExit();
            FileUtils.copyInputStreamToFile(super.getInputStream(), tempFile);
            length = tempFile.length();
            fileInputStream = new DeleteOnCloseFileInputStream(tempFile);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't create temp file", e);
        }
    }
}
