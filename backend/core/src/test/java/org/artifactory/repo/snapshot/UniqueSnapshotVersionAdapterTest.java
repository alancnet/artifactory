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

import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.spring.InternalArtifactoryContext;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.artifactory.mime.MavenNaming.MAVEN_METADATA_NAME;
import static org.easymock.EasyMock.*;

/**
 * Unit tests for {@link UniqueSnapshotVersionAdapter}.<p/> Only the easy to unit test are here, the rest are in the
 * integration tests.
 *
 * @author Yossi Shaul
 */
@Test
public class UniqueSnapshotVersionAdapterTest extends BaseSnapshotAdapterTest<UniqueSnapshotVersionAdapter> {

    private static final String SNAPSHOT_PATH = "groupPart1/groupPart2/artifactId/2.5-SNAPSHOT/";

    public UniqueSnapshotVersionAdapterTest() {
        super(new UniqueSnapshotVersionAdapter());
    }

    public void alreadyUniqueArtifact() {
        String path = SNAPSHOT_PATH + "artifactId-2.5-20071014.090200-4.jar";
        adapt(path, path, "Unique snapshots shouldn't be touched");
    }

    public void alreadyUniqueArtifactWithClassifier() {
        String path = SNAPSHOT_PATH + "artifactId-2.5-20071014.090200-4-classifier.jar";

        adapt(path, path, "Unique snapshots shouldn't be touched");
    }

    public void alreadyUniqueChecksumArtifact() {
        String path = SNAPSHOT_PATH + "artifactId-2.5-20071014.090200-4.jar.sha1";

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

    public void testArtifactWithSnapshotInTheClassifier() throws Exception {
        String path = SNAPSHOT_PATH + "artifactId-2.5-SNAPSHOT-lib-2.3-SNAPSHOT-jdk5.jar";
        RepoPath repoPath = RepoPathFactory.create("local", path);
        RepositoryService repositoryServiceMock = createMock(RepositoryService.class);
        expect(repositoryServiceMock.exists(repoPath)).andReturn(false).anyTimes();
        expect(repositoryServiceMock.exists(repoPath.getParent())).andReturn(false).anyTimes();
        expect(repositoryServiceMock.exists(new RepoPathImpl(repoPath.getParent(), MAVEN_METADATA_NAME)))
                .andReturn(false).anyTimes();
        InternalArtifactoryContext contextMock = createMock(InternalArtifactoryContext.class);
        expect(contextMock.getRepositoryService()).andReturn(repositoryServiceMock).anyTimes();
        ArtifactoryContextThreadBinder.bind(contextMock);
        replay(repositoryServiceMock, contextMock);
        String adjustedPath = adjust(path);
        verify(repositoryServiceMock, contextMock);
        Pattern pattern = Pattern.compile(
                "groupPart1/groupPart2/artifactId/2\\.5\\-SNAPSHOT/artifactId\\-2\\.5\\-(\\d{8}\\.\\d{6}\\-\\d{1})\\" +
                        "-lib\\-2\\.3\\-SNAPSHOT\\-jdk5\\.jar");
        Matcher matcher = pattern.matcher(adjustedPath);
        Assert.assertTrue(matcher.matches(), "Unexpected transformed path");
        ArtifactoryContextThreadBinder.unbind();
    }
}