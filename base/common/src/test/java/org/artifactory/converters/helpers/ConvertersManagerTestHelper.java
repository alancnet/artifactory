package org.artifactory.converters.helpers;

import org.apache.commons.io.FileUtils;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ha.HaNodeProperties;
import org.artifactory.converters.ConvertersManagerImpl;
import org.artifactory.converters.VersionProviderImpl;
import org.artifactory.util.ResourceUtils;
import org.artifactory.version.ArtifactoryVersion;
import org.joda.time.DateTimeUtils;
import org.testng.Assert;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import static org.artifactory.common.ArtifactoryHome.*;
import static org.artifactory.common.ConstantValues.*;

/**
 * Helper static methods for the ConverterManagerTest.
 *
 * @author Gidi Shabat
 */
public class ConvertersManagerTestHelper {
    public static final String home = "target/converterManagerTest/";
    public static final String localHomeTestFile = home + ".history/localHome.test";
    public static final String clusterHomeTestFile = home + ".history/localCluster.test";
    public static final String dbHomeTestFile = home + ".history/db.test";

    /**
     * Creates Complete environment files and database simulator
     */
    public static ArtifactoryContext createEnvironment(ArtifactoryVersion homeVersion, ArtifactoryVersion dbVersion,
            ArtifactoryVersion clusterVersion)
            throws IOException {
        createHomeEnvironment(homeVersion, clusterVersion);
        ArtifactoryHome artifactoryHome = new MockArtifactoryHome();
        VersionProviderImpl versionProvider = new VersionProviderImpl(artifactoryHome);
        ConvertersManagerImpl convertersManager = new ConvertersManagerImpl(artifactoryHome, versionProvider);
        convertersManager.getLocalHomeConverters().add(new MockHomeConverter(localHomeTestFile));
        convertersManager.getClusterHomeConverters().add(new MockHomeConverter(clusterHomeTestFile));
        convertersManager.convertHomes();
        MockArtifactoryContext artifactoryContext = new MockArtifactoryContext(dbVersion, 1, convertersManager,
                versionProvider, true);
        ArtifactoryContextThreadBinder.bind(artifactoryContext);
        ArtifactoryHome.bind(artifactoryHome);
        ((VersionProviderImpl) artifactoryContext.getVersionProvider()).loadDbVersion();
        MockHomeConverter artifactoryConverter = new MockHomeConverter(dbHomeTestFile);
        convertersManager.beforeInits();
        convertersManager.serviceConvert(artifactoryConverter);
        convertersManager.afterAllInits();
        convertersManager.afterContextReady();
        return artifactoryContext;
    }

    public static boolean isArtifactoryClusterHomePropertiesHasBeenUpdated() throws IOException {
        return isArtifactoryPropertiesHasBeenUpdated(clusterHomeTestFile);
    }

    public static boolean isArtifactoryLocalHomePropertiesHasBeenUpdated() throws IOException {
        return isArtifactoryPropertiesHasBeenUpdated(localHomeTestFile);
    }

    public static boolean isArtifactoryDBPropertiesHasBeenUpdated() throws IOException {
        return isArtifactoryPropertiesHasBeenUpdated(dbHomeTestFile);
    }

    public static boolean isArtifactoryClusterHomePropertiesHasBeenUpdatedToCurrent() throws IOException {
        return isArtifactoryPropertiesHasBeenUpdatedToCurrentVersion(".artifactory-ha/ha-data/artifactory.properties");
    }

    public static boolean isArtifactoryLocalHomePropertiesHasBeenUpdatedToCurrent() throws IOException {
        return isArtifactoryPropertiesHasBeenUpdatedToCurrentVersion(".artifactory/data/artifactory.properties");
    }

    private static boolean isArtifactoryPropertiesHasBeenUpdated(String path) throws IOException {
        File file = new File(path);
        return file.exists();
    }

    private static boolean isArtifactoryPropertiesHasBeenUpdatedToCurrentVersion(String path) throws IOException {
        Properties properties = new Properties();
        try (FileInputStream inStream = new FileInputStream(home + path)) {
            properties.load(inStream);
            // Assert that the Artifactory.properties file has been updated
            String runningVersion = ContextHelper.get().getVersionProvider().getRunning().getVersion().getValue();
            return properties.get("artifactory.version").equals(runningVersion);
        }
    }

