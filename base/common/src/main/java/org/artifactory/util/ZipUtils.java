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

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.archive.ArchiveType;
import org.artifactory.common.ConstantValues;
import org.artifactory.sapi.fs.VfsFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * A utility class to perform different archive related actions
 *
 * @author Noam Tenne
 */
public abstract class ZipUtils {
    private static final Logger log = LoggerFactory.getLogger(ZipUtils.class);

    /**
     * Archives the contents of the given directory into the given archive using the apache commons compress tools
     *
     * @param sourceDirectory    Directory to archive
     * @param destinationArchive Archive file to create
     * @param recurse            True if should recurse file scan of source directory. False if not
     * @throws IOException              Any exceptions that might occur while handling the given files and used streams
     * @throws IllegalArgumentException Thrown when given invalid destinations
     * @see ArchiveUtils#archive(java.io.File, java.io.File, boolean, org.artifactory.api.archive.ArchiveType)
     */
    public static void archive(File sourceDirectory, File destinationArchive, boolean recurse)
            throws IOException {
        ArchiveUtils.archive(sourceDirectory, destinationArchive, recurse, ArchiveType.ZIP);
    }

    /**
     * Extracts the given archive file into the given directory
     *
     * @param sourceArchive        Archive to extract
     * @param destinationDirectory Directory to extract achive to
     * @throws Exception                Any exception which are thrown
     * @throws IllegalArgumentException Thrown when given invalid destinations
     * @throws Exception                Thrown when any error occures while extracting
     */
    public static void extract(File sourceArchive, File destinationDirectory) throws Exception {
        if ((sourceArchive == null) || (destinationDirectory == null)) {
            throw new IllegalArgumentException("Supplied destinations cannot be null.");
        }
        if (!sourceArchive.isFile()) {
            throw new IllegalArgumentException("Supplied source archive must be an existing file.");
        }
        String sourcePath = sourceArchive.getAbsolutePath();
        String destinationPath = destinationDirectory.getAbsolutePath();
        log.debug("Beginning extraction of '{}' into '{}'", sourcePath, destinationPath);
        extractFiles(sourceArchive, destinationDirectory);
        log.debug("Completed extraction of '{}' into '{}'", sourcePath, destinationPath);
    }

    /**
     * @param zis       The zip input stream
     * @param entryPath The entry path to search for
     * @return The entry if found, null otherwise
     * @throws IOException On failure to read the stream
     * @see ZipUtils#locateEntry(java.util.zip.ZipInputStream, java.lang.String, java.util.List<java.lang.String>)
     */
    public static ArchiveEntry locateArchiveEntry(ArchiveInputStream zis, String entryPath) throws IOException {
        return locateArchiveEntry(zis, entryPath, null);
    }

    /**
     * @param zis       The zip input stream
     * @param entryPath The entry path to search for
     * @return The entry if found, null otherwise
     * @throws IOException On failure to read the stream
     * @see ZipUtils#locateEntry(java.util.zip.ZipInputStream, java.lang.String, java.util.List<java.lang.String>)
     */
    public static ZipEntry locateEntry(ZipInputStream zis, String entryPath) throws IOException {
        return locateEntry(zis, entryPath, null);
    }


    /**
     * Searches for an entry inside the zip stream by entry path. If there are alternative extensions, will also look
     * for entry with alternative extension. The search stops reading the stream when the entry is found, so calling
     * read on the stream will read the returned entry. <p/>
     * The zip input stream doesn't support mark/reset so once this method is used you cannot go back - either the
     * stream was fully read (when entry is not found) or the stream was read until the current entry.
     *
     * @param zis                   The zip input stream
     * @param entryPath             The entry path to search for
     * @param alternativeExtensions List of alternative file extensions to try if the main entry path is not found.
     * @return The entry if found, null otherwise
     * @throws IOException On failure to read the stream
     */
    public static ZipEntry locateEntry(ZipInputStream zis, String entryPath, List<String> alternativeExtensions)
            throws IOException {
        ZipEntry zipEntry;
        while ((zipEntry = zis.getNextEntry()) != null) {
            String zipEntryName = zipEntry.getName();
            if (zipEntryName.equals(entryPath)) {
                return zipEntry;
            } else if (alternativeExtensions != null) {
                String basePath = PathUtils.stripExtension(entryPath);
                for (String alternativeExtension : alternativeExtensions) {
                    String alternativeSourcePath = basePath + "." + alternativeExtension;
                    if (zipEntryName.equals(alternativeSourcePath)) {
                        return zipEntry;
                    }
                }
            }
        }
        return null;
    }


