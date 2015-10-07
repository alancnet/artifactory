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

package org.artifactory.common;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.artifactory.common.ha.ClusterProperties;
import org.artifactory.common.ha.HaNodeProperties;
import org.artifactory.common.property.ArtifactorySystemProperties;
import org.artifactory.mime.MimeTypes;
import org.artifactory.mime.MimeTypesReader;
import org.artifactory.version.ArtifactoryVersionReader;
import org.artifactory.version.CompoundVersionDetails;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * @author yoavl
 */
public class ArtifactoryHome {
    public static final String SYS_PROP = "artifactory.home";
    public static final String SERVLET_CTX_ATTR = "artifactory.home.obj";
    public static final String STORAGE_PROPS_FILE_NAME = "storage.properties";
    public static final String MISSION_CONTROL_FILE_NAME = "mission.control.properties";
    public static final String ARTIFACTORY_CONVERTER_OBJ = "artifactory.converter.manager.obj";
    public static final String ARTIFACTORY_VERSION_PROVIDER_OBJ = "artifactory.version.provider.obj";
    public static final String ARTIFACTORY_CONFIG_FILE = "artifactory.config.xml";
    public static final String ARTIFACTORY_CONFIG_BOOTSTRAP_FILE = "artifactory.config.bootstrap.xml";
    public static final String ARTIFACTORY_SYSTEM_PROPERTIES_FILE = "artifactory.system.properties";
    public static final String ARTIFACTORY_PROPERTIES_FILE = "artifactory.properties";
    public static final String LOGBACK_CONFIG_FILE_NAME = "logback.xml";
    public static final String MIME_TYPES_FILE_NAME = "mimetypes.xml";
    public static final String ARTIFACTORY_HA_NODE_PROPERTIES_FILE = "ha-node.properties";
    public static final String CLUSTER_PROPS_FILE = "cluster.properties";
    private static final String ENV_VAR = "ARTIFACTORY_HOME";
    private static final String ARTIFACTORY_CONFIG_LATEST_FILE = "artifactory.config.latest.xml";
    private static final String ARTIFACTORY_CONFIG_IMPORT_FILE = "artifactory.config.import.xml";
    private static final InheritableThreadLocal<ArtifactoryHome> current = new InheritableThreadLocal<>();
    private final File homeDir;
    private MimeTypes mimeTypes;
    private ArtifactorySystemProperties artifactorySystemProperties;
    private HaNodeProperties HaNodeProperties;
    private ClusterProperties clusterProperties;
    private File etcDir;
    private File dataDir;
    private File logDir;
    private File backupDir;
    private File tempWorkDir;
    private File tempUploadDir;
    private File pluginsDir;
    private File logoDir;

    private File haEtcDir;
    private File haDataDir;
    private File haBackupDir;

    /**
     * protected constructor for testing usage only.
     */
    protected ArtifactoryHome() {
        homeDir = null;
    }

    public ArtifactoryHome(SimpleLog logger) {
        String homeDirPath = findArtifactoryHome(logger);
        homeDir = new File(homeDirPath);
        create();
    }

    public ArtifactoryHome(File homeDir) {
        if (homeDir == null) {
            throw new IllegalArgumentException("Home dir path cannot be null");
        }
        this.homeDir = homeDir;
        create();
    }

    private static void checkWritableDirectory(File dir) {
        if (!dir.exists() || !dir.isDirectory() || !dir.canWrite()) {
            String message = "Directory '" + dir.getAbsolutePath() + "' is not writable!";
            System.out.println(ArtifactoryHome.class.getName() + " - Warning: " + message);
            throw new IllegalArgumentException(message);
        }
    }

    public static boolean isBound() {
        return current.get() != null;
    }

    public static ArtifactoryHome get() {
        ArtifactoryHome home = current.get();
        if (home == null) {
            throw new IllegalStateException("Artifactory home is not bound to the current thread.");
        }
        return home;
    }

    public static void bind(ArtifactoryHome props) {
        current.set(props);
    }

