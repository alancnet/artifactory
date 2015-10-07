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

package org.artifactory;

import org.artifactory.util.ResourceUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Makes sure that the repo layouts configuration block (in the config) was not auto-formatted
 *
 * @author Noam Y. Tenne
 */
@Test
public class DefaultConfigLayoutTest {

    public void testDefaultConfigXmlNotAutoFormatted() {
        testLayoutsNotAutoFormatted("/META-INF/default/artifactory.config.xml");
    }

    public void testConfigV147NotAutoFormatted() {
        testLayoutsNotAutoFormatted("/config/install/config.1.4.7.xml");
    }

    public void testConfigV148NotAutoFormatted() {
        testLayoutsNotAutoFormatted("/config/test/config.1.4.8_old_replication.xml");
    }

    public void testConfigV149NotAutoFormatted() {
        testLayoutsNotAutoFormatted("/config/test/config.1.4.9.no.gc.xml");
    }

    private void testLayoutsNotAutoFormatted(String resourcePath) {
        String configString = ResourceUtils.getResourceAsString(resourcePath);
        Assert.assertTrue(configString.contains("<artifactPathPattern>[org]/[module]/[baseRev](-[folderItegRev])/" +
                "[type]s/[module](-[classifier])-[baseRev](-[fileItegRev]).[ext]</artifactPathPattern>"),
                "It looks like the repo layouts have been auto formatted.");
    }
}
