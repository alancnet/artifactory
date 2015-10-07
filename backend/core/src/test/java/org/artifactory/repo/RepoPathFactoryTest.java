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

package org.artifactory.repo;

import org.artifactory.model.common.RepoPathImpl;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Yoav Landman
 */
@Test
public class RepoPathFactoryTest {

    public void testRepoPathFactory() {
        RepoPath repoPath = InternalRepoPathFactory.create("repox", "/a/b/c/");
        assertEquals(repoPath, InternalRepoPathFactory.create("repox", "/a/b/c/"));
        assertEquals(repoPath, InternalRepoPathFactory.create("repox", "a/b/c"));
        assertEquals(repoPath, InternalRepoPathFactory.create("repox", "a/b/c/"));
        assertEquals(repoPath, InternalRepoPathFactory.create("repox", "/a/b/c"));
    }

    public void pathWithLeadingSlash() {
        assertEquals(RepoPathFactory.create("/a/b/c"), new RepoPathImpl("a", "b/c"));
        assertEquals(RepoPathFactory.create("/a"), new RepoPathImpl("a", ""));
    }

    public void rootRepositoryPathSingleArgument() {
        RepoPath repoPath = RepoPathFactory.create("myrepo");
        assertEquals(repoPath.getRepoKey(), "myrepo");
        assertEquals(repoPath.getPath(), "");
        assertTrue(repoPath.isFolder());
        assertTrue(repoPath.isRoot());
    }

    public void rootRepositoryPath() {
        RepoPath repoPath = RepoPathFactory.create("myrepo", "");
        assertEquals(repoPath.getRepoKey(), "myrepo");
        assertEquals(repoPath.getPath(), "");
        assertTrue(repoPath.isFolder());
        assertTrue(repoPath.isRoot());
    }

    public void singleArgumentFactoryFolder() {
        RepoPath repoPath = RepoPathFactory.create("myrepo/a/b/c/");
        assertEquals(repoPath.getRepoKey(), "myrepo");
        assertEquals(repoPath.getPath(), "a/b/c");
        assertTrue(repoPath.isFolder());
    }
}