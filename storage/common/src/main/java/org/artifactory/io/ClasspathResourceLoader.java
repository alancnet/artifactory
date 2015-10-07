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

import org.artifactory.resource.ResourceStreamHandle;

import java.io.IOException;
import java.io.InputStream;

/**
 * User: freds Date: Jun 1, 2008 Time: 8:39:25 PM
 */
public class ClasspathResourceLoader implements ResourceStreamHandle {
    private final String resourceName;
    private InputStream is;

    public ClasspathResourceLoader(String resourceName) {
        this.resourceName = resourceName;
    }

    @Override
    public InputStream getInputStream() {
        if (is == null) {
            is = getClass().getResourceAsStream(resourceName);
            if (is == null) {
                throw new RuntimeException(
                        "Did not find resource " + resourceName + " in the classpath");
            }
        }
        return is;
    }

    @Override
    public long getSize() {
        return -1;
    }

    @Override
    public void close() {
        try {
            if (is != null) {
                is.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            is = null;
        }
    }
}
