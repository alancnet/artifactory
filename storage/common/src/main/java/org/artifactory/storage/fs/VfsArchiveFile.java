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

package org.artifactory.storage.fs;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.io.IOUtils;
import org.artifactory.sapi.fs.VfsFile;
import org.artifactory.util.ZipUtils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;

/**
 * A wrapper on a zip resource around a vfs file. This class is non tread safe.
 */
public class VfsArchiveFile implements Closeable {

    private VfsFile vfsFile;
    private List<ArchiveEntry> entries;
    private List<InputStream> streams = new ArrayList<>();

    public VfsArchiveFile(VfsFile vfsFile) {
        this.vfsFile = vfsFile;
    }

    public ArchiveEntry getEntry(String name) throws IOException {
        List<? extends ArchiveEntry> entries = entries();
        for (ArchiveEntry entry : entries) {
            if (name.equals(entry.getName())) {
                return entry;
            }
        }
        return null;
    }

    public InputStream getInputStream(ZipEntry entry) throws IOException {
        ArchiveInputStream zis = getArchiveInputStream();
        ArchiveEntry currentEntry;
        while ((currentEntry = zis.getNextEntry()) != null) {
            if (currentEntry.getName().equals(entry.getName())) {
                return zis;
            }
        }
        throw new IOException("Failed to read zip entry '" + entry.getName() + "' from '" + getName() + "'.");
    }

    public String getName() {
        return vfsFile.getName();
    }

    public List<? extends ArchiveEntry> entries() throws IOException {
        if (entries == null) {
            ArchiveInputStream zis = null;
            entries = new ArrayList<>();
            zis = getArchiveInputStream();
            ArchiveEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                entries.add(entry);
            }
        }
        return entries;
    }

    @Override
    public void close() throws IOException {
        for (InputStream stream : streams) {
            IOUtils.closeQuietly(stream);
        }
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VfsArchiveFile)) {
            return false;
        }
        VfsArchiveFile file = (VfsArchiveFile) o;
        return vfsFile.equals(file.vfsFile);

    }

    public int hashCode() {
        return vfsFile.hashCode();
    }

    private ArchiveInputStream getArchiveInputStream() throws IOException {
        ArchiveInputStream archiveInputStream;
        archiveInputStream = ZipUtils.getArchiveInputStream(vfsFile);
        streams.add(archiveInputStream);
        return archiveInputStream;
    }
}