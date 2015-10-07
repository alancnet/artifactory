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

package org.artifactory.addon.layouts.translate;

import org.artifactory.descriptor.repo.RepoLayout;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.artifactory.util.RepoLayoutUtils.*;

/**
 * Tests the module info path translator
 *
 * @author Noam Y. Tenne
 */
@Test
public class PathTranslationHelperTest extends ArtifactoryHomeBoundTest {

    private static final String[] MAVEN_PATHS = {"org/momo/popo/1.0/popo-1.0.jar", "org/momo/popo/1.0/popo-1.0.tar.gz",
            "org/momo/popo/1.0/popo-1.0.jar.md5", "org/momo/popo/1.0/popo-1.0.jar:metadata.xml",
            "org/momo/popo/1.0/popo-1.0.pom", "org/momo/popo/1.0/popo-1.0-sources.jar",
            "org/momo/popo/1.0/popo-1.0-sources.tar.gz", "org/momo/popo/1.0/popo-1.0-sources.jar.md5",
            "org/momo/popo/1.0/popo-1.0-sources.jar:metadata.xml",
            "really/super/ultra/mega/long/artifact-of-death/1.0/artifact-of-death-1.0.jar",
            "really/super/ultra/mega-hyper/long/artifact-of-death/1.0/artifact-of-death-1.0-sources.jar"};

    private static final String[] IVY_PATHS = {"org.momo/popo/1.0/jars/popo-1.0.jar",
            "org.momo/popo/1.0/tar.gzs/popo-1.0.tar.gz", "org.momo/popo/1.0/jars/popo-1.0.jar.md5",
            "org.momo/popo/1.0/jars/popo-1.0.jar:metadata.xml", "org.momo/popo/1.0/ivys/ivy-1.0.xml",
            "org.momo/popo/1.0/java-sources/popo-sources-1.0.jar", "org.momo/popo/1.0/tar.gzs/popo-sources-1.0.tar.gz",
            "org.momo/popo/1.0/java-sources/popo-sources-1.0.jar.md5",
            "org.momo/popo/1.0/java-sources/popo-sources-1.0.jar:metadata.xml",
            "really.super.ultra.mega.long/artifact-of-death/1.0/jars/artifact-of-death-1.0.jar",
            "really.super.ultra.mega-hyper.long/artifact-of-death/1.0/java-sources/artifact-of-death-sources-1.0.jar"};

    private static final String[] GRADLE_PATHS = {"org.momo/popo/1.0/popo-1.0.jar", "org.momo/popo/1.0/popo-1.0.tar.gz",
            "org.momo/popo/1.0/popo-1.0.jar.md5", "org.momo/popo/1.0/popo-1.0.jar:metadata.xml",
            "org.momo/popo/ivy-1.0.xml", "org.momo/popo/1.0/popo-1.0-sources.jar",
            "org.momo/popo/1.0/popo-1.0-sources.tar.gz", "org.momo/popo/1.0/popo-1.0-sources.jar.md5",
            "org.momo/popo/1.0/popo-1.0-sources.jar:metadata.xml",
            "really.super.ultra.mega.long/artifact-of-death/1.0/artifact-of-death-1.0.jar",
            "really.super.ultra.mega-hyper.long/artifact-of-death/1.0/artifact-of-death-1.0-sources.jar"};

    public void testMavenToMaven() {
        for (String mavenPath : MAVEN_PATHS) {
            assertSameTranslation(MAVEN_2_DEFAULT, mavenPath);
        }
    }

