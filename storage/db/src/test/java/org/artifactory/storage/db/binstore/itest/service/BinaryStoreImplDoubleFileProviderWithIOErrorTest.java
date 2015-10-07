package org.artifactory.storage.db.binstore.itest.service;

import org.artifactory.binstore.BinaryInfo;
import org.artifactory.storage.binstore.service.FileBinaryProvider;
import org.artifactory.test.TestUtils;
import org.artifactory.util.ResourceUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static org.testng.Assert.*;

/**
 * Created 11/6/14
 *
 * @author freds
 */
@Test
public class BinaryStoreImplDoubleFileProviderWithIOErrorTest extends BinaryStoreImplDoubleFileProviderTest {
    int blockedProviderIdx = 0;

    @AfterClass
    void setAllWritable() {
        FileBinaryProvider[] allProviders = getAllProviders();
        for (FileBinaryProvider provider : allProviders) {
            File binariesDir = provider.getBinariesDir();
            assertTrue(binariesDir.setWritable(true, false));
            File[] files = binariesDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    assertTrue(file.setWritable(true, false));
                }
            }
            // Set back the active flag
            provider.isAccessible();
        }
    }

    @Override
    protected BinaryInfo addBinary(String resName, String sha1, String md5, long length) throws IOException {
        // First make sure all providers activated
        setAllWritable();

        // Block the provider marked, but do not mark it (the write action should do the job)
        FileBinaryProvider[] allProviders = getAllProviders();
        FileBinaryProvider blockedProvider = allProviders[blockedProviderIdx];
        if (length <= 300L) {
            // for 100 and 300 we block the destination only
            assertTrue(blockedProvider.getBinariesDir().setWritable(false, false));
        } else {
            // for the other we block the _pre only
            assertTrue(new File(blockedProvider.getBinariesDir(), "_pre").setWritable(false, false));
        }

        blockedProviderIdx++;
        if (blockedProviderIdx == allProviders.length) {
            blockedProviderIdx = 0;
        }
        BinaryInfo binaryInfo = binaryStore.addBinary(ResourceUtils.getResource("/binstore/" + resName));

        assertNotNull(binaryInfo);
        // Check the blocked provider marked
        assertFalse(blockedProvider.isAccessible());
        // But the all binary store still on
        FileBinaryProvider fileBinaryProvider = binaryStore.getFileBinaryProvider();
        assertNotNull(fileBinaryProvider);
        assertTrue(fileBinaryProvider.isAccessible());
        return binaryInfo;
    }

    @Override
    protected void checkBinariesDirAfterLoad(Map<String, Object[]> subFolders) {
        // Since each filestore has only 2 binaries the filestore folder count is 3 (2 + _pre) instead of 5
        FileBinaryProvider[] allProviders = getAllProviders();
        for (FileBinaryProvider provider : allProviders) {
            File binariesDir = provider.getBinariesDir();
            File[] files = binariesDir.listFiles();
            assertNotNull(files);
            assertEquals(files.length, 3);
            checkFilesAreValid(subFolders, files);
        }

        // Now reset all filestore to writable and activate DoubleFilestore sync
        setAllWritable();
        TestUtils.invokeMethodNoArgs(binaryStore.getFileBinaryProvider(), "syncFilestores");

        // Now all normal checks are good
        super.checkBinariesDirAfterLoad(subFolders);
    }
}