    public static void createHomeEnvironment(ArtifactoryVersion homeVersion, ArtifactoryVersion clusterVersion)
            throws IOException {
        File homeDir = new File(home);
        File artDir = new File(homeDir, ".artifactory");
        File artHa = new File(homeDir, ".artifactory-ha");

        FileUtils.deleteDirectory(artDir);
        FileUtils.deleteDirectory(artHa);
        FileUtils.deleteDirectory(new File(homeDir, ".history"));

        homeDir.mkdir();
        Assert.assertTrue(artDir.mkdir());
        Assert.assertTrue(artHa.mkdir());
        Assert.assertTrue(new File(homeDir, ".history").mkdir());
        Assert.assertTrue(new File(artHa, "ha-data").mkdir());
        Assert.assertTrue(new File(artHa, "ha-etc").mkdir());
        if (homeVersion != null) {
            File dataDir = new File(artDir, "data");
            File etcDir = new File(artDir, "etc");
            Assert.assertTrue(dataDir.mkdir());
            Assert.assertTrue(etcDir.mkdir());
            Properties artifactoryProperties = createArtifactoryProperties(homeVersion);
            String basePath = "/converters/templates/home/" + homeVersion.getValue();
            Properties haNodeProperties = createHaNodeProperties();
            try (FileOutputStream out = new FileOutputStream(new File(dataDir, ARTIFACTORY_PROPERTIES_FILE))) {
                artifactoryProperties.store(out, "");
            }
            try (FileOutputStream out = new FileOutputStream(new File(etcDir, ARTIFACTORY_HA_NODE_PROPERTIES_FILE))) {
                haNodeProperties.store(out, "");
            }
            if (!homeVersion.isCurrent()) {
                String logbackValue = ResourceUtils.getResourceAsString(basePath + "/" + LOGBACK_CONFIG_FILE_NAME);
                String mimetypesValue = ResourceUtils.getResourceAsString(basePath + "/" + MIME_TYPES_FILE_NAME);
                // HOME
                FileUtils.write(new File(etcDir, LOGBACK_CONFIG_FILE_NAME), logbackValue);
                FileUtils.write(new File(etcDir, MIME_TYPES_FILE_NAME), mimetypesValue);
            }
        }
        if (clusterVersion != null) {
            String basePath = "/converters/templates/home/" + clusterVersion.getValue();
            Properties artifactoryProperties = createArtifactoryProperties(clusterVersion);
            String clusterPropertiesValue = "security.token=76b07383dcda344979681e01efa5ac50";
            FileUtils.write(new File(home + ".artifactory-ha/ha-etc/" + CLUSTER_PROPS_FILE), clusterPropertiesValue);
            try (FileOutputStream out = new FileOutputStream(
                    home + ".artifactory-ha/ha-data/" + ARTIFACTORY_PROPERTIES_FILE)) {
                artifactoryProperties.store(out, "");
            }
            if (!clusterVersion.isCurrent()) {
                String logbackValue = ResourceUtils.getResourceAsString(basePath + "/" + LOGBACK_CONFIG_FILE_NAME);
                String mimetypesValue = ResourceUtils.getResourceAsString(basePath + "/" + MIME_TYPES_FILE_NAME);
                // HA
                FileUtils.write(new File(home + ".artifactory-ha/ha-etc/" + LOGBACK_CONFIG_FILE_NAME), logbackValue);
                FileUtils.write(new File(home + ".artifactory-ha/ha-etc/" + MIME_TYPES_FILE_NAME), mimetypesValue);
            }

        }
    }

    private static Properties createHaNodeProperties() {
        Properties properties = new Properties();
        properties.put(HaNodeProperties.PROP_NODE_ID, "pom");
        properties.put(HaNodeProperties.PROP_CLUSTER_HOME,
                System.getProperties().get("user.dir") + "/" + home + ".artifactory-ha");
        properties.put(HaNodeProperties.PROP_CONTEXT_URL, "localhost");
        // TODO: Test block when conf as slave
        properties.put(HaNodeProperties.PROP_PRIMARY, "true");
        return properties;
    }

    private static Properties createArtifactoryProperties(ArtifactoryVersion version) {
        Properties properties = new Properties();
        properties.put(artifactoryVersion.getPropertyName(), version.getValue());
        properties.put(artifactoryRevision.getPropertyName(), "" + version.getRevision());
        properties.put(artifactoryTimestamp.getPropertyName(), "" + DateTimeUtils.currentTimeMillis());
        return properties;
    }
}
