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

package org.artifactory.traffic;

import org.artifactory.traffic.entry.DownloadEntry;
import org.artifactory.traffic.entry.RequestEntry;
import org.artifactory.traffic.entry.TokenizedTrafficEntryFactory;
import org.artifactory.traffic.entry.TrafficEntry;
import org.artifactory.traffic.entry.UploadEntry;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit test for the TrafficAction enum
 *
 * @author Noam Tenne
 */
@Test
public class TrafficActionTest {

    /**
     * Construct a request entry with an expression using the TrafficAction
     */
    public void testRequestAction() {
        String entry = "20090319110249|10|REQUEST|127.0.0.1|admin|GET|/remote-repos/antlr/antlr/2.7.6/antlr-2.7.6.jar" +
                "|HTTP/1.1|200|443432";
        TrafficEntry trafficEntry = TokenizedTrafficEntryFactory.newTrafficEntry(entry);
        Assert.assertNotNull(trafficEntry);
        Assert.assertEquals(trafficEntry.getAction(), TrafficAction.REQUEST);
        Assert.assertTrue(trafficEntry instanceof RequestEntry);
    }

    /**
     * Construct a download entry with an expression using the TrafficAction
     */
    public void testDownloadAction() {
        String entry = "20090319110249|10|DOWNLOAD|127.0.0.1|repo1:antlr/antlr/2.7.6/antlr-2.7.6.jar|443432";
        TrafficEntry trafficEntry = TokenizedTrafficEntryFactory.newTrafficEntry(entry);
        Assert.assertNotNull(trafficEntry);
        Assert.assertEquals(trafficEntry.getAction(), TrafficAction.DOWNLOAD);
        Assert.assertTrue(trafficEntry instanceof DownloadEntry);
    }

    /**
     * Construct an upload entry with an expression using the TrafficAction
     */
    public void testUploadAction() {
        String entry = "20090319110249|10|UPLOAD|127.0.0.1|repo1-cache:antlr/antlr/2.7.6/antlr-2.7.6.jar|443432";
        TrafficEntry trafficEntry = TokenizedTrafficEntryFactory.newTrafficEntry(entry);
        Assert.assertNotNull(trafficEntry);
        Assert.assertEquals(trafficEntry.getAction(), TrafficAction.UPLOAD);
        Assert.assertTrue(trafficEntry instanceof UploadEntry);
    }
}
