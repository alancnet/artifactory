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

package org.artifactory.storage.db.fs.itest;

import org.artifactory.fs.ItemInfo;
import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.artifactory.storage.fs.service.FileService;
import org.artifactory.storage.fs.tree.ItemNode;
import org.artifactory.storage.fs.tree.ItemNodeFilter;
import org.artifactory.storage.fs.tree.ItemTree;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;

import javax.annotation.Nonnull;

import static org.testng.Assert.*;

/**
 * Tests the {@link org.artifactory.storage.fs.tree.ItemTree}.
 *
 * @author Yossi Shaul
 */
public class ItemTreeTest extends DbBaseTest {

    @Autowired
    FileService fileService;

    @BeforeClass
    public void setup() {
        importSql("/sql/nodes-for-service.sql");
    }

    public void rootItemTree() {
        RepoPathImpl repo1 = new RepoPathImpl("repo1", "");
        ItemTree itemTree = new ItemTree(repo1);
        ItemNode rootNode = itemTree.getRootNode();
        assertNotNull(rootNode);
        assertEquals(rootNode.getRepoPath(), repo1);
        assertTrue(rootNode.hasChildren());
        assertEquals(rootNode.getChildren().size(), 2);
    }

    public void preBuildTree() {
        RepoPathImpl repo1 = new RepoPathImpl("repo1", "");
        ItemTree itemTree = new ItemTree(repo1);
        ItemNode rootNode = itemTree.buildTree();
        assertNotNull(rootNode);
        assertEquals(rootNode.getRepoPath(), repo1);
    }

    public void treeWithFilter() {
        RepoPathImpl repo1 = new RepoPathImpl("repo1", "");
        ItemTree itemTree = new ItemTree(repo1, new ItemNodeFilter() {
            @Override
            public boolean accepts(@Nonnull ItemInfo itemInfo) {
                return false;   // filter everything
            }
        });
        ItemNode rootNode = itemTree.buildTree();
        assertNotNull(rootNode);    // filter doesn't affect root node
        assertEquals(rootNode.getRepoPath(), repo1);
        assertTrue(rootNode.getChildren().isEmpty());
    }

}
