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

import org.apache.maven.model.Model;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Tests the {@link MavenPomBuilder}.
 *
 * @author Yossi Shaul
 */
@Test
public class MavenPomBuilderTest {

    public void buildSimple() {
        Model model = new MavenPomBuilder().groupId("a").artifactId("b").version("1.8").packaging("zip").build();

        assertEquals(model.getModelVersion(), "4.0.0");
        assertEquals(model.getGroupId(), "a");
        assertEquals(model.getArtifactId(), "b");
        assertEquals(model.getVersion(), "1.8");
        assertEquals(model.getPackaging(), "zip");
    }
}
