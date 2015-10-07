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

import org.artifactory.api.maven.MavenArtifactInfo;
import org.artifactory.mime.MavenNaming;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Tests the MavenNaming.
 *
 * @author Yossi Shaul
 */
@Test
public class MavenNamingTest extends ArtifactoryHomeBoundTest {

    public void isSnapshotVersion() {
        assertTrue(MavenNaming.isSnapshot("1.5-SNAPSHOT"));
        assertFalse(MavenNaming.isSnapshot("1.5-SNAPSHOT123"));
        assertFalse(MavenNaming.isSnapshot("1.5"));
        assertFalse(MavenNaming.isSnapshot("1.5SNAPSHOT/1.5SNAPSHOT"));
        assertFalse(MavenNaming.isSnapshot("1.5.SNAPSHOT"));
    }

    public void isNonUniqueSnapshotVersion() {
        assertTrue(MavenNaming.isNonUniqueSnapshotVersion("1.2-SNAPSHOT"));
        assertFalse(MavenNaming.isNonUniqueSnapshotVersion("1.2-SNAPSHOT123"));
        assertFalse(MavenNaming.isNonUniqueSnapshotVersion("1.2"));
        assertFalse(MavenNaming.isNonUniqueSnapshotVersion("1.2SNAPSHOT"));
    }

    public void testIsNonUniqueSnapshotFilePath() {
        assertFalse(MavenNaming.isNonUniqueSnapshot("a/path/1.0-SNAPSHOT/1.0-SNAPSHOT"));
        assertTrue(MavenNaming.isNonUniqueSnapshot("a/path/1.0-SNAPSHOT/1.0-SNAPSHOT.pom"));
        assertTrue(MavenNaming.isNonUniqueSnapshot("a/path/1.0-SNAPSHOT/1.0-SNAPSHOT-sources.jar"));
        assertFalse(MavenNaming.isNonUniqueSnapshot("a/path/5.4-SNAPSHOT/path-5.4-20081214.090217-4.pom"));
    }

    public void testIsClientOrServerPom() {
        assertTrue(MavenNaming.isClientOrServerPom("a/path/1.0-SNAPSHOT/1.0-SNAPSHOT.pom"));
        assertTrue(MavenNaming.isClientOrServerPom("a/path/1.0-SNAPSHOT/pom.xml"));
        assertTrue(MavenNaming.isClientOrServerPom("a/path/1.0-SNAPSHOT/1.0.pom"));
        assertFalse(MavenNaming.isClientOrServerPom("a/path/1.0-SNAPSHOT/ivy.xml"));
        assertFalse(MavenNaming.isClientOrServerPom("a/path/1.0-SNAPSHOT/1.0-SNAPSHOT.jar"));
        assertFalse(MavenNaming.isClientOrServerPom("a/path/1.0-SNAPSHOT/1.0-SNAPSHOT.txt"));
    }

    public void testIsVersionUniqueSnapshot() {
        assertTrue(MavenNaming.isUniqueSnapshot("g/a/1.0-SNAPSHOT/artifact-5.4-20081214.090217-4.pom"));
        assertFalse(MavenNaming.isUniqueSnapshot("g/a/1.0SNAPSHOT/artifact-5.4-20081214.090217-4.pom"));
        assertFalse(MavenNaming.isUniqueSnapshot("g/a/1.0/artifact-5.4-20081214.090217-4.pom"));
    }

    public void testIsVersionUniqueSnapshotFileName() {
        assertTrue(MavenNaming.isUniqueSnapshotFileName("artifact-5.4-20081214.090217-4.pom"));
        assertTrue(MavenNaming.isUniqueSnapshotFileName("artifact-5.4-20081214.090217-4-classifier.pom"));
        assertFalse(MavenNaming.isUniqueSnapshotFileName("-20081214.090217-4.pom"), "No artifact id");
        assertFalse(MavenNaming.isUniqueSnapshotFileName("5.4-20081214.090217-4.pom"), "No artifact id");
        assertFalse(MavenNaming.isUniqueSnapshotFileName("artifact-5.4-20081214.090217-4"), "no type");
        assertFalse(MavenNaming.isUniqueSnapshotFileName("artifact-5.4-20081214.090217-4."), "empty type");
    }

