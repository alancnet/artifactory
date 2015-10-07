package org.artifactory.security.interceptor;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.security.crypto.CryptoHelper;
import org.artifactory.storage.StorageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * @author Chen Keinan
 */
public class StoragePropertiesEncryptInterceptor {
    private static final Logger log = LoggerFactory.getLogger(StoragePropertiesEncryptInterceptor.class);

    /**
     * encrypt or decrypt storage properties file
     *
     * @param encrypt - if true encrypt else decrypt
     * @throws java.io.IOException
     */
    public void encryptOrDecryptStoragePropertiesFile(boolean encrypt) {
        try {
            File propertiesFile = getPropertiesStorageFile();
            StorageProperties storageProperties = ContextHelper.get().beanForType(StorageProperties.class);
            String password = storageProperties.getProperty(StorageProperties.Key.password);
            if (StringUtils.isNotBlank(password)) {
                storageProperties.setPassword(getNewPassword(encrypt, password));
            }
            String s3Credential = storageProperties.getProperty(StorageProperties.Key.binaryProviderS3Credential);
            if (StringUtils.isNotBlank(s3Credential)) {
                storageProperties.setS3Credential(getNewPassword(encrypt, s3Credential));
            }
            String s3ProxyCredential = storageProperties.getProperty(StorageProperties.Key.binaryProviderS3ProxyCredential);
            if (StringUtils.isNotBlank(s3ProxyCredential)) {
                storageProperties.setS3ProxyCredential(getNewPassword(encrypt, s3ProxyCredential));
            }
            storageProperties.updateStoragePropertiesFile(propertiesFile);
        } catch (IOException e) {
            log.error("Error Loading encrypt storage properties File" + e.getMessage(), e, log);
        }
    }

    /**
     * get properties file from context Artifactory home
     * getPropertiesStorageFile@return Storage properties File
     */
    private File getPropertiesStorageFile() {
        ArtifactoryHome artifactoryHome = ContextHelper.get().getArtifactoryHome();
        File storagePropsFile = artifactoryHome.getStoragePropertiesFile();
        return storagePropsFile;
    }

    private String getNewPassword(boolean encrypt, String password) {
        if (StringUtils.isNotBlank(password)) {
            if (encrypt) {
                return CryptoHelper.encryptIfNeeded(password);
            } else {
                return CryptoHelper.decryptIfNeeded(password);
            }
        }
        return null;
    }

}
