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

import org.artifactory.repo.cache.expirable.MavenIndexCacheExpirable;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Noam Y. Tenne
 */
public class MavenIndexCacheExpirableTest {

    MavenIndexCacheExpirable mavenIndexCacheExpirable = new MavenIndexCacheExpirable();

    @Test
    public void testIsExpirable() throws Exception {
        expectExpirable("nexus-maven-repository-index");
        expectExpirable("nexus-maven-repository-index.tar");
        expectExpirable("nexus-maven-repository-index.gz");
        expectExpirable("nexus-maven-repository-index.zip");
        expectExpirable("nexus-maven-repository-index.bob");
        expectExpirable("nexus-maven-repository-index.properties");

        expectExpirable("org/nexus-maven-repository-index");
        expectExpirable("org/nexus-maven-repository-index.tar");
        expectExpirable("org/nexus-maven-repository-index.gz");
        expectExpirable("org/nexus-maven-repository-index.zip");
        expectExpirable("org/nexus-maven-repository-index.bob");
        expectExpirable("org/nexus-maven-repository-index.properties");

        expectExpirable("org/bob/1.0/nexus-maven-repository-index");
        expectExpirable("org/bob/1.0/nexus-maven-repository-index.tar");
        expectExpirable("org/bob/1.0/nexus-maven-repository-index.gz");
        expectExpirable("org/bob/1.0/nexus-maven-repository-index.zip");
        expectExpirable("org/bob/1.0/nexus-maven-repository-index.bob");
        expectExpirable("org/bob/1.0/nexus-maven-repository-index.properties");

        expectUnExpirable("bob.zip");
        expectUnExpirable("bob.gz");
        expectUnExpirable("org/bob.zip");
        expectUnExpirable("org/bob.gz");
        expectUnExpirable("org/artifact/bob.zip");
        expectUnExpirable("org/artifact/bob.gz");
        expectUnExpirable("org/artifact/1.0/bob.zip");
        expectUnExpirable("org/artifact/1.0/bob.gz");
    }

    private void expectExpirable(String artifactPath) {
        assertTrue(mavenIndexCacheExpirable.isExpirable(null, artifactPath),
                artifactPath + " Should be expirable.");
    }

    private void expectUnExpirable(String artifactPath) {
        assertFalse(mavenIndexCacheExpirable.isExpirable(null, artifactPath),
                artifactPath + " Shouldn't be expirable.");
    }
}
