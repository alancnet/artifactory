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

package org.artifactory.util;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.artifactory.api.archive.ArchiveType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

/**
 * A utility class to help create archives for the supported {@link org.artifactory.api.archive.ArchiveType}s.
 *
 * @author Shay Yaakov
 */
public abstract class ArchiveUtils {
    private static final Logger log = LoggerFactory.getLogger(ArchiveUtils.class);

    /**
     * Archives the contents of the given directory into the given archive using the apache commons compress tools
     *
     * @param sourceDirectory    Directory to archive
     * @param destinationArchive Archive file to create
     * @param recurse            True if should recurse file scan of source directory. False if not
     * @param archiveType        Archive type to create
     * @throws java.io.IOException      Any exceptions that might occur while handling the given files and used streams
     * @throws IllegalArgumentException Thrown when given invalid destinations
     */
    public static void archive(File sourceDirectory, File destinationArchive, boolean recurse, ArchiveType archiveType)
            throws IOException {
        if ((sourceDirectory == null) || (destinationArchive == null)) {
            throw new IllegalArgumentException("Supplied destinations cannot be null.");
        }
        if (!sourceDirectory.isDirectory()) {
            throw new IllegalArgumentException("Supplied source directory must be an existing directory.");
        }
        String sourcePath = sourceDirectory.getAbsolutePath();
        String archivePath = destinationArchive.getAbsolutePath();
        log.debug("Beginning to archive '{}' into '{}'", sourcePath, archivePath);
        FileOutputStream destinationOutputStream = new FileOutputStream(destinationArchive);
        ArchiveOutputStream archiveOutputStream = createArchiveOutputStream(
                new BufferedOutputStream(destinationOutputStream), archiveType);
        try {
            @SuppressWarnings({"unchecked"})
            Collection<File> childrenFiles = org.apache.commons.io.FileUtils.listFiles(sourceDirectory, null, recurse);
            childrenFiles.remove(destinationArchive);

            ArchiveEntry archiveEntry;
            FileInputStream fileInputStream;
            for (File childFile : childrenFiles) {
                String childPath = childFile.getAbsolutePath();
                String relativePath = childPath.substring((sourcePath.length() + 1), childPath.length());

                /**
                 * Need to convert separators to unix format since zipping on windows machines creates windows specific
                 * FS file paths
                 */
                relativePath = FilenameUtils.separatorsToUnix(relativePath);
                archiveEntry = createArchiveEntry(childFile, relativePath, archiveType);
                fileInputStream = new FileInputStream(childFile);
                archiveOutputStream.putArchiveEntry(archiveEntry);

                try {
                    IOUtils.copy(fileInputStream, archiveOutputStream);
                } finally {
                    IOUtils.closeQuietly(fileInputStream);
                    archiveOutputStream.closeArchiveEntry();
                }
                log.debug("Archive '{}' into '{}'", childPath, archivePath);
            }
        } finally {
            IOUtils.closeQuietly(archiveOutputStream);
        }

        log.debug("Completed archiving of '{}' into '{}'", sourcePath, archivePath);
    }

    public static ArchiveOutputStream createArchiveOutputStream(OutputStream outputStream, ArchiveType archiveType)
            throws IOException {
        ArchiveOutputStream result = null;
        switch (archiveType) {
            case ZIP:
                result = new ZipArchiveOutputStream(outputStream);
                break;
            case TAR:
                result = new TarArchiveOutputStream(outputStream);
                ((TarArchiveOutputStream) result).setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
                break;
            case TARGZ:
                result = new TarArchiveOutputStream(new GzipCompressorOutputStream(outputStream));
                ((TarArchiveOutputStream) result).setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
                break;
            case TGZ:
                result = new TarArchiveOutputStream(new GzipCompressorOutputStream(outputStream));
                ((TarArchiveOutputStream) result).setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
                break;
        }

        if (result == null) {
            throw new IllegalArgumentException("Unsupported archive type: '" + archiveType + "'");
        }

        return result;
    }

    /**
     * Use for writing streams - must specify file size in advance as well
     */
    public static ArchiveEntry createArchiveEntry(String relativePath, ArchiveType archiveType, long size) {
        switch (archiveType) {
            case ZIP:
                ZipArchiveEntry zipEntry = new ZipArchiveEntry(relativePath);
                zipEntry.setSize(size);
                return zipEntry;
            case TAR:
            case TARGZ:
            case TGZ:
                TarArchiveEntry tarEntry = new TarArchiveEntry(relativePath);
                tarEntry.setSize(size);
                return tarEntry;
        }
        throw new IllegalArgumentException("Unsupported archive type: '" + archiveType + "'");
    }

    private static ArchiveEntry createArchiveEntry(File file, String relativePath, ArchiveType archiveType) {
        switch (archiveType) {
            case ZIP:
                return new ZipArchiveEntry(file, relativePath);
            case TAR:
                return new TarArchiveEntry(file, relativePath);
            case TARGZ:
                return new TarArchiveEntry(file, relativePath);
            case TGZ:
                return new TarArchiveEntry(file, relativePath);
        }

        throw new IllegalArgumentException("Unsupported archive type: '" + archiveType + "'");
    }
}