    public static void unbind() {
        current.remove();
    }

    public File getHomeDir() {
        return homeDir;
    }

    public File getDataDir() {
        return dataDir;
    }

    public File getEtcDir() {
        return etcDir;
    }

    public File getHaAwareEtcDir() {
        return haEtcDir != null ? haEtcDir : etcDir;
    }

    public File getHaAwareDataDir() {
        return haDataDir != null ? haDataDir : dataDir;
    }

    public File getHaAwareBackupDir() {
        return haBackupDir != null ? haBackupDir : backupDir;
    }

    public File getLogDir() {
        return logDir;
    }

    public File getBackupDir() {
        return backupDir;
    }

    public File getTempWorkDir() {
        return tempWorkDir;
    }

    public File getTempUploadDir() {
        return tempUploadDir;
    }

    public File getPluginsDir() {
        return pluginsDir;
    }

    public File getLogoDir() {
        return logoDir;
    }

    public File getOrCreateSubDir(String subDirName) throws IOException {
        return getOrCreateSubDir(getHomeDir(), subDirName);
    }

    /**
     * @return {@code true} if {@link #ARTIFACTORY_HA_NODE_PROPERTIES_FILE} and {@link #CLUSTER_PROPS_FILE} exists
     */
    public boolean isHaConfigured() {
        return HaNodeProperties != null && clusterProperties != null;
    }

    /**
     * @return the {@link HaNodeProperties} object that represents the
     * {@link #ARTIFACTORY_HA_NODE_PROPERTIES_FILE} contents, or null if HA was not configured properly
     */
    @Nullable
    public HaNodeProperties getHaNodeProperties() {
        return HaNodeProperties;
    }

    @Nullable
    public ClusterProperties getClusterProperties() {
        return clusterProperties;
    }

    private File getOrCreateSubDir(File parent, String subDirName) throws IOException {
        File subDir = new File(parent, subDirName);
        FileUtils.forceMkdir(subDir);
        return subDir;
    }

    private void create() {
        try {
            // Create or find all the needed sub folders
            etcDir = getOrCreateSubDir("etc");
            dataDir = getOrCreateSubDir("data");
            logDir = getOrCreateSubDir("logs");
            backupDir = getOrCreateSubDir("backup");

            File tempRootDir = getOrCreateSubDir(dataDir, "tmp");
            tempWorkDir = getOrCreateSubDir(tempRootDir, "work");
            tempUploadDir = getOrCreateSubDir(tempRootDir, "artifactory-uploads");

            //Manage the artifactory.system.properties file under etc dir
            initAndLoadSystemPropertyFile();

            //Check the write access to all directories that need it
            checkWritableDirectory(dataDir);
            checkWritableDirectory(logDir);
            checkWritableDirectory(backupDir);
            checkWritableDirectory(tempRootDir);
            checkWritableDirectory(tempWorkDir);
            checkWritableDirectory(tempUploadDir);

            //If ha props exist, load the storage from cluster_home/ha-etc
            File haPropertiesFile = getArtifactoryHaPropertiesFile();
            if (haPropertiesFile.exists()) {
                //load ha properties
                HaNodeProperties = new HaNodeProperties();
                HaNodeProperties.load(haPropertiesFile);

                File haArtifactoryHome = HaNodeProperties.getClusterHome();
                if (!haArtifactoryHome.exists()) {
                    throw new RuntimeException(
                            "Artifactory HA home does not exist: " + haArtifactoryHome.getAbsolutePath());
                }

                //create directory structure
                haEtcDir = getOrCreateSubDir(haArtifactoryHome, "ha-etc");
                haDataDir = getOrCreateSubDir(haArtifactoryHome, "ha-data");
                haBackupDir = getOrCreateSubDir(haArtifactoryHome, "ha-backup");

                checkWritableDirectory(haEtcDir);
                checkWritableDirectory(haDataDir);
                checkWritableDirectory(haBackupDir);

                //load cluster properties
                File clusterPropertiesFile = getArtifactoryClusterPropertiesFile();
                clusterProperties = new ClusterProperties();
                clusterProperties.load(clusterPropertiesFile);
            }

            pluginsDir = getOrCreateSubDir(getHaAwareEtcDir(), "plugins");
            logoDir = getOrCreateSubDir(getHaAwareEtcDir(), "ui");

            checkWritableDirectory(pluginsDir);

            try {
                //noinspection ConstantConditions
                for (File rootTmpDirChild : tempRootDir.listFiles()) {
                    if (rootTmpDirChild.isDirectory()) {
                        FileUtils.cleanDirectory(rootTmpDirChild);
                    } else {
                        FileUtils.deleteQuietly(rootTmpDirChild);
                    }
                }
            } catch (Exception e) {
                System.out.println(ArtifactoryHome.class.getName() +
                        " - Warning: unable to clean temporary directories. Cause: " + e.getMessage());
            }

        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Could not initialize artifactory home directory due to: " + e.getMessage(), e);
        }
    }

