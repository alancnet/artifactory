package org.artifactory.storage.binstore.service;

import com.google.common.collect.Lists;
import org.artifactory.storage.StorageProperties;
import org.artifactory.storage.config.model.ChainMetaData;
import org.artifactory.storage.config.model.Param;
import org.artifactory.storage.config.model.ProviderMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * @author Gidi Shabat
 */
public class BinaryProviderFactory {
    private static final Logger log = LoggerFactory.getLogger(BinaryProviderFactory.class);

    public static <T extends BinaryProviderBase> T createByType(String type, String id,
            StorageProperties properties, InternalBinaryStore binaryStore) {
        ProviderMetaData providerMetaData = new ProviderMetaData(id, type);
        T t = create(providerMetaData, properties, binaryStore);
        t.initialize();
        return t;

    }

    public static <T extends BinaryProviderBase> T create(ProviderMetaData providerMetaData,
            StorageProperties properties, InternalBinaryStore binaryStore) {
        try {
            //Map<String, Class> providersMap = loadProvidersMap();
            Map<String, Class> binaryProvidersMap = binaryStore.getBinaryProvidersMap();
            Class binaryProviderClass = binaryProvidersMap.get(providerMetaData.getType());
            BinaryProviderBase instance = (BinaryProviderBase) binaryProviderClass.newInstance();
            instance.setProviderMetaData(providerMetaData);
            instance.setStorageProperties(properties);
            instance.setBinaryStore(binaryStore);
            instance.setEmpty(new EmptyBinaryProvider());
            return (T) instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to initiate binary provider.", e);
        }

    }

    public static BinaryProviderBase buildProviders(ChainMetaData configChain, InternalBinaryStore binaryStore,
            StorageProperties storageProperties) {
        log.debug("Initializing providers by chain; '{}'", configChain.getTemplate());
        List<BinaryProviderBase> binaryProviders = Lists.newArrayList();
        binaryProviders.add(build(configChain.getProviderMetaData(), binaryStore, storageProperties));
        return binaryProviders.get(0);
    }

    private static BinaryProviderBase build(ProviderMetaData providerMetaData, InternalBinaryStore binaryStore,
            StorageProperties storageProperties) {
        if (providerMetaData == null) {
            return null;
        }
        BinaryProviderBase binaryProvider = create(providerMetaData, storageProperties, binaryStore);
        binaryProvider.setBinaryProvider(
                build(providerMetaData.getProviderMetaData(), binaryStore, storageProperties));
        for (ProviderMetaData subProviderMetaData : providerMetaData.getSubProviderMetaDataList()) {
            binaryProvider.addSubBinaryProvider(
                    build(subProviderMetaData, binaryStore, storageProperties));
        }
        binaryProvider.initialize();
        return binaryProvider;
    }


    public static FileBinaryProvider searchForFileBinaryProvider(BinaryProviderBase binaryProvider) {
        if (binaryProvider == null) {
            return null;
        }
        BinaryProviderBase next = binaryProvider.getBinaryProvider();
        if (next instanceof FileBinaryProvider) {
            return (FileBinaryProvider) next;
        }
        FileBinaryProvider fileBinaryProvider = searchForFileBinaryProvider(next);
        if (fileBinaryProvider != null) {
            return fileBinaryProvider;
        }
        for (BinaryProviderBase binaryProviderBase : binaryProvider.getSubBinaryProviders()) {
            if (binaryProviderBase instanceof FileBinaryProvider) {
                return (FileBinaryProvider) binaryProviderBase;
            } else {
                fileBinaryProvider = searchForFileBinaryProvider(binaryProviderBase);
                if (fileBinaryProvider != null) {
                    return fileBinaryProvider;
                }
            }
        }
        return null;
    }

    public static List<BinaryProviderBase> createExternalBinaryProviders(String mode, String externalDir,
            FileBinaryProvider fileBinaryProvider, StorageProperties storageProperties,
            InternalBinaryStore binaryStore) {
        List<BinaryProviderBase> result = Lists.newArrayList();
        if (externalDir != null) {
            if (mode != null) {
                String filestoreDir = fileBinaryProvider.getBinariesDir().getAbsolutePath();
                result.add(createExternalWrapperBinaryProvider(mode, filestoreDir, storageProperties, binaryStore));
            }
            result.add(createExternalFileBinaryProvider(externalDir, storageProperties, binaryStore));
        }
        return result;
    }

    public static BinaryProviderBase createExternalWrapperBinaryProvider(String mode, String dir,
            StorageProperties storageProperties, InternalBinaryStore binaryStore) {
        ProviderMetaData providerMetaData = new ProviderMetaData("external-wrapper", "external-wrapper");
        providerMetaData.addParam(new Param("connectMode", mode));
        providerMetaData.addParam(new Param("dir", dir));
        BinaryProviderBase binaryProvider = create(providerMetaData, storageProperties, binaryStore);
        binaryProvider.initialize();
        return binaryProvider;
    }

    public static BinaryProviderBase createExternalFileBinaryProvider(String dir, StorageProperties storageProperties,
            InternalBinaryStore binaryStore) {
        ProviderMetaData providerMetaData = new ProviderMetaData("external-file", "external-file");
        providerMetaData.addParam(new Param("dir", dir));
        BinaryProviderBase binaryProvider = create(providerMetaData, storageProperties, binaryStore);
        binaryProvider.initialize();
        return binaryProvider;
    }
}
