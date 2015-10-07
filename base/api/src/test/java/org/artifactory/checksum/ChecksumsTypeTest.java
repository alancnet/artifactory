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

package org.artifactory.checksum;

import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests the {@link org.artifactory.checksum.ChecksumType} enum.
 *
 * @author Yossi Shaul
 */
@Test
public class ChecksumsTypeTest {

    public void nullAndEmptyAreNotValidChecksums() {
        for (ChecksumType checksumType : ChecksumType.values()) {
            assertFalse(checksumType.isValid(null));
            assertFalse(checksumType.isValid(""));
        }
    }

    public void wrongLengthChecksums() {
        for (ChecksumType checksumType : ChecksumType.values()) {
            assertFalse(checksumType.isValid("aaa"));
        }
    }

    public void invalidMD5Checksum() {
        // good length but not hexadecimal
        assertFalse(ChecksumType.md5.isValid("xf222ca7499ed5bc49fe25a1182c59f7"));
    }

    public void invalidSha1Checksum() {
        // good length but not hexadecimal
        assertFalse(ChecksumType.sha1.isValid("96bcc93bec1f99e45b6c1bdfcef73948b8fa122g"));
    }

    public void invalidSha256Checksum() {
        // good length but not hexadecimal
        assertFalse(ChecksumType.sha256.isValid("e3b0x44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"));
    }

    public void validMD5Checksum() {
        assertTrue(ChecksumType.md5.isValid("2f222ca7499ed5bc49fe25a1182c59f7"));
        assertTrue(ChecksumType.md5.isValid("d06a3ab307d28384a235d0ab6b70d3ae"));
    }

    public void validSha1Checksum() {
        assertTrue(ChecksumType.sha1.isValid("911ca40cdb527969ee47dc6f782425d94a36b510"));
        assertTrue(ChecksumType.sha1.isValid("96bcc93bec1f99e45b6c1bdfcef73948b8fa122c"));
    }

    public void validSha256Checksum() {
        assertTrue(ChecksumType.sha256.isValid("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"));
        assertTrue(ChecksumType.sha256.isValid("5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8"));
    }

}
