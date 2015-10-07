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

package org.artifactory.spring;

import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.WebstartAddon;
import org.artifactory.addon.ha.HaCommonAddon;
import org.artifactory.addon.license.LicensesAddon;
import org.artifactory.addon.plugin.PluginsAddon;
import org.artifactory.api.build.BuildService;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.config.ExportSettingsImpl;
import org.artifactory.api.config.ImportSettingsImpl;
import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.security.SecurityService;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.MutableStatusHolder;
import org.artifactory.common.ha.HaNodeProperties;
import org.artifactory.converters.ConverterManager;
import org.artifactory.converters.VersionProvider;
import org.artifactory.converters.VersionProviderImpl;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.logging.LoggingService;
import org.artifactory.repo.service.ExportJob;
import org.artifactory.repo.service.ImportJob;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.sapi.common.BaseSettings;
import org.artifactory.sapi.common.ExportSettings;
import org.artifactory.sapi.common.ImportSettings;
import org.artifactory.schedule.TaskCallback;
import org.artifactory.schedule.TaskService;
import org.artifactory.security.crypto.CryptoHelper;
import org.artifactory.security.interceptor.StoragePropertiesEncryptInterceptor;
import org.artifactory.state.model.ArtifactoryStateManager;
import org.artifactory.storage.binstore.service.BinaryStore;
import org.artifactory.update.utils.BackupUtils;
import org.artifactory.util.ZipUtils;
import org.artifactory.version.ArtifactoryVersion;
import org.artifactory.version.CompoundVersionDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Yoav Landman
 */
