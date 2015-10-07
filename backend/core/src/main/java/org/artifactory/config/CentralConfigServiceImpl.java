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

package org.artifactory.config;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.HaAddon;
import org.artifactory.addon.ha.HaCommonAddon;
import org.artifactory.api.config.VersionInfo;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.security.AuthorizationException;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.MutableStatusHolder;
import org.artifactory.converters.ConverterManager;
import org.artifactory.converters.VersionProvider;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.reader.CentralConfigReader;
import org.artifactory.descriptor.repo.ProxyDescriptor;
import org.artifactory.jaxb.JaxbHelper;
import org.artifactory.sapi.common.ExportSettings;
import org.artifactory.sapi.common.ImportSettings;
import org.artifactory.security.AccessLogger;
import org.artifactory.spring.InternalArtifactoryContext;
import org.artifactory.spring.InternalContextHelper;
import org.artifactory.spring.Reloadable;
import org.artifactory.state.ArtifactoryServerState;
import org.artifactory.storage.db.DbService;
import org.artifactory.storage.db.servers.model.ArtifactoryServer;
import org.artifactory.storage.db.servers.service.ArtifactoryServersCommonService;
import org.artifactory.storage.fs.service.ConfigsService;
import org.artifactory.util.Files;
import org.artifactory.util.SerializablePair;
import org.artifactory.version.ArtifactoryConfigVersion;
import org.artifactory.version.CompoundVersionDetails;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.artifactory.addon.ha.message.HaMessageTopic.CONFIG_CHANGE_TOPIC;

/**
 * This class wraps the JAXB config descriptor.
 */
@Repository("centralConfig")
@Reloadable(beanClass = InternalCentralConfigService.class,
        initAfter = {DbService.class, ConfigurationChangesInterceptors.class})
public class CentralConfigServiceImpl implements InternalCentralConfigService {
    private static final Logger log = LoggerFactory.getLogger(CentralConfigServiceImpl.class);

    private CentralConfigDescriptor descriptor;
    private DateTimeFormatter dateFormatter;
    private String serverName;

    @Autowired
    private AuthorizationService authService;

    @Autowired
    private ConfigsService configsService;

    @Autowired
    private ConfigurationChangesInterceptors interceptors;

    @Autowired
    private AddonsManager addonsManager;

    @Autowired
    private ArtifactoryServersCommonService serversService;

    public CentralConfigServiceImpl() {
    }

    @Override
    public void init() {
        SerializablePair<CentralConfigDescriptor, Boolean> result = getCurrentConfig();
        CentralConfigDescriptor currentConfig = result.getFirst();
        boolean updateDescriptor = result.getSecond();
        setDescriptor(currentConfig, updateDescriptor);
    }

    private SerializablePair<CentralConfigDescriptor, Boolean> getCurrentConfig() {
        ArtifactoryHome artifactoryHome = ContextHelper.get().getArtifactoryHome();

        //First try to see if there is an import config file to load
        String currentConfigXml = artifactoryHome.getImportConfigXml();

        boolean updateDescriptor = true;

        //If no import config file exists, or is empty, load from storage
        if (StringUtils.isBlank(currentConfigXml)) {
            currentConfigXml = loadConfigFromStorage();
            if (!StringUtils.isBlank(currentConfigXml)) {
                // TODO: Check the version is good, because if converted means update = true
                updateDescriptor = false;
            }
        }
        //Otherwise, load bootstrap config
        if (StringUtils.isBlank(currentConfigXml)) {
            log.info("Loading bootstrap configuration (artifactory home dir is {}).", artifactoryHome.getHomeDir());
            currentConfigXml = artifactoryHome.getBootstrapConfigXml();
        }
        artifactoryHome.renameInitialConfigFileIfExists();
        log.trace("Current config xml is:\n{}", currentConfigXml);
        return new SerializablePair<>(new CentralConfigReader().read(currentConfigXml), updateDescriptor);
    }

    @Nullable
    private String loadConfigFromStorage() {
        //Check in DB
        String dbConfigName = ArtifactoryHome.ARTIFACTORY_CONFIG_FILE;
        if (configsService.hasConfig(dbConfigName)) {
            log.debug("Loading existing configuration from storage.");
            return configsService.getConfig(dbConfigName);
        }
        return null;
    }

    @Override
    public void setDescriptor(CentralConfigDescriptor descriptor) {
        setDescriptor(descriptor, true);
    }

    @Override
    public CentralConfigDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public DateTimeFormatter getDateFormatter() {
        return dateFormatter;
    }

    @Override
    public String getServerName() {
        return serverName;
    }

    @Override
    public String format(long date) {
        return dateFormatter.print(date);
    }

    @Override
    public VersionInfo getVersionInfo() {
        return new VersionInfo(ConstantValues.artifactoryVersion.getString(),
                ConstantValues.artifactoryRevision.getString());
    }

    @Override
    public String getConfigXml() {
        return JaxbHelper.toXml(descriptor);
    }

