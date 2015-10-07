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

package org.artifactory.storage.db.fs.itest.service;

import ch.qos.logback.classic.Level;
import org.artifactory.api.repo.exception.FolderExpectedException;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.fs.FileInfo;
import org.artifactory.fs.FolderInfo;
import org.artifactory.fs.ItemInfo;
import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.model.xstream.fs.FolderInfoImpl;
import org.artifactory.sapi.fs.VfsItem;
import org.artifactory.storage.db.DbService;
import org.artifactory.storage.db.fs.service.FileServiceImpl;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.artifactory.storage.fs.VfsException;
import org.artifactory.storage.fs.VfsItemNotFoundException;
import org.artifactory.storage.fs.service.FileService;
import org.artifactory.test.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.*;

/**
 * Low level integration tests for the file service.
 *
 * @author Yossi Shaul
 */
public class FileServiceImplTest extends DbBaseTest {

    @Autowired
    private FileService fileService;

    @BeforeClass
    public void setup() {
        importSql("/sql/nodes-for-service.sql");
    }

    public void countAllFiles() throws VfsException {
        int count = fileService.getFilesCount();
        assertEquals(count, 6);
    }

    public void countFilesUnderRepo() throws VfsException {
        int count = fileService.getFilesCount(new RepoPathImpl("repo1", ""));
        assertEquals(count, 1);
    }

    public void countFilesUnderFolder() throws VfsException {
        int count = fileService.getFilesCount(new RepoPathImpl("repo1", "ant"));
        assertEquals(count, 1);
    }

    public void countRepoFilesUnderNonExistentRepo() throws VfsException {
        int count = fileService.getFilesCount(new RepoPathImpl("repoXXX", ""));
        assertEquals(count, 0, "Dummy repo name");
    }

    @Test(dependsOnMethods = "deleteFolderById")
    public void countFolderAndFilesUnderRepo() throws VfsException {
        int count = fileService.getNodesCount(new RepoPathImpl("repo1", ""));
        assertEquals(count, 7);
    }

    public void countFolderAndFilesUnderFolder() throws VfsException {
        int count = fileService.getNodesCount(new RepoPathImpl("repo1", "ant"));
        assertEquals(count, 3);
    }

    public void countRepoFilesAndFoldersUnderNonExistentRepo() throws VfsException {
        int count = fileService.getNodesCount(new RepoPathImpl("repoXXX", ""));
        assertEquals(count, 0, "Dummy repo name");
    }

    public void getSha1BadChecksums() throws Exception {
        List<FileInfo> files = fileService.searchFilesWithBadChecksum(ChecksumType.sha1);
        assertNotNull(files);
        assertEquals(files.size(), 1, "Expected 1 bad SHA-1 checksum");
        FileInfo file = files.get(0);
        assertEquals(file.getName(), "badsha1.jar", "Expected file name to be badsha1.jar");
        assertFalse(file.getChecksumsInfo().getChecksumInfo(ChecksumType.sha1).checksumsMatch(),
                "SHA-1 checksums should not match");
    }

    public void getMd5BadChecksums() throws Exception {
        List<FileInfo> files = fileService.searchFilesWithBadChecksum(ChecksumType.md5);
        assertNotNull(files);
        assertEquals(files.size(), 1, "Expected 1 bad MD5 checksum");
        FileInfo file = files.get(0);
        assertEquals(file.getName(), "badmd5.jar", "Expected file name to be badmd5.jar");
        assertFalse(file.getChecksumsInfo().getChecksumInfo(ChecksumType.md5).checksumsMatch(),
                "MD5 checksums should not match");
    }

    public void loadFolderUnderRoot() throws Exception {
        RepoPathImpl repoPath = new RepoPathImpl("repo1", "ant");
        FolderInfo folderInfo = (FolderInfo) fileService.loadItem(repoPath);
        assertNotNull(folderInfo);
        assertEquals(folderInfo.getRepoPath(), repoPath);
        assertEquals(folderInfo.getCreated(), 1340283204448L);
        assertEquals(folderInfo.getCreatedBy(), "yossis-1");
        assertEquals(folderInfo.getLastModified(), 1340283205448L);
        assertEquals(folderInfo.getModifiedBy(), "yossis-2");
        assertEquals(folderInfo.getLastUpdated(), 1340283205448L);
    }