    public void testUniqueVersionTimestamp() {
        String versionFile = "artifact-5.4-20081214.090217-4.pom";
        assertEquals(MavenNaming.getUniqueSnapshotVersionTimestamp(versionFile), "20081214.090217");
    }

    public void testUniqueVersionTimestampAndBuildNumber() {
        String versionFile = "artifact-5.4-20081214.090217-4.pom";
        assertEquals(MavenNaming.getUniqueSnapshotVersionTimestampAndBuildNumber(versionFile), "20081214.090217-4");
    }

    public void testUniqueSnapshotVersionBuildNumber() {
        String versionFile = "artifact-5.4-20081214.090217-4.pom";
        assertEquals(MavenNaming.getUniqueSnapshotVersionBuildNumber(versionFile), 4);

        versionFile = "artifact-456-20081214.120217-777.pom";
        assertEquals(MavenNaming.getUniqueSnapshotVersionBuildNumber(versionFile), 777);
    }

    public void uniqueSnapshotVersionBaseVersion() {
        String versionFile = "artifact-5.4-20081214.090217-4.pom";
        assertEquals(MavenNaming.getUniqueSnapshotVersionBaseVersion(versionFile), "5.4");

        versionFile = "artifact-aaa-20081214.120217-777.pom";
        assertEquals(MavenNaming.getUniqueSnapshotVersionBaseVersion(versionFile), "aaa");
    }

    public void isMetadata() {
        assertTrue(MavenNaming.isMavenMetadata("path/1.0-SNAPSHOT/maven-metadata.xml"));
        assertTrue(MavenNaming.isMavenMetadata("path/1.0-SNAPSHOT:maven-metadata.xml"));
        assertTrue(MavenNaming.isMavenMetadata("path/1.0:maven-metadata.xml"));
        assertFalse(MavenNaming.isMavenMetadata(
                "org/apache/maven/plugins/maven-plugin-plugin/maven-metadata-xyz-snapshots.xml"), "Not maven metadata");
    }

    public void isSnapshotMavenMetadata() {
        assertTrue(MavenNaming.isSnapshotMavenMetadata("path/1.0-SNAPSHOT/maven-metadata.xml"));
        assertTrue(MavenNaming.isSnapshotMavenMetadata("path/1.0-SNAPSHOT:maven-metadata.xml"));
        assertFalse(MavenNaming.isSnapshotMavenMetadata("path/1.0-SNAPSHOT/resource:maven-metadata.xml"));
        assertFalse(MavenNaming.isSnapshotMavenMetadata("path/1.0/maven-metadata.xml"), "Not a snapshot");
        assertFalse(MavenNaming.isSnapshotMavenMetadata("path/1.0/resource:maven-metadata.xml"), "Not a snapshot");
        assertFalse(MavenNaming.isSnapshotMavenMetadata("path/1.0-SNAPSHOT/other.metadata.xml"), "Not maven metadata");
        assertFalse(MavenNaming.isSnapshotMavenMetadata("path/1.0-SNAPSHOT/resource:other.metadata.xml"),
                "Not maven metadata");
        assertFalse(MavenNaming.isSnapshotMavenMetadata("path/1.0-SNAPSHOT"), "Not metadata path");
        assertFalse(MavenNaming.isSnapshotMavenMetadata("path/1.0-SNAPSHOT/"), "Not metadata path");
        assertFalse(MavenNaming.isSnapshotMavenMetadata("path/1.0-SNAPSHOT/:matadata-name"), "Not maven metadata");
        assertFalse(MavenNaming.isSnapshotMavenMetadata("path/1.0.SNAPSHOT/maven-metadata.xml"));
    }

    public void isMavenMetadataChecksum() {
        assertFalse(MavenNaming.isMavenMetadataChecksum(null));
        assertFalse(MavenNaming.isMavenMetadataChecksum(""));
        assertFalse(MavenNaming.isMavenMetadataChecksum("path/1.0-SNAPSHOT/maven-metadata.xml"));
        assertTrue(MavenNaming.isMavenMetadataChecksum("path/1.0-SNAPSHOT/maven-metadata.xml.md5"));
        assertFalse(MavenNaming.isMavenMetadataChecksum("path/1.0/maven-metadata.xml"));
        assertTrue(MavenNaming.isMavenMetadataChecksum("path/1.0/maven-metadata.xml.sha1"));
    }

