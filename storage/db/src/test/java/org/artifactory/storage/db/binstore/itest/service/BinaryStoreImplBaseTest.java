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

package org.artifactory.storage.db.binstore.itest.service;

import com.beust.jcommander.internal.Maps;
import com.beust.jcommander.internal.Sets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.storage.StorageUnit;
import org.artifactory.binstore.BinaryInfo;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.storage.StorageProperties;
import org.artifactory.storage.binstore.GarbageCollectorInfo;
import org.artifactory.storage.db.binstore.dao.BinariesDao;
import org.artifactory.storage.db.binstore.entity.BinaryData;
import org.artifactory.storage.db.binstore.service.BinaryStoreImpl;
import org.artifactory.storage.db.fs.dao.NodesDao;
import org.artifactory.storage.db.fs.entity.Node;
import org.artifactory.storage.db.fs.entity.NodeBuilder;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.artifactory.test.TestUtils;
import org.artifactory.util.ResourceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import static org.testng.Assert.*;

/**
 * Date: 12/10/12
 * Time: 9:54 PM
 *
 * @author freds
 */
public abstract class BinaryStoreImplBaseTest extends DbBaseTest {

    @Autowired
    BinaryStoreImpl binaryStore;

    @Autowired
    NodesDao nodesDao;

    @Autowired
    BinariesDao binariesDao;

    @Autowired
    StorageProperties storageProperties;

    @Override
    protected ArtifactoryHomeBoundTest createArtifactoryHomeTest() throws IOException {
        ArtifactoryHomeBoundTest artifactoryHomeTest = super.createArtifactoryHomeTest();
        artifactoryHomeTest.bindArtifactoryHome();
        ArtifactoryHome artifactoryHome = ArtifactoryHome.get();

        File workDir = new File("target", "binstoretest").getAbsoluteFile();
        TestUtils.setField(artifactoryHome, "homeDir", workDir);
        TestUtils.setField(artifactoryHome, "dataDir", artifactoryHome.getOrCreateSubDir("data"));

        File filestoreDir = getFilestoreFolder(artifactoryHome);
        if (filestoreDir.exists()) {
            FileUtils.deleteDirectory(filestoreDir);
            assertFalse(filestoreDir.exists(), "Could not clean filestore " + filestoreDir.getAbsolutePath());
        }
        return artifactoryHomeTest;
    }

    private File getFilestoreFolder(ArtifactoryHome artifactoryHome) {
        String binaryStoreDirName = getBinaryStoreDirName();
        File filestoreDir;
        if (new File(binaryStoreDirName).isAbsolute()) {
            filestoreDir = new File(binaryStoreDirName);
        } else {
            filestoreDir = new File(artifactoryHome.getDataDir(), binaryStoreDirName);
        }
        return filestoreDir;
    }

    @BeforeClass
    public void initBinaryStore() {
        try {
            bindDummyContext();
            updateStorageProperties();
            binaryStore.initialize();
        } finally {
            unbindDummyContext();
        }
    }

    protected void updateStorageProperties() {
        // customize the storage properties before the binary store initialization
        updateStorageProperty(StorageProperties.Key.binaryProviderType, getBinaryStoreType().name());
        String binaryStoreDirName = getBinaryStoreDirName();
        if (binaryStoreDirName.startsWith("cache")) {
            updateStorageProperty(StorageProperties.Key.binaryProviderCacheDir, binaryStoreDirName);
        } else {
            updateStorageProperty(StorageProperties.Key.binaryProviderFilesystemDir, binaryStoreDirName);
        }
    }

    protected void updateStorageProperty(StorageProperties.Key key, String value) {
        Object propsField = ReflectionTestUtils.getField(storageProperties, "props");
        ReflectionTestUtils.invokeMethod(propsField, "setProperty", key.key(), value);
    }

    protected abstract StorageProperties.BinaryProviderType getBinaryStoreType();

    protected abstract String getBinaryStoreDirName();

