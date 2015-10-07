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

package org.artifactory.converters;

import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.storage.db.properties.model.DbProperties;
import org.artifactory.storage.db.properties.service.ArtifactoryCommonDbPropertiesService;
import org.artifactory.version.ArtifactoryVersion;
import org.artifactory.version.ArtifactoryVersionReader;
import org.artifactory.version.CompoundVersionDetails;
import org.artifactory.version.ConfigVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static java.lang.String.valueOf;
import static org.artifactory.version.ArtifactoryVersion.*;

/**
 * @author Gidi Shabat
 */
public class VersionProviderImpl implements VersionProvider {
    private static final Logger log = LoggerFactory.getLogger(VersionProviderImpl.class);

    /**
     * The current running version, discovered during runtime.
     */
    private CompoundVersionDetails runningVersion;
    /**
     * The initial version from the local home properties file on startup. Effective only until the conversion starts.
     */
    private CompoundVersionDetails originalHomeVersion;

    /**
     * The initial version from the cluster home properties file on startup. Effective only until the conversion starts.
     */
    private CompoundVersionDetails originalHaVersion;

    /**
     * The initial version from the database. Not null only after the call to loadDbVersion() method which
     * occurs first thing after access to the database is allowed.
     */
    private CompoundVersionDetails originalDatabaseVersion;

    private ArtifactoryHome artifactoryHome;


    public VersionProviderImpl(ArtifactoryHome artifactoryHome) {
        this.artifactoryHome = artifactoryHome;
        init();
    }

    private void init() {
        try {
            runningVersion = artifactoryHome.readRunningArtifactoryVersion();
            log.debug("Current running version is : {}", runningVersion.getVersion().name());
            originalHomeVersion = runningVersion;
            originalHaVersion = runningVersion;
            updateOriginalHomeVersion();
            verifyVersion(originalHomeVersion.getVersion(), runningVersion.getVersion());
            log.debug("Last Artifactory home version is: {}", originalHomeVersion.getVersion().name());
            if (artifactoryHome.isHaConfigured()) {
                updateOriginalHaVersion();
                verifyVersion(originalHaVersion.getVersion(), runningVersion.getVersion());
                log.debug("Last Artifactory cluster home version is: {}", originalHaVersion.getVersion().name());
            }
        } catch (Exception e) {
            log.error("Fail to load artifactory.properties", e);
        }
    }

