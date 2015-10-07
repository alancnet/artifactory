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

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Yossi Shaul
 */
@Test
public class FilesTest {
    private File baseTestDir;

    @BeforeMethod
    public void createTempDir() {
        baseTestDir = new File(System.getProperty("java.io.tmpdir"), "fileutilstest");
        baseTestDir.mkdirs();
        assertTrue(baseTestDir.exists(), "Failed to create base test dir");
    }

    @AfterMethod
    public void dleteTempDir() throws IOException {
        org.apache.commons.io.FileUtils.deleteDirectory(baseTestDir);
    }

    public void cleanupEmptyDirectoriesNonExistentDir() {
        File nonExistentFile = new File("pampam123");
        assertFalse(nonExistentFile.exists());
        Files.cleanupEmptyDirectories(nonExistentFile);
    }

    public void cleanupEmptyDirectoriesEmptyDir() {
        Files.cleanupEmptyDirectories(baseTestDir);
        assertTrue(baseTestDir.exists(), "Method should not delete base directory");
        Assert.assertEquals(baseTestDir.listFiles().length, 0, "Expected empty directory");
    }

    public void cleanupEmptyDirectoriesDirWithEmptyNestedDirectories() {
        File nested1 = createNestedDirectory("org/test");
        File nested2 = createNestedDirectory("org/apache");
        createNestedDirectory("org/apache/empty");

        Files.cleanupEmptyDirectories(baseTestDir);

        assertTrue(baseTestDir.exists(), "Method should not delete base directory");
        assertFalse(nested1.exists() || nested2.exists(), "Nested empty directory wasn't deleted");
        File[] files = baseTestDir.listFiles();
        Assert.assertEquals(files.length, 0, "Expected empty directory but received: " + Arrays.asList(files));
    }

    public void cleanupEmptyDirectoriesDirWithFiles() throws IOException {
        File nested1 = createNestedDirectory("org/test");
        File nested2 = createNestedDirectory("org/apache");
        // create empty file
        File file = new File(nested2, "emptyfile");
        org.apache.commons.io.FileUtils.touch(file);

        Files.cleanupEmptyDirectories(baseTestDir);

        assertTrue(baseTestDir.exists(), "Method should not delete base directory");
        assertFalse(nested1.exists(), "Nested empty directory wasn't deleted");
        assertTrue(nested2.exists(), "Nested directory was deleted but wasn't empty");
        Assert.assertEquals(nested2.listFiles().length, 1, "One file expected");
        Assert.assertEquals(nested2.listFiles()[0], file, "Unexpected file found " + file);
        File[] files = baseTestDir.listFiles();
        Assert.assertEquals(files.length, 1, "Expected 1 directory but received: " + Arrays.asList(files));
    }

    private File createNestedDirectory(String relativePath) {
        File nested = new File(baseTestDir, relativePath);
        assertTrue(nested.mkdirs(), "Failed to create nested directory" + nested);
        return nested;
    }

}
