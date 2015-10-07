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

package org.artifactory.io.checksum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/*
 * @author Yoav Landman
 */
public class ChecksumInputStream extends BufferedInputStream {
    private static final Logger log = LoggerFactory.getLogger(ChecksumInputStream.class);

    private final Checksum[] checksums;
    private boolean closed;
    /**
     * Total bytes read by this stream
     */
    private long totalBytesRead;

    public ChecksumInputStream(InputStream is, Checksum... checksums) {
        super(is);
        this.checksums = checksums;
    }

    public Checksum[] getChecksums() {
        return checksums;
    }

    public boolean isClosed() {
        return closed;
    }

    @Override
    public synchronized long skip(long n) throws IOException {
        throw new UnsupportedOperationException("Checksum input stream calculator does not support skip!");
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public synchronized void mark(int readlimit) {
        throw new UnsupportedOperationException("Checksum input stream calculator does not support mark!");
    }

    @Override
    public synchronized void reset() throws IOException {
        log.trace("Resetting {}", in);
        super.reset();
        totalBytesRead = 0L;
        for (Checksum checksum : checksums) {
            checksum.reset();
        }
    }

    @Override
    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read() throws IOException {
        byte b[] = new byte[1];
        return read(b);
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException {
        int bytesRead = super.read(b, off, len);
        log.trace("{} bytes read from {}", bytesRead, in);
        if (bytesRead != -1) {
            totalBytesRead += bytesRead;
            for (Checksum checksum : checksums) {
                checksum.update(b, off, bytesRead);
            }
        }
        return bytesRead;
    }

    @Override
    public void close() throws IOException {
        super.close();
        if (!closed) {
            log.trace("Total bytes read: {}", totalBytesRead);
            for (Checksum checksum : checksums) {
                checksum.calc();
                log.trace("Calculated checksum: '{}:{}'", checksum.getType(), checksum.getChecksum());
            }
            closed = true;
        }
    }

    /**
     * @return The total bytes read by this stream
     */
    public long getTotalBytesRead() {
        return totalBytesRead;
    }
}