    public void testMavenToIvy() {
        assertCrossTranslation(MAVEN_2_DEFAULT, IVY_DEFAULT, MAVEN_PATHS[0], "org.momo/popo/1.0/jars/popo-1.0.jar");
        assertCrossTranslation(MAVEN_2_DEFAULT, IVY_DEFAULT, MAVEN_PATHS[1],
                "org.momo/popo/1.0/tar.gzs/popo-1.0.tar.gz");
        assertCrossTranslation(MAVEN_2_DEFAULT, IVY_DEFAULT, MAVEN_PATHS[2], "org.momo/popo/1.0/jars/popo-1.0.jar.md5");
        assertCrossTranslation(MAVEN_2_DEFAULT, IVY_DEFAULT, MAVEN_PATHS[3],
                "org.momo/popo/1.0/jars/popo-1.0.jar:metadata.xml");
        assertCrossTranslation(MAVEN_2_DEFAULT, IVY_DEFAULT, MAVEN_PATHS[4], "org.momo/popo/1.0/poms/popo-1.0.pom");
        assertCrossTranslation(MAVEN_2_DEFAULT, IVY_DEFAULT, MAVEN_PATHS[5],
                "org.momo/popo/1.0/jars/popo-sources-1.0.jar");
        assertCrossTranslation(MAVEN_2_DEFAULT, IVY_DEFAULT, MAVEN_PATHS[6],
                "org.momo/popo/1.0/tar.gzs/popo-sources-1.0.tar.gz");
        assertCrossTranslation(MAVEN_2_DEFAULT, IVY_DEFAULT, MAVEN_PATHS[7],
                "org.momo/popo/1.0/jars/popo-sources-1.0.jar.md5");
        assertCrossTranslation(MAVEN_2_DEFAULT, IVY_DEFAULT, MAVEN_PATHS[8],
                "org.momo/popo/1.0/jars/popo-sources-1.0.jar:metadata.xml");
        assertCrossTranslation(MAVEN_2_DEFAULT, IVY_DEFAULT, MAVEN_PATHS[9],
                "really.super.ultra.mega.long/artifact-of-death/1.0/jars/artifact-of-death-1.0.jar");
        assertCrossTranslation(MAVEN_2_DEFAULT, IVY_DEFAULT, MAVEN_PATHS[10],
                "really.super.ultra.mega-hyper.long/artifact-of-death/1.0/jars/artifact-of-death-sources-1.0.jar");
    }

    public void testMavenToGradle() {
        assertCrossTranslation(MAVEN_2_DEFAULT, GRADLE_DEFAULT, MAVEN_PATHS[0], "org.momo/popo/1.0/popo-1.0.jar");
        assertCrossTranslation(MAVEN_2_DEFAULT, GRADLE_DEFAULT, MAVEN_PATHS[1], "org.momo/popo/1.0/popo-1.0.tar.gz");
        assertCrossTranslation(MAVEN_2_DEFAULT, GRADLE_DEFAULT, MAVEN_PATHS[2], "org.momo/popo/1.0/popo-1.0.jar.md5");
        assertCrossTranslation(MAVEN_2_DEFAULT, GRADLE_DEFAULT, MAVEN_PATHS[3],
                "org.momo/popo/1.0/popo-1.0.jar:metadata.xml");
        assertCrossTranslation(MAVEN_2_DEFAULT, GRADLE_DEFAULT, MAVEN_PATHS[4], "org.momo/popo/1.0/popo-1.0.pom");
        assertCrossTranslation(MAVEN_2_DEFAULT, GRADLE_DEFAULT, MAVEN_PATHS[5],
                "org.momo/popo/1.0/popo-1.0-sources.jar");
        assertCrossTranslation(MAVEN_2_DEFAULT, GRADLE_DEFAULT, MAVEN_PATHS[6],
                "org.momo/popo/1.0/popo-1.0-sources.tar.gz");
        assertCrossTranslation(MAVEN_2_DEFAULT, GRADLE_DEFAULT, MAVEN_PATHS[7],
                "org.momo/popo/1.0/popo-1.0-sources.jar.md5");
        assertCrossTranslation(MAVEN_2_DEFAULT, GRADLE_DEFAULT, MAVEN_PATHS[8],
                "org.momo/popo/1.0/popo-1.0-sources.jar:metadata.xml");
        assertCrossTranslation(MAVEN_2_DEFAULT, GRADLE_DEFAULT, MAVEN_PATHS[9],
                "really.super.ultra.mega.long/artifact-of-death/1.0/artifact-of-death-1.0.jar");
        assertCrossTranslation(MAVEN_2_DEFAULT, GRADLE_DEFAULT, MAVEN_PATHS[10],
                "really.super.ultra.mega-hyper.long/artifact-of-death/1.0/artifact-of-death-1.0-sources.jar");
    }

    public void testIvyToIvy() {
        for (String ivyPath : IVY_PATHS) {
            assertSameTranslation(IVY_DEFAULT, ivyPath);
        }
    }

