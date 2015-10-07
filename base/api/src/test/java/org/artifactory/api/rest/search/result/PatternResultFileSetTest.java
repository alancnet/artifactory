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

package org.artifactory.api.rest.search.result;

import com.google.common.collect.Sets;
import org.testng.annotations.Test;

import java.util.Set;

import static org.testng.Assert.*;

/**
 * Tests the behavior of the pattern search result class
 *
 * @author Noam Y. Tenne
 */
@Test
public class PatternResultFileSetTest {

    /**
     * Test the object values when initializing the default constructor
     */
    public void testDefaultConstructor() {
        PatternResultFileSet fileSet = new PatternResultFileSet();

        assertNull(fileSet.getRepoUri(), "Default repo URI should be null.");
        assertNull(fileSet.getSourcePattern(), "Default source pattern should be null.");
        assertNotNull(fileSet.getFiles(), "Default file set should not be null.");
        assertTrue(fileSet.getFiles().isEmpty(), "Default file set should be empty.");
    }

    /**
     * Test the object values when initializing the constructor that does not specify the file set
     */
    public void testNoFilesConstructor() {
        String repoUri = "repoUri";
        String sourcePattern = "sourcePattern";

        PatternResultFileSet fileSet = new PatternResultFileSet(repoUri, sourcePattern);

        assertEquals(fileSet.getRepoUri(), repoUri, "Unexpected repo URI.");
        assertEquals(fileSet.getSourcePattern(), sourcePattern, "Unexpected source pattern.");
        assertNotNull(fileSet.getFiles(), "Default file set should not be null.");
        assertTrue(fileSet.getFiles().isEmpty(), "Default file set should be empty.");
    }

    /**
     * Test the object values when initializing the full constructor
     */
    public void testFullConstructor() {
        String repoUri = "repoUri";
        String sourcePattern = "sourcePattern";
        Set<String> files = Sets.newHashSet("file");

        PatternResultFileSet fileSet = new PatternResultFileSet(repoUri, sourcePattern, files);

        assertEquals(fileSet.getRepoUri(), repoUri, "Unexpected repo URI.");
        assertEquals(fileSet.getSourcePattern(), sourcePattern, "Unexpected source pattern.");
        assertEquals(fileSet.getFiles(), files, "Unexpected file set.");
    }

    /**
     * Test the object values after initializing them with setters
     */
    public void testSetters() {
        String repoUri = "repoUri";
        String sourcePattern = "sourcePattern";
        Set<String> files = Sets.newHashSet("file");

        PatternResultFileSet fileSet = new PatternResultFileSet();
        fileSet.setFiles(files);
        fileSet.setRepoUri(repoUri);
        fileSet.setSourcePattern(sourcePattern);

        assertEquals(fileSet.getRepoUri(), repoUri, "Unexpected repo URI.");
        assertEquals(fileSet.getSourcePattern(), sourcePattern, "Unexpected source pattern.");
        assertEquals(fileSet.getFiles(), files, "Unexpected file set.");
    }

    /**
     * Test the file set value after initializing with the add method
     */
    public void testAddMethod() {
        PatternResultFileSet fileSet = new PatternResultFileSet();
        fileSet.setFiles(null);
        fileSet.addFile("file");
        assertNotNull(fileSet.getFiles(), "File set should have been initialized.");
        assertFalse(fileSet.getFiles().isEmpty(), "File set should contain the added element.");
        assertEquals(fileSet.getFiles().iterator().next(), "file", "Unexpected file.");
    }
}
