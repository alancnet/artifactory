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

import org.apache.commons.io.IOUtils;
import org.artifactory.resource.ResourceStreamHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * @author Yoav Landman
 */
public class TempFileStreamHandle implements ResourceStreamHandle {
    private static final Logger log = LoggerFactory.getLogger(NonClosingInputStream.class);

    private final File tmpFile;
    private final InputStream is;

    public TempFileStreamHandle(File tmpFile) throws FileNotFoundException {
        this.tmpFile = tmpFile;
        this.is = new BufferedInputStream(new FileInputStream(tmpFile));
    }

    @Override
    public InputStream getInputStream() {
        return is;
    }

    @Override
    public long getSize() {
        return -1;
    }

    @Override
    public void close() {
        IOUtils.closeQuietly(is);
        boolean deleted = tmpFile.delete();
        if (!deleted) {
            log.warn("Failed to delete temporary file '" + tmpFile.getPath() + "'.");
        }
    }
}