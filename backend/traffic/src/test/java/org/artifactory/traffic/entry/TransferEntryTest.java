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

import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.repo.RepoPath;
import org.artifactory.traffic.TrafficAction;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Unit test for the traffic's TransferEntry object
 *
 * @author Noam Tenne
 */
@Test
public class TransferEntryTest {

    /**
     * Creates a new entry with the data constructor, and compares it's string representation with it's equivalent entry
     * Expression
     */
    public void uploadEntryToString() {
        RepoPath repoPath = InfoFactoryHolder.get()
                .createRepoPathFromId("libs-releases-local:antlr/antlr/2.7.7/antlr-2.7.7.pom");
        int contentLength = 632;
        String clientRemoteAddress = "127.0.0.1";
        UploadEntry uploadEntry = new UploadEntry(repoPath.getId(), contentLength, 10, clientRemoteAddress);

        assertEquals(uploadEntry.getDuration(), 10, "Duration should be equal");
        assertEquals(uploadEntry.getAction(), TrafficAction.UPLOAD, "Entry action should be equal");
        assertEquals(uploadEntry.getRepoPath(), repoPath.getId(), "Entry repo paths should be equal");
        assertEquals(uploadEntry.getContentLength(), contentLength, "Entry content lengths should be equal");
        assertEquals(uploadEntry.getUserAddress(), clientRemoteAddress, "Entry remote address should be equal");

        String expected = uploadEntry.getFormattedDate()
                + "|10|UPLOAD|127.0.0.1|libs-releases-local:antlr/antlr/2.7.7/antlr-2.7.7.pom|632";

        assertEquals(uploadEntry.toString(), expected, "Entry expressions should be equal");
    }

    /**
     * Creates a new entry using an entry expression, and validates it
     */
    public void stringToUploadEntry() {
        String entry = "20050318162747|10|UPLOAD|127.0.0.1|libs-releases-local:antlr/antlr/2.7.7/antlr-2.7.7.pom|456";
        UploadEntry uploadEntry = new UploadEntry(entry);

        assertEquals(entry, uploadEntry.toString(), "Upload entry should be equal to the expression that created it");

        assertEquals(uploadEntry.getDuration(), 10, "Duration should be equal");
        assertEquals(uploadEntry.getAction(), TrafficAction.UPLOAD, "Action should be equal");
        assertEquals(uploadEntry.getRepoPath(),
                InfoFactoryHolder.get()
                        .createRepoPath("libs-releases-local", "antlr/antlr/2.7.7/antlr-2.7.7.pom").getId(),
                "Entry repo paths should be equal");
        assertEquals(uploadEntry.getContentLength(), 456, "Entry content lengths should be equal");
        assertEquals(uploadEntry.getUserAddress(), "127.0.0.1", "Entry content lengths should be equal");
    }

    /**
     * Creates a new entry using an entry expression with an invalid content length
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvalidUploadEntryContentLength() {
        new UploadEntry("20090318162747|10|MOO|127.0.0.1|libs-releases-local:antlr/antlr/2.7.7/antlr-2.7.7.pom|63a2");
    }
}
