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

package org.artifactory.storage.db.fs.itest.dao;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.storage.db.DbService;
import org.artifactory.storage.db.DbType;
import org.artifactory.storage.db.fs.dao.NodesDao;
import org.artifactory.storage.db.fs.entity.Node;
import org.artifactory.storage.db.fs.entity.NodeBuilder;
import org.artifactory.storage.db.fs.entity.NodePath;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.util.List;

import static org.testng.Assert.*;

/**
 * Low level tests of the {@link org.artifactory.storage.db.fs.dao.NodesDao}.
 *
 * @author Yossi Shaul
 */
@Test
public class NodesDaoTest extends DbBaseTest {

    @Autowired
    private NodesDao nodesDao;

    private NodePath fileNodePath = new NodePath("repo1", "ant/ant/1.5", "ant-1.5.jar", true);

    @BeforeClass
    public void setup() {
        importSql("/sql/nodes.sql");
    }

    public void createDirectoryNode() throws SQLException {
        NodeBuilder b = new NodeBuilder().nodeId(800).file(false).repo("repo").path("path/to/dir").name("name")
                .createdBy("yossis").modifiedBy("yossis");

        nodesDao.create(b.build());
    }

    @Test(dependsOnMethods = "createDirectoryNode")
    public void loadDirectoryNodeByPath() throws SQLException {
        Node node = nodesDao.get(new NodePath("repo", "path/to/dir", "name", false));
        assertNotNull(node);
        assertEquals(node.getNodeId(), 800);
        assertFalse(node.isFile());
        assertEquals(node.getDepth(), 4);
        assertEquals(node.getRepo(), "repo");
        assertEquals(node.getPath(), "path/to/dir");
        assertEquals(node.getName(), "name");
        assertEquals(node.getCreatedBy(), "yossis");
        assertEquals(node.getModifiedBy(), "yossis");
    }

    @Test(dependsOnMethods = "loadDirectoryNodeByPath")
    public void loadDirectoryNodeById() throws SQLException {
        Node node = nodesDao.get(800);
        EqualsBuilder.reflectionEquals(node, nodesDao.get(new NodePath("repo", "path/to/dir", "name", false)));
    }

    public void itemExists() throws SQLException {
        assertTrue(nodesDao.exists(new NodePath("repo1", "", "org", false)));
    }

    public void itemNotExists() throws SQLException {
        assertFalse(nodesDao.exists(new NodePath("repo1", "", "nosuchfile", false)));
    }

    public void deleteDirectoryNode() throws SQLException {
        NodeBuilder b = new NodeBuilder().nodeId(801).file(false).repo("repo").path("path/to/dir").name("todelete")
                .createdBy("yossis").modifiedBy("yossis");

        Node inserted = b.build();
        nodesDao.create(inserted);
        Node loaded = nodesDao.get(inserted.getNodePath());
        assertNotNull(loaded);
        boolean deleted = nodesDao.delete(loaded.getNodeId());
        assertTrue(deleted);
        assertFalse(nodesDao.exists(inserted.getNodePath()));
    }

    public void deleteNonExistent() throws SQLException {
        boolean deleted = nodesDao.delete(990);
        assertFalse(deleted);
    }

    public void getChildrenOfRoot() throws SQLException {
        NodePath path = new NodePath("repo1", "", "", false);
        List<? extends Node> children = nodesDao.getChildren(path);
        assertEquals(children.size(), 3);

        assertTrue(nodesDao.hasChildren(path));
    }

    public void getChildrenOfNodeDirectlyUnderRoot() throws SQLException {
        // nodes directly under root has name but no path - hence special test case
        NodePath path = new NodePath("repo1", "", "org", false);
        List<? extends Node> children = nodesDao.getChildren(path);
        assertEquals(children.size(), 1);

        assertTrue(nodesDao.hasChildren(path));
    }

    public void getChildrenOfNodeDirectlyUnderRootWithCousinStartingWithSamePrefix() throws SQLException {
        NodePath path = new NodePath("repo1", "", "ant", false);
        List<? extends Node> children = nodesDao.getChildren(path);
        assertEquals(children.size(), 1);

        assertTrue(nodesDao.hasChildren(path));
    }

