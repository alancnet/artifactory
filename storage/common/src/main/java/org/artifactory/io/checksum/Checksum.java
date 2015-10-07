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
import org.apache.commons.lang.StringUtils;
import org.artifactory.checksum.ChecksumType;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * @author Yoav Landman
 */
public class Checksum {

    private final ChecksumType type;
    private final MessageDigest digest;
    private String checksum;

    /**
     * @param type The checksum type
     */
    public Checksum(ChecksumType type) {
        this.type = type;
        String algorithm = type.alg();
        try {
            digest = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(
                    "Cannot create a digest for algorithm: " + algorithm);
        }
    }

    public ChecksumType getType() {
        return type;
    }

    public String getChecksum() {
        if (checksum == null) {
            throw new IllegalStateException("Checksum not calculated yet.");
        }
        return checksum;
    }

    public void reset() {
        digest.reset();
    }

    void update(byte[] bytes, int off, int length) {
        digest.update(bytes, off, length);
    }

    void calc() {
        if (checksum != null) {
            throw new IllegalStateException("Checksum already calculated.");
        }
        // Encodes a byte array into a String that should be the length of the type (2 chars per byte)
        byte[] bytes = digest.digest();
        if (bytes.length * 2 != type.length()) {
            int bitLength = bytes.length * 8;
            throw new IllegalArgumentException(
                    "Unrecognised length for binary data: " + bitLength + " bits instead of " + (type.length() * 4));
        }
        StringBuilder sb = new StringBuilder();
        for (byte aBinaryData : bytes) {
            String t = Integer.toHexString(aBinaryData & 0xff);
            if (t.length() == 1) {
                sb.append("0");
            }
            sb.append(t);
        }
        checksum = sb.toString().trim();
    }

    /**
     * Reads and formats the checksum value from the given stream of a checksum file
     *
     * @param inputStream An input stream of checksum file
     * @return Extracted checksum value
     * @throws java.io.IOException If failed to read from the input stream
     */
    @SuppressWarnings({"unchecked"})
    public static String checksumStringFromStream(InputStream inputStream) throws IOException {
        List<String> lineList = IOUtils.readLines(inputStream, "utf-8");
        for (String line : lineList) {
            //Make sure the line isn't blank or commented out
            if (StringUtils.isNotBlank(line) && !line.startsWith("//")) {
                //Remove white spaces at the end
                line = line.trim();
                //Check for 'MD5 (name) = CHECKSUM'
                int prefixPos = line.indexOf(")= ");
                if (prefixPos != -1) {
                    line = line.substring(prefixPos + 3);
                }
                //We don't simply return the file content since some checksum files have more
                //characters at the end of the checksum file.
                String checksum = StringUtils.split(line)[0];
                return checksum;
            }
        }
        return "";
    }
}
