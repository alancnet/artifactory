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

package org.artifactory.api.fs;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.artifactory.checksum.ChecksumInfo;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.model.xstream.fs.FileAdditionalInfo;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;

import static org.testng.Assert.*;

/**
 * Tests FileAdditionalInfo class.
 *
 * @author Yossi Shaul
 */
@Test
public class FileAdditionalInfoTest {
    public static final String DUMMY_SHA1 = "1234567890123456789012345678901234567890";
    public static final String DUMMY2_SHA1 = "3234567890123456789012345678901234567890";

    public static final String DUMMY_MD5 = "12345678901234567890123456789012";
    public static final String DUMMY2_MD5 = "32345678901234567890123456789012";

    private FileAdditionalInfo info;
    private ChecksumInfo sha1;
    private ChecksumInfo md5;

    @BeforeMethod
    public void setup() {
        info = new FileAdditionalInfo();
        sha1 = new ChecksumInfo(ChecksumType.sha1, DUMMY_SHA1, DUMMY2_SHA1);
        md5 = new ChecksumInfo(ChecksumType.md5, DUMMY_MD5, DUMMY_MD5);
        HashSet<org.artifactory.checksum.ChecksumInfo> checksums = new HashSet<ChecksumInfo>(Arrays.asList(sha1, md5));
        info.setChecksums(checksums);
    }

    public void defaultConstructor() {
        FileAdditionalInfo info = new FileAdditionalInfo();
        assertNotNull(info.getChecksums(), "Checksums should not be null by default");
        assertNull(info.getSha1(), "Sha1 should be null by default");
        assertNull(info.getMd5(), "md5 should be null by default");
    }

    public void settingChecksums() {
        //Assert.assertEquals(info.getChecksums(), new HashSet<ChecksumInfo>(Arrays.asList(md5, sha1)));
        assertEquals(info.getSha1(), sha1.getActual());
        assertEquals(info.getMd5(), md5.getActual());
    }

    public void testIsIdentical() {
        FileAdditionalInfo copy = new FileAdditionalInfo(info);
        assertTrue(EqualsBuilder.reflectionEquals(info, copy), "Orig and copy differ");
        assertTrue(info.isIdentical(copy), "Orig and copy differ");
    }

    public void testNotIdentical() {
        FileAdditionalInfo copy = new FileAdditionalInfo(info);
        copy.getChecksumsInfo().addChecksumInfo(
                new ChecksumInfo(ChecksumType.md5, md5.getOriginal(), DUMMY_MD5.replace('3', 'a')));
        assertFalse(info.isIdentical(copy), "Orig and copy should differ");
    }

    public void copyConstructor() {
        ChecksumInfo checksum = new ChecksumInfo(ChecksumType.sha1, DUMMY2_SHA1, DUMMY_SHA1);
        FileAdditionalInfo orig = new FileAdditionalInfo();
        orig.addChecksumInfo(checksum);
        FileAdditionalInfo copy = new FileAdditionalInfo(orig);

        assertTrue(EqualsBuilder.reflectionEquals(orig, copy), "Orig and copy differ");
        assertTrue(orig.isIdentical(copy), "Orig and copy differ");
        assertNotSame(orig.getChecksumsInfo(), copy.getChecksumsInfo(),
                "Should have made a copy, not use the same object");
    }
}