    /**
     * Searches for an entry inside the zip stream by entry path. If there are alternative extensions, will also look
     * for entry with alternative extension. The search stops reading the stream when the entry is found, so calling
     * read on the stream will read the returned entry. <p/>
     * The zip input stream doesn't support mark/reset so once this method is used you cannot go back - either the
     * stream was fully read (when entry is not found) or the stream was read until the current entry.
     *
     * @param zis                   The ar input stream
     * @param entryPath             The entry path to search for
     * @param alternativeExtensions List of alternative file extensions to try if the main entry path is not found.
     * @return The entry if found, null otherwise
     * @throws IOException On failure to read the stream
     */
    public static ArchiveEntry locateArchiveEntry(ArchiveInputStream zis, String entryPath,
            List<String> alternativeExtensions)
            throws IOException {
        ArchiveEntry archiveEntry;
        while ((archiveEntry = zis.getNextEntry()) != null) {
            String zipEntryName = archiveEntry.getName();
            if (zipEntryName.equals(entryPath)) {
                return archiveEntry;
            } else if (alternativeExtensions != null) {
                String basePath = PathUtils.stripExtension(entryPath);
                for (String alternativeExtension : alternativeExtensions) {
                    String alternativeSourcePath = basePath + "." + alternativeExtension;
                    if (zipEntryName.equals(alternativeSourcePath)) {
                        return archiveEntry;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Extracts the given archive file into the given directory
     *
     * @param sourceArchive        Archive to extract
     * @param destinationDirectory Directory to extract archive to
     */
    private static void extractFiles(File sourceArchive, File destinationDirectory) {
        ArchiveInputStream archiveInputStream = null;
        try {
            archiveInputStream = createArchiveInputStream(sourceArchive);
            ArchiveEntry zipEntry;
            while ((zipEntry = archiveInputStream.getNextEntry()) != null) {
                //Validate entry name before extracting
                String validatedEntryName = validateEntryName(zipEntry.getName());

                if (StringUtils.isNotBlank(validatedEntryName)) {
                    extractFile(sourceArchive, destinationDirectory, archiveInputStream, validatedEntryName,
                            zipEntry.getLastModifiedDate(), zipEntry.isDirectory());
                }
            }

        } catch (IOException ioe) {
            throw new RuntimeException("Error while extracting " + sourceArchive.getPath(), ioe);
        } finally {
            IOUtils.closeQuietly(archiveInputStream);
        }
    }

    /**
     * get archive input stream from File Object
     *
     * @param sourceArchive - archive File
     * @return archive input stream
     * @throws IOException
     */
    private static ArchiveInputStream createArchiveInputStream(File sourceArchive) throws IOException {
        String fileName = sourceArchive.getName();
        String extension = PathUtils.getExtension(fileName);
        verifySupportedExtension(extension);
        FileInputStream fis = new FileInputStream(sourceArchive);
        ArchiveInputStream archiveInputStream = returnArchiveInputStream(fis, extension);
        if (archiveInputStream != null) {
            return archiveInputStream;
        }
        throw new IllegalArgumentException("Unsupported archive extension: '" + extension + "'");
    }

    /**
     *  get archive input stream from VfsFile Object
     * @param file - archive vfs file
     * @return archive input stream
     * @throws IOException
     */
    public static ArchiveInputStream getArchiveInputStream(VfsFile file) throws IOException {
        String archiveSuffix = file.getPath().toLowerCase();
        ArchiveInputStream archiveInputStream = returnArchiveInputStream(file.getStream(), archiveSuffix);
        if (archiveInputStream != null) {
            return archiveInputStream;
        }
        return new TarArchiveInputStream(file.getStream());
    }

    /**
     * return archive input stream
     *
     * @param inputStream - file  input Stream
     * @param archiveSuffix   - archive suffix
     * @return archive input stream
     * @throws IOException
     */
    public static ArchiveInputStream returnArchiveInputStream(InputStream inputStream, String archiveSuffix)
            throws IOException {
        if (isZipFamilyArchive(archiveSuffix)) {
            return new ZipArchiveInputStream(inputStream);
        }

        if (isTarArchive(archiveSuffix)) {
            return new TarArchiveInputStream(inputStream);
        }

        if (isTgzFamilyArchive(archiveSuffix) || isGzCompress(archiveSuffix)) {
            return new TarArchiveInputStream(new GzipCompressorInputStream(inputStream));
        }
        return null;
    }

    /**
     * get archive input stream array
     * @param file - file archive
     * @param length - length of array
     * @return  -array of archive input stream
     * @throws IOException
     */
    public static ArchiveInputStream[] getArchiveInputStreamArray(String file, int length) throws IOException {
        String archiveSuffix = file.toLowerCase();
        if (isZipFamilyArchive(archiveSuffix)) {
            return new ZipArchiveInputStream[length];
        }
        if (isTarArchive(archiveSuffix) || isTgzFamilyArchive(archiveSuffix)) {
            return new TarArchiveInputStream[length];
        }
        return new TarArchiveInputStream[length];
    }

    /**
     * is file suffix related to gz compress
     *
     * @param archiveSuffix - archive file suffix
     * @return
     */
    private static boolean isGzCompress(String archiveSuffix) {
        return archiveSuffix.equals("gz");
    }

    /**
     * is file suffix related to tar archive
     *
     * @param archiveSuffix - archive suffix
     * @return
     */
    private static boolean isTarArchive(String archiveSuffix) {
        return archiveSuffix.endsWith("tar");
    }

    private static boolean isTgzFamilyArchive(String archiveSuffix) {
        return archiveSuffix.endsWith("tar.gz") || archiveSuffix.endsWith("tgz");
    }

    private static boolean isZipFamilyArchive(String archiveSuffix) {
        return archiveSuffix.endsWith("zip") || archiveSuffix.endsWith("jar") || archiveSuffix.toLowerCase().endsWith(
                "nupkg");
    }

    private static void verifySupportedExtension(String extension) {
        Set<String> supportedExtensions = Sets.newHashSet();
        try {
            String supportedExtensionsNames = ConstantValues.requestExplodedArchiveExtensions.getString();
            supportedExtensions = Sets.newHashSet(
                    Iterables.transform(Sets.newHashSet(StringUtils.split(supportedExtensionsNames, ",")),
                            new Function<String, String>() {
                                @Override
                                public String apply(@Nullable String input) {
                                    String result = StringUtils.isBlank(input) ? input : StringUtils.trim(input);
                                    return StringUtils.equals(result, "tar.gz") ? "gz" : result;
                                }
                            }
                    )
            );
        } catch (Exception e) {
            log.error("Failed to parse global default excludes. Using default values: " + e.getMessage());
        }

        if (StringUtils.isBlank(extension) || !supportedExtensions.contains(extension)) {
            throw new IllegalArgumentException("Unsupported archive extension: '" + extension + "'");
        }
    }

    /**
     * Extracts the given zip entry
     *
     * @param sourceArchive        Archive that is being extracted
     * @param destinationDirectory Extracted file destination
     * @param zipInputStream       Input stream of archive
     * @param entryName            Entry to extract
     * @param entryDate            Last modification date of zip entry
     * @param isEntryDirectory     Indication if the entry is a directory or not
     * @throws IOException
     */
    private static void extractFile(File sourceArchive, File destinationDirectory, InputStream zipInputStream,
            String entryName, Date entryDate, boolean isEntryDirectory) throws IOException {

        File resolvedEntryFile = org.codehaus.plexus.util.FileUtils.resolveFile(destinationDirectory, entryName);
        try {
            File parentFile = resolvedEntryFile.getParentFile();

            //If the parent file isn't null, attempt to create it because it might not exist
            if (parentFile != null) {
                parentFile.mkdirs();
            }

            if (isEntryDirectory) {
                //Create directory entry
                resolvedEntryFile.mkdirs();
            } else {
                //Extract file entry
                byte[] buffer = new byte[1024];
                int length;
                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = new FileOutputStream(resolvedEntryFile);

                    while ((length = zipInputStream.read(buffer)) >= 0) {
                        fileOutputStream.write(buffer, 0, length);
                    }
                } finally {
                    IOUtils.closeQuietly(fileOutputStream);
                }
            }

            //Preserve last modified date
            resolvedEntryFile.setLastModified(entryDate.getTime());
        } catch (FileNotFoundException ex) {
            throw new RuntimeException("Can't extract file " + sourceArchive.getPath(), ex);
        }
    }

    /**
     * Validates the given entry name by removing different slashes that might appear in the begining of the name and
     * any occurences of relative paths like "../", so we can protect from path traversal attacks
     *
     * @param entryName Name of zip entry
     */
    private static String validateEntryName(String entryName) {
        entryName = FilenameUtils.separatorsToUnix(entryName);
        entryName = PathUtils.trimLeadingSlashes(entryName);
        entryName = removeDotSegments(entryName);

        return entryName;
    }

    //"Borrowed" from com.sun.jersey.server.impl.uri.UriHelper
    // alg taken from http://gbiv.com/protocols/uri/rfc/rfc3986.html#relative-dot-segments
    // the alg works as follows:
    //       1. The input buffer is initialized with the now-appended path components and the output buffer is initialized to the empty string.
    //   2. While the input buffer is not empty, loop as follows:
    //         A. If the input buffer begins with a prefix of "../" or "./", then remove that prefix from the input buffer; otherwise,
    //         B. if the input buffer begins with a prefix of "/./"
    //            or "/.", where "." is a complete path segment, then replace that prefix with "/" in the input buffer; otherwise,
    //         C. if the input buffer begins with a prefix of "/../"
    //            or "/..", where ".." is a complete path segment,
    //            then replace that prefix with "/" in the input buffer and remove the last segment and its preceding "/" (if any) from the output buffer; otherwise,
    //         D. if the input buffer consists only of "." or "..", then remove that from the input buffer; otherwise,
    //         E. move the first path segment in the input buffer to the end of the output buffer,
    //            including the initial "/" character (if any) and any subsequent characters up to, but not including,
    //            the next "/" character or the end of the input buffer.
    //   3. Finally, the output buffer is returned as the result of remove_dot_segments.

    @SuppressWarnings({"OverlyComplexMethod"})
    private static String removeDotSegments(String path) {

        if (null == path) {
            return null;
        }

        List<String> outputSegments = new LinkedList<>();

        while (path.length() > 0) {
            if (path.startsWith("../")) {   // rule 2A
                path = PathUtils.trimLeadingSlashes(path.substring(3));
            } else if (path.startsWith("./")) { // rule 2A
                path = PathUtils.trimLeadingSlashes(path.substring(2));
            } else if (path.startsWith("/./")) { // rule 2B
                path = "/" + PathUtils.trimLeadingSlashes(path.substring(3));
            } else if ("/.".equals(path)) { // rule 2B
                path = "/";
            } else if (path.startsWith("/../")) { // rule 2C
                path = "/" + PathUtils.trimLeadingSlashes(path.substring(4));
                if (!outputSegments.isEmpty()) { // removing last segment if any
                    outputSegments.remove(outputSegments.size() - 1);
                }
            } else if ("/..".equals(path)) { // rule 2C
                path = "/";
                if (!outputSegments.isEmpty()) { // removing last segment if any
                    outputSegments.remove(outputSegments.size() - 1);
                }
            } else if ("..".equals(path) || ".".equals(path)) { // rule 2D
                path = "";
            } else { // rule E
                int slashStartSearchIndex;
                if (path.startsWith("/")) {
                    path = "/" + PathUtils.trimLeadingSlashes(path.substring(1));
                    slashStartSearchIndex = 1;
                } else {
                    slashStartSearchIndex = 0;
                }
                int segLength = path.indexOf('/', slashStartSearchIndex);
                if (-1 == segLength) {
                    segLength = path.length();
                }
                outputSegments.add(path.substring(0, segLength));
                path = path.substring(segLength);
            }
        }

        StringBuffer result = new StringBuffer();
        for (String segment : outputSegments) {
            result.append(segment);
        }

        return result.toString();
    }
}
