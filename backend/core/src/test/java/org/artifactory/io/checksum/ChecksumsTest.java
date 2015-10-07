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
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Tests the ChecksumCalculator.
 *
 * @author Yossi Shaul
 */
@Test
public class ChecksumsTest {

    public void calculateSha1() throws IOException {
        byte[] bytes = "this is a test".getBytes();
        Checksum result = Checksums.calculate(new ByteArrayInputStream(bytes), ChecksumType.sha1);
        assertEquals(result.getChecksum(), "fa26be19de6bff93f70bc2308434e4a440bbad02",
                "Wrong SHA1 calculated");
    }

    public void calculateMd5() throws IOException {
        byte[] bytes = "this is a test".getBytes();
        Checksum result = Checksums.calculate(new ByteArrayInputStream(bytes), ChecksumType.md5);
        assertEquals(result.getChecksum(), "54b0c58c7ce9f2a8b551351102ee0938",
                "Wrong SHA1 calculated");
    }

    public void calculateSha1AndMd5() throws IOException {
        byte[] bytes = "and this is another test".getBytes();
        Checksum[] results = Checksums.calculate(new ByteArrayInputStream(bytes), ChecksumType.BASE_CHECKSUM_TYPES);
        assertNotNull(results, "Results should not be null");
        assertEquals(results.length, 2, "Expecting two calculated value");
        assertEquals(results[0].getChecksum(), "5258d99970d60aed055c0056a467a0422acf7cb8",
                "Wrong SHA1 calculated");
        assertEquals(results[1].getChecksum(), "72f1aea68f75f79889b99cd4ff7acc83",
                "Wrong MD5 calculated");
    }

    public void calculateAllKnownChecksums() throws IOException {
        byte[] bytes = "and this is another test".getBytes();
        Checksum[] results = Checksums.calculate(new ByteArrayInputStream(bytes), ChecksumType.BASE_CHECKSUM_TYPES);
        assertNotNull(results, "Results should not be null");
        assertEquals(results.length, ChecksumType.BASE_CHECKSUM_TYPES.length, "Expecting two calculated value");
        assertEquals(results[0].getChecksum(), "5258d99970d60aed055c0056a467a0422acf7cb8",
                "Wrong SHA1 calculated");
        assertEquals(results[1].getChecksum(), "72f1aea68f75f79889b99cd4ff7acc83",
                "Wrong MD5 calculated");
    }

}