    @DataProvider(name = "testBinFiles")
    protected Object[][] getBinFileData() {
        // File Name, SHA1, MD5, size
        return new Object[][]{
                {"100c.bin", "8018634e43a47494119601b857356a5a1875f888", "7c9703f5909d78ab0bf18147aee0a5b3", 100L, 71L},
                {"300c.bin", "e5dc83f4c8d6f5f23c00b61ee40dfcbf18c0a7ba", "270a150e83246818c8524cd04514aa67", 300L, 72L},
                {"256w.bin", "b397ec1546ff6ada8c937cc8f8d988be57324f5e", "1a8e102c605bbb502d36848d48989498", 512L, 73L},
                {"2k.bin", "195573fd008c06ea08fb66c2bbe76d4995b3f40a", "76632b91e81884b19b88ff86850b8f2e", 2048L, 74L},
        };
    }

    @Test
    public void testEmpty() throws IOException {
        // Check initialized with folders correctly
        File filestoreDir = getFilestoreFolder(ArtifactoryHome.get());
        assertEquals(binaryStore.getBinariesDir().getAbsolutePath(), filestoreDir.getAbsolutePath());
        File[] files = filestoreDir.listFiles();
        assertNotNull(files);
        assertEquals(files.length, 1);
        File preFolder = files[0];
        assertTrue(preFolder.isDirectory(), "File " + preFolder.getAbsolutePath() + " should be a folder");
        assertEquals(preFolder.getName(), "_pre");

        // Ping all OK => Ping throws exception if something wrong
        binaryStore.ping();

        // Check all is empty
        assertTrue(binaryStore.findAllBinaries().isEmpty());
        assertEquals(binaryStore.getStorageSize(), 0L);

        // Finder should returns null and empty collections
        Set<String> allChecksums = Sets.newHashSet();
        Object[][] binFileData = getBinFileData();
        for (Object[] binFile : binFileData) {
            String sha1 = (String) binFile[1];
            Assert.assertNull(binaryStore.findBinary(sha1));
            allChecksums.add(sha1);
            allChecksums.add((String) binFile[2]);
        }
        assertTrue(binaryStore.findBinaries(allChecksums).isEmpty());

        // Garbage collection should do nothing
        GarbageCollectorInfo collectorInfo = binaryStore.garbageCollect();
        assertEquals(collectorInfo.initialSize, 0L);
        assertEquals(collectorInfo.initialCount, 0);
        assertEquals(collectorInfo.candidatesForDeletion, 0);
        assertEquals(collectorInfo.binariesCleaned, 0);
        assertEquals(collectorInfo.checksumsCleaned, 0);
        assertEquals(collectorInfo.totalSizeCleaned, 0L);

        testPrune(0, 0, 0);

        for (Object[] binData : binFileData) {
            checkSha1OnEmpty((String) binData[1]);
        }
    }

    protected void checkSha1OnEmpty(String sha1) throws IOException {
        assertNull(binaryStore.findBinary(sha1));
        File file = binaryStore.getFileBinaryProvider().getFile(sha1);
        assertFalse(file.exists());
        assertBinaryExistsEmpty(sha1);
    }

    protected abstract void assertBinaryExistsEmpty(String sha1) throws IOException;

    @Test(dependsOnMethods = "testEmpty", dataProvider = "testBinFiles")
    public void testLoadResources(final String resName, final String sha1, final String md5,
            final long length, long nodeId)
            throws IOException, SQLException {
        BinaryInfo binaryInfo = dbService.invokeInTransaction("testLoadResources", new Callable<BinaryInfo>() {
            @Override
            public BinaryInfo call() throws Exception {
                return addBinary(resName, sha1, md5, length);
            }
        });
        assertNotNull(binaryInfo);
        assertEquals(binaryInfo.getSha1(), sha1);
        assertEquals(binaryInfo.getMd5(), md5);
        assertEquals(binaryInfo.getLength(), length);
        // Add a dummy node to enforce usage...
        createDummyNode(resName, sha1, md5, length, nodeId);
    }

    private void createDummyNode(String resName, String sha1, String md5, long length, long nodeId)
            throws SQLException {
        Node node = new NodeBuilder().nodeId(nodeId).repo("repo1").name(resName).file(true)
                .sha1Actual(sha1).md5Actual(md5).length(length).build();
        nodesDao.create(node);
    }

    protected abstract BinaryInfo addBinary(String resName, String sha1, String md5, long length) throws IOException;

