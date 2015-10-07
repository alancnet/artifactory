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

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Tests the behavior of the file list element object
 *
 * @author Noam Y. Tenne
 */
@Test
public class FileListElementTest {

    /**
     * Tests the values of the object after initializing with the default constructor
     */
    public void testDefaultConstructor() {
        FileListElement fileListElement = new FileListElement();

        assertNull(fileListElement.getLastModified(), "Default last modified time should be null.");
        assertEquals(fileListElement.getSize(), 0L, "Unexpected default size.");
        assertNull(fileListElement.getUri(), "Default URI should be null.");
        assertFalse(fileListElement.isFolder(), "Default element should represent a file.");
    }

    /**
     * Tests the values of the object after initializing with the full constructor
     */
    public void testFullConstructor() {
        String uri = "uri";
        long size = 2323L;
        String lastModified = "lastModified";
        boolean folder = true;

        FileListElement fileListElement = new FileListElement(uri, size, lastModified, folder);

        assertEquals(fileListElement.getLastModified(), lastModified, "Unexpected last modified value.");
        assertEquals(fileListElement.getSize(), size, "Unexpected size value.");
        assertEquals(fileListElement.getUri(), uri, "Unexpected URI value.");
        assertTrue(fileListElement.isFolder(), "Unexpected Element type state.");
    }

    /**
     * Tests the values of the object after initializing them with their setters
     */
    public void testSetters() {
        String uri = "uri";
        long size = 2323L;
        String lastModified = "lastModified";
        boolean folder = true;

        FileListElement fileListElement = new FileListElement();

        fileListElement.setLastModified(lastModified);
        fileListElement.setSize(size);
        fileListElement.setUri(uri);
        fileListElement.setFolder(folder);

        assertEquals(fileListElement.getLastModified(), lastModified, "Unexpected last modified value.");
        assertEquals(fileListElement.getSize(), size, "Unexpected size value.");
        assertEquals(fileListElement.getUri(), uri, "Unexpected URI value.");
        assertTrue(fileListElement.isFolder(), "Unexpected Element type state.");
    }
}
