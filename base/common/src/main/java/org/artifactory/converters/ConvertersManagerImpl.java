package org.artifactory.converters;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.ha.HaCommonAddon;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.property.ArtifactoryConverter;
import org.artifactory.common.property.FatalConversionException;
import org.artifactory.storage.db.properties.model.DbProperties;
import org.artifactory.storage.db.properties.service.ArtifactoryCommonDbPropertiesService;
import org.artifactory.version.CompoundVersionDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.artifactory.addon.ha.message.HaMessageTopic.CONFIG_CHANGE_TOPIC;


/**
 * The class manages the conversions process during Artifactory life cycle.
 * It creates a complete separation between the HOME, CLUSTER and database
 * environments, thoughts Artifactory converts each environment independently,
 * each environment has its own original version and each original version might
 * trigger the relevant conversion.
 * For HA environment, only the primary master node can do a conversion of the
 * cluster home and DB. And once primary is up and running,
 * shutdown events are sent to the slaves.
 *
 * @author Gidi Shabat
 */
public class ConvertersManagerImpl implements ConverterManager {
    private static final Logger log = LoggerFactory.getLogger(ConvertersManagerImpl.class);

    private final ArtifactoryHome artifactoryHome;
    private final VersionProviderImpl vp;
    private List<ArtifactoryConverterAdapter> localHomeConverters = new ArrayList<>();
    private List<ArtifactoryConverterAdapter> clusterHomeConverters = new ArrayList<>();
    private boolean homeConversionRunning = false;
    private boolean databaseConversionRunning = false;

    public ConvertersManagerImpl(ArtifactoryHome artifactoryHome, VersionProviderImpl vp) {
        // Initialize
        this.artifactoryHome = artifactoryHome;
        this.vp = vp;
        // create home converters
        localHomeConverters.add(new LoggingConverter(artifactoryHome.getEtcDir()));
        localHomeConverters.add(new MimeTypeConverter(artifactoryHome.getMimeTypesFile()));
        // create cluster converters
        clusterHomeConverters.add(new MimeTypeConverter(artifactoryHome.getHaAwareMimeTypesFile()));
    }

    @Override
    public void convertHomes() {
        convertHome();
        convertClusterHome();
    }

    @Override
    public void beforeInits() {
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        if (homeConversionRunning) {
            boolean lockdown = addonsManager.lockdown();
            if (lockdown) {
                cleanRevertConverters(localHomeConverters, LocalConverterTaskType.revert);
                cleanRevertConverters(clusterHomeConverters, LocalConverterTaskType.revert);
                throw new FatalConversionException(
                        "Reverting conversions and stopping Artifactory due to missing or invalid license");
            } else {
                cleanRevertConverters(localHomeConverters, LocalConverterTaskType.clean);
                cleanRevertConverters(clusterHomeConverters, LocalConverterTaskType.clean);
            }
        }
    }

