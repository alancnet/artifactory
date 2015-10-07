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

import org.apache.commons.io.IOUtils;
import org.artifactory.checksum.ChecksumInfo;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.checksum.ChecksumsInfo;
import org.artifactory.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Checksums calculations helper that wraps and uses the ChecksumInputStream.
 *
 * @author Yossi Shaul
 */
public abstract class Checksums {
    private static final Logger log = LoggerFactory.getLogger(Checksums.class);

    private Checksums() {
        // utility class
    }

    /**
     * Calculate checksum for the input type. Closes the input stream when done.
     *
     * @param in   Input streams for which checksums are calculated
     * @param type Checksum type to calculate
     * @return The computed checksum
     * @throws IOException On any exception reading from the stream
     */
    public static Checksum calculate(InputStream in, ChecksumType type) throws IOException {
        return calculate(in, new ChecksumType[]{type})[0];
    }

    /**
     * Calculate checksums for all the input types. Closes the input stream when done.
     *
     * @param in    Input streams for which checksums are calculated
     * @param types Checksum types to calculate
     * @return Array of all computed checksums
     * @throws IOException On any exception reading from the stream
     */
    public static Checksum[] calculate(InputStream in, ChecksumType... types) throws IOException {
        return calculateWithLength(in, types).getSecond();
    }

    /**
     * Calculate checksums for all the input types. Closes the input stream when done.
     *
     * @param in    Input streams for which checksums are calculated
     * @param types Checksum types to calculate
     * @return Pair where the first element is the amount of bytes read and the second is array of all computed
     * checksums
     * @throws IOException On any exception reading from the stream
     */
    public static Pair<Long, Checksum[]> calculateWithLength(InputStream in, ChecksumType... types) throws IOException {
        Checksum[] checksums = new Checksum[types.length];
        for (int i = 0; i < types.length; i++) {
            checksums[i] = new Checksum(types[i]);
        }

        ChecksumInputStream checksumsInputStream = new ChecksumInputStream(in, checksums);
        try {
            byte[] bytes = new byte[1024];
            while (checksumsInputStream.read(bytes) != -1) {
                // nothing to do, checksum output stream updates the checksums and calculate on stream close
            }
        } finally {
            IOUtils.closeQuietly(checksumsInputStream);
        }
        return new Pair<>(checksumsInputStream.getTotalBytesRead(), checksums);
    }

    /**
     * Calculate checksum for the input type.
     *
     * @param in   File for which checksums are calculated
     * @param type Checksum type to calculate
     * @return The computed checksum
     * @throws IOException On any exception reading from the stream
     */
    public static Checksum calculate(File file, ChecksumType type) throws IOException {
        return calculate(file, new ChecksumType[]{type})[0];
    }

    /**
     * Calculate checksums for all the input types.
     *
     * @param file  File for which checksums are calculated
     * @param types Checksum types to calculate
     * @return Array of all computed checksums
     * @throws IOException On any exception reading from the file
     */
    public static Checksum[] calculate(File file, ChecksumType[] types) throws IOException {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            return calculate(fileInputStream, types);
        } finally {
            IOUtils.closeQuietly(fileInputStream);
        }
    }

    public static ChecksumsInfo getChecksumsInfo(Checksum[] checksums, boolean withEqualOrigin) {
        ChecksumsInfo result = new ChecksumsInfo();
        for (Checksum checksum : checksums) {
            ChecksumType checksumType = checksum.getType();
            String calculatedChecksum = checksum.getChecksum();
            String original = null;
            if (withEqualOrigin) {
                original = calculatedChecksum;
            }
            ChecksumInfo missingChecksumInfo = new ChecksumInfo(checksumType, original, calculatedChecksum);
            result.addChecksumInfo(missingChecksumInfo);
        }
        return result;
    }

    public static void fillChecksumInfo(ChecksumsInfo checksumsInfo, Checksum[] checksums) {
        for (Checksum checksum : checksums) {
            ChecksumType checksumType = checksum.getType();
            String calculatedChecksum = checksum.getChecksum();
            ChecksumInfo checksumInfo = checksumsInfo.getChecksumInfo(checksumType);
            if (checksumInfo != null) {
                // set the actual checksum
                String original = checksumInfo.isMarkedAsTrusted() ?
                        ChecksumInfo.TRUSTED_FILE_MARKER : checksumInfo.getOriginal();
                checksumInfo = new ChecksumInfo(checksumType, original, calculatedChecksum);
                checksumsInfo.addChecksumInfo(checksumInfo);
                if (!checksumInfo.checksumsMatch()) {
                    log.debug("Checksum mismatch {}. original: {} calculated: {}",
                            new String[]{checksumType.toString(), checksumInfo.getOriginal(), calculatedChecksum});
                }
            } else {
                ChecksumInfo missingChecksumInfo = new ChecksumInfo(checksumType, null, calculatedChecksum);
                checksumsInfo.addChecksumInfo(missingChecksumInfo);
            }
        }
    }
}
