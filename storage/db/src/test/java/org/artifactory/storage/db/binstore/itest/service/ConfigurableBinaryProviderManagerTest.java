package org.artifactory.storage.db.binstore.itest.service;

import org.artifactory.storage.StorageProperties;
import org.artifactory.storage.binstore.service.BinaryProviderBase;
import org.artifactory.storage.binstore.service.EmptyBinaryProvider;
import org.artifactory.storage.binstore.service.FileBinaryProvider;
import org.artifactory.storage.binstore.service.providers.DoubleFileBinaryProviderImpl;
import org.artifactory.storage.binstore.service.providers.DynamicFileBinaryProviderImpl;
import org.artifactory.storage.binstore.service.providers.FileBinaryProviderImpl;
import org.artifactory.storage.binstore.service.providers.FileCacheBinaryProviderImpl;
import org.artifactory.storage.binstore.service.providers.RetryBinaryProvider;
import org.artifactory.storage.config.model.ChainMetaData;
import org.artifactory.storage.db.binstore.service.BinaryStoreImpl;
import org.artifactory.storage.db.binstore.service.BlobBinaryProviderImpl;
import org.artifactory.storage.db.binstore.service.ConfigurableBinaryProviderManager;
import org.artifactory.storage.db.binstore.service.UsageTrackingBinaryProvider;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static org.artifactory.storage.StorageProperties.BinaryProviderType;
import static org.artifactory.storage.StorageProperties.Key;
import static org.artifactory.storage.binstore.service.BinaryProviderFactory.buildProviders;

/**
 * @author Gidi Shabat
 */
@Test
public class ConfigurableBinaryProviderManagerTest extends DbBaseTest {

    @Autowired
    BinaryStoreImpl binaryStore;
    @Autowired
    StorageProperties storageProperties;

    @Test
    public void binaryProviderWithOverrideProviderTest() throws IOException {
        File file = getFile("config/binarystoreWithOverideProviders.xml");
        ChainMetaData chainMetaData = ConfigurableBinaryProviderManager.buildByConfig(file);
        BinaryProviderBase binaryProvider = buildProviders(chainMetaData, binaryStore, storageProperties);
        Assert.assertTrue(binaryProvider instanceof UsageTrackingBinaryProvider);
        BinaryProviderBase doubleBinaryProvider = binaryProvider.next();
        Assert.assertTrue(doubleBinaryProvider instanceof DoubleFileBinaryProviderImpl);
        BinaryProviderBase empty = doubleBinaryProvider.next();
        Assert.assertTrue(empty instanceof EmptyBinaryProvider);
        BinaryProviderBase dynamic1 = doubleBinaryProvider.getSubBinaryProviders().get(0);
        BinaryProviderBase dynamic2 = doubleBinaryProvider.getSubBinaryProviders().get(1);
        Assert.assertTrue(dynamic1 instanceof DynamicFileBinaryProviderImpl);
        Assert.assertTrue(dynamic2 instanceof DynamicFileBinaryProviderImpl);
        Assert.assertEquals("test1", ((FileBinaryProvider) dynamic1).getBinariesDir().getName());
        Assert.assertEquals("test2", ((FileBinaryProvider) dynamic2).getBinariesDir().getName());
    }

    @Test
    public void binaryProviderWithTemplateTest() throws IOException {
        File file = getFile("config/binarystoreWithTemplate.xml");
        ChainMetaData chainMetaData = ConfigurableBinaryProviderManager.buildByConfig(file);
        BinaryProviderBase binaryProvider = buildProviders(chainMetaData, binaryStore, storageProperties);
        Assert.assertTrue(binaryProvider instanceof UsageTrackingBinaryProvider);
        BinaryProviderBase fileSystem = binaryProvider.next();
        Assert.assertTrue(fileSystem instanceof FileBinaryProviderImpl);
        BinaryProviderBase empty = fileSystem.next();
        Assert.assertTrue(empty instanceof EmptyBinaryProvider);
        Assert.assertEquals("filestore", ((FileBinaryProvider) fileSystem).getBinariesDir().getName());
    }