    public void getChildrenOfLeafFolderNode() throws SQLException {
        NodePath leaf = new NodePath("repo1", "org/yossis/tools", "test.bin", true);
        assertTrue(nodesDao.exists(leaf));
        assertEquals(nodesDao.getChildren(leaf).size(), 0);
        assertFalse(nodesDao.hasChildren(leaf));
    }

    public void getChildrenOfLeafFileNode() throws SQLException {
        assertTrue(nodesDao.exists(fileNodePath));
        assertEquals(nodesDao.getChildren(fileNodePath).size(), 0);
        assertFalse(nodesDao.hasChildren(fileNodePath));
    }

    public void countRepositoryFiles() throws SQLException {
        assertEquals(nodesDao.getFilesCount("repo1"), 4);
    }

    public void countFilesUnderFolder() throws SQLException {
        assertEquals(nodesDao.getFilesCount(new NodePath("repo1", "", "ant", false)), 1);
    }

    public void countFilesUnderFolderWithDirectChildren() throws SQLException {
        assertEquals(nodesDao.getFilesCount(new NodePath("repo1", "ant/ant", "1.5", false)), 1);
    }

    public void countFilesUnderNonExistentFolder() throws SQLException {
        assertEquals(nodesDao.getFilesCount(new NodePath("repo1", "xxx", "boo", false)), 0);
    }

    public void countFilesUnderFile() throws SQLException {
        assertEquals(nodesDao.getFilesCount(fileNodePath), 0);
    }

    public void getFilesTotalSize() throws SQLException {
        //hardcoded in nodes.sql
        final int antExpectedSize = 716139;
        final int totalExpectedSize = 846441;
        final int toolsExpectedSize = 130302;

        assertEquals(nodesDao.getFilesTotalSize(new NodePath("repo1", "", "ant", false)), antExpectedSize,
                "single file size should be " + antExpectedSize);

        assertEquals(nodesDao.getFilesTotalSize("repo1"), totalExpectedSize,
                "total size of repo1 should be " + totalExpectedSize);

        long filesTotalSize = 0;
        for (Node node : nodesDao.getChildren(new NodePath("repo1", "", "", false))) {
            filesTotalSize += nodesDao.getFilesTotalSize(node.getNodePath());
        }

        assertEquals(filesTotalSize, totalExpectedSize,
                "sum of children size in repo1 should be " + filesTotalSize);

        assertEquals(nodesDao.getFilesTotalSize(new NodePath("repo1", "org/yossis", "tools", false)), toolsExpectedSize,
                "total size of files under org/yossis should be " + toolsExpectedSize);
    }

    public void countRepositoryFilesAndFolders() throws SQLException {
        assertEquals(nodesDao.getNodesCount("repo1"), 14);
    }

    public void countRepositoryFilesAndFoldersUnderFolder() throws SQLException {
        assertEquals(nodesDao.getNodesCount(new NodePath("repo1", "", "ant", false)), 3);
    }

    public void countFilesAndFoldersUnderFolderWithDirectChildren() throws SQLException {
        assertEquals(nodesDao.getNodesCount(new NodePath("repo1", "ant/ant", "1.5", false)), 1);
    }

    public void countFilesAndFoldersUnderNonExistentFolder() throws SQLException {
        assertEquals(nodesDao.getNodesCount(new NodePath("repo1", "xxx", "boo", false)), 0);
    }

    public void countFilesAndFoldersUnderFile() throws SQLException {
        assertEquals(nodesDao.getNodesCount(fileNodePath), 0);
    }

    public void countFilesUnderFolderWithPrefix() throws SQLException {
        assertEquals(nodesDao.getFilesCount(new NodePath("repo2", "", "a", false)), 2, "Files from 'repo2:/aa' were counted");
    }

    public void countNodesUnderFolderWithPrefix() throws SQLException {
        assertEquals(nodesDao.getNodesCount(new NodePath("repo2", "", "a", false)), 3, "Nodes from 'repo2:/aa' were counted");
    }

    public void getTotalSizeUnderFolderWithPrefix() throws SQLException {
        assertEquals(nodesDao.getFilesTotalSize(new NodePath("repo2", "", "a", false)),  716139 * 2, "Files from 'repo2:/aa' were counted");
    }