    public void getArtifactInfo() {
        String[] invalidArtifactNames = new String[]{".jar", "1.0.jar", "artifactId.jar", "groupId/artifactId/1.0/",
                "1.0-artifactId.jar"};
        for (String invalidArtifactName : invalidArtifactNames) {
            MavenArtifactInfo info = MavenModelUtils.getInfoByMatching(invalidArtifactName);
            assertEquals(info.getArtifactId(), MavenArtifactInfo.NA, "Not a valid artifact name");
            assertEquals(info.getVersion(), MavenArtifactInfo.NA, "Not a valid artifact name");
            assertEquals(info.getClassifier(), null, "Not a valid artifact name");
        }
    }

    public void artifactWithPath() {
        MavenArtifactInfo info = MavenModelUtils.getInfoByMatching("groupId/artifactId/1.0/artifactId-1.0.jar");
        assertEquals(info.getArtifactId(), "groupId/artifactId/1.0/artifactId", "Matcher should not separate path");
        assertEquals(info.getVersion(), "1.0", "Version should be 1.0");
        assertEquals(info.getClassifier(), null, "No classifier expected");
        assertEquals(info.getType(), "jar", "Unexpected type");
    }

    public void pathWithCompositeFileExtension() {
        MavenArtifactInfo info = MavenModelUtils.getInfoByMatching("artifactId-1.0.another.jar");
        assertEquals(info.getArtifactId(), "artifactId", "Unexpected artifact id");
        assertEquals(info.getVersion(), "1.0.another", "Unexpected version");
        assertEquals(info.getClassifier(), null, "No classifier expected");
        assertEquals(info.getType(), "jar", "Unexpected type");
    }

    public void artifactWithNoClassifier() {
        MavenArtifactInfo info = MavenModelUtils.getInfoByMatching("artifactId-1.0.war");
        assertEquals(info.getArtifactId(), "artifactId", "Artifact id should be 'artifactId'");
        assertEquals(info.getVersion(), "1.0", "Version should be 1.0");
        assertEquals(info.getClassifier(), null, "No classifier expected");
        assertEquals(info.getType(), "war", "Unexpected type");
    }

    public void artifactWithNumericArtifactId() {
        MavenArtifactInfo info = MavenModelUtils.getInfoByMatching("12354-1.0.zara");
        assertEquals(info.getArtifactId(), "12354", "Artifact id should be '12354'");
        assertEquals(info.getVersion(), "1.0", "Version should be 1.0");
        assertEquals(info.getClassifier(), null, "No classifier expected");
        assertEquals(info.getType(), "zara", "Unexpected type");
    }

    @DataProvider(name = "artifactWithAlphaNumberArtifactId")
    private Object[][] getArtifactWithAlphaNumberArtifactId() {
        return new Object[][]{
                {"artifactId1231"},
                {"1213artifactId"},
                {"1213artifactId1231"}
        };
    }

    @Test(dataProvider = "artifactWithAlphaNumberArtifactId")
    public void artifactWithAlphaNumberArtifactId(String artifactName) {
        MavenArtifactInfo info = MavenModelUtils.getInfoByMatching(artifactName + "-1.0.jar");
        assertEquals(info.getArtifactId(), artifactName,
                "Artifact id should be '" + artifactName + "'");
        assertEquals(info.getVersion(), "1.0", "Version should be 1.0");
        assertEquals(info.getClassifier(), null, "No classifier expected");
        assertEquals(info.getType(), "jar", "Unexpected type");
    }

    @DataProvider(name = "artifactWithMultipleArtifactIds")
    private Object[][] getArtifactWithMultipleArtifactIds() {
        return new Object[][]{
                {"artifactId-artifactId"},
                {"artifactId2-artifactId"},
                {"artifactId-artifactId2"},
                {"artifactId2-artifactId2"},
                {"2artifactId-artifactId"}
                //{"artifactId-2artifactId"},
                //{"2artifactId-2artifactId"},
                //{"123123-123123"},
        };
    }

    @Test(dataProvider = "artifactWithMultipleArtifactIds")
    public void artifactWithMultipleArtifactIds(String artifactName) {
        MavenArtifactInfo info = MavenModelUtils.getInfoByMatching(artifactName + "-1.0.jar");
        assertEquals(info.getArtifactId(), artifactName,
                "Artifact id should be '" + artifactName + "'");
        assertEquals(info.getVersion(), "1.0", "Version should be 1.0");
        assertEquals(info.getClassifier(), null, "No classifier expected");
        assertEquals(info.getType(), "jar", "Unexpected type");
    }

