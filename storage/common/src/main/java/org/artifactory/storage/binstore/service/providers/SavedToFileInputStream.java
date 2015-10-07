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

package org.artifactory.storage.binstore.service.providers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Date: 12/17/12
 * Time: 8:54 AM
 *
 * @author freds
 */
public abstract class SavedToFileInputStream extends FilterInputStream {
    private static final Logger log = LoggerFactory.getLogger(SavedToFileInputStream.class);

    final File tempFile;
    final FileOutputStream toWriteTo;

    long bytesRead = 0L;
    boolean fullyRead = false;
    boolean closed = false;
    IOException somethingWrong = null;

    SavedToFileInputStream(InputStream in, File tempFile) throws IOException {
        super(in);
        this.tempFile = tempFile;
        this.toWriteTo = new FileOutputStream(tempFile);
    }

    public File getTempFile() {
        return tempFile;
    }

    @Override
    public int read() throws IOException {
        int b = in.read();
        if (b == -1) {
            fullyRead = true;
        } else {
            toWriteTo.write(b);
            bytesRead++;
        }
        return b;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int byteRead = in.read(b);
        if (byteRead == -1) {
            fullyRead = true;
        } else {
            toWriteTo.write(b, 0, byteRead);
            bytesRead += byteRead;
        }
        return byteRead;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int byteRead = in.read(b, off, len);
        if (byteRead == -1) {
            fullyRead = true;
        } else {
            toWriteTo.write(b, off, byteRead);
            bytesRead += byteRead;
        }
        return byteRead;
    }

    @Override
    public long skip(long n) throws IOException {
        somethingWrong = new IOException("Someone called skipped " + n);
        return super.skip(n);
    }

    @Override
    public void close() throws IOException {
        super.close();
        if (!closed) {
            try {
                try {
                    toWriteTo.close();
                } catch (IOException e) {
                    somethingWrong = e;
                    log.error("Could not close the file output stream: " + e.getMessage(), e);
                }
            } finally {
                closed = true;
                if (afterClose()) {
                    verifyTempDeleted();
                }
            }
        }
    }

    void verifyTempDeleted() {
        if (tempFile.exists()) {
            log.debug("Deleting temp file file " + tempFile.getAbsolutePath());
            if (!tempFile.delete()) {
                log.error("Could not delete temp file " + tempFile.getAbsolutePath());
            }
        }
    }

    /**
     * Called after close method called and delegate stream closed.
     *
     * @return true if temp file should be deleted, false otherwise
     * @throws java.io.IOException
     */
    protected abstract boolean afterClose() throws IOException;

    void moveTempFileTo(File cachedFile) throws IOException {
        File parentFile = cachedFile.getParentFile();
        if (!parentFile.exists()) {
            log.debug("Creating first level filestore folder " + parentFile.getAbsolutePath());
            if (!parentFile.mkdir()) {
                log.info("First level filestore folder "
                        + parentFile.getAbsolutePath() + " returned false on mkdir!");
            }
        }
        if (!parentFile.exists()) {
            throw new IOException("Could not create filestore folder " + parentFile.getAbsolutePath());
        }
        if (!tempFile.renameTo(cachedFile)) {
            log.warn("Did not managed to rename temp file " + tempFile + " to " + cachedFile);
        }
    }

    @Override
    public void reset() throws IOException {
        somethingWrong = new IOException("Someone called reset");
        super.reset();
    }
}