    @Test
    public void binaryProviderWithExistingProviderTest() throws IOException {
        File file = getFile("config/binarystoreWithExistingProviders.xml");
        ChainMetaData chainMetaData = ConfigurableBinaryProviderManager.buildByConfig(file);
        BinaryProviderBase binaryProvider = buildProviders(chainMetaData, binaryStore, storageProperties);
        Assert.assertTrue(binaryProvider instanceof UsageTrackingBinaryProvider);
        BinaryProviderBase doubleBinaryProvider = binaryProvider.next();
        Assert.assertTrue(doubleBinaryProvider instanceof DoubleFileBinaryProviderImpl);
        BinaryProviderBase empty = doubleBinaryProvider.next();
        Assert.assertTrue(empty instanceof EmptyBinaryProvider);
        BinaryProviderBase dynamic1 = doubleBinaryProvider.getSubBinaryProviders().get(0);
        BinaryProviderBase dynamic2 = doubleBinaryProvider.getSubBinaryProviders().get(1);
        Assert.assertTrue(dynamic1 instanceof DynamicFileBinaryProviderImpl);
        Assert.assertTrue(dynamic2 instanceof DynamicFileBinaryProviderImpl);
        Assert.assertEquals("filestore", ((FileBinaryProvider) dynamic1).getBinariesDir().getName());
        Assert.assertEquals("second-filestore", ((FileBinaryProvider) dynamic2).getBinariesDir().getName());
    }

    @Test
    public void binaryProviderWithTemplateAndProviderTest() throws IOException {
        File file = getFile("config/binarystoreWithTemplateAndProvider.xml");
        ChainMetaData chainMetaData = ConfigurableBinaryProviderManager.buildByConfig(file);
        BinaryProviderBase binaryProvider = buildProviders(chainMetaData, binaryStore, storageProperties);
        Assert.assertTrue(binaryProvider instanceof UsageTrackingBinaryProvider);
        BinaryProviderBase fileSystem = binaryProvider.next();
        Assert.assertTrue(fileSystem instanceof FileBinaryProviderImpl);
        BinaryProviderBase empty = fileSystem.next();
        Assert.assertTrue(empty instanceof EmptyBinaryProvider);
        Assert.assertEquals("hello", ((FileBinaryProvider) fileSystem).getBinariesDir().getName());
    }

    @Test
    public void binaryProviderWithUserChainTest() throws IOException {
        File file = getFile("config/binarystoreWithUserChain.xml");
        ChainMetaData chainMetaData = ConfigurableBinaryProviderManager.buildByConfig(file);
        BinaryProviderBase binaryProvider = buildProviders(chainMetaData, binaryStore, storageProperties);
        Assert.assertTrue(binaryProvider instanceof UsageTrackingBinaryProvider);
        BinaryProviderBase cacheFs = binaryProvider.next();
        Assert.assertTrue(cacheFs instanceof FileCacheBinaryProviderImpl);
        BinaryProviderBase fileSystem1 = cacheFs.next();
        Assert.assertTrue(fileSystem1 instanceof FileBinaryProviderImpl);
        BinaryProviderBase retry = fileSystem1.next();
        Assert.assertTrue(retry instanceof RetryBinaryProvider);
        BinaryProviderBase fileSystem2 = retry.next();
        Assert.assertTrue(fileSystem2 instanceof FileBinaryProviderImpl);
        BinaryProviderBase empty = fileSystem2.next();
        Assert.assertTrue(empty instanceof EmptyBinaryProvider);
        Assert.assertEquals("test1", ((FileBinaryProvider) fileSystem1).getBinariesDir().getName());
        Assert.assertEquals("test2", ((FileBinaryProvider) fileSystem2).getBinariesDir().getName());
    }

