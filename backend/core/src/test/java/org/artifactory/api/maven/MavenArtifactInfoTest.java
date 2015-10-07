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

package org.artifactory.api.maven;

import org.artifactory.common.ConstantValues;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.repo.RepoPath;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Tests the MavenArtifactInfo.
 *
 * @author Yossi Shaul
 */
@Test
public class MavenArtifactInfoTest extends ArtifactoryHomeBoundTest {

    @BeforeClass
    protected void setUp() throws Exception {
        System.setProperty(ConstantValues.mvnCustomTypes.getPropertyName(), "tar.gz, custom.jar, tar.bz2");
    }

    @AfterClass
    protected void tearDown() throws Exception {
        System.clearProperty(ConstantValues.mvnCustomTypes.getPropertyName());
    }

    public void fromSimplePath() {
        RepoPath path = InfoFactoryHolder.get().createRepoPath("repo",
                "/org/jfrog/artifactory-core/2.0/artifactory-core-2.0.pom");
        MavenArtifactInfo artifactInfo = MavenArtifactInfo.fromRepoPath(path);
        assertTrue(artifactInfo.isValid());
        assertEquals(artifactInfo.getGroupId(), "org.jfrog");
        assertEquals(artifactInfo.getArtifactId(), "artifactory-core");
        assertEquals(artifactInfo.getVersion(), "2.0");
        assertNull(artifactInfo.getClassifier());
        assertEquals(artifactInfo.getType(), "pom");
    }

    public void fromPathWithClassifier() {
        RepoPath path = InfoFactoryHolder.get().createRepoPath("repo",
                "/org/jfrog/artifactory-core/2.0/artifactory-core-2.0-sources.jar");
        MavenArtifactInfo artifactInfo = MavenArtifactInfo.fromRepoPath(path);
        assertTrue(artifactInfo.isValid());
        assertEquals(artifactInfo.getGroupId(), "org.jfrog");
        assertEquals(artifactInfo.getArtifactId(), "artifactory-core");
        assertEquals(artifactInfo.getVersion(), "2.0");
        assertEquals(artifactInfo.getClassifier(), "sources");
        assertEquals(artifactInfo.getType(), "jar");
    }

    public void fromPathNonUniqueSnapshotVersion() {
        // unique snapshot version is a version that includes the timestamp-buildnumber string in the version
        RepoPath path = InfoFactoryHolder.get().createRepoPath("repo",
                "com/core/5.4-SNAPSHOT/core-5.4-SNAPSHOT-sources.jar");
        MavenArtifactInfo artifactInfo = MavenArtifactInfo.fromRepoPath(path);
        assertTrue(artifactInfo.isValid());
        assertEquals(artifactInfo.getGroupId(), "com");
        assertEquals(artifactInfo.getArtifactId(), "core");
        assertEquals(artifactInfo.getVersion(), "5.4-SNAPSHOT");
        assertEquals(artifactInfo.getClassifier(), "sources");
        assertEquals(artifactInfo.getType(), "jar");
    }

    public void fromPathUniqueSnapshotVersion() {
        // unique snapshot version is a version that includes the SNAPSHOT string in the version and not the
        // timestamp-buildnumber
        RepoPath path = InfoFactoryHolder.get().createRepoPath("repo",
                "com/core/5.4-SNAPSHOT/core-5.4-20081214.090217-4-sources.jar");
        MavenArtifactInfo artifactInfo = MavenArtifactInfo.fromRepoPath(path);
        assertTrue(artifactInfo.isValid());
        assertEquals(artifactInfo.getGroupId(), "com");
        assertEquals(artifactInfo.getArtifactId(), "core");
        assertEquals(artifactInfo.getVersion(), "5.4-20081214.090217-4");
        assertEquals(artifactInfo.getClassifier(), "sources");
        assertEquals(artifactInfo.getType(), "jar");
    }