    @Test(enabled = false)
    //Difficult to guess
    public void artifactWithAlphaVersion() {
        MavenArtifactInfo info = MavenModelUtils.getInfoByMatching("artifactId-alpha.pom");
        assertEquals(info.getArtifactId(), "artifact", "Artifact id should be 'artifactId'");
        assertEquals(info.getVersion(), "alpha", "Version should be 'alpha'");
        assertEquals(info.getClassifier(), null, "No classifier expected");
        assertEquals(info.getType(), "pom", "Unexpected type");
    }

    @DataProvider(name = "artifactWithAlphaNumericVersion")
    private Object[][] getArtifactWithAlphaNumericVersion() {
        return new Object[][]{
                {"1.0alpha"}
                //{"alpha1.0"}, //Difficult to guess
                //{"1.0-version"} //Difficult to guess
        };
    }

    @Test(dataProvider = "artifactWithAlphaNumericVersion")
    public void artifactWithAlphaNumericVersion(String version) {
        MavenArtifactInfo info = MavenModelUtils.getInfoByMatching("artifactId-" + version + ".jar");
        assertEquals(info.getArtifactId(), "artifactId",
                "Artifact id should be 'artifactId'");
        assertEquals(info.getVersion(), version, "Version should be '" + version + "'");
        assertEquals(info.getClassifier(), null, "No classifier expected");
        assertEquals(info.getType(), "jar", "Unexpected type");
    }

    public void artifactWithClassifier() {
        MavenArtifactInfo info = MavenModelUtils.getInfoByMatching("artifactId-1.0-classifier.jar");
        assertEquals(info.getArtifactId(), "artifactId", "Artifact id should be 'artifactId'");
        assertEquals(info.getVersion(), "1.0", "Version should be '1.0'");
        assertEquals(info.getClassifier(), "classifier", "Classifier should be 'classifier'");
        assertEquals(info.getType(), "jar", "Unexpected type");
    }

    public void artifactWithNumericClassifier() {
        MavenArtifactInfo info = MavenModelUtils.getInfoByMatching("artifactId-1.0-1234.jar");
        assertEquals(info.getArtifactId(), "artifactId", "Artifact id should be 'artifactId'");
        assertEquals(info.getVersion(), "1.0", "Version should be '1.0'");
        assertEquals(info.getClassifier(), "1234", "Classifier should be '1234'");
        assertEquals(info.getType(), "jar", "Unexpected type");
    }

    @DataProvider(name = "artifactWithAlphaNumericClassifier")
    private Object[][] getArtifactWithAlphaNumericClassifier() {
        return new Object[][]{
                {"classifier123"},
                {"123classifier"}
        };
    }

    @Test(dataProvider = "artifactWithAlphaNumericClassifier")
    public void artifactWithAlphaNumericClassifier(String classifier) {
        MavenArtifactInfo info = MavenModelUtils.getInfoByMatching("artifactId-1.0-" + classifier + ".jar");
        assertEquals(info.getArtifactId(), "artifactId",
                "Artifact id should be 'artifactId'");
        assertEquals(info.getVersion(), "1.0", "Version should be '1.0'");
        assertEquals(info.getClassifier(), classifier, "Classifier should be '" + classifier + "'");
        assertEquals(info.getType(), "jar", "Unexpected type");
    }

    public void artifactWithShortExtension() {
        MavenArtifactInfo info = MavenModelUtils.getInfoByMatching("artifactId-1.0.ba");
        assertEquals(info.getArtifactId(), "artifactId", "Artifact id should be 'artifactId'");
        assertEquals(info.getVersion(), "1.0", "Version should be '1.0'");
        assertNull(info.getClassifier(), "Classifier should be null");
        assertEquals(info.getType(), "ba", "Unexpected type");
    }

    public void artifactWithLongExtension() {
        MavenArtifactInfo info = MavenModelUtils.getInfoByMatching("artifactId-1.0.bababababa");
        assertEquals(info.getArtifactId(), "artifactId", "Artifact id should be 'artifactId'");
        assertEquals(info.getVersion(), "1.0", "Version should be '1.0'");
        assertNull(info.getClassifier(), "Classifier should be null");
        assertEquals(info.getType(), "bababababa", "Unexpected type");
    }
}