    @Test(dependsOnMethods = "testLoadResources")
    public void testLoadedNothingToDelete() throws IOException, SQLException {
        Object[][] binFileData = getBinFileData();
        Map<String, Object[]> subFolders = getSubFoldersMap();

        // Check initialized with folders correctly
        checkBinariesDirAfterLoad(subFolders);

        // Ping all OK => Ping throws exception if something wrong
        binaryStore.ping();

        // Check store size match
        Collection<BinaryInfo> allBinaries = binaryStore.findAllBinaries();
        assertEquals(allBinaries.size(), 4);
        long totSize = 0L;
        for (BinaryInfo allBinary : allBinaries) {
            long expected = assertBinData(allBinary, subFolders);
            totSize += expected;
        }
        assertEquals(binaryStore.getStorageSize(), totSize);

        // Finder should returns null and empty collections
        Set<String> allChecksums1 = Sets.newHashSet();
        Set<String> allChecksums2 = Sets.newHashSet();
        for (Object[] binFile : binFileData) {
            String sha1 = (String) binFile[1];
            BinaryInfo bd = binaryStore.findBinary(sha1);
            Assert.assertNotNull(bd);
            assertBinData(bd, binFile);
            allChecksums1.add(sha1);
            allChecksums2.add((String) binFile[2]);
        }
        Set<BinaryInfo> binaries = binaryStore.findBinaries(allChecksums1);
        assertEquals(binaries.size(), 4);
        for (BinaryInfo binary : binaries) {
            assertBinData(binary, subFolders);
        }
        binaries = binaryStore.findBinaries(allChecksums2);
        assertEquals(binaries.size(), 4);
        for (BinaryInfo binary : binaries) {
            assertBinData(binary, subFolders);
        }

        for (Object[] binData : binFileData) {
            String sha1 = (String) binData[1];
            // Should NOT print warning => TODO: How to test for that?
            long size = (Long) binData[3];
            long nodeId = (long) binData[4];
            nodesDao.delete(nodeId);
            Collection<BinaryData> potentialDeletion = binariesDao.findPotentialDeletion();
            assertEquals(potentialDeletion.size(), 1);
            assertBinData(potentialDeletion.iterator().next(), binData);
            BinaryInfo binaryInfo = binaryStore.findBinary(sha1);
            assertNotNull(binaryInfo);
            assertBinData(binaryInfo, binData);
            createDummyNode("trans-" + binData[0], binaryInfo.getSha1(), binaryInfo.getMd5(), binaryInfo.getLength(),
                    nodeId);
            assertTrue(binariesDao.findPotentialDeletion().isEmpty());

            InputStream bis = binaryStore.getBinary(sha1);
            assertEquals(IOUtils.toByteArray(bis),
                    IOUtils.toByteArray(ResourceUtils.getResource("/binstore/" + binData[0])));
            bis.close();

            assertFileExistsAfterLoad(sha1, size);
        }

        // Garbage collection should do nothing
        GarbageCollectorInfo collectorInfo = binaryStore.garbageCollect();
        assertEquals(collectorInfo.initialSize, totSize);
        assertEquals(collectorInfo.initialCount, 4);
        assertEquals(collectorInfo.candidatesForDeletion, 0);
        assertEquals(collectorInfo.binariesCleaned, 0);
        assertEquals(collectorInfo.checksumsCleaned, 0);
        assertEquals(collectorInfo.totalSizeCleaned, 0L);

        testPruneAfterLoad();
    }

    protected Map<String, Object[]> getSubFoldersMap() {
        Object[][] binFileData = getBinFileData();
        Map<String, Object[]> subFolders = Maps.newHashMap();
        subFolders.put("_pre", new Object[0]);
        for (Object[] binFile : binFileData) {
            String folderName = ((String) binFile[1]).substring(0, 2);
            subFolders.put(folderName, binFile);
        }
        return subFolders;
    }

    protected void assertFileExistsAfterLoad(String sha1, long size) {
        File file = binaryStore.getFileBinaryProvider().getFile(sha1);
        assertTrue(file.exists());
        assertEquals(file.getName(), sha1);
        assertEquals(file.length(), size);
    }

    protected void checkBinariesDirAfterLoad(Map<String, Object[]> subFolders) {
        File filestoreDir = binaryStore.getBinariesDir();
        checkFilestoreDirIsFull(subFolders, filestoreDir);
    }

    protected void checkFilestoreDirIsFull(Map<String, Object[]> subFolders, File filestoreDir) {
        File[] files = filestoreDir.listFiles();
        assertNotNull(files);
        assertEquals(files.length, 5);
        checkFilesAreValid(subFolders, files);
    }

