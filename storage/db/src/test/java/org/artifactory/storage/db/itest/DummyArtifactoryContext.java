package org.artifactory.storage.db.itest;

import com.google.common.collect.Maps;
import org.artifactory.addon.AddonType;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.ArtifactoryRunningMode;
import org.artifactory.addon.OssAddonsManager;
import org.artifactory.addon.ha.HaCommonAddon;
import org.artifactory.addon.ha.message.HaMessage;
import org.artifactory.addon.ha.message.HaMessageTopic;
import org.artifactory.addon.ha.semaphore.JVMSemaphoreWrapper;
import org.artifactory.addon.ha.semaphore.SemaphoreWrapper;
import org.artifactory.addon.smartrepo.SmartRepoAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.converters.ConverterManager;
import org.artifactory.converters.VersionProvider;
import org.artifactory.fs.StatsInfo;
import org.artifactory.repo.RepoPath;
import org.artifactory.sapi.common.ExportSettings;
import org.artifactory.sapi.common.ImportSettings;
import org.artifactory.spring.SpringConfigPaths;
import org.artifactory.storage.db.servers.model.ArtifactoryServer;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * @author mamo
 */
public class DummyArtifactoryContext implements ArtifactoryContext {
    private ApplicationContext applicationContext;
    private Map<Class<?>, Object> beans = Maps.newHashMap();

    public DummyArtifactoryContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void addBean(Object bean, Class<?>... types) {
        for (Class<?> type : types) {
            beans.put(type, bean);
        }
    }
    @Override
    public CentralConfigService getCentralConfig() {
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T beanForType(Class<T> type) {
        if (AddonsManager.class.equals(type)) {
            return (T) new DummyOssAddonsManager();
        }
        if (type.equals(HaCommonAddon.class)) {
            return (T) new DummyHaCommonAddon();
        }
        if (type.equals(RepositoryService.class)) {
            return (T) Mockito.mock(RepositoryService.class);
        }
        if (type.equals(SmartRepoAddon.class)) {
            return (T) new SmartRepoAddon(){

                @Override
                public boolean isDefault() {
                    return false;
                }

                @Override
                public boolean supportRemoteStats() {
                    return true;
                }

                @Override
                public void fileDownloadedRemotely(StatsInfo statsInfo, String remoteHost, RepoPath repoPath) {

                }
            };
        }
        if (beans.containsKey(type)) {
            return (T) beans.get(type);
        }
        return applicationContext.getBean(type);
    }

    @Override
    public <T> T beanForType(String name, Class<T> type) {
        return null;
    }

    @Override
    public <T> Map<String, T> beansForType(Class<T> type) {
        return null;
    }

    @Override
    public Object getBean(String name) {
        return applicationContext.getBean(name);
    }

    @Override
    public RepositoryService getRepositoryService() {
        return null;
    }

    @Override
    public AuthorizationService getAuthorizationService() {
        return null;
    }

    @Override
    public long getUptime() {
        return 0;
    }

    @Override
    public ArtifactoryHome getArtifactoryHome() {
        return null;
    }

    @Override
    public String getContextId() {
        return null;
    }

    @Override
    public SpringConfigPaths getConfigPaths() {
        return null;
    }

    @Override
    public String getServerId() {
        return null;
    }

    @Override
    public boolean isOffline() {
        return false;
    }

    @Override
    public void setOffline() {
    }

    @Override
    public ConverterManager getConverterManager() {
        return null;
    }

    @Override
    public VersionProvider getVersionProvider() {
        return null;
    }

    @Override
    public void destroy() {
    }

    @Override
    public void exportTo(ExportSettings settings) {
    }

    @Override
    public void importFrom(ImportSettings settings) {
    }

    private static class DummyHaCommonAddon implements HaCommonAddon {

        @Override
        public boolean isHaEnabled() {
            return false;
        }

        @Override
        public boolean isPrimary() {
            return false;
        }

        @Override
        public boolean isHaConfigured() {
            return false;
        }

        @Override
        public void notify(HaMessageTopic haMessageTopic, HaMessage haMessage) {
        }

        @Override
        public String getHostId() {
            return null;
        }

        @Override
        public SemaphoreWrapper getSemaphore(String semaphoreName) {
            Semaphore semaphore = new Semaphore(HaCommonAddon.DEFAULT_SEMAPHORE_PERMITS);
            return new JVMSemaphoreWrapper(semaphore);
        }

        @Override
        public void shutdown() {
        }

        @Override
        public List<ArtifactoryServer> getAllArtifactoryServers() {
            return new ArrayList<>();
        }

        @Override
        public boolean deleteArtifactoryServer(String id) {
            return false;
        }

        @Override
        public boolean artifactoryServerHasHeartbeat(ArtifactoryServer artifactoryServer) {
            return false;
        }

        @Override
        public boolean isDefault() {
            return false;
        }
    }

    private static class DummyOssAddonsManager extends OssAddonsManager {

        private DummyOssAddonsManager() {
            context = ContextHelper.get();
        }

        @Override
        public boolean isAddonSupported(AddonType addonType) {
            return false;
        }

        @Override
        public boolean isProLicensed(String licenseKeyHash) {
            return false;
        }

        @Override
        public ArtifactoryRunningMode getArtifactoryRunningMode() {
            return ArtifactoryRunningMode.OSS;
        }

        @Override
        public boolean isPartnerLicense() {
            return false;
        }
    }
}