    public void updateFolderNode() throws SQLException {
        NodeBuilder b = new NodeBuilder().nodeId(50).file(false).repo("repo").path("path/to/dir").name("toupdate")
                .createdBy("yossis").modifiedBy("yossis");

        nodesDao.create(b.build());

        Node nodeToUpdate = b.repo("repo2").path("new/path").name("updatedfolder")
                .created(1111).createdBy("updater-creator")
                .modified(2222).modifiedBy("updater").updated(3333).build();

        int updateCount = nodesDao.update(nodeToUpdate);
        assertEquals(updateCount, 1);

        Node updatedNode = nodesDao.get(nodeToUpdate.getNodePath());
        assertNotNull(updatedNode);
        assertEquals(updatedNode.getRepo(), nodeToUpdate.getRepo());
        assertEquals(updatedNode.getPath(), nodeToUpdate.getPath());
        assertEquals(updatedNode.getName(), nodeToUpdate.getName());
        assertEquals(updatedNode.getCreated(), nodeToUpdate.getCreated());
        assertEquals(updatedNode.getCreatedBy(), nodeToUpdate.getCreatedBy());
        assertEquals(updatedNode.getModified(), nodeToUpdate.getModified());
        assertEquals(updatedNode.getModifiedBy(), nodeToUpdate.getModifiedBy());
        assertEquals(updatedNode.getUpdated(), nodeToUpdate.getUpdated());
        assertEquals(updatedNode.getNodeId(), 50, "Node id shouldn't have been updated");
        assertEquals(updatedNode.isFile(), false, "Node type shouldn't have been updated");
    }


    public void nodeIdRoot() throws SQLException {
        assertEquals(nodesDao.getNodeId(new NodePath("repo1", "", "", false)), 1);
    }

    public void nodeIdNoSuchNode() throws SQLException {
        assertEquals(nodesDao.getNodeId(new NodePath("repo2", "no", "folder", false)), DbService.NO_DB_ID);
    }

    public void nodeSha1OfFile() throws SQLException {
        assertEquals(nodesDao.getNodeSha1(new NodePath("repo1", "org/yossis/tools", "test.bin", true)),
                "acab88fc2a043c2479a6de676a2f8179e9ea2167");
    }

    public void nodeSha1NotExist() throws SQLException {
        assertNull(nodesDao.getNodeSha1(new NodePath("repo2", "no", "folder", false)));
    }

    public void nodeSha1OfFolder() throws SQLException {
        assertTrue(nodesDao.exists(new NodePath("repo1", "org/yossis", "tools", false)));
        assertNull(nodesDao.getNodeSha1(new NodePath("repo1", "org/yossis", "tools", false)));
    }

    public void searchFilesByProperty() throws SQLException {
        List<Node> nodes = nodesDao.searchFilesByProperty("repo1", "build.number", "67");
        assertNotNull(nodes);
        assertEquals(nodes.size(), 1);
        assertEquals(nodes.get(0).getNodeId(), 5);
    }

    public void searchFilesByPropertyNoMatch() throws SQLException {
        List<Node> nodes = nodesDao.searchFilesByProperty("repo1", "build.number", "68");
        assertNotNull(nodes);
        assertEquals(nodes.size(), 0);
    }