    public void testIvyToMaven() {
        assertCrossTranslation(IVY_DEFAULT, MAVEN_2_DEFAULT, IVY_PATHS[0], "org/momo/popo/1.0/popo-1.0.jar");
        assertCrossTranslation(IVY_DEFAULT, MAVEN_2_DEFAULT, IVY_PATHS[1], "org/momo/popo/1.0/popo-1.0.tar.gz");
        assertCrossTranslation(IVY_DEFAULT, MAVEN_2_DEFAULT, IVY_PATHS[2], "org/momo/popo/1.0/popo-1.0.jar.md5");
        assertCrossTranslation(IVY_DEFAULT, MAVEN_2_DEFAULT, IVY_PATHS[3],
                "org/momo/popo/1.0/popo-1.0.jar:metadata.xml");
        assertCrossTranslation(IVY_DEFAULT, MAVEN_2_DEFAULT, IVY_PATHS[4], "org/momo/popo/1.0/popo-1.0.xml");
        assertCrossTranslation(IVY_DEFAULT, MAVEN_2_DEFAULT, IVY_PATHS[5], "org/momo/popo/1.0/popo-1.0-sources.jar");
        assertCrossTranslation(IVY_DEFAULT, MAVEN_2_DEFAULT, IVY_PATHS[6], "org/momo/popo/1.0/popo-1.0-sources.tar.gz");
        assertCrossTranslation(IVY_DEFAULT, MAVEN_2_DEFAULT, IVY_PATHS[7],
                "org/momo/popo/1.0/popo-1.0-sources.jar.md5");
        assertCrossTranslation(IVY_DEFAULT, MAVEN_2_DEFAULT, IVY_PATHS[8],
                "org/momo/popo/1.0/popo-1.0-sources.jar:metadata.xml");
        assertCrossTranslation(IVY_DEFAULT, MAVEN_2_DEFAULT, IVY_PATHS[9],
                "really/super/ultra/mega/long/artifact-of-death/1.0/artifact-of-death-1.0.jar");
        assertCrossTranslation(IVY_DEFAULT, MAVEN_2_DEFAULT, IVY_PATHS[10],
                "really/super/ultra/mega-hyper/long/artifact-of-death/1.0/artifact-of-death-1.0-sources.jar");
    }

    public void testIvyToGradle() {
        assertCrossTranslation(IVY_DEFAULT, GRADLE_DEFAULT, IVY_PATHS[0], "org.momo/popo/1.0/popo-1.0.jar");
        assertCrossTranslation(IVY_DEFAULT, GRADLE_DEFAULT, IVY_PATHS[1], "org.momo/popo/1.0/popo-1.0.tar.gz");
        assertCrossTranslation(IVY_DEFAULT, GRADLE_DEFAULT, IVY_PATHS[2], "org.momo/popo/1.0/popo-1.0.jar.md5");
        assertCrossTranslation(IVY_DEFAULT, GRADLE_DEFAULT, IVY_PATHS[3],
                "org.momo/popo/1.0/popo-1.0.jar:metadata.xml");
        assertCrossTranslation(IVY_DEFAULT, GRADLE_DEFAULT, IVY_PATHS[4], "org.momo/popo/ivy-1.0.xml");
        assertCrossTranslation(IVY_DEFAULT, GRADLE_DEFAULT, IVY_PATHS[5], "org.momo/popo/1.0/popo-1.0-sources.jar");
        assertCrossTranslation(IVY_DEFAULT, GRADLE_DEFAULT, IVY_PATHS[6], "org.momo/popo/1.0/popo-1.0-sources.tar.gz");
        assertCrossTranslation(IVY_DEFAULT, GRADLE_DEFAULT, IVY_PATHS[7], "org.momo/popo/1.0/popo-1.0-sources.jar.md5");
        assertCrossTranslation(IVY_DEFAULT, GRADLE_DEFAULT, IVY_PATHS[8],
                "org.momo/popo/1.0/popo-1.0-sources.jar:metadata.xml");
        assertCrossTranslation(IVY_DEFAULT, GRADLE_DEFAULT, IVY_PATHS[9],
                "really.super.ultra.mega.long/artifact-of-death/1.0/artifact-of-death-1.0.jar");
        assertCrossTranslation(IVY_DEFAULT, GRADLE_DEFAULT, IVY_PATHS[10],
                "really.super.ultra.mega-hyper.long/artifact-of-death/1.0/artifact-of-death-1.0-sources.jar");
    }

    public void testGradleToGradle() {
        for (String gradlePath : GRADLE_PATHS) {
            assertSameTranslation(GRADLE_DEFAULT, gradlePath);
        }
    }

