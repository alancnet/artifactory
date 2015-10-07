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

package org.artifactory.io;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.io.IOUtils;
import org.artifactory.fs.RepoResource;
import org.artifactory.fs.ZipEntryRepoResource;
import org.artifactory.resource.ResourceStreamHandle;
import org.artifactory.util.PathUtils;
import org.artifactory.util.ZipUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * This stream handle encapsulates a stream of zip file and returns the input stream of a one resource inside the zip.
 *
 * @author Yossi Shaul
 */
public class ZipResourceStreamHandle implements ResourceStreamHandle {
    private static final Logger log = LoggerFactory.getLogger(ZipResourceStreamHandle.class);

    private final ZipEntryRepoResource zipResource;
    private final InputStream stream;
    private final ArchiveInputStream[] zipStreams;

    public ZipResourceStreamHandle(RepoResource zipResource, InputStream stream) throws IOException {
        if (!(zipResource instanceof ZipEntryRepoResource)) {
            throw new IllegalArgumentException(
                    "Unexpected resource type: " + zipResource.getClass() + " value=" + zipResource);
        }
        this.zipResource = (ZipEntryRepoResource) zipResource;
        this.stream = stream;
        String[] zipEntryNames = PathUtils.splitZipResourcePathIfExist(this.zipResource.getEntryPath(), true);
        String name = zipResource.getInfo().getName();
        zipStreams = ZipUtils.getArchiveInputStreamArray(name, zipEntryNames.length);
        try {
            for (int i = 0; i < zipEntryNames.length; i++) {
                String zipEntryName = zipEntryNames[i];
                if (i == 0) {
                    zipStreams[i] = ZipUtils.returnArchiveInputStream(stream, name);
                } else {
                    zipStreams[i] = ZipUtils.returnArchiveInputStream(zipStreams[i - 1], name);
                }
                ArchiveEntry zipEntry = ZipUtils.locateArchiveEntry(zipStreams[i], zipEntryName);
                if (zipEntry == null) {
                    throw new IOException(String.format("Zip resource '%s' not found in '%s'",
                            zipEntryName, zipResource.getRepoPath()));
                }
            }
        } catch (IOException e) {
            close();    // close stream now
            log.error(String.format("Failed to retrieve zip resource '%s' from '%s'",
                    this.zipResource.getEntryPath(), zipResource.getRepoPath()), e);
            throw e;
        }

    }

    @Override
    public InputStream getInputStream() {
        // the zip stream is in a state where it points to the start of the requested entry. calling getInputStream will
        // return the stream of the requested entry (not of the zip file)
        return zipStreams[zipStreams.length - 1];
    }

    @Override
    public long getSize() {
        return zipResource.getSize();
    }

    @Override
    public void close() {
        for (ArchiveInputStream zipStream : zipStreams) {
            IOUtils.closeQuietly(zipStream);
        }
        IOUtils.closeQuietly(stream);   // in case the zip stream creation failed
    }
}
