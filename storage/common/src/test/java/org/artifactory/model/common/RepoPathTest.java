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

package org.artifactory.model.common;

import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Tests the RepoPath class.
 *
 * @author Yossi Shaul
 */
@Test
public class RepoPathTest {

    public void getParentRepoPathWithParent() {
        RepoPath child = new RepoPathImpl("repo", "a/b/c");
        RepoPath parent = child.getParent();
        Assert.assertEquals(parent, new RepoPathImpl("repo", "/a/b"));
    }

    public void getParentRepoPathLastParent() {
        RepoPath child = new RepoPathImpl("repo", "a/");
        RepoPath parent = child.getParent();
        Assert.assertEquals(parent, new RepoPathImpl("repo", ""));
    }

    public void getParentRepoPathForRoot() {
        RepoPath child = new RepoPathImpl("repo", "/");
        RepoPath parent = child.getParent();
        assertNull(parent);
    }

    public void getParentRepoPathWithNoParent() {
        RepoPath child = new RepoPathImpl("repo", "");
        RepoPath parent = child.getParent();
        assertNull(parent);
    }

    public void repoRootPath() {
        RepoPath repoPath = InternalRepoPathFactory.repoRootPath("repokey");
        assertEquals(repoPath.getRepoKey(), "repokey");
        assertEquals("", repoPath.getPath(), "Repository root path should be an empty string");
        assertTrue(repoPath.isRoot());
    }

    public void rootPath() {
        assertFalse(new RepoPathImpl("1", "2").isRoot());
        assertTrue(new RepoPathImpl("1", "").isRoot());
        assertTrue(new RepoPathImpl("1", "     ").isRoot());
        assertTrue(new RepoPathImpl("1", "     ").isRoot());
        assertTrue(new RepoPathImpl("1", null).isRoot());
        nullRepoKey();
    }

    private void nullRepoKey() {
        try {
            // Should throw exception repoKey can't be null
            new RepoPathImpl((String) null, "");
            fail();
        } catch (IllegalArgumentException e) {
            //We expected failure during RepoPath creation  (repo key can't be null);
        }
    }

    public void archiveResourcePath() {
        RepoPathImpl archivePath = new RepoPathImpl("key", "file.jar");
        RepoPath resourcePath = InternalRepoPathFactory.archiveResourceRepoPath(archivePath, "path/to/resource");
        Assert.assertEquals(resourcePath.getRepoKey(), archivePath.getRepoKey());
        assertEquals(resourcePath.getPath(), "file.jar!/path/to/resource");
    }

    public void archiveResourcePathLeadingSlash() {
        RepoPathImpl archivePath = new RepoPathImpl("key", "folder");
        RepoPath resourcePath = InternalRepoPathFactory.archiveResourceRepoPath(archivePath, "/path/to/resource");
        Assert.assertEquals(resourcePath.getRepoKey(), archivePath.getRepoKey());
        assertEquals(resourcePath.getPath(), "folder!/path/to/resource");
    }

    public void toPathWithRelativePath() {
        RepoPathImpl repoPath = new RepoPathImpl("key", "path/to/");
        assertEquals(repoPath.toPath(), "key/path/to/");
    }

    public void toPathRootRepoPath() {
        RepoPathImpl repoPath = new RepoPathImpl("key", "");
        assertEquals(repoPath.toPath(), "key/");
    }
}
