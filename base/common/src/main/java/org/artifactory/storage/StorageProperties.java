/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2013 JFrog Ltd.
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

package org.artifactory.storage;

import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.storage.StorageUnit;
import org.artifactory.security.crypto.CryptoHelper;
import org.artifactory.storage.db.DbType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;

/**
 * A convenient class to parse the storage properties file.
 *
 * @author Yossi Shaul
 */
public class StorageProperties {
    protected static final int DEFAULT_MAX_ACTIVE_CONNECTIONS = 100;
    protected static final int DEFAULT_MAX_IDLE_CONNECTIONS = 10;
    private static final Logger log = LoggerFactory.getLogger(StorageProperties.class);
    private static final String DEFAULT_MAX_CACHE_SIZE = "5GB";

    private LinkedProperties props = null;
    private DbType dbType = null;

    public StorageProperties(File storagePropsFile) throws IOException {
        props = new LinkedProperties();
        try (FileInputStream pis = new FileInputStream(storagePropsFile)) {
            props.load(pis);
        }

        trimValues();
        assertMandatoryProperties();

        // cache commonly used properties
        dbType = DbType.parse(getProperty(Key.type));

        // verify that the database is supported (will throw an exception if not found)
        log.debug("Loaded storage properties for supported database type: {}", getDbType());
    }

    /**
     * update storage properties file;
     *
     * @param updateStoragePropFile
     * @throws IOException
     */
    public void updateStoragePropertiesFile(File updateStoragePropFile) throws IOException {
        if (props != null) {
            OutputStream outputStream = new FileOutputStream(updateStoragePropFile);
            props.store(outputStream, "");
        }
    }

    public DbType getDbType() {
        return dbType;
    }

    public String getConnectionUrl() {
        return getProperty(Key.url);
    }

    /**
     * Update the connection URL property (should only be called for derby when the url contains place holders)
     *
     * @param connectionUrl The new connection URL
     */
    public void setConnectionUrl(String connectionUrl) {
        props.setProperty(Key.url.key, connectionUrl);
    }

    public String getDriverClass() {
        return getProperty(Key.driver);
    }

    public String getUsername() {
        return getProperty(Key.username);
    }

    public String getPassword() {
        String password = getProperty(Key.password);
        password = CryptoHelper.decryptIfNeeded(password);
        return password;
    }

    public void setPassword(String updatedPassword) {
        props.setProperty(Key.password.key, updatedPassword);
    }

    public int getMaxActiveConnections() {
        return getIntProperty(Key.maxActiveConnections.key, DEFAULT_MAX_ACTIVE_CONNECTIONS);
    }

    public int getMaxIdleConnections() {
        return getIntProperty(Key.maxIdleConnections.key, DEFAULT_MAX_IDLE_CONNECTIONS);
    }

    @Nonnull
    public BinaryProviderType getBinariesStorageType() {
        return BinaryProviderType.valueOf(
                getProperty(Key.binaryProviderType, BinaryProviderType.filesystem.name()));
    }

    public String getS3BucketName() {
        return getProperty(Key.binaryProviderS3BucketName, "artifactory");
    }

    public int getMaxRetriesNumber() {
        return getIntProperty(Key.binaryProviderRetryMaxRetriesNumber.key, 5);
    }

    public int getDelayBetweenRetries() {
        return getIntProperty(Key.binaryProviderRetryDelayBetweenRetries.key, 5000);
    }

    public String getS3BucketPath() {
        return getProperty(Key.binaryProviderS3BucketPath, "filestore");
    }

    public String getS3Credential() {
        String credential = getProperty(Key.binaryProviderS3Credential, null);
        return CryptoHelper.decryptIfNeeded(credential);
    }

    public void setS3Credential(String credential) {
        props.setProperty(Key.binaryProviderS3Credential.key(), credential);
    }

    public String getS3ProviderId() {
        return getProperty(Key.binaryProviderS3ProviderId, "s3");
    }

