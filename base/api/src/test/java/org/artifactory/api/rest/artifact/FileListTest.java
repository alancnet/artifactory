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

package org.artifactory.api.rest.artifact;

import com.google.common.collect.Lists;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * Tests the behavior of the file list object
 *
 * @author Noam Y. Tenne
 */
@Test
public class FileListTest {

    /**
     * Tests the values of the object after initializing with the default constructor
     */
    @Test
    public void testDefaultConstructor() throws Exception {
        FileList fileList = new FileList();

        assertNull(fileList.getCreated(), "Default created time should be null.");
        assertNull(fileList.getFiles(), "Default file list should be null.");
        assertNull(fileList.getUri(), "Default URI should be null.");
    }

    /**
     * Tests the values of the object after initializing with the full constructor
     */
    @Test
    public void testFullConstructor() throws Exception {
        String uri = "uri";
        FileListElement folder = new FileListElement("uri", 324234L, "lastModified", true);
        FileListElement file = new FileListElement("uri", 324234L, "lastModified", false);
        List<FileListElement> files = Lists.newArrayList(folder, file);
        String created = "created";

        FileList fileList = new FileList(uri, created, files);

        assertEquals(fileList.getCreated(), created, "Unexpected created time value.");
        assertEquals(fileList.getFiles(), files, "Unexpected file list value.");
        assertEquals(fileList.getFiles().get(0), folder, "Unexpected list content value.");
        assertEquals(fileList.getFiles().get(1), file, "Unexpected list content value.");
    }

    /**
     * Tests the values of the object after initializing them with their setters
     */
    @Test
    public void testSetters() throws Exception {
        String uri = "uri";
        FileListElement folder = new FileListElement("uri", 324234L, "lastModified", true);
        FileListElement file = new FileListElement("uri", 324234L, "lastModified", false);
        List<FileListElement> files = Lists.newArrayList(folder, file);
        String created = "created";

        FileList fileList = new FileList();

        fileList.setCreated(created);
        fileList.setFiles(files);
        fileList.setUri(uri);

        assertEquals(fileList.getCreated(), created, "Unexpected created time value.");
        assertEquals(fileList.getFiles(), files, "Unexpected file list value.");
        assertEquals(fileList.getFiles().get(0), folder, "Unexpected list content value.");
        assertEquals(fileList.getFiles().get(1), file, "Unexpected list content value.");
    }
}