    public void findChecksumsBySha1() throws SQLException {
        List<Node> nodes;
        nodes = nodesDao.searchByChecksum(ChecksumType.sha1, "dcab88fc2a043c2479a6de676a2f8179e9ea2167");
        assertEquals(nodes.size(), 1);
        assertEquals(nodes.get(0).getNodeId(), 5L);
        assertEquals(nodes.get(0).getRepo(), "repo1");
        assertEquals(nodes.get(0).getName(), "ant-1.5.jar");
        nodes = nodesDao.searchByChecksum(ChecksumType.sha1, "dddd88fc2a043c2479a6de676a2f8179e9eadddd");
        assertEquals(nodes.size(), 2);
        Node node = getById(nodes, 13);
        assertNotNull(node);
        assertEquals(node.getRepo(), "repo1");
        assertEquals(node.getName(), "file3.bin");
        node = getById(nodes, 15);
        assertNotNull(node);
        assertEquals(node.getRepo(), "repo-copy");
        assertEquals(node.getName(), "file3.bin");
        nodes = nodesDao.searchByChecksum(ChecksumType.sha1, "acab88fc2a043c2479a6de676a2f8179e9ea2222");
        assertTrue(nodes.isEmpty());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testFindEmptyChecksumsBySha1() throws SQLException {
        nodesDao.searchByChecksum(ChecksumType.sha1, "wrong");
    }

    public void findChecksumsByMd5() throws SQLException {
        List<Node> nodes;
        nodes = nodesDao.searchByChecksum(ChecksumType.md5, "902a360ecad98a34b59863c1e65bcf71");
        assertEquals(nodes.size(), 1);
        assertEquals(nodes.get(0).getNodeId(), 5L);
        assertEquals(nodes.get(0).getRepo(), "repo1");
        assertEquals(nodes.get(0).getName(), "ant-1.5.jar");
        nodes = nodesDao.searchByChecksum(ChecksumType.md5, "502a360ecad98a34b59863c1e65bcf71");
        assertEquals(nodes.size(), 2);
        Node node = getById(nodes, 13);
        assertNotNull(node);
        assertEquals(node.getRepo(), "repo1");
        assertEquals(node.getName(), "file3.bin");
        node = getById(nodes, 15);
        assertNotNull(node);
        assertEquals(node.getRepo(), "repo-copy");
        assertEquals(node.getName(), "file3.bin");
        nodes = nodesDao.searchByChecksum(ChecksumType.md5, "902a360ecad98a34b59863c1e65b2222");
        assertTrue(nodes.isEmpty());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testFindEmptyChecksumsByMd5() throws SQLException {
        nodesDao.searchByChecksum(ChecksumType.md5, "wrong");
    }

    public void searchBadSha1Checksums() throws Exception {
        List<Node> nodes = nodesDao.searchBadChecksums(ChecksumType.sha1);
        assertEquals(nodes.size(), 3);
        Node node = getById(nodes, 12);
        assertNotNull(node);
        assertEquals(node.getRepo(), "repo1");
        assertEquals(node.getName(), "file2.bin");
        node = getById(nodes, 13);
        assertNotNull(node);
        assertEquals(node.getRepo(), "repo1");
        assertEquals(node.getName(), "file3.bin");
        node = getById(nodes, 15);
        assertNotNull(node);
        assertEquals(node.getRepo(), "repo-copy");
        assertEquals(node.getName(), "file3.bin");
    }

    public void findMissingDirectories() throws Exception {
        if (storageProperties.getDbType() == DbType.DERBY) {
            return; // CONCAT doesn't work on Derby + it's irrelevant
        }
        List<Node> nodes = nodesDao.getOrphanNodes(new NodePath("repo3", null, null, false));
        assertEquals(nodes.size(), 2);
        Node node = getById(nodes, 33);
        assertNotNull(node);
        assertEquals(node.getPath(), "a/B");
        assertEquals(node.getName(), "C");
        node = getById(nodes, 34);
        assertNotNull(node);
        assertEquals(node.getPath(), "B");
        assertEquals(node.getName(), "test.txt");
    }

    public void findMissingDirectoriesUnderPath() throws Exception {
        if (storageProperties.getDbType() == DbType.DERBY) {
            return; // CONCAT doesn't work on Derby + it's irrelevant
        }
        List<Node> nodes = nodesDao.getOrphanNodes(new NodePath("repo3", "a", "B", false));
        assertEquals(nodes.size(), 1);
        Node node = getById(nodes, 33);
        assertNotNull(node);
        assertEquals(node.getPath(), "a/B");
        assertEquals(node.getName(), "C");
    }

    public void searchBadMd5Checksums() throws Exception {
        List<Node> nodes = nodesDao.searchBadChecksums(ChecksumType.md5);
        assertEquals(nodes.size(), 1);
        Node node = getById(nodes, 17);
        assertNotNull(node);
        assertEquals(node.getRepo(), "repo-copy");
        assertEquals(node.getName(), "badmd5.jar");
    }

    private Node getById(List<Node> nodes, long id) {
        for (Node node : nodes) {
            if (node.getNodeId() == id) {
                return node;
            }
        }
        return null;
    }

}