    @Override
    public void serviceConvert(ArtifactoryConverter artifactoryConverter) {
        try {
            if (isDatabaseConversionInterested()) {
                assertDatabaseConversionOnPrimaryOnly();
                CompoundVersionDetails running = vp.getRunning();
                CompoundVersionDetails originalService = vp.getOriginalDatabaseVersion();
                log.debug("Starting ReloadableBean conversion for: {}, from {} to {}",
                        artifactoryConverter.getClass().getName(), originalService, running);
                databaseConversionRunning = true;
                artifactoryConverter.convert(originalService, running);
                log.debug("Finished ReloadableBean conversion for: {}", artifactoryConverter.getClass().getName());
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    @Override
    public void afterAllInits() {
        if (isConverting()) {
            try {
                // Save home artifactory.properties
                log.info("Updating local file data/artifactory.properties to running version");
                artifactoryHome.writeBundledHomeArtifactoryProperties();
                vp.reloadArtifactorySystemProperties(artifactoryHome.getHomeArtifactoryPropertiesFile());
                // HA etc cluster home or DB for non HA or primary only
                if (isNonHaOrConfiguredPrimary()) {
                    // Save cluster artifactory.properties
                    if (artifactoryHome.isHaConfigured()) {
                        log.info("Updating cluster file ha-data/artifactory.properties to running version");
                        artifactoryHome.writeBundledHaArtifactoryProperties();
                        vp.reloadArtifactorySystemProperties(artifactoryHome.getHaArtifactoryPropertiesFile());
                    }
                    //Insert the new version to the database only if have to
                    ArtifactoryCommonDbPropertiesService dbPropertiesService = ContextHelper.get().beanForType(
                            ArtifactoryCommonDbPropertiesService.class);
                    if (isDatabaseConversionInterested()) {
                        log.info("Updating database properties to running version");
                        dbPropertiesService.updateDbProperties(createDbPropertiesFromVersion(vp.getRunning()));
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to finish conversion", e);
            }
        }
    }

    @Override
    public void afterContextReady() {
        if (isConverting()) {
            try {
                // Now HA Addon is initialized check for primary with complete server list
                AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
                if (addonsManager != null) {
                    HaCommonAddon haAddon = addonsManager.addonByType(HaCommonAddon.class);
                    if (haAddon != null) {
                        // Send message to slaves shutdown
                        log.info("Sending configuration update message to slaves");
                        haAddon.notify(CONFIG_CHANGE_TOPIC, null);
                    }
                }
            } finally {
                homeConversionRunning = false;
                databaseConversionRunning = false;
            }
        }
    }

    private void convertHome() {
        try {
            if (isLocalHomeInterested()) {
                CompoundVersionDetails originalHome = vp.getOriginalHome();
                CompoundVersionDetails running = vp.getRunning();
                homeConversionRunning = true;
                String message = "Starting home conversion, from {}, to {}";
                log.info(message, originalHome.getVersion(), running.getVersion());
                runConverters(localHomeConverters, originalHome, running);
                log.info("Finished home conversion");
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    private void convertClusterHome() {
        try {
            if (isHaInterested()) {
                CompoundVersionDetails originalHa = vp.getOriginalHa();
                CompoundVersionDetails running = vp.getRunning();
                homeConversionRunning = true;
                String message = "Starting cluster home conversion, from {}, to {}";
                log.info(message, originalHa.getVersion(), running.getVersion());
                runConverters(clusterHomeConverters, originalHa, running);
                log.info("Finished cluster home conversion");
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    private void handleException(Exception e) {
        log.error("Conversion failed. You should analyze the error and retry launching " +
                "Artifactory. Error is: {}", e.getMessage());
        homeConversionRunning = false;
        databaseConversionRunning = false;
        throw new RuntimeException(e.getMessage(), e);
    }

    private DbProperties createDbPropertiesFromVersion(CompoundVersionDetails versionDetails) {
        long installTime = System.currentTimeMillis();
        return new DbProperties(installTime,
                versionDetails.getVersionName(),
                versionDetails.getRevisionInt(),
                versionDetails.getTimestamp()
        );
    }

    @Override
    public boolean isConverting() {
        return homeConversionRunning || databaseConversionRunning;
    }

    private void runConverters(List<ArtifactoryConverterAdapter> converters, CompoundVersionDetails fromVersion,
            CompoundVersionDetails toVersion) {
        for (ArtifactoryConverterAdapter converter : converters) {
            if (converter.isInterested(fromVersion, toVersion)) {
                converter.backup();
                converter.convert(fromVersion, toVersion);
            }
        }
    }

    private void cleanRevertConverters(List<ArtifactoryConverterAdapter> converters,
            LocalConverterTaskType localConverterTaskType) {
        for (ArtifactoryConverterAdapter converter : converters) {
            switch (localConverterTaskType) {
                case clean: {
                    converter.clean();
                    break;
                }
                case revert: {
                    converter.revert();
                    break;
                }
            }
        }
    }

    private void assertDatabaseConversionOnPrimaryOnly() {
        if (artifactoryHome.isHaConfigured() && !isConfiguredPrimary()) {
            throw new RuntimeException("Stopping Artifactory, couldn't start Artifactory upgrade, on slave node!\n" +
                    "Please run Artifactory upgrade on the master first!");
        }
    }

    private boolean isNonHaOrConfiguredPrimary() {
        return (!artifactoryHome.isHaConfigured() || isConfiguredPrimary());
    }

    private boolean isConfiguredPrimary() {
        return artifactoryHome.isHaConfigured() && artifactoryHome.getHaNodeProperties() != null
                && artifactoryHome.getHaNodeProperties().isPrimary();
    }

    private boolean isDatabaseConversionInterested() {
        return vp.getOriginalDatabaseVersion() != null && !vp.getOriginalDatabaseVersion().isCurrent();
    }

    private boolean isLocalHomeInterested() {
        for (ArtifactoryConverterAdapter converter : localHomeConverters) {
            if (converter.isInterested(vp.getOriginalHome(), vp.getRunning())) {
                return true;
            }
        }
        return false;
    }

    private boolean isHaInterested() {
        if (!artifactoryHome.isHaConfigured() || !isConfiguredPrimary()) {
            return false;
        }
        for (ArtifactoryConverterAdapter converter : clusterHomeConverters) {
            if (converter.isInterested(vp.getOriginalHa(), vp.getRunning())) {
                return true;
            }
        }
        return false;
    }

    public List<ArtifactoryConverterAdapter> getLocalHomeConverters() {
        return localHomeConverters;
    }

    public List<ArtifactoryConverterAdapter> getClusterHomeConverters() {
        return clusterHomeConverters;
    }

    private static enum LocalConverterTaskType {
        convert, backup, revert, clean
    }

}
