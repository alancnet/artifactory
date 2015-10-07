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

package org.artifactory.storage.db.util;

import org.apache.commons.io.IOUtils;
import org.artifactory.storage.db.util.blob.BlobWrapper;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

/**
 * Tests {@link org.artifactory.storage.db.util.blob.BlobWrapper}.
 *
 * @author Yossi Shaul
 */
@Test
public class BlobWrapperTest {

    public void constructWithInputStream() throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[]{1, 5, 6});
        BlobWrapper blobWrapper = new BlobWrapper(in, 3);
        assertEquals(blobWrapper.getLength(), 3);
        assertSame(blobWrapper.getInputStream(), in);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void nullInputStream() throws Exception {
        new BlobWrapper(null, 0);
    }

    public void stringConstructor() throws IOException {
        String testData = "test data";
        BlobWrapper blobWrapper = new BlobWrapper(testData);
        assertEquals(blobWrapper.getLength(), testData.length());
        assertEquals(IOUtils.toString(blobWrapper.getInputStream()), testData);
    }
}
