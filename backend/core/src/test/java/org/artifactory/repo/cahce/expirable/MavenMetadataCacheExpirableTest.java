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

package org.artifactory.repo.cahce.expirable;

import org.artifactory.repo.cache.expirable.MavenMetadataCacheExpirable;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Noam Y. Tenne
 */
public class MavenMetadataCacheExpirableTest {

    MavenMetadataCacheExpirable mavenMetadataCacheExpirable = new MavenMetadataCacheExpirable();

    @Test
    public void testIsExpirable() throws Exception {
        expectExpirable("maven-metadata.xml");
        expectExpirable("org/maven-metadata.xml");
        expectExpirable("org:maven-metadata.xml");
        expectExpirable("org/artifact/maven-metadata.xml");
        expectExpirable("org/artifact:maven-metadata.xml");
        expectExpirable("org/artifact/1.0/maven-metadata.xml");
        expectExpirable("org/artifact/1.0:maven-metadata.xml");

        expectUnExpirable("another-metadata.xml");
        expectUnExpirable("org/another-metadata.xml");
        expectUnExpirable("org:another-metadata.xml");
        expectUnExpirable("org/artifact/another-metadata.xml");
        expectUnExpirable("org/artifact:another-metadata.xml");
        expectUnExpirable("org/artifact/1.0/another-metadata.xml");
        expectUnExpirable("org/artifact/1.0:another-metadata.xml");

        expectUnExpirable("bob.jar");
        expectUnExpirable("bob.pom");
        expectUnExpirable("org/bob.jar");
        expectUnExpirable("org/bob.pom");
        expectUnExpirable("org/artifact/bob.jar");
        expectUnExpirable("org/artifact/bob.pom");
        expectUnExpirable("org/artifact/1.0/bob.jar");
        expectUnExpirable("org/artifact/1.0/bob.pom");
    }

    private void expectExpirable(String artifactPath) {
        assertTrue(mavenMetadataCacheExpirable.isExpirable(null, artifactPath),
                artifactPath + " Should be expirable.");
    }

    private void expectUnExpirable(String artifactPath) {
        assertFalse(mavenMetadataCacheExpirable.isExpirable(null, artifactPath),
                artifactPath + " Shouldn't be expirable.");
    }
}