    private String findArtifactoryHome(SimpleLog logger) {
        String home = System.getProperty(SYS_PROP);
        String artHomeSource = "System property";
        if (home == null) {
            //Try the environment var
            home = System.getenv(ENV_VAR);
            artHomeSource = "Environment variable";
            if (home == null) {
                home = new File(System.getProperty("user.home", "."), ".artifactory").getAbsolutePath();
                artHomeSource = "Default (user home)";
            }
        }
        home = home.replace('\\', '/');
        logger.log("Using artifactory.home at '" + home + "' resolved from: " + artHomeSource);
        return home;
    }

    /**
     * Checks the existence of the logback configuration file under the etc directory. If the file doesn't exist this
     * method will extract a default one from the war.
     */
    public File getLogbackConfig() {
        File etcDir = new File(getHomeDir(), "etc");
        File logbackFile = new File(etcDir, LOGBACK_CONFIG_FILE_NAME);
        if (!logbackFile.exists()) {
            try {
                //Copy from default
                URL configUrl = ArtifactoryHome.class.getResource("/META-INF/default/" + LOGBACK_CONFIG_FILE_NAME);
                FileUtils.copyURLToFile(configUrl, logbackFile);
            } catch (IOException e) {
                // we don't have the logger configuration - use System.err
                System.err.printf("Could not create default %s into %s\n", LOGBACK_CONFIG_FILE_NAME, logbackFile);
                e.printStackTrace();
            }
        }
        return logbackFile;
    }

    /**
     * Returns the content of the artifactory.config.import.xml file
     *
     * @return Content of artifactory.config.import.xml if exists, null if not
     */
    public String getImportConfigXml() {
        File importConfigFile = getArtifactoryConfigImportFile();
        if (importConfigFile.exists()) {
            try {
                String configContent = FileUtils.readFileToString(importConfigFile, "utf-8");
                if (StringUtils.isNotBlank(configContent)) {
                    File bootstrapConfigFile = getArtifactoryConfigBootstrapFile();
                    org.artifactory.util.Files.switchFiles(importConfigFile, bootstrapConfigFile);
                    return configContent;
                }
            } catch (IOException e) {
                throw new RuntimeException("Could not read data from '" + importConfigFile.getAbsolutePath() +
                        "' file due to: " + e.getMessage(), e);
            }
        }
        return null;
    }