    /**
     * Loads the original version from the database.
     * Calling this method is allowed only after the DBService "PostConstruct", before this stage there is no access
     * to the database and any try to access it wil fail.
     */
    public void loadDbVersion() {
        try {
            ArtifactoryCommonDbPropertiesService dbPropertiesService = getArtifactoryCommonDbPropertiesService();
            boolean dbPropertiesTableExists = dbPropertiesService.isDbPropertiesTableExists();
            if (dbPropertiesTableExists) {
                DbProperties dbProperties = dbPropertiesService.getDbProperties();
                // If the db_properties table exists, but no version found the we can't conclude the original version
                // in such case the vest choice is to assume that the home version is equals to the database version.
                if (dbProperties == null) {
                    if (ConstantValues.test.getBoolean()) {
                        String version = v410.name();
                        String revision = valueOf(v410.getRevision());
                        originalDatabaseVersion = new CompoundVersionDetails(v410, version, revision, 1387059697274l);
                    } else {
                        //TODO [Gidi] this case should not happened check with Yossi
                        // In this case it is ok to assume that the version is 3.1.0 since the DB_PROPERTIES table exists and above is
                        // allowed only from version 3.0.0 and no conversion has been added between version 3.0.0 to 3.1.0
                        // except the addition of DB_PROPERTIES and the ARTIFACTORY_SERVERS tables.
                        String version = v310.name();
                        String revision = valueOf(v310.getRevision());
                        long timestampOfVersion311 = 1387059697274l;
                        originalDatabaseVersion = new CompoundVersionDetails(v310, version, revision,
                                timestampOfVersion311);
                    }
                } else {
                    originalDatabaseVersion = getDbCompoundVersionDetails(dbProperties);
                }
            } else {
                // In this case it is ok to assume that the version is 3.0.4 since upgrade to 3.1.0 and above is
                // allowed only from version 3.0.0 and no conversion has been added between version 3.0.0 to 3.0.4.
                log.info("Failed to find the db_properties table: assuming that the db version is 3.0.4");
                String version = v304.name();
                String revision = valueOf(v304.getRevision());
                long timestampOfVersion304 = 1382872758304l;
                originalDatabaseVersion = new CompoundVersionDetails(v304, version, revision, timestampOfVersion304);
            }
            verifyVersion(originalDatabaseVersion.getVersion(), runningVersion.getVersion());
            log.debug("Last Artifactory database version is: {}", originalDatabaseVersion.getVersion().name());
        } catch (Exception e) {
            log.error("Failed to resolve DbProperties from database", originalDatabaseVersion.getVersion().name());
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private ArtifactoryCommonDbPropertiesService getArtifactoryCommonDbPropertiesService() {
        return ContextHelper.get().beanForType(
                ArtifactoryCommonDbPropertiesService.class);
    }

    @Override
    public CompoundVersionDetails getOriginalHome() {
        return originalHomeVersion;
    }

    @Override
    public CompoundVersionDetails getOriginalHa() {
        return originalHaVersion;
    }

    @Override
    public CompoundVersionDetails getRunning() {
        return runningVersion;
    }

    @Override
    public boolean isOriginalDatabaseVersionReady() {
        return originalDatabaseVersion != null;
    }

    /**
     * The originalServiceVersion value is null until access to db is allowed
     *
     * @return
     */
    @Override
    public CompoundVersionDetails getOriginalDatabaseVersion() {
        if (originalDatabaseVersion == null) {
            throw new RuntimeException(
                    "The original version from the database is not ready, use this method after dbService initialization");
        }
        return originalDatabaseVersion;
    }

    private void updateOriginalHaVersion() throws IOException {
        File artifactoryPropertiesFile = artifactoryHome.getHaArtifactoryPropertiesFile();
        // If the properties file doesn't exists, then create it
        if (!artifactoryPropertiesFile.exists()) {
            artifactoryHome.writeBundledHaArtifactoryProperties();
        }
        // Load the original home version
        originalHaVersion = ArtifactoryVersionReader.read(artifactoryPropertiesFile);
    }

    private void updateOriginalHomeVersion() throws IOException {
        File artifactoryPropertiesFile = artifactoryHome.getHomeArtifactoryPropertiesFile();
        // If the properties file doesn't exists, then create it
        if (!artifactoryPropertiesFile.exists()) {
            artifactoryHome.writeBundledHomeArtifactoryProperties();
        }
        // Load the original home version
        originalHomeVersion = ArtifactoryVersionReader.read(artifactoryPropertiesFile);
    }

    void reloadArtifactorySystemProperties(File artifactoryPropertiesFile) throws IOException {
        Properties properties = new Properties();
        try (FileInputStream inStream = new FileInputStream(artifactoryPropertiesFile)) {
            properties.load(inStream);
        }
        for (Object o : properties.keySet()) {
            artifactoryHome.getArtifactoryProperties().setProperty((String) o, properties.getProperty((String) o));
        }
    }

    public static CompoundVersionDetails getDbCompoundVersionDetails(DbProperties dbProperties) {
        return ArtifactoryVersionReader.getCompoundVersionDetails(
                dbProperties.getArtifactoryVersion(),
                getRevisionStringFromInt(dbProperties.getArtifactoryRevision()),
                "" + dbProperties.getArtifactoryRelease());
    }

    private static String getRevisionStringFromInt(int rev) {
        if (rev <= 0 || rev == Integer.MAX_VALUE) {
            return "" + Integer.MAX_VALUE;
        }
        return "" + rev;
    }

    public void verifyVersion(ArtifactoryVersion original, ArtifactoryVersion running) {
        if (!running.equals(original)) {
            // the version written in the jar and the version read from the data directory/DB are different
            // make sure the version from the data directory/DB is supported by the current deployed artifactory
            ConfigVersion actualConfigVersion = ConfigVersion.findCompatibleVersion(original);
            //No compatible version -> conversion needed, but supported only from v4 onward
            if (!actualConfigVersion.isCurrent()) {
                String msg = "The stored version for (" + original.getValue() + ") " +
                        "is not up-to-date with the currently deployed Artifactory (" +
                        running + ")";
                if (!actualConfigVersion.isAutoUpdateCapable()) {
                    //Cannot convert
                    msg += ": no automatic conversion is possible. Exiting now...";
                    throw new IllegalStateException(msg);
                }
            }
        }
    }
}