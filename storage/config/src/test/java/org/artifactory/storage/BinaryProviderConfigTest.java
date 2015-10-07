package org.artifactory.storage;

import org.artifactory.storage.config.BinaryProviderConfigBuilder;
import org.artifactory.storage.config.model.ChainMetaData;
import org.artifactory.storage.config.model.ProviderMetaData;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.artifactory.storage.config.BinaryProviderConfigBuilder.USER_TEMPLATE;

/**
 * @author Gidi Shabat
 */
@Test
public class BinaryProviderConfigTest {

    @Test
    public void userChainWithExistingProviderTest() throws IOException {
        InputStream userConfig = getInputStream("binarystoreWithExistingProviders.xml");
        InputStream defaultConfig = getInputStream("default-storage-config.xml");
        ChainMetaData chain = BinaryProviderConfigBuilder.buildByUserConfig(defaultConfig, userConfig);
        Assert.assertEquals(chain.getTemplate(), "double", "Expecting user-chain");
        ProviderMetaData trackin = chain.getProviderMetaData();
        Assert.assertNotNull(trackin, "Expecting providerMetaData");
        Assert.assertEquals(trackin.getType(), "tracking", "Expecting providerMetaData type to be tracking");
        Assert.assertEquals(trackin.getId(), "tracking", "Expecting providerMetaData id to be tracking");
        ProviderMetaData doubleProvider = trackin.getProviderMetaData();
        Assert.assertEquals(doubleProvider.getType(), "double", "Expecting providerMetaData type to be double");
        Assert.assertEquals(doubleProvider.getId(), "double", "Expecting providerMetaData id to be double");
        ProviderMetaData dynamic1 = doubleProvider.getSubProviderMetaDataList().get(0);
        Assert.assertEquals(dynamic1.getType(), "dynamic", "Expecting providerMetaData type to be eventual");
        Assert.assertEquals(dynamic1.getId(), "dynamic1", "Expecting providerMetaData id to be eventual");
        Assert.assertEquals(dynamic1.getParamValue("dir"), "filestore");
        ProviderMetaData dynamic2 = doubleProvider.getSubProviderMetaDataList().get(1);
        Assert.assertEquals(dynamic2.getType(), "dynamic", "Expecting providerMetaData type to be eventual");
        Assert.assertEquals(dynamic2.getId(), "dynamic2", "Expecting providerMetaData id to be eventual");
        Assert.assertEquals(dynamic2.getParamValue("dir"), "second-filestore");
    }

    @Test
    public void userChainWithOverrideProviderTest() throws IOException {
        InputStream userConfig = getInputStream("binarystoreWithOverideProviders.xml");
        InputStream defaultConfig = getInputStream("default-storage-config.xml");
        ChainMetaData chain = BinaryProviderConfigBuilder.buildByUserConfig(defaultConfig, userConfig);
        Assert.assertEquals(chain.getTemplate(), "double", "Expecting user-chain");
        ProviderMetaData trackin = chain.getProviderMetaData();
        Assert.assertNotNull(trackin, "Expecting providerMetaData");
        Assert.assertEquals(trackin.getType(), "tracking", "Expecting providerMetaData type to be tracking");
        Assert.assertEquals(trackin.getId(), "tracking", "Expecting providerMetaData id to be tracking");
        ProviderMetaData doubleProvider = trackin.getProviderMetaData();
        Assert.assertEquals(doubleProvider.getType(), "double", "Expecting providerMetaData type to be double");
        Assert.assertEquals(doubleProvider.getId(), "double", "Expecting providerMetaData id to be double");
        ProviderMetaData dynamic1 = doubleProvider.getSubProviderMetaDataList().get(0);
        Assert.assertEquals(dynamic1.getType(), "dynamic", "Expecting providerMetaData type to be eventual");
        Assert.assertEquals(dynamic1.getId(), "dynamic1", "Expecting providerMetaData id to be eventual");
        Assert.assertEquals(dynamic1.getParamValue("dir"), "test1");
        ProviderMetaData dynamic2 = doubleProvider.getSubProviderMetaDataList().get(1);
        Assert.assertEquals(dynamic2.getType(), "dynamic", "Expecting providerMetaData type to be eventual");
        Assert.assertEquals(dynamic2.getId(), "dynamic2", "Expecting providerMetaData id to be eventual");
        Assert.assertEquals(dynamic2.getParamValue("dir"), "test2");
    }

