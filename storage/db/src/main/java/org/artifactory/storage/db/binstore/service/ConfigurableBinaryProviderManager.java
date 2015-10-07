package org.artifactory.storage.db.binstore.service;

import org.artifactory.common.ArtifactoryHome;
import org.artifactory.storage.StorageProperties;
import org.artifactory.storage.config.BinaryProviderConfigBuilder;
import org.artifactory.storage.config.model.ChainMetaData;
import org.artifactory.storage.config.model.Param;
import org.artifactory.storage.config.model.ProviderMetaData;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.artifactory.storage.config.BinaryProviderConfigBuilder.buildUserTemplate;


/**
 * @author Gidi Shabat
 */
public class ConfigurableBinaryProviderManager {
    private static final Logger log = LoggerFactory.getLogger(ConfigurableBinaryProviderManager.class);
    /**
     * New generation binary providers config.
     * The method uses the new binarystore.xml in the etc dir to load and override the default-storage-config.xml file
     */
    public static ChainMetaData buildByConfig(File userConfigFile) throws IOException {
        log.debug("Using the new generation binary provider config");
        FileInputStream userConfigStream = new FileInputStream(userConfigFile);
        String defaultConfigPath = "/default-storage-config.xml";
        InputStream defaultConfigStream = ArtifactoryHome.class.getResource(defaultConfigPath).openStream();
        return BinaryProviderConfigBuilder.buildByUserConfig(defaultConfigStream, userConfigStream);
    }

    /**
     * This method should be removed after converting the old storage.properties config to the new generation config
     * It is kind of hack to support the old storage.properties.
     */
    public static ChainMetaData buildByStorageProperties(StorageProperties storageProperties) throws IOException {
        log.debug("Using the old generation binary provider config");
        String defaultConfigPath = "/default-storage-config.xml";
        InputStream defaultConfigStream = ArtifactoryHome.class.getResource(defaultConfigPath).openStream();
        StorageProperties.BinaryProviderType binaryProviderName = storageProperties.getBinariesStorageType();
        switch (binaryProviderName) {
            case filesystem:
                if (StringUtils.isBlank(storageProperties.getBinaryProviderFilesystemSecondDir())) {
                    log.debug("Initializing 'file-system' chain");
                    ChainMetaData chain = buildUserTemplate(defaultConfigStream, "file-system");
                    // Set the store dir in the file binary provider
                    String providerDir = storageProperties.getBinaryProviderDir();
                    String filestore = getDataFolder(providerDir, "filestore");
                    ProviderMetaData fileProvider = chain.getProviderMetaData().getProviderMetaData();
                    overrideParam(fileProvider, "dir", filestore);
                    return chain;
                } else {
                    log.debug("Initializing 'double' chain");
                    ChainMetaData chain = buildUserTemplate(defaultConfigStream, "double");
                    ProviderMetaData doubleProviderMetaData = chain.getProviderMetaData().getProviderMetaData();
                    // Set the store dir in the double binary provider
                    String providerDir1 = storageProperties.getBinaryProviderDir();
                    String providerDir2 = storageProperties.getBinaryProviderFilesystemSecondDir();
                    String filestoreDir = providerDir2;
                    if (StringUtils.isBlank(providerDir2)) {
                        filestoreDir = getDataFolder(providerDir1, "filestore");
                    }
                    overrideParam(doubleProviderMetaData, "dir", filestoreDir);
                    // Set the store dir in the first dynamic binary provider
                    overrideParam(doubleProviderMetaData.getSubProviderMetaDataList().get(0), "dir",
                            getDataFolder(providerDir1, "filestore"));
                    // Set the store dir in the second dynamic binary provider
                    overrideParam(doubleProviderMetaData.getSubProviderMetaDataList().get(1), "dir",
                            getDataFolder(providerDir2, "second-filestore"));
                    return chain;
                }
            case cachedFS:
                if (storageProperties.getBinaryProviderCacheMaxSize() > 0) {
                    log.debug("Initializing 'cache-fs' chain");
                    ChainMetaData chain = buildUserTemplate(defaultConfigStream, "cache-fs");
                    // Set the store dir in the file (cache) binary provider
                    String providerDir = storageProperties.getBinaryCacheProviderDir();
                    overrideParam(chain.getProviderMetaData().getProviderMetaData(), "dir", providerDir);
                    return chain;
                } else {
                    throw new IllegalStateException("Binary provider typed cachedFS cannot have a zero cached size!");
                }
            case fullDb:
                if (storageProperties.getBinaryProviderCacheMaxSize() > 0) {
                    log.debug("Initializing 'full-db' chain");
                    ChainMetaData chain = buildUserTemplate(defaultConfigStream, "full-db");
                    // Set the store dir in the file (cache) binary provider
                    String providerDir = storageProperties.getBinaryCacheProviderDir();
                    ProviderMetaData providerMetaData = chain.getProviderMetaData().getProviderMetaData();
                    overrideParam(providerMetaData, "dir", providerDir);
                    return chain;
                } else {
                    log.debug("Initializing 'full-db-direct' chain");
                    return buildUserTemplate(defaultConfigStream, "full-db-direct");

                }
            case S3: {
                log.debug("Initializing 's3' chain");
                String providerDir = storageProperties.getBinaryCacheProviderDir();
                ChainMetaData chain = buildUserTemplate(defaultConfigStream, "s3");
                // Set the store dir in the file (cache) binary provider
                overrideParam(chain.getProviderMetaData().getProviderMetaData(), "dir", providerDir);
                return chain;
            }
            default:
                throw new RuntimeException("Fail to initiate binary provider config. Reason: invalid storage" +
                        " type in storage.properties");
        }
    }

    private static void overrideParam(ProviderMetaData providerMetaData, String key, String value) {
        Param param = providerMetaData.getParam(key);
        if (param != null) {
            param.setValue(value);
        } else {
            providerMetaData.addParam(new Param(key, value));
        }
    }

    private static String getDataFolder(String name, String defaultName) {
        if (StringUtils.isBlank(name)) {
            return defaultName;
        }
        return name;
    }
}
