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

import org.artifactory.checksum.ChecksumType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Noam Y. Tenne
 */
public class ChecksumOutputStream extends BufferedOutputStream {
    private static final Logger log = LoggerFactory.getLogger(ChecksumOutputStream.class);

    private final Checksum[] checksums;
    private boolean closed;
    /**
     * Total bytes written by this stream
     */
    private long totalBytesWritten;

    public ChecksumOutputStream(OutputStream out, Checksum... checksums) {
        super(out);
        this.checksums = checksums;
    }

    public Checksum[] getChecksums() {
        return checksums;
    }

    public Checksum getChecksum(ChecksumType type) {
        for (Checksum checksum : checksums) {
            if (checksum.getType().equals(type)) {
                return checksum;
            }
        }

        return null;
    }

    @Override
    public void write(int b) throws IOException {
        write(new byte[]{((byte) b)}, 0, 1);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        super.write(b, off, len);
        log.trace("{} bytes written to {}", len, out);
        if (len != -1) {
            totalBytesWritten += len;
            for (Checksum checksum : checksums) {
                checksum.update(b, off, len);
            }
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void close() throws IOException {
        super.close();
        if (!closed) {
            log.trace("Total bytes written: {}", totalBytesWritten);
            for (Checksum checksum : checksums) {
                checksum.calc();
                log.trace("Calculated checksum: '{}:{}'", checksum.getType(), checksum.getChecksum());
            }
            closed = true;
        }
    }

    public long getTotalBytesWritten() {
        return totalBytesWritten;
    }
}
