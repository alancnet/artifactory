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

package org.artifactory.descriptor.repo;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.artifactory.util.RepoLayoutUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author Noam Y. Tenne
 */
@Test
public class RepoLayoutTest {

    public void testDefaultConstructor() {
        RepoLayout repoLayout = new RepoLayout();
        assertNull(repoLayout.getName(), "Default name should be null.");
        assertNull(repoLayout.getArtifactPathPattern(), "Default artifact path should be null.");
        Assert.assertFalse(repoLayout.isDistinctiveDescriptorPathPattern(),
                "Default separate descriptor path usage should be null.");
        assertNull(repoLayout.getDescriptorPathPattern(), "Default descriptor path should be null.");
        assertNull(repoLayout.getFolderIntegrationRevisionRegExp(),
                "Default path snapshot integration should be null.");
        assertNull(repoLayout.getFileIntegrationRevisionRegExp(),
                "Default artifact snapshot integration should be null.");
    }

    public void testCopyConstructor() {
        RepoLayout repoLayout = new RepoLayout();
        repoLayout.setName("name");
        repoLayout.setArtifactPathPattern("artifactPath");
        repoLayout.setDistinctiveDescriptorPathPattern(true);
        repoLayout.setDescriptorPathPattern("descriptorPath");
        repoLayout.setFolderIntegrationRevisionRegExp("pathSnapshotIntegrationRegexp");
        repoLayout.setFileIntegrationRevisionRegExp("artifactSnapshotIntegrationRegexp");

        RepoLayout copy = new RepoLayout(repoLayout);
        Assert.assertTrue(EqualsBuilder.reflectionEquals(repoLayout, copy),
                "Copy constructor did not produce an equal object.");
    }

    public void testM2Constants() {
        assertEquals(RepoLayoutUtils.MAVEN_2_DEFAULT_NAME, "maven-2-default",
                "Unexpected default maven 2 layout name");

        assertEquals(RepoLayoutUtils.MAVEN_2_DEFAULT.getName(), RepoLayoutUtils.MAVEN_2_DEFAULT_NAME,
                "Unexpected default maven 2 layout name");
        assertEquals(RepoLayoutUtils.MAVEN_2_DEFAULT.getArtifactPathPattern(), "[orgPath]/[module]/" +
                "[baseRev](-[folderItegRev])/[module]-[baseRev](-[fileItegRev])"
                + "(-[classifier]).[ext]", "Unexpected default maven 2 layout artifact path");
        assertTrue(RepoLayoutUtils.MAVEN_2_DEFAULT.isDistinctiveDescriptorPathPattern(),
                "Default maven 2 layout descriptor path should be true");
        assertEquals(RepoLayoutUtils.MAVEN_2_DEFAULT.getDescriptorPathPattern(), "[orgPath]/[module]/" +
                "[baseRev](-[folderItegRev])/[module]-[baseRev](-[fileItegRev])" +
                "(-[classifier]).pom",
                "Unexpected default maven 2 layout descriptor path");
        assertEquals(RepoLayoutUtils.MAVEN_2_DEFAULT.getFolderIntegrationRevisionRegExp(), "SNAPSHOT",
                "Unexpected default maven 2 layout path snapshot integration reg exp");
        assertEquals(RepoLayoutUtils.MAVEN_2_DEFAULT.getFileIntegrationRevisionRegExp(),
                "SNAPSHOT|(?:(?:[0-9]{8}.[0-9]{6})-(?:[0-9]+))",
                "Unexpected default maven 2 layout artifact snapshot integration reg exp");
    }

