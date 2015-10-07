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

package org.artifactory.maven;

import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.io.xpp3.SettingsXpp3Reader;
import org.artifactory.api.maven.MavenSettings;
import org.artifactory.api.maven.MavenSettingsServer;
import org.artifactory.util.StringInputStream;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Eli Givoni
 */
@Test
public class MavenServiceImplTest {

    public void generateMavenSettings() throws Exception {
        MavenServiceImpl mavenService = new MavenServiceImpl();
        MavenSettings mavenSettings = new MavenSettings("blabla");
        mavenSettings.addServer(new MavenSettingsServer("server1", "elig", "secret"));
        String result = mavenService.generateSettings(mavenSettings);
        assertTrue(result.contains("http://maven.apache.org/xsd/settings-1.1.0.xsd"),
                "Schema declaration not found:\n " + result);
        SettingsXpp3Reader reader = new SettingsXpp3Reader();
        Settings resultedSettings = reader.read(new StringInputStream(result));
        assertEquals(resultedSettings.getServers().size(), 1);
        Server resultedServer = resultedSettings.getServers().get(0);
        assertEquals(resultedServer.getId(), "server1");
    }

}