    @Test
    public void oldGenerationWithFullDB() throws IOException {
        updateStorageProperty(Key.binaryProviderCacheMaxSize, "1000");
        updateStorageProperty(Key.binaryProviderType, BinaryProviderType.fullDb.name());
        ChainMetaData chainMetaData = ConfigurableBinaryProviderManager.buildByStorageProperties(storageProperties);
        BinaryProviderBase binaryProvider = buildProviders(chainMetaData, binaryStore, storageProperties);
        Assert.assertTrue(binaryProvider instanceof UsageTrackingBinaryProvider);
        BinaryProviderBase cacheFs = binaryProvider.next();
        Assert.assertTrue(cacheFs instanceof FileCacheBinaryProviderImpl);
        BinaryProviderBase blob = cacheFs.next();
        Assert.assertTrue(blob instanceof BlobBinaryProviderImpl);
        BinaryProviderBase empty = blob.next();
        Assert.assertTrue(empty instanceof EmptyBinaryProvider);
        Assert.assertEquals("cache", ((FileBinaryProvider) cacheFs).getBinariesDir().getName());
    }

    @Test
    public void oldGenerationWithFullDBDirect() throws IOException {
        // If the cache size is 0 then no cache binary provider will be created
        updateStorageProperty(Key.binaryProviderCacheMaxSize, "0");
        updateStorageProperty(Key.binaryProviderType, BinaryProviderType.fullDb.name());
        ChainMetaData chainMetaData = ConfigurableBinaryProviderManager.buildByStorageProperties(storageProperties);
        BinaryProviderBase binaryProvider = buildProviders(chainMetaData, binaryStore, storageProperties);
        Assert.assertTrue(binaryProvider instanceof UsageTrackingBinaryProvider);
        BinaryProviderBase blob = binaryProvider.next();
        Assert.assertTrue(blob instanceof BlobBinaryProviderImpl);
        BinaryProviderBase empty = blob.next();
        Assert.assertTrue(empty instanceof EmptyBinaryProvider);
    }

    @Test
    public void oldGenerationWithFileSystemDBDirect() throws IOException {
        // If the cache size is 0 then no cache binary provider will be created
        updateStorageProperty(Key.binaryProviderCacheMaxSize, "0");
        updateStorageProperty(Key.binaryProviderType, BinaryProviderType.filesystem.name());
        ChainMetaData chainMetaData = ConfigurableBinaryProviderManager.buildByStorageProperties(storageProperties);
        BinaryProviderBase binaryProvider = buildProviders(chainMetaData, binaryStore, storageProperties);
        Assert.assertTrue(binaryProvider instanceof UsageTrackingBinaryProvider);
        BinaryProviderBase fileSystem = binaryProvider.next();
        Assert.assertTrue(fileSystem instanceof FileBinaryProviderImpl);
        BinaryProviderBase empty = fileSystem.next();
        Assert.assertTrue(empty instanceof EmptyBinaryProvider);
    }

    @Test
    public void oldGenerationWithCacheAndFile() throws IOException {
        // If the cache size is 0 then no cache binary provider will be created
        updateStorageProperty(Key.binaryProviderType, BinaryProviderType.cachedFS.name());
        ChainMetaData chainMetaData = ConfigurableBinaryProviderManager.buildByStorageProperties(storageProperties);
        BinaryProviderBase binaryProvider = buildProviders(chainMetaData, binaryStore, storageProperties);
        Assert.assertTrue(binaryProvider instanceof UsageTrackingBinaryProvider);
        BinaryProviderBase cacheFS = binaryProvider.next();
        Assert.assertTrue(cacheFS instanceof FileCacheBinaryProviderImpl);
        BinaryProviderBase fileSystem = cacheFS.next();
        Assert.assertTrue(fileSystem instanceof FileBinaryProviderImpl);
        BinaryProviderBase empty = fileSystem.next();
        Assert.assertTrue(empty instanceof EmptyBinaryProvider);
    }

    private File getFile(String name) throws IOException {
        return new File(Thread.currentThread().getContextClassLoader().getResources(
                name).nextElement().getFile());
    }

    protected void updateStorageProperty(Key key, String value) {
        Object propsField = ReflectionTestUtils.getField(storageProperties, "props");
        ReflectionTestUtils.invokeMethod(propsField, "setProperty", key.key(), value);
    }
}
