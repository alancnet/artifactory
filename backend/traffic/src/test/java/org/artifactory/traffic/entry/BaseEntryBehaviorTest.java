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

package org.artifactory.traffic.entry;

import org.testng.annotations.Test;

/**
 * Unit test for the behavior of the traffic's BaseEntry object
 *
 * @author Noam Tenne
 */
@Test
public class BaseEntryBehaviorTest {

    /**
     * Try to create a download entry with a null expression
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNullDownloadEntry() {
        new DownloadEntry(null);
    }

    /**
     * Try to create an upload entry with an empty expression
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testEmptyUploadEntry() {
        new UploadEntry("");
    }

    /**
     * Try to create a request entry with an expression that contains no data
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNoDataRequestEntry() {
        new RequestEntry("|||");
    }

    /**
     * Try to create a download entry with an expression that doesn't have enough columns
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testShortDownloadEntry() {
        new DownloadEntry("20090318162747|UPLOAD|127.0.0.1|libs-releases-local:antlr/antlr/2.7.7/antlr-2.7.7.pom");
    }

    /**
     * Try to create an upload entry with an expression that has too many columns
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testLongUploadEntry() {
        new UploadEntry("20090318162747|UPLOAD|127.0.0.1|libs-releases-local:antlr/antlr/2.7.7/antlr-2.7.7.pom|632|MOO");
    }

    /**
     * Try to create a request entry that contains an invalid date
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvalidRequestEntryDate() {
        new RequestEntry("200903a816l747|UPLOAD|127.0.0.1|libs-releases-local:antlr/antlr/2.7.7/antlr-2.7.7.pom|632");
    }

    /**
     * Try to create a download entry that contains a date in an invalid format
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvalidDownloadEntryDateFormat() {
        new DownloadEntry("2-00---78|UPLOAD|127.0.0.1|libs-releases-local:antlr/antlr/2.7.7/antlr-2.7.7.pom|632");
    }
}
