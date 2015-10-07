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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * @author yoavl
 */
public abstract class ResourceUtils {
    private ResourceUtils() {
        // utility class
    }

    public static void copyResource(String resourcePath, File outputFile) throws IOException {
        copyResource(resourcePath, outputFile, null, null);
    }

    public static void copyResource(String resourcePath, File outputFile, InputStreamManipulator manipulator)
            throws IOException {
        copyResource(resourcePath, outputFile, manipulator, null);
    }

    public static void copyResource(String resourcePath, File outputFile, Class clazz) throws IOException {
        copyResource(resourcePath, outputFile, null, clazz);
    }

    public static void copyResource(String resourcePath, File outputFile, InputStreamManipulator manipulator,
            Class clazz) throws IOException {
        OutputStream os = null;
        try {
            os = new BufferedOutputStream(FileUtils.openOutputStream(outputFile));
            copyResource(resourcePath, os, manipulator, clazz);
        } finally {
            IOUtils.closeQuietly(os);
        }
    }

    public static void copyResource(String resourcePath, OutputStream outputStream, InputStreamManipulator manipulator,
            Class clazz) throws IOException {
        InputStream origInputStream = null;
        InputStream usedInputStream = null;
        try {
            origInputStream = clazz != null ?
                    clazz.getResourceAsStream(resourcePath) :
                    Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
            assertResourceNotNull(resourcePath, origInputStream);
            if (manipulator != null) {
                InputStream mip = manipulator.manipulate(origInputStream);
                if (mip == null) {
                    throw new RuntimeException("Received a null stream from stream manipulation");
                }
                usedInputStream = mip;
            } else {
                usedInputStream = origInputStream;
            }
            IOUtils.copy(usedInputStream, outputStream);
        } finally {
            IOUtils.closeQuietly(outputStream);
            IOUtils.closeQuietly(usedInputStream);
            IOUtils.closeQuietly(origInputStream);
        }
    }

    /**
     * Returns the specified resource input stream. Throws an exception if the resource is not found int the class path.
     *
     * @param path The resource path
     * @return The classpath resource input stream
     * @see Class#getResourceAsStream(java.lang.String)
     */
    public static InputStream getResource(String path) {
        InputStream is = ResourceUtils.class.getResourceAsStream(path);
        assertResourceNotNull(path, is);
        return is;
    }

    public static boolean resourceExists(String path) {
        return ResourceUtils.class.getResource(path) != null;
    }

    public static File getResourceAsFile(String path) {
        URL resource = ResourceUtils.class.getResource(path);
        assertResourceNotNull(path, resource);
        return new File(resource.getFile());
    }

    public static String getResourceAsString(String path) {
        InputStream is = null;
        try {
            is = getResource(path);
            return IOUtils.toString(is, "utf-8");
        } catch (IOException e) {
            throw new RuntimeException("Failed to transform resource at '" + path + "'to string", e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    public interface InputStreamManipulator {
        InputStream manipulate(InputStream origStream) throws IOException;
    }

    private static void assertResourceNotNull(String resourcePath, Object resourceHandle) {
        if (resourceHandle == null) {
            throw new IllegalArgumentException("Could not find the classpath resource at: " + resourcePath + ".");
        }
    }

}