    public void fromPathUniqueMd5SnapshotVersion() {
        // non-unique snapshot version is a version that includes the SNAPSHOT string in the version and not the
        // timestamp-buildnumber
        RepoPath path = InfoFactoryHolder.get().createRepoPath("repo",
                "com/core/5.4-SNAPSHOT/core-5.4-20081214.090217-4-sources.jar");
        MavenArtifactInfo artifactInfo = MavenArtifactInfo.fromRepoPath(path);
        assertTrue(artifactInfo.isValid());
        assertEquals(artifactInfo.getGroupId(), "com");
        assertEquals(artifactInfo.getArtifactId(), "core");
        assertEquals(artifactInfo.getVersion(), "5.4-20081214.090217-4");
        assertEquals(artifactInfo.getClassifier(), "sources");
        assertEquals(artifactInfo.getType(), "jar");
    }

    public void fromPathWithOneDigitVersion() {
        RepoPath path = new RepoPathImpl("repo", "com/google/google/1/google-1-sources.pom");
        MavenArtifactInfo artifactInfo = MavenArtifactInfo.fromRepoPath(path);
        assertTrue(artifactInfo.isValid());
        assertEquals(artifactInfo.getGroupId(), "com.google");
        assertEquals(artifactInfo.getArtifactId(), "google");
        assertEquals(artifactInfo.getVersion(), "1");
        assertEquals(artifactInfo.getClassifier(), "sources");
        assertEquals(artifactInfo.getType(), "pom");
    }

    public void testConfigPath() {
        RepoPath path = new RepoPathImpl("repo",
                "org/hi/common.atg/1.0.0/common.atg-1.0.0-20111104.164556-1-config+config.jar");
        MavenArtifactInfo artifactInfo = MavenArtifactInfo.fromRepoPath(path);
        assertTrue(artifactInfo.isValid());
        assertEquals(artifactInfo.getGroupId(), "org.hi");
        assertEquals(artifactInfo.getArtifactId(), "common.atg");
        assertEquals(artifactInfo.getVersion(), "1.0.0-20111104.164556-1");
        assertEquals(artifactInfo.getClassifier(), "config+config");
        assertEquals(artifactInfo.getType(), "jar");
    }

    public void testCustomUserType() {
        RepoPath path = new RepoPathImpl("repo",
                "org/hi/common.atg/1.0.0-SNAPSHOT/common.atg-1.0.0-SNAPSHOT-lib+DAS-1.0.0-SNAPSHOT-lib+commons-codec-1.3.custom.jar");
        MavenArtifactInfo artifactInfo = MavenArtifactInfo.fromRepoPath(path);
        assertTrue(artifactInfo.isValid());
        assertEquals(artifactInfo.getGroupId(), "org.hi");
        assertEquals(artifactInfo.getArtifactId(), "common.atg");
        assertEquals(artifactInfo.getVersion(), "1.0.0-SNAPSHOT");
        assertEquals(artifactInfo.getClassifier(), "lib+DAS-1.0.0-SNAPSHOT-lib+commons-codec-1.3");
        assertEquals(artifactInfo.getType(), "custom.jar");
    }

    public void testClassifierWithDot() {
        RepoPath path = new RepoPathImpl("repo", "atg/REST/10.0-SNAPSHOT/REST-10.0-SNAPSHOT-lib+org.json.jar");
        MavenArtifactInfo artifactInfo = MavenArtifactInfo.fromRepoPath(path);
        assertTrue(artifactInfo.isValid());
        assertEquals(artifactInfo.getGroupId(), "atg");
        assertEquals(artifactInfo.getArtifactId(), "REST");
        assertEquals(artifactInfo.getVersion(), "10.0-SNAPSHOT");
        assertEquals(artifactInfo.getClassifier(), "lib+org.json");
        assertEquals(artifactInfo.getType(), "jar");
    }

