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

package org.artifactory.storage.db.binstore.entity;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Date: 12/10/12
 * Time: 9:42 PM
 *
 * @author freds
 */
@Test
public class BinaryDataTest {
    public void simpleBinaryData() {
        BinaryData bd = new BinaryData("8018634e43a47494119601b857356a5a1875f888",
                "7c9703f5909d78ab0bf18147aee0a5b3", 13L);
        assertEquals(bd.getSha1(), "8018634e43a47494119601b857356a5a1875f888");
        assertEquals(bd.getMd5(), "7c9703f5909d78ab0bf18147aee0a5b3");
        assertEquals(bd.getLength(), 13L);
        assertTrue(bd.isValid());
    }

    public void maxNullBinaryData() {
        // Sha1 and Md5 good length but invalid
        BinaryData bd = new BinaryData(
                "length matters but not content!!12345678",
                "length matters but not content!!", 0L);
        assertEquals(bd.getSha1(), "length matters but not content!!12345678");
        assertEquals(bd.getMd5(), "length matters but not content!!");
        assertEquals(bd.getLength(), 0L);
        assertFalse(bd.isValid());

        // Sha1 and Md5 good length but Md5 invalid
        bd = new BinaryData(
                "8018634e43a47494119601b857356a5a1875f888",
                "length matters but not content!!", 0L);
        assertEquals(bd.getSha1(), "8018634e43a47494119601b857356a5a1875f888");
        assertEquals(bd.getMd5(), "length matters but not content!!");
        assertEquals(bd.getLength(), 0L);
        assertFalse(bd.isValid());

        // Sha1 and Md5 good length but Sha1 invalid
        bd = new BinaryData(
                "length matters but not content!!12345678",
                "7c9703f5909d78ab0bf18147aee0a5b3", 0L);
        assertEquals(bd.getSha1(), "length matters but not content!!12345678");
        assertEquals(bd.getMd5(), "7c9703f5909d78ab0bf18147aee0a5b3");
        assertEquals(bd.getLength(), 0L);
        assertFalse(bd.isValid());
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = ".*SHA1.*not.*valid.*")
    public void nullSha1BinaryData() {
        new BinaryData(
                null,
                "length matters but not content!!", 1L);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = ".*MD5.*not.*valid.*")
    public void nullMd5BinaryData() {
        new BinaryData(
                "length matters but not content!!12345678",
                null, 1L);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = ".*SHA1.*not.*valid.*")
    public void emptySha1BinaryData() {
        new BinaryData(
                "  ",
                "length matters but not content!!", 1L);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = ".*MD5.*not.*valid.*")
    public void emptyMd5BinaryData() {
        new BinaryData(
                "length matters but not content!!12345678",
                "  ", 1L);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = ".*SHA1.*not.*valid.*")
    public void wrongSha1BinaryData() {
        new BinaryData(
                "length matters but not content!!123456789",
                "length matters but not content!!", 1L);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = ".*MD5.*not.*valid.*")
    public void wrongMd5BinaryData() {
        new BinaryData(
                "length matters but not content!!12345678",
                "length matters but not content!!1", 1L);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = ".*Length.*not.*valid.*")
    public void wrongLengthBinaryData() {
        new BinaryData(
                "length matters but not content!!12345678",
                "length matters but not content!!", -1L);
    }
}