    public void testIvyConstants() {
        assertEquals(RepoLayoutUtils.IVY_DEFAULT_NAME, "ivy-default", "Unexpected default ivy layout name");

        assertEquals(RepoLayoutUtils.IVY_DEFAULT.getName(), RepoLayoutUtils.IVY_DEFAULT_NAME,
                "Unexpected default ivy layout name");
        assertEquals(RepoLayoutUtils.IVY_DEFAULT.getArtifactPathPattern(), "[org]/[module]/" +
                "[baseRev](-[folderItegRev])/[type]s/[module](-[classifier])-[baseRev]" +
                "(-[fileItegRev]).[ext]", "Unexpected default ivy layout artifact path");
        assertTrue(RepoLayoutUtils.IVY_DEFAULT.isDistinctiveDescriptorPathPattern(),
                "Default ivy layout descriptor path should be true");
        assertEquals(RepoLayoutUtils.IVY_DEFAULT.getDescriptorPathPattern(), "[org]/[module]/" +
                "[baseRev](-[folderItegRev])/[type]s/ivy-[baseRev]" +
                "(-[fileItegRev]).xml", "Unexpected default ivy layout descriptor path");
        assertEquals(RepoLayoutUtils.IVY_DEFAULT.getFolderIntegrationRevisionRegExp(), "\\d{14}",
                "Unexpected default ivy layout path snapshot integration reg exp");
        assertEquals(RepoLayoutUtils.IVY_DEFAULT.getFileIntegrationRevisionRegExp(), "\\d{14}",
                "Unexpected default ivy layout artifact snapshot integration reg exp");
    }

    public void testGradleConstants() {
        assertEquals(RepoLayoutUtils.GRADLE_DEFAULT_NAME, "gradle-default",
                "Unexpected default gradle layout name");

        assertEquals(RepoLayoutUtils.GRADLE_DEFAULT.getName(), RepoLayoutUtils.GRADLE_DEFAULT_NAME,
                "Unexpected default gradle layout name");
        assertEquals(RepoLayoutUtils.GRADLE_DEFAULT.getArtifactPathPattern(), "[org]/[module]/" +
                "[baseRev](-[folderItegRev])/[module]-[baseRev](-[fileItegRev])" +
                "(-[classifier]).[ext]", "Unexpected default gradle layout artifact path");
        assertTrue(RepoLayoutUtils.GRADLE_DEFAULT.isDistinctiveDescriptorPathPattern(),
                "Default gradle layout descriptor path should be true");
        assertEquals(RepoLayoutUtils.GRADLE_DEFAULT.getDescriptorPathPattern(), "[org]/[module]/ivy-[baseRev]" +
                "(-[fileItegRev]).xml", "Unexpected default gradle layout descriptor path");
        assertEquals(RepoLayoutUtils.GRADLE_DEFAULT.getFolderIntegrationRevisionRegExp(), "\\d{14}",
                "Unexpected default gradle layout path snapshot integration reg exp");
        assertEquals(RepoLayoutUtils.GRADLE_DEFAULT.getFileIntegrationRevisionRegExp(), "\\d{14}",
                "Unexpected default gradle layout artifact snapshot integration reg exp");
    }

    public void testM1Constants() {
        assertEquals(RepoLayoutUtils.MAVEN_1_DEFAULT_NAME, "maven-1-default",
                "Unexpected default maven 1 layout name");

        assertEquals(RepoLayoutUtils.MAVEN_1_DEFAULT.getName(), RepoLayoutUtils.MAVEN_1_DEFAULT_NAME,
                "Unexpected default maven 1 layout name");
        assertEquals(RepoLayoutUtils.MAVEN_1_DEFAULT.getArtifactPathPattern(),
                "[org]/[type]s/[module]-[baseRev](-[fileItegRev])(-[classifier]).[ext]",
                "Unexpected default maven 1 layout artifact path");
        assertTrue(RepoLayoutUtils.MAVEN_1_DEFAULT.isDistinctiveDescriptorPathPattern(),
                "Default maven 1 layout descriptor path should be true");
        assertEquals(RepoLayoutUtils.MAVEN_1_DEFAULT.getDescriptorPathPattern(),
                "[org]/[type]s/[module]-[baseRev](-[fileItegRev]).pom",
                "Unexpected default maven 1 layout descriptor path");
        assertEquals(RepoLayoutUtils.MAVEN_1_DEFAULT.getFolderIntegrationRevisionRegExp(), ".+",
                "Unexpected default maven 1 layout path snapshot integration reg exp");
        assertEquals(RepoLayoutUtils.MAVEN_1_DEFAULT.getFileIntegrationRevisionRegExp(), ".+",
                "Unexpected default maven 1 layout artifact snapshot integration reg exp");
    }
}
