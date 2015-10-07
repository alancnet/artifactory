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

package org.artifactory.storage;

import org.artifactory.api.storage.StorageUnit;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Test the {@link org.artifactory.api.storage.StorageUnit} enum
 *
 * @author Tomer Cohen
 */
@Test
public class StorageUnitTest {

    private static final long twoGbInBytes = 2147483648L;
    private static final long twoAndAHalfGigsInBytes = 2684354560L;
    private static final long twoMbInBytes = 2097152L;

    public void convertBytesToGb() {
        double bytesInGiga = StorageUnit.GB.fromBytes(twoGbInBytes);
        assertEquals(bytesInGiga, 2.0, "The convert bytes in giga don't match");
    }

    public void revertGbToBytes() {
        double gigasInBytes = StorageUnit.GB.toBytes(2);
        assertEquals(gigasInBytes, 2147483648.0, "The convert giga in bytes don't match");
    }

    public void convertBytesToMb() {
        double bytesInGiga = StorageUnit.MB.fromBytes(twoMbInBytes);
        assertEquals(bytesInGiga, 2.0, "The convert bytes in giga don't match");
    }

    public void revertMbToBytes() {
        double gigasInBytes = StorageUnit.MB.toBytes(2);
        assertEquals(gigasInBytes, 2097152.0, "The convert giga in bytes don't match");
    }

    public void bytesToReadableFormat() {
        String megabytes = StorageUnit.toReadableString(twoMbInBytes);
        assertEquals(megabytes, "2.00 MB");

        String gigabytes = StorageUnit.toReadableString(twoGbInBytes);
        assertEquals(gigabytes, "2.00 GB");

        String twoAndAHalfGigs = StorageUnit.toReadableString(twoAndAHalfGigsInBytes);
        assertEquals(twoAndAHalfGigs, "2.50 GB");
    }

    public void readableFormatToBytes() {
        assertEquals(StorageUnit.fromReadableString("3"), 3);
        assertEquals(StorageUnit.fromReadableString("5368709120"), 5368709120L);

        assertEquals(StorageUnit.fromReadableString("4k"), 4096);
        assertEquals(StorageUnit.fromReadableString("12K"), 12288);
        assertEquals(StorageUnit.fromReadableString("3kb"), 3072);
        assertEquals(StorageUnit.fromReadableString("32KB"), 32768);

        assertEquals(StorageUnit.fromReadableString("1024m"), 1073741824);
        assertEquals(StorageUnit.fromReadableString("500M"), 524288000);
        assertEquals(StorageUnit.fromReadableString("40mb"), 41943040);
        assertEquals(StorageUnit.fromReadableString("128MB"), 134217728);

        assertEquals(StorageUnit.fromReadableString("4g"), 4294967296L);
        assertEquals(StorageUnit.fromReadableString("12G"), 12884901888L);
        assertEquals(StorageUnit.fromReadableString("3gb"), 3221225472L);
        assertEquals(StorageUnit.fromReadableString("32GB"), 34359738368L);
    }
}