public class ArtifactoryApplicationContext extends ClassPathXmlApplicationContext
        implements InternalArtifactoryContext {
    public static final String CURRENT_TIME_EXPORT_DIR_NAME = "current";
    private static final Logger log = LoggerFactory.getLogger(ArtifactoryApplicationContext.class);
    private final ArtifactoryHome artifactoryHome;
    private final ConverterManager converterManager;
    private final String contextId;
    private final SpringConfigPaths springConfigPaths;
    private Set<Class<? extends ReloadableBean>> toInitialize = new HashSet<>();
    private ConcurrentHashMap<Class, Object> beansForType = new ConcurrentHashMap<>();
    private List<ReloadableBean> reloadableBeans;
    private VersionProviderImpl versionProvider;
    private volatile boolean ready;
    private long started;
    private boolean offline;

    public ArtifactoryApplicationContext(
            String contextId, SpringConfigPaths springConfigPaths, ArtifactoryHome artifactoryHome,
            ConverterManager converterManager, VersionProviderImpl versionProvider)
            throws BeansException {
        super(springConfigPaths.getAllPaths(), false, null);
        this.contextId = contextId;
        this.artifactoryHome = artifactoryHome;
        this.springConfigPaths = springConfigPaths;
        this.converterManager = converterManager;
        this.versionProvider = versionProvider;
        this.started = System.currentTimeMillis();
        refresh();
        contextCreated();
    }

    @Override
    public ArtifactoryHome getArtifactoryHome() {
        return artifactoryHome;
    }

    @Override
    public String getContextId() {
        return contextId;
    }

    @Override
    public String getDisplayName() {
        return contextId;
    }

    @Override
    public SpringConfigPaths getConfigPaths() {
        return springConfigPaths;
    }

    @Override
    public String getServerId() {
        //For a cluster node take it from the cluster property, otherwise use the license hash
        HaNodeProperties HaNodeProperties = getArtifactoryHome().getHaNodeProperties();
        if (HaNodeProperties != null) {
            return HaNodeProperties.getServerId();
        }
        return HaCommonAddon.ARTIFACTORY_PRO;
    }

    @Override
    public boolean isOffline() {
        return offline;
    }

    @Override
    public void setOffline() {
        this.offline = true;
    }

    @Override
    public ConverterManager getConverterManager() {
        return converterManager;
    }

    @Override
    public VersionProvider getVersionProvider() {
        return versionProvider;
    }

    @Override
    public long getUptime() {
        return System.currentTimeMillis() - started;
    }

    @Override
    public CentralConfigService getCentralConfig() {
        return beanForType(CentralConfigService.class);
    }

    @Override
    public SecurityService getSecurityService() {
        return beanForType(SecurityService.class);
    }

    @Override
    public AuthorizationService getAuthorizationService() {
        return beanForType(AuthorizationService.class);
    }

    @Override
    public TaskService getTaskService() {
        return beanForType(TaskService.class);
    }

    @Override
    public RepositoryService getRepositoryService() {
        return beanForType(InternalRepositoryService.class);
    }

    @Override
    public void addReloadableBean(Class<? extends ReloadableBean> beanClass) {
        toInitialize.add(beanClass);
    }

    @Override
    public void refresh() throws BeansException, IllegalStateException {
        try {
            setReady(false);
            beansForType.clear();
            ArtifactoryContextThreadBinder.bind(this);
            super.refresh();
            reloadableBeans = new ArrayList<>(toInitialize.size());
            Set<Class<? extends ReloadableBean>> toInit = new HashSet<>(toInitialize);
            for (Class<? extends ReloadableBean> beanClass : toInitialize) {
                orderReloadableBeans(toInit, beanClass);
            }
            log.debug("Reloadable list of beans: {}", reloadableBeans);
            log.info("Artifactory context starting up...");
            versionProvider.loadDbVersion();    // db should have been initialized by now -> we can read the db version
            converterManager.beforeInits();
            for (ReloadableBean reloadableBean : reloadableBeans) {
                String beanIfc = getInterfaceName(reloadableBean);
                log.info("Initializing {}", beanIfc);
                converterManager.serviceConvert(reloadableBean);
                try {
                    reloadableBean.init();
                } catch (Exception e) {
                    throw new BeanInitializationException("Failed to initialize bean '" + beanIfc + "'.", e);
                }
                log.debug("Initialized {}", beanIfc);
            }
            converterManager.afterAllInits();
            setReady(true);
            converterManager.afterContextReady();
        } finally {
            ArtifactoryContextThreadBinder.unbind();
        }
    }

    private void contextCreated() {
        try {
            ArtifactoryContextThreadBinder.bind(this);
            Map<String, ContextReadinessListener> contextReadinessListeners =
                    beansForType(ContextReadinessListener.class);
            log.debug("Signaling context created to context readiness listener beans.");
            for (ContextReadinessListener bean : contextReadinessListeners.values()) {
                String beanIfc = getInterfaceName(bean);
                log.debug("Signaling context created to {}.", beanIfc);
                bean.onContextCreated();
            }
        } finally {
            ArtifactoryContextThreadBinder.unbind();
        }
    }

    @Override
    protected void prepareBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        super.prepareBeanFactory(beanFactory);
        //Add our own post processor that registers all reloadable beans auto-magically after construction
        beanFactory.addBeanPostProcessor(new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                Class<?> targetClass = AopUtils.getTargetClass(bean);
                if (ReloadableBean.class.isAssignableFrom(targetClass)) {
                    Reloadable annotation;
                    if (targetClass.isAnnotationPresent(Reloadable.class)) {
                        annotation = targetClass.getAnnotation(Reloadable.class);
                        Class<? extends ReloadableBean> beanClass = annotation.beanClass();
                        addReloadableBean(beanClass);
                    } else {
                        throw new IllegalStateException("Bean " + targetClass.getName() +
                                " requires initialization beans to be initialized, but no such beans were found");
                    }
                }
                return bean;
            }

            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
                //Do nothing
                return bean;
            }
        });
    }

    @Override
    public void init() {
        // Nothing
    }

    @Override
    public void destroy() {
        setReady(false);
        ArtifactoryContextThreadBinder.bind(this);
        ArtifactoryHome.bind(getArtifactoryHome());
        try {
            try {
                if (reloadableBeans != null && !reloadableBeans.isEmpty()) {
                    // TODO[By Gidi] find better way to update the ArtifactoryStateManager on beforeDestroy event
                    beanForType(ArtifactoryStateManager.class).beforeDestroy();
                    log.debug("Destroying beans: {}", reloadableBeans);
                    for (int i = reloadableBeans.size() - 1; i >= 0; i--) {
                        ReloadableBean bean = reloadableBeans.get(i);
                        String beanIfc = getInterfaceName(bean);
                        log.info("Destroying {}", beanIfc);
                        try {
                            bean.destroy();
                        } catch (Exception e) {
                            if (log.isDebugEnabled() || Boolean.getBoolean(ConstantValues.test.getPropertyName())) {
                                log.error("Exception while destroying bean '" + beanIfc + "'.", e);
                            } else {
                                log.error("Exception while destroying {} ({}).", beanIfc, e.getMessage());
                            }
                        }
                        log.debug("Destroyed {}", beanIfc);
                    }
                }
            } finally {
                super.destroy();
            }
        } finally {
            ArtifactoryContextThreadBinder.unbind();
            ArtifactoryHome.unbind();
        }
    }

    @Override
    public void convert(CompoundVersionDetails source, CompoundVersionDetails target) {
    }

    @Override
    public void reload(CentralConfigDescriptor oldDescriptor) {
        setReady(false);
        log.debug("Reloading beans: {}", reloadableBeans);
        for (ReloadableBean reloadableBean : reloadableBeans) {
            String beanIfc = getInterfaceName(reloadableBean);
            log.debug("Reloading {}", beanIfc);
            reloadableBean.reload(oldDescriptor);
            log.debug("Reloaded {}", beanIfc);
        }
        setReady(true);
    }

    private String getInterfaceName(Object bean) {
        return bean.getClass().getInterfaces()[0].getName();
    }

    private void orderReloadableBeans(Set<Class<? extends ReloadableBean>> beansLeftToInit,
            Class<? extends ReloadableBean> beanClass) {
        if (!beansLeftToInit.contains(beanClass)) {
            // Already done
            return;
        }
        ReloadableBean initializingBean = beanForType(beanClass);
        Class<?> targetClass = AopUtils.getTargetClass(initializingBean);
        Reloadable annotation;
        if (targetClass.isAnnotationPresent(Reloadable.class)) {
            annotation = targetClass.getAnnotation(Reloadable.class);
        } else {
            throw new IllegalStateException(
                    "Bean " + targetClass.getName() + " requires the @Reloadable annotation to be present.");
        }
        Class<? extends ReloadableBean>[] dependsUpon = annotation.initAfter();
        for (Class<? extends ReloadableBean> doBefore : dependsUpon) {
            //Sanity check that prerequisite bean was registered
            if (!toInitialize.contains(doBefore)) {
                throw new IllegalStateException(
                        "Bean '" + beanClass.getName() + "' requires bean '" + doBefore.getName() +
                                "' to be initialized, but no such bean is registered for init.");
            }
            if (!doBefore.isInterface()) {
                throw new IllegalStateException(
                        "Cannot order bean with implementation class.\n" +
                                " Please provide an interface extending " +
                                ReloadableBean.class.getName());
            }
            orderReloadableBeans(beansLeftToInit, doBefore);
        }
        // Avoid double init
        if (beansLeftToInit.remove(beanClass)) {
            reloadableBeans.add(initializingBean);
        }
    }

    @Override
    public boolean isReady() {
        return ready;
    }

    private void setReady(boolean ready) {
        this.ready = ready;
        if (hasBeanFactory()) {
            //Signal to all the context ready listener beans
            final Map<String, ContextReadinessListener> contextReadinessListeners =
                    beansForType(ContextReadinessListener.class);
            log.debug("Signaling context ready={} to context readiness listener beans.", ready);
            for (ContextReadinessListener bean : contextReadinessListeners.values()) {
                String beanIfc = getInterfaceName(bean);
                log.debug("Signaling context ready={} to {}.", ready, beanIfc);
                if (ready) {
                    bean.onContextReady();
                } else {
                    bean.onContextUnready();
                }
            }
        }
        if (ready) {
            log.info("Artifactory application context is ready.");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T beanForType(Class<T> type) {
        //No sync needed. Sync is done on write, so in the worst case we might end up with
        //a bean with the same value, which is fine
        T bean = (T) beansForType.get(type);
        if (bean == null) {
            Map<String, T> beans = getBeansOfType(type);
            if (beans.isEmpty()) {
                throw new RuntimeException("Could not find bean of type '" + type.getName() + "'.");
            }

            bean = beans.values().iterator().next(); // default to the first bean encountered
            if (beans.size() > 1) {
                // prefer beans marked as primary
                for (Map.Entry<String, T> beanEntry : beans.entrySet()) {
                    BeanDefinition beanDefinition = getBeanFactory().getBeanDefinition(beanEntry.getKey());
                    if (beanDefinition != null && beanDefinition.isPrimary()) {
                        bean = beanEntry.getValue();
                    }
                }
            }
        }
        beansForType.put(type, bean);
        return bean;
    }

    @Override
    public <T> Map<String, T> beansForType(Class<T> type) {
        return getBeansOfType(type);
    }

    @Override
    public <T> T beanForType(String name, Class<T> type) {
        return getBean(name, type);
    }

    @Override
    public BinaryStore getBinaryStore() {
        return beanForType(BinaryStore.class);
    }

    @Override
    public void importFrom(ImportSettings settings) {
        MutableStatusHolder status = settings.getStatusHolder();
        status.status("### Beginning full system import ###", log);
        // First sync status and settings
        status.setFastFail(settings.isFailFast());
        status.setVerbose(settings.isVerbose());
        // First check the version of the folder imported
        ArtifactoryVersion backupVersion = BackupUtils.findVersion(settings.getBaseDir());
        // We don't support import from 125 and below
        ArtifactoryVersion supportFrom = ArtifactoryVersion.v125;
        if (backupVersion.before(supportFrom)) {
            throw new IllegalArgumentException("Folder " + settings.getBaseDir().getAbsolutePath() +
                    " contains an export from a version older than " + supportFrom.getValue() + ".\n" +
                    "Please use the dump-legacy-dbs first, to dump this version's data, then import it " +
                    "into Artifactory.");
        }
        ((ImportSettingsImpl) settings).setExportVersion(backupVersion);
        List<String> stoppedTasks = Lists.newArrayList();
        try {
            stopRelatedTasks(ImportJob.class, stoppedTasks);
            importResourcesFromEtcDirectory(settings);
            AddonsManager addonsManager = beanForType(AddonsManager.class);

            encryptStorageProperties();
            // import central configuration
            getCentralConfig().importFrom(settings);
            // import security settings
            getSecurityService().importFrom(settings);
            // import webstart keystore
            addonsManager.addonByType(WebstartAddon.class).importKeyStore(settings);
            // import 3rd party licenses
            addonsManager.addonByType(LicensesAddon.class).importLicenses(settings);
            // import user plugins
            addonsManager.addonByType(PluginsAddon.class).importFrom(settings);
            // import builds
            beanForType(BuildService.class).importFrom(settings);
            // import logback conf
            beanForType(LoggingService.class).importFrom(settings);
            if (!settings.isExcludeContent()) {
                // import repositories content
                getRepositoryService().importFrom(settings);
            }
            status.status("### Full system import finished ###", log);
        } finally {
            resumeTasks(stoppedTasks);
        }
    }

    /**
     * encrypt Storage Properties if master key exist
     */
    private void encryptStorageProperties() {
        StoragePropertiesEncryptInterceptor storagePropertiesEncryptInterceptor = new StoragePropertiesEncryptInterceptor();
        storagePropertiesEncryptInterceptor.encryptOrDecryptStoragePropertiesFile(true);
    }

    @Override
    public void exportTo(ExportSettings settings) {
        MutableStatusHolder status = settings.getStatusHolder();
        status.status("Beginning full system export...", log);
        String timestamp;
        boolean incremental = settings.isIncremental();
        if (!incremental) {
            DateFormat formatter = new SimpleDateFormat("yyyyMMdd.HHmmss");
            timestamp = formatter.format(settings.getTime());
        } else {
            timestamp = CURRENT_TIME_EXPORT_DIR_NAME;
        }
        File baseDir = settings.getBaseDir();

        //Only create a temp dir when not performing incremental backup
        File workingExportDir;
        if (incremental) {
            //Will always be baseDir/CURRENT_TIME_EXPORT_DIR_NAME
            workingExportDir = new File(baseDir, timestamp);
        } else {
            workingExportDir = new File(baseDir, timestamp + ".tmp");
            //Make sure the directory does not already exist
            try {
                FileUtils.deleteDirectory(workingExportDir);
            } catch (IOException e) {
                status.error("Failed to delete old temp export directory: " + workingExportDir.getAbsolutePath(), e,
                        log);
                return;
            }
        }
        status.status("Creating temp export directory: " + workingExportDir.getAbsolutePath(), log);
        try {
            FileUtils.forceMkdir(workingExportDir);
        } catch (IOException e) {
            status.error("Failed to create backup dir: " + workingExportDir.getAbsolutePath(), e, log);
            return;
        }
        status.status("Using backup directory: '" + workingExportDir.getAbsolutePath() + "'.", log);

        ExportSettingsImpl exportSettings = new ExportSettingsImpl(workingExportDir, settings);

        List<String> stoppedTasks = Lists.newArrayList();
        try {
            AddonsManager addonsManager = beanForType(AddonsManager.class);

            stopRelatedTasks(ExportJob.class, stoppedTasks);

            // central config
            getCentralConfig().exportTo(exportSettings);
            if (status.isError() && settings.isFailFast()) {
                return;
            }
            // security
            exportSecurity(exportSettings);
            if (status.isError() && settings.isFailFast()) {
                return;
            }
            // keystore
            WebstartAddon webstartAddon = addonsManager.addonByType(WebstartAddon.class);
            webstartAddon.exportKeyStore(exportSettings);
            if (status.isError() && settings.isFailFast()) {
                return;
            }

            // licenses
            LicensesAddon licensesAddon = addonsManager.addonByType(LicensesAddon.class);
            licensesAddon.exportLicenses(exportSettings);
            if (status.isError() && settings.isFailFast()) {
                return;
            }

            //artifactory.properties and etc files
            exportArtifactoryProperties(exportSettings);
            if (status.isError() && settings.isFailFast()) {
                return;
            }
            exportEtcDirectory(exportSettings);
            if (status.isError() && settings.isFailFast()) {
                return;
            }
            exportHaEtcDirectory(exportSettings);
            if (status.isError() && settings.isFailFast()) {
                return;
            }

            // build info
            exportBuildInfo(exportSettings);
            if (status.isError() && settings.isFailFast()) {
                return;
            }

            // repositories content
            if (settings.isIncludeMetadata() || !settings.isExcludeContent()) {
                getRepositoryService().exportTo(exportSettings);
            }
            if (status.isError() && settings.isFailFast()) {
                return;
            }

            if (incremental && settings.isCreateArchive()) {
                log.warn("Cannot create archive for an in place backup.");
            }
            if (!incremental) {
                //Create an archive if necessary
                if (settings.isCreateArchive()) {
                    createArchive(settings, status, timestamp, workingExportDir);
                } else {
                    moveTmpToBackupDir(settings, status, timestamp, workingExportDir);
                }
            } else {
                settings.setOutputFile(workingExportDir);
            }

            settings.cleanCallbacks();

            status.status("Full system export completed successfully.", log);
        } catch (RuntimeException e) {
            status.error("Full system export failed: " + e.getMessage(), e, log);
        } finally {
            resumeTasks(stoppedTasks);
        }
    }

    private void moveTmpToBackupDir(ExportSettings settings, MutableStatusHolder status, String timestamp,
            File workingExportDir) {
        //Delete any exiting final export dir
        File exportDir = new File(settings.getBaseDir(), timestamp);
        try {
            FileUtils.deleteDirectory(exportDir);
        } catch (IOException e) {
            log.warn("Failed to delete existing final export directory.", e);
        }
        //Switch the directories
        try {
            FileUtils.moveDirectory(workingExportDir, exportDir);
        } catch (IOException e) {
            log.error("Failed to move '{}' to '{}': {}", workingExportDir, exportDir, e.getMessage());
        } finally {
            settings.setOutputFile(exportDir);
        }
    }

    private void createArchive(ExportSettings settings, MutableStatusHolder status, String timestamp,
            File workingExportDir) {
        status.status("Creating archive...", log);

        File tempArchiveFile = new File(settings.getBaseDir(), timestamp + ".tmp.zip");
        try {
            ZipUtils.archive(workingExportDir, tempArchiveFile, true);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create system export archive.", e);
        }
        //Delete the temp export dir
        try {
            FileUtils.deleteDirectory(workingExportDir);
        } catch (IOException e) {
            log.warn("Failed to delete temp export directory.", e);
        }

        // From now on use only java.io.File for the file actions!

        //Delete any exiting final archive
        File archive = new File(settings.getBaseDir(), timestamp + ".zip");
        if (archive.exists()) {
            boolean deleted = archive.delete();
            if (!deleted) {
                status.warn("Failed to delete existing final export archive.", log);
            }
        }
        //Rename the archive file
        try {
            FileUtils.moveFile(tempArchiveFile, archive);
        } catch (IOException e) {
            status.error(String.format("Failed to move '%s' to '%s'.", tempArchiveFile.getAbsolutePath(),
                    archive.getAbsolutePath()), e, log);
        } finally {
            settings.setOutputFile(archive.getAbsoluteFile());
        }
    }

    private void exportArtifactoryProperties(ExportSettings settings) {
        MutableStatusHolder status = settings.getStatusHolder();
        File artifactoryPropFile = artifactoryHome.getHomeArtifactoryPropertiesFile();
        if (artifactoryPropFile.exists()) {
            try {
                FileUtils.copyFileToDirectory(artifactoryPropFile, settings.getBaseDir());
            } catch (IOException e) {
                status.error("Failed to copy artifactory.properties file", e, log);
            }
        } else {
            status.status("No KeyVal defined no export done", log);
        }
    }

    private void exportEtcDirectory(ExportSettings settings) {
        try {
            File targetBackupDir = new File(settings.getBaseDir(), "etc");
            // TODO: [by fsi] Find a way to copy with permissions kept
            FileUtils.copyDirectory(artifactoryHome.getEtcDir(), targetBackupDir,
                    new NotFileFilter(new NameFileFilter("artifactory.lic")), true);
            checkSecurityFolder(targetBackupDir);
        } catch (IOException e) {
            settings.getStatusHolder().error(
                    "Failed to export etc directory: " + artifactoryHome.getEtcDir().getAbsolutePath(), e, log);
        }
    }

    private void exportHaEtcDirectory(ExportSettings settings) {
        if (artifactoryHome.isHaConfigured()) {
            try {
                File targetBackupDir = new File(settings.getBaseDir(), "ha-etc");
                // TODO: [by fsi] Find a way to copy with permissions kept
                FileUtils.copyDirectory(artifactoryHome.getHaAwareEtcDir(), targetBackupDir,
                        new NotFileFilter(new NameFileFilter("artifactory.lic")));
                checkSecurityFolder(targetBackupDir);
            } catch (IOException e) {
                settings.getStatusHolder().error(
                        "Failed to export etc directory: " + artifactoryHome.getEtcDir().getAbsolutePath(), e, log);
            }
        }
    }

    private void checkSecurityFolder(File targetBackupDir) throws IOException {
        File masterKeyDest = new File(targetBackupDir, "etc/" + ConstantValues.securityMasterKeyLocation.getDefValue());
        if (masterKeyDest.exists()) {
            CryptoHelper.setPermissionsOnSecurityFolder(masterKeyDest.getParentFile());
        }
    }

    /**
     * Import selected files from the etc directory. Note that while the export simply copies the etc directory, here we
     * are only wish to import some of the files while ignoring others. The reason is that the etc may contain custom
     * settings that are environment dependant (like db configuration) which will fail the import of will fail
     * Artifactory on the next startup. So changes to the repo.xml and/or artifactory.system.properties has to be
     * imported manually.
     *
     * @param settings basic settings with conf files
     */
    private void importResourcesFromEtcDirectory(ImportSettings settings) {
        File importEtcDir = new File(settings.getBaseDir(), "etc");
        if (!importEtcDir.exists()) {
            // older versions didn't export the etc directory
            log.info("Skipping etc directory import. File doesn't exist: " + importEtcDir.getAbsolutePath());
            return;
        }
        // copy the logo if it exists
        File customUiDir = new File(importEtcDir, "ui");
        if (customUiDir.exists()) {
            try {
                FileUtils.copyDirectory(customUiDir, artifactoryHome.getLogoDir());
            } catch (IOException e) {
                settings.getStatusHolder().error(
                        "Failed to import ui directory: " + customUiDir.getAbsolutePath(), e, log);
            }
        }

        // copy the master encryption key if it exists
        File etcSecurityDir = new File(importEtcDir, "security");
        if (etcSecurityDir.exists()) {
            try {
                File destSecurityFolder = new File(artifactoryHome.getHaAwareEtcDir(), "security");
                // TODO: [by fsi] Find a way to copy with permissions kept
                FileUtils.copyDirectory(etcSecurityDir, destSecurityFolder);
                CryptoHelper.setPermissionsOnSecurityFolder(destSecurityFolder);
            } catch (IOException e) {
                settings.getStatusHolder().error(
                        "Failed to import security directory: " + etcSecurityDir.getAbsolutePath(), e, log);
            }
        }
    }

    private void exportSecurity(ExportSettingsImpl settings) {
        MutableStatusHolder status = settings.getStatusHolder();
        SecurityService security = getSecurityService();
        status.status("Exporting security...", log);
        security.exportTo(settings);
    }

    private void exportBuildInfo(ExportSettingsImpl exportSettings) {
        MutableStatusHolder status = exportSettings.getStatusHolder();
        if (exportSettings.isExcludeBuilds()) {
            status.status("Skipping build info ...", log);
            return;
        }

        BuildService build = beanForType(BuildService.class);
        status.status("Exporting build info...", log);
        build.exportTo(exportSettings);
    }

    public List<ReloadableBean> getBeans() {
        return reloadableBeans;
    }

    private void stopRelatedTasks(Class<? extends TaskCallback> jobCommandClass, List<String> stoppedTokens) {
        if (TaskCallback.currentTaskToken() != null) {
            // Already stopped by standard task manager
            return;
        }
        TaskService taskService = getTaskService();
        taskService.stopRelatedTasks(jobCommandClass, stoppedTokens, BaseSettings.FULL_SYSTEM);
    }

    private void resumeTasks(List<String> tokens) {
        if (TaskCallback.currentTaskToken() != null) {
            // Already stopped by standard task manager
            return;
        }
        TaskService taskService = getTaskService();
        for (String token : tokens) {
            taskService.resumeTask(token);
        }
    }
}