    public void testClassifierWithDotAndNumber() {
        RepoPath path = new RepoPathImpl("repo",
                "org/test/common.atg/1.0.0-SNAPSHOT/common.atg-1.0.0-20111104.164556-1-lib+bcprov-jdk16-1.46.jar");
        MavenArtifactInfo artifactInfo = MavenArtifactInfo.fromRepoPath(path);
        assertTrue(artifactInfo.isValid());
        assertEquals(artifactInfo.getGroupId(), "org.test");
        assertEquals(artifactInfo.getArtifactId(), "common.atg");
        assertEquals(artifactInfo.getVersion(), "1.0.0-20111104.164556-1");
        assertEquals(artifactInfo.getClassifier(), "lib+bcprov-jdk16-1.46");
        assertEquals(artifactInfo.getType(), "jar");
    }

    public void testTarGz() {
        RepoPath path = new RepoPathImpl("repo",
                "org/jfrog/artifactory/10.0-SNAPSHOT/artifactory-10.0-20150515.164556-7-sources.tar.gz");
        MavenArtifactInfo artifactInfo = MavenArtifactInfo.fromRepoPath(path);
        assertTrue(artifactInfo.isValid());
        assertEquals(artifactInfo.getGroupId(), "org.jfrog");
        assertEquals(artifactInfo.getArtifactId(), "artifactory");
        assertEquals(artifactInfo.getVersion(), "10.0-20150515.164556-7");
        assertEquals(artifactInfo.getClassifier(), "sources");
        assertEquals(artifactInfo.getType(), "tar.gz");
    }

    public void testTarBz2() {
        RepoPath path = new RepoPathImpl("repo",
                "org/jfrog/artifactory/10.0-SNAPSHOT/artifactory-10.0-20150515.164556-7-sources.tar.bz2");
        MavenArtifactInfo artifactInfo = MavenArtifactInfo.fromRepoPath(path);
        assertTrue(artifactInfo.isValid());
        assertEquals(artifactInfo.getGroupId(), "org.jfrog");
        assertEquals(artifactInfo.getArtifactId(), "artifactory");
        assertEquals(artifactInfo.getVersion(), "10.0-20150515.164556-7");
        assertEquals(artifactInfo.getClassifier(), "sources");
        assertEquals(artifactInfo.getType(), "tar.bz2");
    }

    public void fileExtensionAsNumber() {
        RepoPath path = InfoFactoryHolder.get().createRepoPath("repo",
                "/org/jfrog/artifactory-core/2.0/artifactory-core-2.0.386");
        MavenArtifactInfo artifactInfo = MavenArtifactInfo.fromRepoPath(path);
        assertTrue(artifactInfo.isValid());
        assertEquals(artifactInfo.getType(), "386");
    }

    public void fromInvalidPath() {
        RepoPath path = InfoFactoryHolder.get().createRepoPath("repo", "com/5.4-SNAPSHOT");
        MavenArtifactInfo artifactInfo = MavenArtifactInfo.fromRepoPath(path);
        assertFalse(artifactInfo.isValid());
    }

    public void fromPathWithNoGroupId() {
        RepoPath path = InfoFactoryHolder.get().createRepoPath("repo", "com/5.4-SNAPSHOT/bob.jar");
        MavenArtifactInfo artifactInfo = MavenArtifactInfo.fromRepoPath(path);
        assertFalse(artifactInfo.isValid());
    }

    public void fromPathWithoutExtension() {
        RepoPath path = InfoFactoryHolder.get().createRepoPath("repo", "org/something/1/noExtension");
        MavenArtifactInfo artifactInfo = MavenArtifactInfo.fromRepoPath(path);
        assertTrue(artifactInfo.isValid());
        assertEquals(artifactInfo.getGroupId(), "org");
        assertEquals(artifactInfo.getArtifactId(), "something");
        assertEquals(artifactInfo.getVersion(), "1");
        assertNull(artifactInfo.getClassifier());
        assertEquals(artifactInfo.getType(), "jar");
    }
}
