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

package org.artifactory.repo.snapshot;

import org.testng.annotations.Test;

/**
 * Unit tests for {@link NonUniqueSnapshotVersionAdapter}.<p/> Only the easy to unit test are here, the rest are in the
 * integration tests.
 *
 * @author Yossi Shaul
 */
@Test
public class NonUniqueSnapshotVersionAdapterTest extends BaseSnapshotAdapterTest<NonUniqueSnapshotVersionAdapter> {

    private final String SNAPSHOT_PATH = "groupId/artifactId/1.4-SNAPSHOT/";

    public NonUniqueSnapshotVersionAdapterTest() {
        super(new NonUniqueSnapshotVersionAdapter());
    }

    public void uniqueArtifact() {
        String path = SNAPSHOT_PATH + "artifactId-1.4-20081214.090217-4.jar";

        adapt(path, SNAPSHOT_PATH + "artifactId-1.4-SNAPSHOT.jar");
    }

    public void uniqueArtifactWithClassifier() {
        String path = SNAPSHOT_PATH + "artifactId-1.4-20081214.090217-4-classifier.jar";

        adapt(path, SNAPSHOT_PATH + "artifactId-1.4-SNAPSHOT-classifier.jar");
    }

    public void uniqueArtifactWithComplexClassifier() {
        String path = SNAPSHOT_PATH + "artifactId-1.4-20081214.090217-4-a-2complex-classifier.jar";

        adapt(path, SNAPSHOT_PATH + "artifactId-1.4-SNAPSHOT-a-2complex-classifier.jar");
    }

    public void alreadyNonUniqueArtifact() {
        String path = SNAPSHOT_PATH + "artifactId-1.4-SNAPSHOT.jar";

        adapt(path, path);
    }

    public void uniqueChecksumArtifact() {
        String path = SNAPSHOT_PATH + "artifactId-1.4-20081214.090217-4.jar.sha1";

        adapt(path, SNAPSHOT_PATH + "artifactId-1.4-SNAPSHOT.jar.sha1");
    }

    public void alreadyNonUniqueChecksumArtifact() {
        String path = SNAPSHOT_PATH + "artifactId-1.4-SNAPSHOT.jar.md5";

        adapt(path, path);
    }

    public void artifactWithReleaseVersion() {
        String path = SNAPSHOT_PATH + "artifactId-1.4.ivy";

        adapt(path, path, "Files with release version should not be affected");
    }

    public void artifactWithNoMavenStructure() {
        String path = SNAPSHOT_PATH + "blabla.xml";

        adapt(path, path, "Non-maven structured files with release version should not be affected");
    }

    public void uniqueArtifactVersionWithDashes() {
        String snapshotPath = "groupId/artifact_id/2.5-TEST-SNAPSHOT/";
        String uniqueVersionFile = "artifact_id-2.5-TEST-20071014.090217-2.jar";
        String path = snapshotPath + uniqueVersionFile;

        adapt(path, snapshotPath + "artifact_id-2.5-TEST-SNAPSHOT.jar");
    }
}
