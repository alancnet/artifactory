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

import org.artifactory.binstore.BinaryInfo;
import org.artifactory.storage.StorageProperties;
import org.artifactory.storage.binstore.service.BinaryNotFoundException;
import org.artifactory.storage.binstore.service.BinaryProviderBase;
import org.artifactory.storage.binstore.service.FileBinaryProvider;
import org.artifactory.storage.binstore.service.providers.DoubleFileBinaryProviderImpl;
import org.artifactory.util.ResourceUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.*;

/**
 * Date: 12/10/12
 * Time: 9:54 PM
 *
 * @author freds
 */
@Test
public class BinaryStoreImplDoubleFileProviderTest extends BinaryStoreImplBaseTest {
    String double_filestore_test_2 = "double_filestore_test_2";

    @Override
    protected StorageProperties.BinaryProviderType getBinaryStoreType() {
        return StorageProperties.BinaryProviderType.filesystem;
    }

    @Override
    protected String getBinaryStoreDirName() {
        return "double_filestore_test";
    }

    @Override
    protected void updateStorageProperties() {
        super.updateStorageProperties();
        // Set property to activate external file provider
        updateStorageProperty(StorageProperties.Key.binaryProviderFilesystemSecondDir, double_filestore_test_2);
        updateStorageProperty(StorageProperties.Key.binaryProviderFilesystemSecondCheckPeriod, "1");
    }

    // TODO [by fsi]: For some reason the storage properties stick to the next tests!?!
    @AfterClass
    protected void cleanStorageProps() {
        updateStorageProperty(StorageProperties.Key.binaryProviderFilesystemSecondDir, "");
        updateStorageProperty(StorageProperties.Key.binaryProviderFilesystemSecondCheckPeriod, "");
    }

    @Override
    protected void assertBinaryExistsEmpty(String sha1) {
        try {
            binaryStore.getBinary(sha1);
            fail("Should have send " + BinaryNotFoundException.class + " exception!");
        } catch (BinaryNotFoundException e) {
            // Message should be "Couldn't find content for '" + sha1 + "'"
            String message = e.getMessage();
            assertTrue(message.contains("content for '" + sha1 + "'"), "Wrong exception message " + message);
        }
    }

    @Override
    protected BinaryInfo addBinary(String resName, String sha1, String md5, long length) throws IOException {
        return binaryStore.addBinary(ResourceUtils.getResource("/binstore/" + resName));
    }

    @Override
    protected void testPruneAfterLoad() {
        testPrune(0, 0, 0);
    }

    @Override
    protected void assertPruneAfterOneGc() {
        testPrune(1, 0, 0);
    }

    @Override
    protected void checkBinariesDirAfterLoad(Map<String, Object[]> subFolders) {
        super.checkBinariesDirAfterLoad(subFolders);
        checkFilestoreDirIsFull(subFolders, getSecondFilestore());
    }

    protected File getSecondFilestore() {
        File binariesDir = binaryStore.getBinariesDir();
        assertNotNull(binariesDir);
        return new File(binariesDir.getParentFile(), double_filestore_test_2);
    }

    @Override
    protected void checkSha1OnEmpty(String sha1) throws IOException {
        super.checkSha1OnEmpty(sha1);
        // Should be empty for all providers
        FileBinaryProvider[] providers = getAllProviders();
        for (FileBinaryProvider provider : providers) {
            File file = provider.getFile(sha1);
            assertFalse(file.exists());
            assertBinaryExistsEmpty(sha1);
        }
    }

    protected FileBinaryProvider[] getAllProviders() {
        List<BinaryProviderBase> subBinaryProviders = ((DoubleFileBinaryProviderImpl) binaryStore.
                getFileBinaryProvider()).getSubBinaryProviders();
        FileBinaryProvider[] array = new FileBinaryProvider[2];
        for (int i = 0; i < array.length; i++) {
            array[i] = (FileBinaryProvider) subBinaryProviders.get(i);
        }
        return array;
    }
}
