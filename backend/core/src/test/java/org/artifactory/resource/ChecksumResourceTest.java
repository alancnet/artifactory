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

package org.artifactory.resource;

import org.artifactory.checksum.ChecksumType;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.mime.MimeType;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Unit tests the ChecksumResource.
 *
 * @author Yossi Shaul
 */
@Test
public class ChecksumResourceTest extends ArtifactoryHomeBoundTest {

    public void checksumResource() {
        RepoPath fileRepoPath = InternalRepoPathFactory.create("test", "test.jar");
        FileResource fileResource = new FileResource(InfoFactoryHolder.get().createFileInfo(fileRepoPath));

        ChecksumResource resource = new ChecksumResource(fileResource, ChecksumType.sha1, "456789");

        assertEquals(resource.getRepoPath(), InternalRepoPathFactory.create("test", "test.jar.sha1"));
        assertEquals(resource.getResponseRepoPath(), InternalRepoPathFactory.create("test", "test.jar.sha1"));
        assertEquals(resource.getMimeType(), MimeType.checksum);
        assertEquals(resource.getSize(), "456789".length());
    }
}
