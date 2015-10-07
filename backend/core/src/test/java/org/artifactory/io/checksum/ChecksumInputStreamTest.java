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
import org.artifactory.util.StringInputStream;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.assertEquals;

/**
 * Tests {@link ChecksumInputStream}.
 *
 * @author Yossi Shaul
 */
@Test
public class ChecksumInputStreamTest {

    public void simpleInputStream() throws IOException {
        ChecksumInputStream in = new ChecksumInputStream(new StringInputStream("test"),
                new Checksum(ChecksumType.sha1), new Checksum(ChecksumType.md5));
        while (in.read() != -1) {
            // make the calculation
        }
        in.close();

        Checksum[] checksums = in.getChecksums();
        Checksum sha1 = checksums[0];
        assertEquals(sha1.getType(), ChecksumType.sha1);
        assertEquals(sha1.getChecksum(), "a94a8fe5ccb19ba61c4c0873d391e987982fbbd3");
        Checksum md5 = checksums[1];
        assertEquals(md5.getType(), ChecksumType.md5);
        assertEquals(md5.getChecksum(), "098f6bcd4621d373cade4e832627b4f6");
    }

    // RTFACT-4104 - wrong checksums are calculated if using read with offset
    public void offsetInputStream() throws IOException {
        ChecksumInputStream in = new ChecksumInputStream(new StringInputStream("test"),
                new Checksum(ChecksumType.sha1), new Checksum(ChecksumType.md5));
        byte[] tempBuff = new byte[4];  // 4 bytes in the string
        int bytesRead = 0;
        for (int i = 0; i < tempBuff.length && bytesRead != -1; i++) {
            bytesRead = in.read(tempBuff, i, 1);
        }
        in.close();

        assertEquals(in.getChecksums()[0].getChecksum(), "a94a8fe5ccb19ba61c4c0873d391e987982fbbd3");
        assertEquals(in.getChecksums()[1].getChecksum(), "098f6bcd4621d373cade4e832627b4f6");
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = ".*not calculated.*")
    public void nonClosedStream() throws IOException {
        ChecksumInputStream in = new ChecksumInputStream(new StringInputStream("test"),
                new Checksum(ChecksumType.sha1));
        while (in.read() != -1) {
            // make the calculation
        }
        // don't close the stream and expect an exception
        in.getChecksums()[0].getChecksum();
    }

}