    @Test
    public void userChainConfigTest() throws IOException {
        InputStream userConfig = getInputStream("binarystoreWithUserChain.xml");
        InputStream defaultConfig = getInputStream("default-storage-config.xml");
        ChainMetaData chain = BinaryProviderConfigBuilder.buildByUserConfig(defaultConfig, userConfig);
        Assert.assertEquals(chain.getTemplate(), USER_TEMPLATE, "Expecting user-chain");
        ProviderMetaData trackin = chain.getProviderMetaData();
        Assert.assertNotNull(trackin, "Expecting providerMetaData");
        Assert.assertEquals(trackin.getType(), "tracking", "Expecting providerMetaData type to be tracking");
        Assert.assertEquals(trackin.getId(), "tracking", "Expecting providerMetaData id to be tracking");
        ProviderMetaData cachefs = trackin.getProviderMetaData();
        Assert.assertEquals(cachefs.getType(), "cache-fs", "Expecting providerMetaData type to be cache-fs");
        Assert.assertEquals(cachefs.getId(), "cache-fs", "Expecting providerMetaData id to be cache-fs");
        ProviderMetaData eventual = cachefs.getProviderMetaData();
        Assert.assertEquals(eventual.getType(), "eventual", "Expecting providerMetaData type to be eventual");
        Assert.assertEquals(eventual.getId(), "eventual", "Expecting providerMetaData id to be eventual");
        ProviderMetaData retry = eventual.getProviderMetaData();
        Assert.assertEquals(retry.getType(), "retry", "Expecting providerMetaData type to be retry");
        Assert.assertEquals(retry.getId(), "retry", "Expecting providerMetaData id to be retry");
        ProviderMetaData fileSystem = retry.getProviderMetaData();
        Assert.assertEquals(fileSystem.getType(), "file-system", "Expecting providerMetaData type to be fileSystem");
        Assert.assertEquals(fileSystem.getId(), "file-system", "Expecting providerMetaData id to be fileSystem");
        Assert.assertEquals(fileSystem.getParamValue("dir"), "test123", "Expecting override file system path");
    }

    @Test
    public void templateTest() throws IOException {
        InputStream userConfig = getInputStream("binarystoreWithTemplate.xml");
        InputStream defaultConfig = getInputStream("default-storage-config.xml");
        ChainMetaData chain = BinaryProviderConfigBuilder.buildByUserConfig(defaultConfig, userConfig);
        Assert.assertEquals(chain.getTemplate(), "s3", "Expecting s3");
        ProviderMetaData trackin = chain.getProviderMetaData();
        Assert.assertNotNull(trackin, "Expecting providerMetaData");
        Assert.assertEquals(trackin.getType(), "tracking", "Expecting providerMetaData type to be tracking");
        Assert.assertEquals(trackin.getId(), "tracking", "Expecting providerMetaData id to be tracking");
        ProviderMetaData cachefs = trackin.getProviderMetaData();
        Assert.assertEquals(cachefs.getType(), "cache-fs", "Expecting providerMetaData type to be cache-fs");
        Assert.assertEquals(cachefs.getId(), "cache-fs", "Expecting providerMetaData id to be cache-fs");
        ProviderMetaData eventual = cachefs.getProviderMetaData();
        Assert.assertEquals(eventual.getType(), "eventual", "Expecting providerMetaData type to be eventual");
        Assert.assertEquals(eventual.getId(), "eventual", "Expecting providerMetaData id to be eventual");
        ProviderMetaData retry = eventual.getProviderMetaData();
        Assert.assertEquals(retry.getType(), "retry", "Expecting providerMetaData type to be retry");
        Assert.assertEquals(retry.getId(), "retry", "Expecting providerMetaData id to be retry");
        ProviderMetaData s3 = retry.getProviderMetaData();
        Assert.assertEquals(s3.getType(), "s3", "Expecting providerMetaData type to be s3");
        Assert.assertEquals(s3.getId(), "s3", "Expecting providerMetaData id to be s3");
        Assert.assertEquals(s3.getParams().size(), 0);
    }

    @Test
    public void templateWithProviderTest() throws IOException {
        InputStream userConfig = getInputStream("binarystoreWithTemplateAndProvider.xml");
        InputStream defaultConfig = getInputStream("default-storage-config.xml");
        ChainMetaData chain = BinaryProviderConfigBuilder.buildByUserConfig(defaultConfig, userConfig);
        Assert.assertEquals(chain.getTemplate(), "s3", "Expecting s3");
        ProviderMetaData trackin = chain.getProviderMetaData();
        Assert.assertNotNull(trackin, "Expecting providerMetaData");
        Assert.assertEquals(trackin.getType(), "tracking", "Expecting providerMetaData type to be tracking");
        Assert.assertEquals(trackin.getId(), "tracking", "Expecting providerMetaData id to be tracking");
        ProviderMetaData cachefs = trackin.getProviderMetaData();
        Assert.assertEquals(cachefs.getType(), "cache-fs", "Expecting providerMetaData type to be cache-fs");
        Assert.assertEquals(cachefs.getId(), "cache-fs", "Expecting providerMetaData id to be cache-fs");
        ProviderMetaData eventual = cachefs.getProviderMetaData();
        Assert.assertEquals(eventual.getType(), "eventual", "Expecting providerMetaData type to be eventual");
        Assert.assertEquals(eventual.getId(), "eventual", "Expecting providerMetaData id to be eventual");
        ProviderMetaData retry = eventual.getProviderMetaData();
        Assert.assertEquals(retry.getType(), "retry", "Expecting providerMetaData type to be retry");
        Assert.assertEquals(retry.getId(), "retry", "Expecting providerMetaData id to be retry");
        ProviderMetaData s3 = retry.getProviderMetaData();
        Assert.assertEquals(s3.getType(), "s3", "Expecting providerMetaData type to be s3");
        Assert.assertEquals(s3.getId(), "s3", "Expecting providerMetaData id to be s3");
        Assert.assertEquals(s3.getParams().size(), 2);
    }

    private InputStream getInputStream(String name) throws IOException {
        return Thread.currentThread().getContextClassLoader().getResources(
                name).nextElement().openStream();
    }
}