    public void loadFolder() throws Exception {
        RepoPathImpl repoPath = new RepoPathImpl("repo1", "ant/ant");
        FolderInfo folderInfo = (FolderInfo) fileService.loadItem(repoPath);
        assertNotNull(folderInfo);
        assertEquals(folderInfo.getRepoPath(), repoPath);
        assertEquals(folderInfo.getCreated(), 1340283204450L);
        assertEquals(folderInfo.getCreatedBy(), "yossis-1");
        assertEquals(folderInfo.getLastModified(), 1340283204450L);
        assertEquals(folderInfo.getModifiedBy(), "yossis-3");
        assertEquals(folderInfo.getLastUpdated(), 1340283214450L);
    }

    public void loadItemById() throws Exception {
        ItemInfo itemInfo = fileService.loadItem(4);
        assertNotNull(itemInfo);
        RepoPathImpl repoPath = new RepoPathImpl("repo1", "ant/ant/1.5");
        assertEquals(itemInfo.getRepoPath(), repoPath);
    }

    @Test(expectedExceptions = VfsItemNotFoundException.class)
    public void loadNonExistentById() throws Exception {
        fileService.loadItem(47483);
    }

    /*
    @Test(expectedExceptions = FolderExpectedException.class)
    public void loadFileWithFolderPath() throws Exception {
        RepoPathImpl repoPath = new RepoPathImpl("repo1", "ant/ant/1.5");
        fileService.loadFile(repoPath);
    }
    */

    public void loadChildren() throws Exception {
        List<ItemInfo> children = fileService.loadChildren(new RepoPathImpl("repo1", ""));
        assertNotNull(children);
        assertEquals(children.size(), 2, "Expected 2 direct children but got: " + children);
    }

    @Test(enabled = false, expectedExceptions = VfsItemNotFoundException.class)
    public void loadChildrenPathNotFound() throws Exception {
        fileService.loadChildren(new RepoPathImpl("repoYYY", ""));
    }

    @Test(enabled = false, expectedExceptions = FolderExpectedException.class)
    public void loadChildrenOfFile() throws Exception {
        fileService.loadChildren(new RepoPathImpl("repo1", "ant/ant/1.5/ant-1.5.jar"));
    }

    @Test(expectedExceptions = VfsItemNotFoundException.class)
    public void loadNonExistentFolder() throws Exception {
        fileService.loadItem(new RepoPathImpl("repoXXX", "ant"));
    }

    public void createFolder() throws Exception {
        FolderInfoImpl folderSave = new FolderInfoImpl(new RepoPathImpl("repo1", "new/folder"));
        fileService.createFolder(folderSave);
        RepoPathImpl repoPath = new RepoPathImpl("repo1", "new/folder");
        FolderInfo folderLoaded = (FolderInfo) fileService.loadItem(repoPath);
        assertNotNull(folderLoaded);
        assertEquals(folderSave, folderLoaded);
    }

    @Test(dependsOnMethods = "createFolder")
    public void deleteFolderById() throws Exception {
        RepoPathImpl folderRepoPath = new RepoPathImpl("repo1", "new/folder");
        VfsItem item = fileService.loadVfsItem(null, folderRepoPath);
        boolean deleted = fileService.deleteItem(item.getId());
        assertTrue(deleted);
        assertFalse(fileService.exists(folderRepoPath));
    }

    public void deleteNonExistentItem() throws Exception {
        boolean deleted = fileService.deleteItem(89894);
        assertFalse(deleted);
    }

    public void itemExists() throws VfsException {
        assertTrue(fileService.exists(new RepoPathImpl("repo1", "org")));
    }

    public void itemNotExists() throws VfsException {
        assertFalse(fileService.exists(new RepoPathImpl("repo1", "nosuchfile")));
    }

    public void itemExistsRepoRoot() throws VfsException {
        assertTrue(fileService.exists(new RepoPathImpl("repo1", "")));
    }

    public void nodeIdRoot() {
        assertEquals(fileService.getNodeId(new RepoPathImpl("repo2", "")), 500);
    }

    public void nodeIdNoSuchNode() {
        assertEquals(fileService.getNodeId(new RepoPathImpl("repo2", "no/folder")), DbService.NO_DB_ID);
    }

    public void nodeSha1OfFile() {
        assertEquals(fileService.getNodeSha1(new RepoPathImpl("repo2", "org/jfrog/test/test.jar")),
                "dcab88fc2a043c2479a6de676a2f8179e9ea2167");
    }

    public void nodeSha1NotExist() {
        assertNull(fileService.getNodeSha1(new RepoPathImpl("repo2", "no/folder")));
    }

    public void nodeSha1OfFolder() {
        assertTrue(fileService.exists(new RepoPathImpl("repo2", "org")));
        assertNull(fileService.getNodeSha1(new RepoPathImpl("repo2", "org")));
    }
}