    public Map<String, String> getParams() {
        return getProperties(Key.binaryProviderS3Param.key + ".");
    }

    public String getS3Identity() {
        return getProperty(Key.binaryProviderS3Identity, null);
    }

    public int getEventuallyPersistedTimeOut() {
        return getIntProperty(Key.binaryProviderEventuallyPersistedTimeOut.key, 120000);
    }

    public int getEventuallyPersistedMaxNumberOfThread() {
        return getIntProperty(Key.binaryProviderEventuallyPersistedMaxNumberOfTreads.key, 5);
    }

    public long getEventuallyPersistedDispatcherSleepTime() {
        return getLongProperty(Key.binaryProviderEventuallyPersistedDispatcherSleepTime.key, 5000);
    }

    public long getEventuallyPersistedWaitForHazelcastTime() {
        return getLongProperty(Key.binaryProviderEventuallyPersistedWaitHazelcastTime.key, 60000);
    }


    public int getS3ProxyPort() {
        return getIntProperty(Key.binaryProviderS3ProxyPort.key, -1);
    }

    public String getS3ProxyHost() {
        return getProperty(Key.binaryProviderS3ProxyHost.key, null);
    }

    public long getS3BlobVerificationTimeout() {
        return getIntProperty(Key.binaryProviderS3BlobVerifyTimeout.key, 60000);
    }
    public String getS3ProxyIdentity() {
        return getProperty(Key.binaryProviderS3ProxyIdentity.key, null);
    }

    public String getS3ProxyCredential() {
        String credential = getProperty(Key.binaryProviderS3ProxyCredential.key, null);
        return CryptoHelper.decryptIfNeeded(credential);
    }

    public void setS3ProxyCredential(String credential) {
        props.setProperty(Key.binaryProviderS3ProxyCredential.key(), credential);
    }

    public String getBinaryProviderExternalDir() {
        return getProperty(Key.binaryProviderExternalDir);
    }

    public String getBinaryProviderDir() {
        return getProperty(Key.binaryProviderFilesystemDir);
    }

    public String getBinaryCacheProviderDir() {
        return getProperty(Key.binaryProviderCacheDir, "cache");
    }

    public String getBinaryProviderExternalMode() {
        return getProperty(Key.binaryProviderExternalMode);
    }

    public String getBinaryProviderFilesystemSecondDir() {
        return getProperty(Key.binaryProviderFilesystemSecondDir);
    }

    public long getBinaryProviderCacheMaxSize() {
        return StorageUnit.fromReadableString(getProperty(Key.binaryProviderCacheMaxSize, DEFAULT_MAX_CACHE_SIZE));
    }

    public String getS3Entpoint() {
        return getProperty(Key.binaryProviderS3Endpoint);
    }

    public String getProperty(Key property) {
        return props.getProperty(property.key);
    }

    public String getProperty(Key property, String defaultValue) {
        return props.getProperty(property.key, defaultValue);
    }

    public String getProperty(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }

    public boolean getBooleanProperty(String key, boolean defaultValue) {
        return Boolean.parseBoolean(props.getProperty(key, defaultValue + ""));
    }

    public int getIntProperty(String key, int defaultValue) {
        return Integer.parseInt(props.getProperty(key, defaultValue + ""));
    }

    public long getLongProperty(String key, long defaultValue) {
        return Long.parseLong(props.getProperty(key, defaultValue + ""));
    }