    protected void checkFilesAreValid(Map<String, Object[]> subFolders, File[] files) {
        for (File file : files) {
            assertTrue(file.isDirectory(), "File " + file.getAbsolutePath() + " should be a folder");
            String fileName = file.getName();
            assertTrue(subFolders.containsKey(fileName), "File " + file + " should be part of " + subFolders.keySet());
            File[] list = file.listFiles();
            assertNotNull(list);
            Object[] binData = subFolders.get(fileName);
            if (binData.length == 5) {
                // Real data one file matching dataProvider
                assertEquals(list.length, 1);
                assertEquals(list[0].getName(), (String) binData[1]);
                assertEquals(list[0].length(), ((Long) binData[3]).longValue());
            } else {
                // Make sure pre is empty
                assertEquals(list.length, 0);
            }
        }
    }

    protected abstract void testPruneAfterLoad();

    protected BasicStatusHolder testPrune(int folders, int files, int bytes) {
        BasicStatusHolder statusHolder = new BasicStatusHolder();
        binaryStore.prune(statusHolder);
        String statusMsg = statusHolder.getStatusMsg();
        assertFalse(statusHolder.isError(), "Error during empty pruning: " + statusMsg);
        assertMessageContains(statusMsg, folders, files, bytes);
        return statusHolder;
    }

    protected void assertMessageContains(String statusMsg, int folders, int files, int bytes) {
        String expected = "" + folders + " empty folders";
        assertTrue(statusMsg.contains(expected), "Expected '" + expected + "' got status message '" + statusMsg + "'");
        expected = "" + files + " files";
        assertTrue(statusMsg.contains(expected), "Expected '" + expected + "' got status message '" + statusMsg + "'");
        expected = "size of " + StorageUnit.toReadableString(bytes);
        assertTrue(statusMsg.contains(expected), "Expected '" + expected + "' got status message '" + statusMsg + "'");
    }

    @Test(dependsOnMethods = "testLoadedNothingToDelete", dataProvider = "testBinFiles")
    public void testGarbageOneByOne(String resName, String sha1, String md5, long length, long nodeId)
            throws IOException, SQLException {
        // Read the stream to lock reader
        InputStream bis = binaryStore.getBinary(sha1);
        try {
            nodesDao.delete(nodeId);

            // Verify node ready for deletion
            Collection<BinaryData> potentialDeletion = binariesDao.findPotentialDeletion();
            assertEquals(potentialDeletion.size(), 1);
            assertBinData(potentialDeletion.iterator().next(), new Object[]{resName, sha1, md5, length});

            // No GC since file is being read
            GarbageCollectorInfo collectorInfo = binaryStore.garbageCollect();
            assertEquals(collectorInfo.candidatesForDeletion, 1);
            assertEquals(collectorInfo.binariesCleaned, 0);
            assertEquals(collectorInfo.checksumsCleaned, 0);
            assertEquals(collectorInfo.totalSizeCleaned, 0L);

            bis.close();

            // Now GC works
            collectorInfo = binaryStore.garbageCollect();
            assertEquals(collectorInfo.candidatesForDeletion, 1);
            assertEquals(collectorInfo.binariesCleaned, 1);
            assertEquals(collectorInfo.checksumsCleaned, 1);
            assertEquals(collectorInfo.totalSizeCleaned, length);

            assertPruneAfterOneGc();
        } finally {
            IOUtils.closeQuietly(bis);
        }
    }

    protected abstract void assertPruneAfterOneGc();

    private long assertBinData(BinaryInfo bd, Map<String, Object[]> binDataMap) {
        Object[] binData = binDataMap.get(bd.getSha1().substring(0, 2));
        return assertBinData(bd, binData);
    }

    private long assertBinData(BinaryInfo bd, Object[] binData) {
        assertNotNull(binData);
        assertEquals(bd.getSha1(), (String) binData[1]);
        assertEquals(bd.getMd5(), (String) binData[2]);
        long expected = (Long) binData[3];
        assertEquals(bd.getLength(), expected);
        return expected;
    }

    private long assertBinData(BinaryData bd, Object[] binData) {
        assertNotNull(binData);
        assertEquals(bd.getSha1(), (String) binData[1]);
        assertEquals(bd.getMd5(), (String) binData[2]);
        long expected = (Long) binData[3];
        assertEquals(bd.getLength(), expected);
        return expected;
    }
}