    @Override
    public void setConfigXml(String xmlConfig, boolean saveConfiguration) {
        CentralConfigDescriptor newDescriptor = new CentralConfigReader().read(xmlConfig);
        reloadConfiguration(newDescriptor, saveConfiguration);
        storeLatestConfigToFile(getConfigXml());
        addonsManager.addonByType(HaAddon.class).notify(CONFIG_CHANGE_TOPIC, null);
    }

    @Override
    public void setLogo(File logo) throws IOException {
        ArtifactoryHome artifactoryHome = ContextHelper.get().getArtifactoryHome();
        final File targetFile = new File(artifactoryHome.getLogoDir(), "logo");
        if (logo == null) {
            FileUtils.deleteQuietly(targetFile);
        } else {
            FileUtils.copyFile(logo, targetFile);
        }
    }

    @Override
    public boolean defaultProxyDefined() {
        List<ProxyDescriptor> proxyDescriptors = descriptor.getProxies();
        for (ProxyDescriptor proxyDescriptor : proxyDescriptors) {
            if (proxyDescriptor.isDefaultProxy()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public MutableCentralConfigDescriptor getMutableDescriptor() {
        return (MutableCentralConfigDescriptor) SerializationUtils.clone(descriptor);
    }

    @Override
    public void saveEditedDescriptorAndReload(CentralConfigDescriptor descriptor) {
        if (descriptor == null) {
            throw new IllegalStateException("Currently edited descriptor is null.");
        }

        if (!authService.isAdmin()) {
            throw new AuthorizationException("Only an admin user can save the artifactory configuration.");
        }

        // before doing anything do a sanity check that the edited descriptor is valid
        // will fail if not valid without affecting the current configuration
        // in any case we will use this newly loaded config as the descriptor
        String configXml = JaxbHelper.toXml(descriptor);
        setConfigXml(configXml, true);
    }

    @Override
    public boolean reloadConfiguration(boolean saveConfiguration) {
        String currentConfigXml = loadConfigFromStorage();
        if (!StringUtils.isBlank(currentConfigXml)) {
            CentralConfigDescriptor newDescriptor = new CentralConfigReader().read(currentConfigXml);
            reloadConfiguration(newDescriptor, saveConfiguration);
            return true;
        } else {
            log.warn("Could not reload configuration.");
            return false;
        }
    }

    @Override
    public void importFrom(ImportSettings settings) {
        MutableStatusHolder status = settings.getStatusHolder();
        File dirToImport = settings.getBaseDir();
        //noinspection ConstantConditions
        if (dirToImport != null && dirToImport.isDirectory() && dirToImport.listFiles().length > 0) {
            status.status("Importing config...", log);
            File newConfigFile = new File(settings.getBaseDir(), ArtifactoryHome.ARTIFACTORY_CONFIG_FILE);
            if (newConfigFile.exists()) {
                status.status("Reloading configuration from " + newConfigFile, log);
                String xmlConfig = Files.readFileToString(newConfigFile);
                setConfigXml(xmlConfig, true);
                status.status("Configuration reloaded from " + newConfigFile, log);
            }
        } else if (settings.isFailIfEmpty()) {
            String error = "The given base directory is either empty, or non-existent";
            throw new IllegalArgumentException(error);
        }
    }

    @Override
    public void exportTo(ExportSettings settings) {
        MutableStatusHolder status = settings.getStatusHolder();
        status.status("Exporting config...", log);
        File destFile = new File(settings.getBaseDir(), ArtifactoryHome.ARTIFACTORY_CONFIG_FILE);
        JaxbHelper.writeConfig(descriptor, destFile);
    }

    private void reloadConfiguration(CentralConfigDescriptor newDescriptor, boolean saveConfiguration) {
        //Reload only if all single artifactory or unique schema version in Artifactory HA cluster
        log.info("Reloading configuration...");
        try {
            CentralConfigDescriptor oldDescriptor = getDescriptor();
            if (oldDescriptor == null) {
                throw new IllegalStateException("The system was not loaded, and a reload was called");
            }

            InternalArtifactoryContext ctx = InternalContextHelper.get();

            //setDescriptor() will set the new date formatter and server name
            setDescriptor(newDescriptor, saveConfiguration);

            // TODO: [by FSI] If reload fails, we have the new descriptor in memory but not used
            // Need to find ways to revert or be very robust on reload.
            ctx.reload(oldDescriptor);
            log.info("Configuration reloaded.");
            AccessLogger.configurationChanged();
            log.debug("Old configuration:\n{}", JaxbHelper.toXml(oldDescriptor));
            log.debug("New configuration:\n{}", JaxbHelper.toXml(newDescriptor));
        } catch (Exception e) {
            String msg = "Failed to reload configuration: " + e.getMessage();
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    private void setDescriptor(CentralConfigDescriptor descriptor, boolean save) {
        log.trace("Setting central config descriptor for config #{}.", System.identityHashCode(this));

        if (save) {
            assertSaveDescriptorAllowd();
            // call the interceptors before saving the new descriptor
            interceptors.onBeforeSave(descriptor);
        }

        this.descriptor = descriptor;
        checkUniqueProxies();
        //Create the date formatter
        String dateFormat = descriptor.getDateFormat();
        dateFormatter = DateTimeFormat.forPattern(dateFormat);
        //Get the server name
        serverName = descriptor.getServerName();
        if (serverName == null) {
            log.debug("No custom server name in configuration. Using hostname instead.");
            try {
                serverName = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                log.warn("Could not use local hostname as the server instance id: {}", e.getMessage());
                serverName = "localhost";
            }
        }
        if (save) {
            log.info("Saving new configuration in storage...");
            String configString = JaxbHelper.toXml(descriptor);
            configsService.addOrUpdateConfig(ArtifactoryHome.ARTIFACTORY_CONFIG_FILE, configString);
            log.info("New configuration saved.");
        }
    }

    private void assertSaveDescriptorAllowd() {
        // Approved if not HA
        HaCommonAddon haAddon = addonsManager.addonByType(HaCommonAddon.class);
        if (haAddon.isHaEnabled()) {
            // Approved if primary and converting
            ArtifactoryServer currentMember = serversService.getCurrentMember();
            ArtifactoryServerState serverState =
                    currentMember == null ? ArtifactoryServerState.STARTING : currentMember.getServerState();
            // Get the context
            ArtifactoryContext artifactoryContext = ContextHelper.get();
            // Get the converter manager
            ConverterManager converterManager = artifactoryContext.getConverterManager();

            if (haAddon.isPrimary() && converterManager != null && converterManager.isConverting()) {
                return;
            }
            // Denied if found two nodes with different versions
            List<ArtifactoryServer> otherRunningHaMembers = serversService.getOtherRunningHaMembers();
            VersionProvider versionProvider = artifactoryContext.getVersionProvider();
            CompoundVersionDetails runningVersion = versionProvider.getRunning();
            for (ArtifactoryServer otherRunningHaMember : otherRunningHaMembers) {
                String otherArtifactoryVersion = otherRunningHaMember.getArtifactoryVersion();
                if (!runningVersion.getVersionName().equals(otherArtifactoryVersion)) {
                    throw new RuntimeException(
                            "unstable environment: Found one or more servers with different version Config Reload denied.");
                }
            }
        }
    }

    private void storeLatestConfigToFile(String configXml) {
        try {
            Files.writeContentToRollingFile(configXml, ArtifactoryHome.get().getArtifactoryConfigLatestFile());
        } catch (IOException e) {
            log.error("Error occurred while performing a backup of the latest configuration.", e);
        }
    }

    @Override
    public void reload(CentralConfigDescriptor oldDescriptor) {
        // Nothing to do
    }

    @Override
    public void destroy() {
        // Nothing to do
    }

    /**
     * Convert and save the artifactory config descriptor
     */
    @Override
    public void convert(CompoundVersionDetails source, CompoundVersionDetails target) {
        //Initialize the enum registration
        ArtifactoryConfigVersion.values();

        // getCurrentConfig() will always return the latest version (ie will do the conversion)
        CentralConfigDescriptor artifactoryConfig = getCurrentConfig().getFirst();
        // Save result in DB
        setDescriptor(artifactoryConfig);

        String artifactoryConfigXml = JaxbHelper.toXml(artifactoryConfig);

        // Save new bootstrap config file
        File bootstrapConfigFile = ArtifactoryHome.get().getArtifactoryConfigBootstrapFile();
        File parentFile = bootstrapConfigFile.getParentFile();
        if (parentFile.canWrite()) {
            try {
                log.info("Automatically converting the config file, original will be saved in " +
                        parentFile.getAbsolutePath());
                File newConfigFile;
                if (bootstrapConfigFile.exists()) {
                    newConfigFile = ArtifactoryHome.get().getArtifactoryConfigNewBootstrapFile();
                } else {
                    newConfigFile = bootstrapConfigFile;
                }
                FileOutputStream fos = new FileOutputStream(newConfigFile);
                IOUtils.write(artifactoryConfigXml, fos);
                fos.close();
                if (newConfigFile != bootstrapConfigFile) {
                    Files.switchFiles(newConfigFile, bootstrapConfigFile);
                }
            } catch (Exception e) {
                log.warn("The converted config xml is:\n" + artifactoryConfigXml +
                        "\nThe new configuration is saved in DB but it failed to be saved automatically to '" +
                        parentFile.getAbsolutePath() + "' due to :" + e.getMessage() + ".\n", e);
            }
        } else {
            log.warn("The converted config xml is:\n" + artifactoryConfigXml +
                    "\nThe new configuration is saved in DB but it failed to be saved automatically to '" +
                    parentFile.getAbsolutePath() + "' since the folder is not writable.\n");
        }
    }

    private void checkUniqueProxies() {
        List<ProxyDescriptor> proxies = getDescriptor().getProxies();
        Map<String, ProxyDescriptor> map = new HashMap<>(proxies.size());
        for (ProxyDescriptor proxy : proxies) {
            String key = proxy.getKey();
            ProxyDescriptor oldProxy = map.put(key, proxy);
            if (oldProxy != null) {
                throw new RuntimeException("Duplicate proxy key in configuration: " + key + ".");
            }
        }
    }
}