    public Map<String, String> getProperties(String prefix) {
        Map<String, String> result = Maps.newHashMap();
        Iterator<Map.Entry<String, String>> iterator = props.iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> next = iterator.next();
            if (next.getKey().startsWith(prefix)) {
                String reminder = next.getKey().replace(prefix, "");
                if (!StringUtils.isBlank(reminder)) {
                    result.put(reminder, next.getValue());
                }
            }
        }
        return result;
    }

    private void trimValues() {
        Iterator<Map.Entry<String, String>> iter = props.iterator();
        while (iter.hasNext()) {
            Map.Entry<String, String> entry = iter.next();
            String value = entry.getValue();
            if (!StringUtils.trimToEmpty(value).equals(value)) {
                entry.setValue(StringUtils.trim(value));
            }
        }
    }

    private void assertMandatoryProperties() {
        Key[] mandatory = {Key.type, Key.url, Key.driver};
        for (Key mandatoryProperty : mandatory) {
            String value = getProperty(mandatoryProperty);
            if (StringUtils.isBlank(value)) {
                throw new IllegalStateException("Mandatory storage property '" + mandatoryProperty + "' doesn't exist");
            }
        }
    }

    public boolean isDerby() {
        return dbType == DbType.DERBY;
    }

    public boolean isPostgres() {
        return dbType == DbType.POSTGRESQL;
    }


    public enum Key {
        username, password, type, url, driver,
        maxActiveConnections("pool.max.active"), maxIdleConnections("pool.max.idle"),
        binaryProviderType("binary.provider.type"),  // see BinaryProviderType
        binaryProviderCacheMaxSize("binary.provider.cache.maxSize"),
        binaryProviderCacheDir("binary.provider.cache.dir"),
        binaryProviderFilesystemDir("binary.provider.filesystem.dir"),
        binaryProviderFilesystemSecondDir("binary.provider.filesystem2.dir"),
        binaryProviderFilesystemSecondCheckPeriod("binary.provider.filesystem2.checkPeriod"),
        binaryProviderExternalDir("binary.provider.external.dir"),
        binaryProviderExternalMode("binary.provider.external.mode"),

        // Retry binary provider
        binaryProviderRetryMaxRetriesNumber("binary.provider.retry.max.retries.number"),
        binaryProviderRetryDelayBetweenRetries("binary.provider.retry.delay.between.retries"),

        // S3 binary provider
        binaryProviderS3BucketName("binary.provider.s3.bucket.name"),
        binaryProviderS3BucketPath("binary.provider.s3.bucket.path"),
        binaryProviderS3Identity("binary.provider.s3.identity"),
        binaryProviderS3Credential("binary.provider.s3.credential"),
        binaryProviderS3ProviderId("binary.provider.s3.provider.id"),
        binaryProviderS3ProxyPort("binary.provider.s3.proxy.port"),
        binaryProviderS3ProxyHost("binary.provider.s3.proxy.host"),
        binaryProviderS3BlobVerifyTimeout("binary.provider.s3.blob.verification.timeout"),
        binaryProviderS3ProxyIdentity("binary.provider.s3.proxy.identity"),
        binaryProviderS3ProxyCredential("binary.provider.s3.proxy.credential"),
        binaryProviderS3Endpoint("binary.provider.s3.endpoint"),

        // Dynamic S3 Param
        binaryProviderS3Param("binary.provider.s3.env"),

        // Eventually persisted binary provider
        binaryProviderEventuallyPersistedMaxNumberOfTreads(
                "binary.provider.eventually.persisted.max.number.of.threads"),
        binaryProviderEventuallyPersistedTimeOut("binary.provider.eventually.persisted.timeout"),
        binaryProviderEventuallyPersistedDispatcherSleepTime(
                "binary.provider.eventually.dispatcher.sleep.time"), // in millis
        binaryProviderEventuallyPersistedWaitHazelcastTime(
                "binary.provider.eventually.persisted.wait.hazelcast.time"); // in millis


        private final String key;

        private Key() {
            this.key = name();
        }

        private Key(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }

    public enum BinaryProviderType {
        filesystem, // binaries are stored in the filesystem
        fullDb,     // binaries are stored as blobs in the db, filesystem is used for caching unless cache size is 0
        cachedFS,   // binaries are stored in the filesystem, but a front cache (faster access) is added
        S3,         // binaries are stored in S3 JClouds API
    }
}