    public void testGradleToMaven() {
        assertCrossTranslation(GRADLE_DEFAULT, MAVEN_2_DEFAULT, GRADLE_PATHS[0], "org/momo/popo/1.0/popo-1.0.jar");
        assertCrossTranslation(GRADLE_DEFAULT, MAVEN_2_DEFAULT, GRADLE_PATHS[1], "org/momo/popo/1.0/popo-1.0.tar.gz");
        assertCrossTranslation(GRADLE_DEFAULT, MAVEN_2_DEFAULT, GRADLE_PATHS[2], "org/momo/popo/1.0/popo-1.0.jar.md5");
        assertCrossTranslation(GRADLE_DEFAULT, MAVEN_2_DEFAULT, GRADLE_PATHS[3],
                "org/momo/popo/1.0/popo-1.0.jar:metadata.xml");
        assertCrossTranslation(GRADLE_DEFAULT, MAVEN_2_DEFAULT, GRADLE_PATHS[4], "org/momo/popo/1.0/popo-1.0.xml");
        assertCrossTranslation(GRADLE_DEFAULT, MAVEN_2_DEFAULT, GRADLE_PATHS[5],
                "org/momo/popo/1.0/popo-1.0-sources.jar");
        assertCrossTranslation(GRADLE_DEFAULT, MAVEN_2_DEFAULT, GRADLE_PATHS[6],
                "org/momo/popo/1.0/popo-1.0-sources.tar.gz");
        assertCrossTranslation(GRADLE_DEFAULT, MAVEN_2_DEFAULT, GRADLE_PATHS[7],
                "org/momo/popo/1.0/popo-1.0-sources.jar.md5");
        assertCrossTranslation(GRADLE_DEFAULT, MAVEN_2_DEFAULT, GRADLE_PATHS[8],
                "org/momo/popo/1.0/popo-1.0-sources.jar:metadata.xml");
        assertCrossTranslation(GRADLE_DEFAULT, MAVEN_2_DEFAULT, GRADLE_PATHS[9],
                "really/super/ultra/mega/long/artifact-of-death/1.0/artifact-of-death-1.0.jar");
        assertCrossTranslation(GRADLE_DEFAULT, MAVEN_2_DEFAULT, GRADLE_PATHS[10],
                "really/super/ultra/mega-hyper/long/artifact-of-death/1.0/artifact-of-death-1.0-sources.jar");
    }

    public void testGradleToIvy() {
        assertCrossTranslation(GRADLE_DEFAULT, IVY_DEFAULT, GRADLE_PATHS[0], "org.momo/popo/1.0/jars/popo-1.0.jar");
        assertCrossTranslation(GRADLE_DEFAULT, IVY_DEFAULT, GRADLE_PATHS[1],
                "org.momo/popo/1.0/tar.gzs/popo-1.0.tar.gz");
        assertCrossTranslation(GRADLE_DEFAULT, IVY_DEFAULT, GRADLE_PATHS[2], "org.momo/popo/1.0/jars/popo-1.0.jar.md5");
        assertCrossTranslation(GRADLE_DEFAULT, IVY_DEFAULT, GRADLE_PATHS[3],
                "org.momo/popo/1.0/jars/popo-1.0.jar:metadata.xml");
        assertCrossTranslation(GRADLE_DEFAULT, IVY_DEFAULT, GRADLE_PATHS[4], "org.momo/popo/1.0/xmls/ivy-1.0.xml");
        assertCrossTranslation(GRADLE_DEFAULT, IVY_DEFAULT, GRADLE_PATHS[5],
                "org.momo/popo/1.0/jars/popo-sources-1.0.jar");
        assertCrossTranslation(GRADLE_DEFAULT, IVY_DEFAULT, GRADLE_PATHS[6],
                "org.momo/popo/1.0/tar.gzs/popo-sources-1.0.tar.gz");
        assertCrossTranslation(GRADLE_DEFAULT, IVY_DEFAULT, GRADLE_PATHS[7],
                "org.momo/popo/1.0/jars/popo-sources-1.0.jar.md5");
        assertCrossTranslation(GRADLE_DEFAULT, IVY_DEFAULT, GRADLE_PATHS[8],
                "org.momo/popo/1.0/jars/popo-sources-1.0.jar:metadata.xml");
        assertCrossTranslation(GRADLE_DEFAULT, IVY_DEFAULT, GRADLE_PATHS[9],
                "really.super.ultra.mega.long/artifact-of-death/1.0/jars/artifact-of-death-1.0.jar");
        assertCrossTranslation(GRADLE_DEFAULT, IVY_DEFAULT, GRADLE_PATHS[10],
                "really.super.ultra.mega-hyper.long/artifact-of-death/1.0/jars/artifact-of-death-sources-1.0.jar");
    }

    private void assertSameTranslation(RepoLayout layout, String path) {
        assertCrossTranslation(layout, layout, path, path);
    }

    private void assertCrossTranslation(RepoLayout source, RepoLayout target, String sourcePath, String expectedPath) {
        Assert.assertEquals(new PathTranslationHelper().translatePath(source, target, sourcePath, null), expectedPath);
    }
}
