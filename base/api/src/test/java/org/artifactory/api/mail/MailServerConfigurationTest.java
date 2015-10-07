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

package org.artifactory.api.mail;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests the mail server configuration object
 *
 * @author Noam Y. Tenne
 */
@Test
public class MailServerConfigurationTest {

    /**
     * Tests the state of the configuration after constructing the the main constructor
     */
    public void testMainConstructor() {
        String host = "momo";
        int port = 2131;
        String username = "popo";
        String password = "jojo";
        String from = "mitzi";
        String subjectPrefix = "gogo";

        MailServerConfiguration config = new MailServerConfiguration(true, host, port, username, password, from,
                subjectPrefix, true, true, "http://artifactory.lal");

        assertEquals(config.isEnabled(), true, "Unexpected mail server activity state.");
        assertEquals(config.getHost(), host, "Unexpected mail server host.");
        assertEquals(config.getPort(), port, "Unexpected mail server port.");
        assertEquals(config.getUsername(), username, "Unexpected mail server username.");
        assertEquals(config.getPassword(), password, "Unexpected mail server password.");
        assertEquals(config.getFrom(), from, "Unexpected mail server from.");
        assertEquals(config.getSubjectPrefix(), subjectPrefix, "Unexpected mail server subject prefix.");
        assertTrue(config.isUseSsl(), "Unexpected mail server SSL state.");
        assertTrue(config.isUseTls(), "Unexpected mail server TLS state.");
        assertEquals(config.getArtifactoryUrl(), "http://artifactory.lal");
    }
}