    public String getBootstrapConfigXml() {
        File oldLocalConfig = getArtifactoryConfigFile();
        File newBootstrapConfig = getArtifactoryConfigBootstrapFile();
        String result;
        if (newBootstrapConfig.exists()) {
            try {
                result = FileUtils.readFileToString(newBootstrapConfig, "utf-8");
            } catch (IOException e) {
                throw new RuntimeException("Could not read data from '" + newBootstrapConfig.getAbsolutePath() +
                        "' file due to: " + e.getMessage(), e);
            }
        } else if (oldLocalConfig.exists()) {
            try {
                result = FileUtils.readFileToString(oldLocalConfig, "utf-8");
            } catch (IOException e) {
                throw new RuntimeException("Could not read data from '" + newBootstrapConfig.getAbsolutePath() +
                        "' file due to: " + e.getMessage(), e);
            }
        } else {
            String resPath = "/META-INF/default/" + ARTIFACTORY_CONFIG_FILE;
            InputStream is = ArtifactoryHome.class.getResourceAsStream(resPath);
            if (is == null) {
                throw new RuntimeException("Could read the default configuration from classpath at " + resPath);
            }
            try {
                result = IOUtils.toString(is, "utf-8");
            } catch (IOException e) {
                throw new RuntimeException("Could not read data from '" + resPath +
                        "' file due to: " + e.getMessage(), e);
            }
        }
        return result;
    }

    public void renameInitialConfigFileIfExists() {
        File initialConfigFile = getArtifactoryConfigFile();
        if (initialConfigFile.isFile()) {
            org.artifactory.util.Files.switchFiles(initialConfigFile,
                    getArtifactoryConfigBootstrapFile());
        }
    }

    public ArtifactorySystemProperties getArtifactoryProperties() {
        return artifactorySystemProperties;
    }

    public MimeTypes getMimeTypes() {
        return mimeTypes;
    }

    public File getHomeArtifactoryPropertiesFile() {
        return new File(dataDir, ARTIFACTORY_PROPERTIES_FILE);
    }

    public File getHaArtifactoryPropertiesFile() {
        return new File(haDataDir, ARTIFACTORY_PROPERTIES_FILE);
    }

    private URL getDefaultArtifactoryPropertiesUrl() {
        return ArtifactoryHome.class.getResource("/META-INF/" + ARTIFACTORY_PROPERTIES_FILE);
    }

    public void writeBundledHomeArtifactoryProperties() {
        File artifactoryPropertiesFile = getHomeArtifactoryPropertiesFile();
        //Copy the artifactory.properties file into the data folder
        try {
            //Copy from default
            FileUtils.copyURLToFile(getDefaultArtifactoryPropertiesUrl(), artifactoryPropertiesFile);
        } catch (IOException e) {
            throw new RuntimeException("Could not copy " + ARTIFACTORY_PROPERTIES_FILE + " to " +
                    artifactoryPropertiesFile.getAbsolutePath(), e);
        }
    }

    public void writeBundledHaArtifactoryProperties() {
        File artifactoryHaPropertiesFile = getHaArtifactoryPropertiesFile();
        //Copy the artifactory.properties file into the data folder
        try {
            //Copy from default
            FileUtils.copyURLToFile(getDefaultArtifactoryPropertiesUrl(), artifactoryHaPropertiesFile);
        } catch (IOException e) {
            throw new RuntimeException("Could not copy " + ARTIFACTORY_PROPERTIES_FILE + " to " +
                    artifactoryHaPropertiesFile.getAbsolutePath(), e);
        }
    }

    public CompoundVersionDetails readRunningArtifactoryVersion() {
        try (InputStream inputStream = ArtifactoryHome.class.getResourceAsStream(
                "/META-INF/" + ARTIFACTORY_PROPERTIES_FILE)) {
            CompoundVersionDetails details = ArtifactoryVersionReader.read(inputStream);
            //Sanity check
            if (!details.isCurrent()) {
                throw new IllegalStateException("Running version is not the current version. " +
                        "Running: " + details + " Current: " + details.getVersion());
            }
            return details;
        } catch (IOException e) {
            throw new RuntimeException(
                    "Unexpected exception occurred: Fail to load artifactory.properties from class resource", e);
        }
    }

