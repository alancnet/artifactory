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

package org.artifactory.mime;

import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.artifactory.util.ResourceUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileFilter;
import java.util.Set;

import static org.testng.Assert.*;

/**
 * Tests the {@link MimeTypesReader}
 *
 * @author Yossi Shaul
 */
@SuppressWarnings("ConstantConditions")
@Test
public class MimeTypesReaderTest {
    private MimeTypes mimeTypes;

    @BeforeClass
    public void setup() {
        MimeTypesReader reader = new MimeTypesReader();
        mimeTypes = reader.read(ResourceUtils.getResourceAsFile("/org/artifactory/mime/mimetypes-test.xml"));
        assertNotNull(mimeTypes, "Should not return null");
        assertEquals(mimeTypes.getMimeTypes().size(), 5, "Unexpected count of mime types");
    }

    public void checkArchiveMime() throws Exception {
        MimeType archive = mimeTypes.getByMime(MimeType.javaArchive);
        assertNotNull(archive, "Couldn't find application/java-archive mime");
        assertEquals(archive.getExtensions().size(), 5, "Unexpected file extensions count");
        assertFalse(archive.isViewable(), "Should not be viewable");
        assertFalse(archive.isIndex(), "Should be marked as indexed");
        assertFalse(archive.isArchive(), "Should be marked as archive");
        assertNull(archive.getSyntax(), "No syntax configured for this type");
        assertNull(archive.getCss(), "No css class configured for this type");
    }

    public void trimmedFileExtensions() throws Exception {
        // the file extensions list is usually with spaces that should be trimmed
        MimeType archive = mimeTypes.getByMime(MimeType.javaArchive);
        Set<String> extensions = archive.getExtensions();
        assertTrue(extensions.contains("war"), "war extension not found in: " + extensions);
        assertTrue(extensions.contains("jar"), "jar extension not found in: " + extensions);
    }

    public void readConvertVersion1() {
        File versionsDirectory = ResourceUtils.getResourceAsFile("/org/artifactory/mime/version/mimetypes-v1.xml");
        MimeTypes result = new MimeTypesReader().read(versionsDirectory);
        assertFalse(result.getByExtension("xml").isIndex());
        assertTrue(result.getByExtension("pom").isIndex());
        assertTrue(result.getByExtension("ivy").isIndex());
    }

    public void readConvertVersion5() {
        File versionsDirectory = ResourceUtils.getResourceAsFile("/org/artifactory/mime/version/mimetypes-v5.xml");
        MimeTypes result = new MimeTypesReader().read(versionsDirectory);
        MimeType json = result.getByExtension("json");
        assertNotNull(json, "Expected new json mime type not found");
        assertTrue(json.isViewable());
    }

    public void versionsRead() {
        File versionsDirectory = ResourceUtils.getResourceAsFile(
                "/org/artifactory/mime/version/mimetypes-v1.xml").getParentFile();
        File[] mimeTypeFiles = versionsDirectory.listFiles((FileFilter) new SuffixFileFilter("xml"));
        assertTrue(mimeTypeFiles.length > 0, "Couldn't find mime types files under "
                + versionsDirectory.getAbsolutePath());
        for (File mimeTypeFile : mimeTypeFiles) {
            MimeTypesReader reader = new MimeTypesReader();
            reader.read(mimeTypeFile);  // will throw an exception on error
        }
    }

}
