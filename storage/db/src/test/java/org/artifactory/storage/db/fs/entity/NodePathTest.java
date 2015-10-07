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

package org.artifactory.storage.db.fs.entity;

import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.repo.RepoPath;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Tests {@link org.artifactory.storage.db.fs.entity.NodePath}.
 *
 * @author Yossi Shaul
 */
@Test
public class NodePathTest {

    public void simpleConstructor() {
        NodePath nodePath = new NodePath("repo", "path/to", "name", true);
        assertEquals(nodePath.getRepo(), "repo");
        assertEquals(nodePath.getPath(), "path/to");
        assertEquals(nodePath.getName(), "name");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void constructorNoRepo() {
        new NodePath("", "path/to", "name", true);
    }

    public void constructorNoPath() {
        NodePath path = new NodePath("repo", null, "name", true);
        assertEquals(path.getRepo(), "repo");
        assertEquals(path.getPath(), "");
        assertEquals(path.getName(), "name");
        assertEquals(path.getDepth(), 1);
    }

    public void constructorRootPath() {
        NodePath path = new NodePath("repo", null, "", true);
        assertEquals(path.getRepo(), "repo");
        assertEquals(path.getPath(), "");
        assertEquals(path.getName(), "");
        assertEquals(path.getDepth(), 0);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void constructorPathWithNoName() {
        new NodePath("repo", "path", "", true);
    }

    public void toRepoPath() {
        NodePath nodePath = new NodePath("repo", "path/to", "name", true);
        RepoPath repoPath = nodePath.toRepoPath();
        assertEquals(repoPath.getRepoKey(), "repo");
        assertEquals(repoPath.getPath(), "path/to/name");
    }

    public void toRepoPathDepth1() {
        NodePath nodePath = new NodePath("repo", "", "name", true);
        RepoPath repoPath = nodePath.toRepoPath();
        assertEquals(repoPath.getRepoKey(), "repo");
        assertEquals(repoPath.getPath(), "name");
    }

    public void buildFromRepoPath() {
        NodePath nodePath = NodePath.fromRepoPath(new RepoPathImpl("repo", "/path/to/name"));
        assertEquals(nodePath.getRepo(), "repo");
        assertEquals(nodePath.getPath(), "path/to");
        assertEquals(nodePath.getName(), "name");
    }

    public void buildFromRootRepoPath() {
        NodePath path = NodePath.fromRepoPath(new RepoPathImpl("repo", ""));
        assertEquals(path.getRepo(), "repo");
        assertEquals(path.getPath(), "");
        assertEquals(path.getName(), "");
        assertEquals(path.getDepth(), 0);
    }

    public void buildFromRepoPathDepth1() {
        NodePath nodePath = NodePath.fromRepoPath(new RepoPathImpl("repo", "toto"));
        assertEquals(nodePath.getRepo(), "repo");
        assertEquals(nodePath.getPath(), "");
        assertEquals(nodePath.getName(), "toto");
    }

    public void pathNameOfRoot() {
        assertEquals(new NodePath("repo", "", "", true).getPathName(), "");
    }

    public void pathNameRootChild() {
        assertEquals(new NodePath("repo", "", "name", true).getPathName(), "name");
    }

    public void pathName() {
        assertEquals(new NodePath("repo", "path", "name", true).getPathName(), "path/name");
    }
}