    /**
     * Copy the system properties file and set its data as system properties
     */
    public void initAndLoadSystemPropertyFile() {
        // Expose the properties inside artifactory.system.properties
        File systemPropertiesFile = getArtifactorySystemPropertiesFile();
        if (!systemPropertiesFile.exists()) {
            try {
                //Copy from default
                URL url = ArtifactoryHome.class.getResource("/META-INF/default/" + ARTIFACTORY_SYSTEM_PROPERTIES_FILE);
                if (url == null) {
                    throw new RuntimeException("Could not read classpath resource '/META-INF/default/" +
                            ARTIFACTORY_SYSTEM_PROPERTIES_FILE +
                            "'. Make sure Artifactory home is readable by the current user.");
                }
                FileUtils.copyURLToFile(url, systemPropertiesFile);
            } catch (IOException e) {
                throw new RuntimeException("Could not create the default '" + ARTIFACTORY_SYSTEM_PROPERTIES_FILE +
                        "' at '" + systemPropertiesFile.getAbsolutePath() + "'.", e);
            }
        }
        artifactorySystemProperties = new ArtifactorySystemProperties();
        artifactorySystemProperties.loadArtifactorySystemProperties(systemPropertiesFile,
                getHomeArtifactoryPropertiesFile());
    }

    public File getArtifactorySystemPropertiesFile() {
        return new File(getHaAwareEtcDir(), ARTIFACTORY_SYSTEM_PROPERTIES_FILE);
    }

    public File getArtifactoryHaPropertiesFile() {
        return new File(etcDir, ARTIFACTORY_HA_NODE_PROPERTIES_FILE);
    }

    public File getArtifactoryClusterPropertiesFile() {
        return new File(haEtcDir, CLUSTER_PROPS_FILE);
    }

    public void initAndLoadMimeTypes() {
        File mimeTypesFile = getHaAwareMimeTypesFile();
        if (!mimeTypesFile.exists()) {
            // Copy default mime types configuration file
            try {
                URL configUrl = ArtifactoryHome.class.getResource(
                        "/META-INF/default/" + ArtifactoryHome.MIME_TYPES_FILE_NAME);
                FileUtils.copyURLToFile(configUrl, mimeTypesFile);
            } catch (Exception e) {
                throw new IllegalStateException("Couldn't start Artifactory. " +
                        "Failed to copy default mime types file: " + mimeTypesFile.getAbsolutePath(), e);
            }
        }

        try {
            String mimeTypesXml = Files.toString(mimeTypesFile, Charsets.UTF_8);
            mimeTypes = new MimeTypesReader().read(mimeTypesXml);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse mime types file from: " + mimeTypesFile.getAbsolutePath(), e);
        }
    }

    public File getMimeTypesFile() {
        return new File(getEtcDir(), MIME_TYPES_FILE_NAME);
    }

    public File getHaAwareMimeTypesFile() {
        return new File(getHaAwareEtcDir(), MIME_TYPES_FILE_NAME);
    }

    public File getStoragePropertiesFile() {
        return new File(getHaAwareEtcDir(), STORAGE_PROPS_FILE_NAME);
    }

    public File getMissionControlPropertiesFile() {
        return new File(getEtcDir(), MISSION_CONTROL_FILE_NAME);
    }

    public File getArtifactoryConfigFile() {
        return new File(getHaAwareEtcDir(), ARTIFACTORY_CONFIG_FILE);
    }

    public File getArtifactoryConfigLatestFile() {
        return new File(getHaAwareEtcDir(), ARTIFACTORY_CONFIG_LATEST_FILE);
    }

    public File getArtifactoryConfigImportFile() {
        return new File(getHaAwareEtcDir(), ARTIFACTORY_CONFIG_IMPORT_FILE);
    }

    public File getArtifactoryConfigBootstrapFile() {
        return new File(getHaAwareEtcDir(), ARTIFACTORY_CONFIG_BOOTSTRAP_FILE);
    }

    public File getArtifactoryConfigNewBootstrapFile() {
        return new File(getHaAwareEtcDir(), "new_" + ArtifactoryHome.ARTIFACTORY_CONFIG_BOOTSTRAP_FILE);
    }

    /**
     * Missing Closure ;-)
     */
    public interface SimpleLog {
        public void log(String message);
    }
}
