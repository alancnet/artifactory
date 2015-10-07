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

import org.apache.commons.io.FileUtils;
import org.artifactory.binstore.BinaryInfo;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.storage.StorageProperties;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.artifactory.util.ResourceUtils;
import org.testng.Assert;

import java.io.File;
import java.io.IOException;

/**
 * Date: 12/17/12
 * Time: 9:22 AM
 *
 * @author freds
 */
public abstract class BinaryStoreExternalProviderBaseTest extends BinaryStoreImplBaseTest {

    protected File testExternal;

    @Override
    protected ArtifactoryHomeBoundTest createArtifactoryHomeTest() throws IOException {
        ArtifactoryHomeBoundTest artifactoryHomeTest = super.createArtifactoryHomeTest();
        testExternal = new File(ArtifactoryHome.get().getDataDir(), "testExternal");
        Object[][] binFileData = getBinFileData();
        for (Object[] binData : binFileData) {
            String resName = (String) binData[0];
            String sha1 = (String) binData[1];
            File destFile = new File(testExternal, sha1.substring(0, 2) + "/" + sha1);
            File parentFile = destFile.getParentFile();
            if (!parentFile.exists()) {
                Assert.assertTrue(parentFile.mkdirs(), "Error creating " + parentFile.getAbsolutePath());
            }
            FileUtils.copyInputStreamToFile(ResourceUtils.getResource("/binstore/" + resName), destFile);
        }
        return artifactoryHomeTest;
    }

    @Override
    protected void updateStorageProperties() {
        super.updateStorageProperties();
        // Set property to activate external file provider
        updateStorageProperty(StorageProperties.Key.binaryProviderExternalDir, testExternal.getAbsolutePath());
        updateStorageProperty(StorageProperties.Key.binaryProviderExternalMode, getExternalConnectMode());
    }

    protected abstract String getExternalConnectMode();

    @Override
    protected BinaryInfo addBinary(String resName, String sha1, String md5, long length) throws IOException {
        return binaryStore.addBinaryRecord(sha1, md5, length);
    }

    @Override
    protected StorageProperties.BinaryProviderType getBinaryStoreType() {
        return StorageProperties.BinaryProviderType.filesystem;
    }

    @Override
    protected String getBinaryStoreDirName() {
        return "filestore_test";
    }
}
