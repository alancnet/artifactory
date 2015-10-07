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

import org.artifactory.traffic.TrafficAction;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Unit test for the traffic's RequestEntry object
 *
 * @author Noam Tenne
 */
@Test
public class RequestEntryTest {

    /**
     * Creates a new entry with the data constructor, and compares it's string representation with it's equivalent entry
     * Expression
     */
    public void requestEntryToString() {
        String userAddress = "127.0.0.1";
        String username = "admin";
        String method = "GET";
        String path = "/remote-repos/antlr/antlr/2.7.6/antlr-2.7.6.jar";
        String protocol = "HTTP/1.1";
        int returnCode = 200;
        int contentLength = 443432;
        RequestEntry requestEntry = new RequestEntry(userAddress, username, method,
                path, protocol, returnCode, contentLength, 10);

        assertEquals(requestEntry.getDuration(), 10, "Duration should be equal");
        assertEquals(requestEntry.getAction(), TrafficAction.REQUEST, "Entry action should be equal");
        assertEquals(requestEntry.getUserAddress(), userAddress, "Entry user addresses should be equal");
        assertEquals(requestEntry.getUsername(), username, "Entry usernames should be equal");
        assertEquals(requestEntry.getMethod(), method, "Entry methods should be equal");
        assertEquals(requestEntry.getPath(), path, "Entry paths should be equal");
        assertEquals(requestEntry.getProtocol(), protocol, "Entry protocols should be equal");
        assertEquals(requestEntry.getReturnCode(), returnCode, "Entry return codes should be equal");
        assertEquals(requestEntry.getContentLength(), contentLength, "Entry return codes should be equal");

        String expected = requestEntry.getFormattedDate()
                + "|10|REQUEST|127.0.0.1|admin|GET|/remote-repos/antlr/antlr/2.7.6/antlr-2.7.6.jar|HTTP/1.1|200|443432";

        assertEquals(requestEntry.toString(), expected, "Entry expressions should be equal");
    }

    /**
     * Creates a new entry using an entry expression, and validates it
     */
    public void stringToRequestEntry() {
        String entry =
                "20090319110249|10|REQUEST|127.0.0.1|admin|GET|/remote-repos/antlr/antlr/2.7.6/antlr-2.7.6.jar|" +
                        "HTTP/1.1|200|443432";
        RequestEntry requestEntry = new RequestEntry(entry);

        assertEquals(entry, requestEntry.toString(), "Entry expressions should be equal");
        assertEquals(requestEntry.getDuration(), 10, "Duration should be equal");
        assertEquals(requestEntry.getAction(), TrafficAction.REQUEST, "Entry action should be equal");
        assertEquals(requestEntry.getUserAddress(), "127.0.0.1", "Entry user addresses should be equal");
        assertEquals(requestEntry.getUsername(), "admin", "Entry usernames should be equal");
        assertEquals(requestEntry.getMethod(), "GET", "Entry methods should be equal");
        assertEquals(requestEntry.getPath(), "/remote-repos/antlr/antlr/2.7.6/antlr-2.7.6.jar",
                "Entry paths should be equal");
        assertEquals(requestEntry.getProtocol(), "HTTP/1.1", "Entry protocols should be equal");
        assertEquals(requestEntry.getReturnCode(), 200, "Entry return codes should be equal");
        assertEquals(requestEntry.getContentLength(), 443432, "Entry return codes should be equal");
    }

    /**
     * Creates a new entry using an entry expression with an invalid content length
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvalidReturnCode() {
        new UploadEntry("20090319110249|REQUEST|127.0.0.1|admin|GET|/remote-repos/antlr/antlr/2.7.6/antlr-2.7.6.jar|" +
                "HTTP/1.1|2a00z|443432");
    }

    /**
     * Creates a new entry using an entry expression with an invalid content length
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvalidContentLength() {
        new UploadEntry("20090319110249|REQUEST|127.0.0.1|admin|GET|/remote-repos/antlr/antlr/2.7.6/antlr-2.7.6.jar|" +
                "HTTP/1.1|200|4434a3a2");
    }
}
