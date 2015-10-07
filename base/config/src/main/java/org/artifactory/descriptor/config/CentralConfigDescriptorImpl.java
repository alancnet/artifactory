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

package org.artifactory.descriptor.config;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.artifactory.descriptor.Descriptor;
import org.artifactory.descriptor.addon.AddonSettings;
import org.artifactory.descriptor.backup.BackupDescriptor;
import org.artifactory.descriptor.bintray.BintrayConfigDescriptor;
import org.artifactory.descriptor.cleanup.CleanupConfigDescriptor;
import org.artifactory.descriptor.download.FolderDownloadConfigDescriptor;
import org.artifactory.descriptor.external.BlackDuckSettingsDescriptor;
import org.artifactory.descriptor.external.ExternalProvidersDescriptor;
import org.artifactory.descriptor.gc.GcConfigDescriptor;
import org.artifactory.descriptor.index.IndexerDescriptor;
import org.artifactory.descriptor.mail.MailServerDescriptor;
import org.artifactory.descriptor.message.SystemMessageDescriptor;
import org.artifactory.descriptor.property.PropertySet;
import org.artifactory.descriptor.quota.QuotaConfigDescriptor;
import org.artifactory.descriptor.replication.LocalReplicationDescriptor;
import org.artifactory.descriptor.replication.RemoteReplicationDescriptor;
import org.artifactory.descriptor.replication.ReplicationBaseDescriptor;
import org.artifactory.descriptor.repo.HttpRepoDescriptor;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.ProxyDescriptor;
import org.artifactory.descriptor.repo.RealRepoDescriptor;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.descriptor.repo.RepoBaseDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.RepoLayout;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;
import org.artifactory.descriptor.repo.jaxb.LocalRepositoriesMapAdapter;
import org.artifactory.descriptor.repo.jaxb.RemoteRepositoriesMapAdapter;
import org.artifactory.descriptor.repo.jaxb.VirtualRepositoriesMapAdapter;
import org.artifactory.descriptor.security.SecurityDescriptor;
import org.artifactory.util.AlreadyExistsException;
import org.artifactory.util.DoesNotExistException;
import org.artifactory.util.PathUtils;
import org.artifactory.util.RepoLayoutUtils;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@XmlRootElement(name = "config")
@XmlType(name = "CentralConfigType",
        propOrder = {"serverName", "offlineMode", "helpLinksEnabled", "fileUploadMaxSizeMb", "dateFormat", "addons", "mailServer",
                "bintrayConfig", "security", "backups", "indexer", "localRepositoriesMap", "remoteRepositoriesMap",
                "virtualRepositoriesMap", "proxies", "propertySets", "urlBase", "logo", "footer", "repoLayouts",
                "remoteReplications", "localReplications", "gcConfig", "cleanupConfig", "virtualCacheCleanupConfig",
                "quotaConfig", "externalProviders", "systemMessageConfig", "folderDownloadConfig"},
        namespace = Descriptor.NS)
@XmlAccessorType(XmlAccessType.FIELD)
public class CentralConfigDescriptorImpl implements MutableCentralConfigDescriptor {

    public static final String DEFAULT_DATE_FORMAT = "dd-MM-yy HH:mm:ss z";

    @XmlElement(name = "localRepositories", required = true)
    @XmlJavaTypeAdapter(LocalRepositoriesMapAdapter.class)
    private Map<String, LocalRepoDescriptor> localRepositoriesMap = Maps.newLinkedHashMap();

    @XmlElement(name = "remoteRepositories", required = false)
    @XmlJavaTypeAdapter(RemoteRepositoriesMapAdapter.class)
    private Map<String, RemoteRepoDescriptor> remoteRepositoriesMap = Maps.newLinkedHashMap();

    @XmlElement(name = "virtualRepositories", required = false)
    @XmlJavaTypeAdapter(VirtualRepositoriesMapAdapter.class)
    private Map<String, VirtualRepoDescriptor> virtualRepositoriesMap = Maps.newLinkedHashMap();

    @XmlElementWrapper(name = "proxies")
    @XmlElement(name = "proxy", required = false)
    private List<ProxyDescriptor> proxies = new ArrayList<>();
    @XmlElement(defaultValue = DEFAULT_DATE_FORMAT)
    private String dateFormat = DEFAULT_DATE_FORMAT;

    @XmlElement(defaultValue = "100", required = false)
    private int fileUploadMaxSizeMb = 100;

    @XmlElementWrapper(name = "backups")
    @XmlElement(name = "backup", required = false)
    private List<BackupDescriptor> backups = new ArrayList<>();

    private IndexerDescriptor indexer;

    /**
     * A name uniquely identifying this artifactory server instance
     */
    @XmlElement
    private String serverName;

    /**
     * if this flag is set all the remote repos will work in offline mode
     */
    @XmlElement(defaultValue = "false", required = false)
    private boolean offlineMode;

    @XmlElement
    private boolean helpLinksEnabled = true;

    private AddonSettings addons = new AddonSettings();

    private MailServerDescriptor mailServer;

    /**
     * security might not be present in the xml but we always want to create it
     */
    @XmlElement
    private SecurityDescriptor security = new SecurityDescriptor();

    @XmlElementWrapper(name = "propertySets")
    @XmlElement(name = "propertySet", required = false)
    private List<PropertySet> propertySets = new ArrayList<>();

    @XmlElement
    private String urlBase;

    @XmlElement
    private String logo;

    @XmlElement
    private SystemMessageDescriptor systemMessageConfig;

    @XmlElement
    private FolderDownloadConfigDescriptor folderDownloadConfig;

    @XmlElement
    private String footer;

    @XmlElementWrapper(name = "repoLayouts")
    @XmlElement(name = "repoLayout", required = false)
    private List<RepoLayout> repoLayouts = Lists.newArrayList();

    @XmlElementWrapper(name = "remoteReplications")
    @XmlElement(name = "remoteReplication", required = false)
    private List<RemoteReplicationDescriptor> remoteReplications = Lists.newArrayList();

    @XmlElementWrapper(name = "localReplications")
    @XmlElement(name = "localReplication", required = false)
    private List<LocalReplicationDescriptor> localReplications = Lists.newArrayList();

    @XmlElement
    private GcConfigDescriptor gcConfig;

    @XmlElement
    private CleanupConfigDescriptor cleanupConfig;

    @XmlElement
    private CleanupConfigDescriptor virtualCacheCleanupConfig;

    @XmlElement
    private ExternalProvidersDescriptor externalProviders;

    @XmlElement
    private BintrayConfigDescriptor bintrayConfig;

    private QuotaConfigDescriptor quotaConfig;

    @Override
    public Map<String, LocalRepoDescriptor> getLocalRepositoriesMap() {
        return localRepositoriesMap;
    }

    @Override
    public void setLocalRepositoriesMap(Map<String, LocalRepoDescriptor> localRepositoriesMap) {
        this.localRepositoriesMap = localRepositoriesMap;
    }

    @Override
    public Map<String, RemoteRepoDescriptor> getRemoteRepositoriesMap() {
        return remoteRepositoriesMap;
    }

    @Override
    public void setRemoteRepositoriesMap(Map<String, RemoteRepoDescriptor> remoteRepositoriesMap) {
        this.remoteRepositoriesMap = remoteRepositoriesMap;
    }

    @Override
    public Map<String, VirtualRepoDescriptor> getVirtualRepositoriesMap() {
        return virtualRepositoriesMap;
    }

    @Override
    public void setVirtualRepositoriesMap(Map<String, VirtualRepoDescriptor> virtualRepositoriesMap) {
        this.virtualRepositoriesMap = virtualRepositoriesMap;
    }

    @Override
    public List<ProxyDescriptor> getProxies() {
        return proxies;
    }

    @Override
    public void setProxies(List<ProxyDescriptor> proxies) {
        this.proxies = proxies;
    }

    @Override
    public ProxyDescriptor getDefaultProxy() {
        for (ProxyDescriptor proxy : proxies) {
            if (proxy.isDefaultProxy()) {
                return proxy;
            }
        }
        return null;
    }

    @Override
    public String getDateFormat() {
        return dateFormat;
    }

    @Override
    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    @Override
    public int getFileUploadMaxSizeMb() {
        return fileUploadMaxSizeMb;
    }

    @Override
    public void setFileUploadMaxSizeMb(int fileUploadMaxSizeMb) {
        this.fileUploadMaxSizeMb = fileUploadMaxSizeMb;
    }

    @Override
    public List<BackupDescriptor> getBackups() {
        return backups;
    }

    @Override
    public void setBackups(List<BackupDescriptor> backups) {
        this.backups = backups;
    }

    @Override
    public IndexerDescriptor getIndexer() {
        return indexer;
    }

    @Override
    public void setIndexer(IndexerDescriptor mavenIndexer) {
        this.indexer = mavenIndexer;
    }

    @Override
    public String getServerName() {
        return serverName;
    }

    @Override
    public void setServerName(String serverName) {
        this.serverName = StringUtils.stripToNull(serverName);
    }

    @Override
    public SecurityDescriptor getSecurity() {
        return security;
    }

    @Override
    public void setSecurity(SecurityDescriptor security) {
        if (security == null) {
            security = new SecurityDescriptor();
        }
        this.security = security;
    }

    @Override
    public AddonSettings getAddons() {
        return addons;
    }

    @Override
    public void setAddons(AddonSettings addons) {
        this.addons = addons;
    }

    @Override
    public MailServerDescriptor getMailServer() {
        return mailServer;
    }

    @Override
    public void setMailServer(MailServerDescriptor mailServer) {
        this.mailServer = mailServer;
    }

    @Override
    public List<PropertySet> getPropertySets() {
        return propertySets;
    }

    @Override
    public void setPropertySets(List<PropertySet> propertySets) {
        this.propertySets = propertySets;
    }

    @Override
    public String getUrlBase() {
        return urlBase;
    }

    @Override
    public void setUrlBase(String urlBase) {
        this.urlBase = PathUtils.trimTrailingSlashes(urlBase);
    }

    @Override
    public RepoDescriptor removeRepository(String repoKey) {
        // first remove the repository itself
        RepoDescriptor removedRepo = localRepositoriesMap.remove(repoKey);
        if (removedRepo == null) {
            removedRepo = remoteRepositoriesMap.remove(repoKey);
        }
        if (removedRepo == null) {
            removedRepo = virtualRepositoriesMap.remove(repoKey);
        }
        if (removedRepo == null) {
            // not found - finish
            return null;
        }

        // remove from any virtual repository
        for (VirtualRepoDescriptor virtualRepoDescriptor : virtualRepositoriesMap.values()) {
            virtualRepoDescriptor.removeRepository(removedRepo);
        }

        if (removedRepo instanceof RealRepoDescriptor) {
            // remove the repository from any backup exclude list
            for (BackupDescriptor backup : getBackups()) {
                backup.removeExcludedRepository((RealRepoDescriptor) removedRepo);
            }
        }

        if (removedRepo instanceof RepoBaseDescriptor) {
            // remove from the indexer include list
            IndexerDescriptor indexer = getIndexer();
            if (indexer != null) {
                indexer.removeIncludedRepository((RepoBaseDescriptor) removedRepo);
            }
        }

        if (removedRepo instanceof HttpRepoDescriptor) {
            RemoteReplicationDescriptor existingReplication = getRemoteReplication(removedRepo.getKey());
            if (existingReplication != null) {
                removeRemoteReplication(existingReplication);
            }
        }

        if (removedRepo instanceof LocalRepoDescriptor) {
            LocalReplicationDescriptor existingReplication = getLocalReplication(removedRepo.getKey());
            if (existingReplication != null) {
                removeLocalReplication(existingReplication);
            }
        }

        return removedRepo;
    }


    @Override
    public String getFooter() {
        return footer;
    }

    public void setFooter(String footer) {
        this.footer = footer;
    }

    @Override
    public boolean isKeyAvailable(String key) {
        return !(VirtualRepoDescriptor.GLOBAL_VIRTUAL_REPO_KEY.equals(key) ||
                isRepositoryExists(key) ||
                isProxyExists(key) ||
                isBackupExists(key) ||
                isLdapExists(key) ||
                isPropertySetExists(key) ||
                isRepoLayoutExists(key));
    }

    @Override
    public boolean isRepositoryExists(String repoKey) {
        return localRepositoriesMap.containsKey(repoKey)
                || remoteRepositoriesMap.containsKey(repoKey)
                || virtualRepositoriesMap.containsKey(repoKey);
    }

    @Override
    public void addLocalRepository(LocalRepoDescriptor localRepoDescriptor)
            throws AlreadyExistsException {
        String repoKey = localRepoDescriptor.getKey();
        repoKeyExists(repoKey, false);
        localRepositoriesMap.put(repoKey, localRepoDescriptor);
        conditionallyAddToBackups(localRepoDescriptor);
    }

    @Override
    public void addRemoteRepository(RemoteRepoDescriptor remoteRepoDescriptor) {
        String repoKey = remoteRepoDescriptor.getKey();
        repoKeyExists(repoKey, false);
        remoteRepositoriesMap.put(repoKey, remoteRepoDescriptor);
        conditionallyAddToBackups(remoteRepoDescriptor);
    }

    private void conditionallyAddToBackups(RealRepoDescriptor remoteRepoDescriptor) {
        // Conditionally add the repository to any backup exclude list
        for (BackupDescriptor backup : getBackups()) {
            backup.addExcludedRepository(remoteRepoDescriptor);
        }
    }

    @Override
    public void addVirtualRepository(VirtualRepoDescriptor virtualRepoDescriptor) {
        String repoKey = virtualRepoDescriptor.getKey();
        repoKeyExists(repoKey, false);
        virtualRepositoriesMap.put(repoKey, virtualRepoDescriptor);
    }

    @Override
    public boolean isProxyExists(String proxyKey) {
        return getProxy(proxyKey) != null;
    }

    @Override
    public void addProxy(ProxyDescriptor proxyDescriptor, boolean defaultForAllRemoteRepo) {
        String proxyKey = proxyDescriptor.getKey();
        if (isProxyExists(proxyKey)) {
            throw new AlreadyExistsException("Proxy " + proxyKey + " already exists");
        }
        if (proxyDescriptor.isDefaultProxy()) {
            proxyChanged(proxyDescriptor, defaultForAllRemoteRepo);
            // remove default flag from other existing proxy if exist
            for (ProxyDescriptor proxy : proxies) {
                proxy.setDefaultProxy(false);
            }
        }
        proxies.add(proxyDescriptor);
    }

    @Override
    public ProxyDescriptor removeProxy(String proxyKey) {
        ProxyDescriptor proxyDescriptor = getProxy(proxyKey);
        if (proxyDescriptor == null) {
            return null;
        }

        // remove the proxy from the proxies list
        proxies.remove(proxyDescriptor);

        // remove references from all remote repositories
        for (RemoteRepoDescriptor remoteRepo : remoteRepositoriesMap.values()) {
            if (remoteRepo instanceof HttpRepoDescriptor) {
                if (((HttpRepoDescriptor) remoteRepo).getProxy() != null
                        && ((HttpRepoDescriptor) remoteRepo).getProxy().getKey().equals(proxyKey)) {
                    ((HttpRepoDescriptor) remoteRepo).setProxy(null);
                }
            }
        }

        for (LocalReplicationDescriptor localReplication : localReplications) {
            localReplication.setProxy(null);
        }

        ExternalProvidersDescriptor externalProvidersDescriptor = getExternalProvidersDescriptor();
        if (externalProvidersDescriptor != null) {
            BlackDuckSettingsDescriptor blackDuckSettingsDescriptor = externalProvidersDescriptor.getBlackDuckSettingsDescriptor();
            if (blackDuckSettingsDescriptor != null && blackDuckSettingsDescriptor.getProxy() != null
                    && blackDuckSettingsDescriptor.getProxy().getKey().equals(proxyKey)) {
                blackDuckSettingsDescriptor.setProxy(null);
            }
        }

        return proxyDescriptor;
    }

    @Override
    public void proxyChanged(ProxyDescriptor proxy, boolean updateExistingRepos) {
        if (proxy.isDefaultProxy()) {
            if (updateExistingRepos) {
                updateExistingRepos(proxy);
                updateExistingLocalReplications(proxy);
            }
            //Unset the previous default if any
            for (ProxyDescriptor proxyDescriptor : proxies) {
                if (!proxy.equals(proxyDescriptor)) {
                    proxyDescriptor.setDefaultProxy(false);
                }
            }
        }
    }

    private void updateExistingRepos(ProxyDescriptor proxy) {
        ProxyDescriptor previousDefaultProxy = findPreviousProxyDescriptor(proxy);
        for (RemoteRepoDescriptor remoteRepoDescriptor : remoteRepositoriesMap.values()) {
            if (remoteRepoDescriptor instanceof HttpRepoDescriptor) {
                HttpRepoDescriptor httpRepoDescriptor = (HttpRepoDescriptor) remoteRepoDescriptor;
                ProxyDescriptor existingRepoProxy = httpRepoDescriptor.getProxy();
                // if the repo doesn't have a proxy, or it is the previous default proxy configured then override it.
                if (existingRepoProxy == null || existingRepoProxy.equals(previousDefaultProxy)) {
                    httpRepoDescriptor.setProxy(proxy);
                }
            }
        }
    }

    private void updateExistingLocalReplications(ProxyDescriptor proxy) {
        ProxyDescriptor previousDefaultProxy = findPreviousProxyDescriptor(proxy);
        for (LocalReplicationDescriptor localReplication : localReplications) {
            ProxyDescriptor existingProxy = localReplication.getProxy();
            if (existingProxy == null || existingProxy.equals(previousDefaultProxy)) {
                localReplication.setProxy(proxy);
            }
        }
    }

    private ProxyDescriptor findPreviousProxyDescriptor(final ProxyDescriptor proxyDescriptor) {
        return Iterables.find(proxies, new Predicate<ProxyDescriptor>() {
            @Override
            public boolean apply(@Nullable ProxyDescriptor input) {
                return (input != null) && input.isDefaultProxy() && !input.getKey().equals(proxyDescriptor.getKey());
            }
        }, null);
    }

    @Override
    public boolean isBackupExists(String backupKey) {
        return getBackup(backupKey) != null;
    }


    @Override
    public String getLogo() {
        return logo;
    }

    @Override
    public void setLogo(String logo) {
        this.logo = logo;
    }

    @Override
    public SystemMessageDescriptor getSystemMessageConfig() {
        return systemMessageConfig;
    }

    public void setSystemMessageConfig(SystemMessageDescriptor systemMessageConfig) {
        this.systemMessageConfig = systemMessageConfig;
    }

    @Override
    public FolderDownloadConfigDescriptor getFolderDownloadConfig() {
        return folderDownloadConfig;
    }

    @Override
    public void setFolderDownloadConfig(FolderDownloadConfigDescriptor folderDownloadConfig) {
        this.folderDownloadConfig = folderDownloadConfig;
    }

    @Override
    public void addBackup(BackupDescriptor backupDescriptor) {
        String backupKey = backupDescriptor.getKey();
        if (isBackupExists(backupKey)) {
            throw new AlreadyExistsException("Backup " + backupKey + " already exists");
        }
        backups.add(backupDescriptor);
    }

    @Override
    public BackupDescriptor removeBackup(String backupKey) {
        BackupDescriptor backupDescriptor = getBackup(backupKey);
        if (backupDescriptor == null) {
            return null;
        }

        // remove the backup from the backups list
        backups.remove(backupDescriptor);

        return backupDescriptor;
    }

    @Override
    public boolean isPropertySetExists(String propertySetName) {
        return getPropertySet(propertySetName) != null;
    }

    @Override
    public void addPropertySet(PropertySet propertySet) {
        String propertySetName = propertySet.getName();
        if (isPropertySetExists(propertySetName)) {
            throw new AlreadyExistsException("Property set " + propertySetName + " already exists");
        }
        propertySets.add(propertySet);
    }

    @Override
    public PropertySet removePropertySet(String propertySetName) {
        PropertySet propertySet = getPropertySet(propertySetName);
        if (propertySet == null) {
            return null;
        }

        //Remove the property set from the property sets list
        propertySets.remove(propertySet);

        //Remove the property set from any local repo which is associated with it
        Collection<LocalRepoDescriptor> localRepoDescriptorCollection = localRepositoriesMap.values();
        for (LocalRepoDescriptor localRepoDescriptor : localRepoDescriptorCollection) {
            localRepoDescriptor.removePropertySet(propertySetName);
        }

        //Remove the property set from any remote repo which is associated with it
        Collection<RemoteRepoDescriptor> remoteRepoDescriptors = remoteRepositoriesMap.values();
        for (RemoteRepoDescriptor remoteRepoDescriptor : remoteRepoDescriptors) {
            remoteRepoDescriptor.removePropertySet(propertySetName);
        }

        return propertySet;
    }

    @Override
    public boolean isOfflineMode() {
        return offlineMode;
    }

    @Override
    public void setOfflineMode(boolean offlineMode) {
        this.offlineMode = offlineMode;
    }

    @Override
    public boolean isHelpLinksEnabled() {
        return helpLinksEnabled;
    }

    @Override
    public void setHelpLinksEnabled(boolean helpLinksEnabled) {
        this.helpLinksEnabled = helpLinksEnabled;
    }

    @Override
    public ProxyDescriptor defaultProxyDefined() {
        for (ProxyDescriptor proxyDescriptor : proxies) {
            if (proxyDescriptor.isDefaultProxy()) {
                return proxyDescriptor;
            }
        }
        return null;
    }

    @Override
    public ProxyDescriptor getProxy(String proxyKey) {
        for (ProxyDescriptor proxy : proxies) {
            if (proxy.getKey().equals(proxyKey)) {
                return proxy;
            }
        }
        return null;
    }

    @Override
    public BackupDescriptor getBackup(String backupKey) {
        for (BackupDescriptor backup : backups) {
            if (backup.getKey().equals(backupKey)) {
                return backup;
            }
        }
        return null;
    }

    private PropertySet getPropertySet(String propertySetName) {
        for (PropertySet propertySet : propertySets) {
            if (propertySet.getName().equals(propertySetName)) {
                return propertySet;
            }
        }

        return null;
    }

    private boolean isLdapExists(String key) {
        return security != null && security.isLdapExists(key);
    }

    private void repoKeyExists(String repoKey, boolean shouldExist) {
        boolean exists = isRepositoryExists(repoKey);
        if (exists && !shouldExist) {
            throw new AlreadyExistsException("Repository " + repoKey + " already exists");
        }

        if (!exists && shouldExist) {
            throw new DoesNotExistException("Repository " + repoKey + " does not exist");
        }
    }

    @Override
    public List<RepoLayout> getRepoLayouts() {
        return repoLayouts;
    }

    @Override
    public void setRepoLayouts(List<RepoLayout> repoLayouts) {
        this.repoLayouts = repoLayouts;
    }

    @Override
    public boolean isRepoLayoutExists(String repoLayoutName) {
        for (RepoLayout repoLayout : repoLayouts) {
            if (repoLayout.getName().equals(repoLayoutName)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void addRepoLayout(RepoLayout repoLayout) {
        String repoLayoutName = repoLayout.getName();
        if (isRepoLayoutExists(repoLayoutName)) {
            throw new AlreadyExistsException("Repo Layout " + repoLayoutName + " already exists");
        }
        repoLayouts.add(repoLayout);
    }

    @Override
    public RepoLayout removeRepoLayout(String repoLayoutName) {
        RepoLayout repoLayout = getRepoLayout(repoLayoutName);
        if (repoLayout == null) {
            return null;
        }

        repoLayouts.remove(repoLayout);


        Collection<LocalRepoDescriptor> localRepoDescriptorCollection = localRepositoriesMap.values();
        for (LocalRepoDescriptor localRepoDescriptor : localRepoDescriptorCollection) {
            if (repoLayout.equals(localRepoDescriptor.getRepoLayout())) {
                localRepoDescriptor.setRepoLayout(RepoLayoutUtils.MAVEN_2_DEFAULT);
            }
        }

        Collection<RemoteRepoDescriptor> remoteRepoDescriptors = remoteRepositoriesMap.values();
        for (RemoteRepoDescriptor remoteRepoDescriptor : remoteRepoDescriptors) {
            if (repoLayout.equals(remoteRepoDescriptor.getRepoLayout())) {
                remoteRepoDescriptor.setRepoLayout(RepoLayoutUtils.MAVEN_2_DEFAULT);
            }
            if (repoLayout.equals(remoteRepoDescriptor.getRemoteRepoLayout())) {
                remoteRepoDescriptor.setRemoteRepoLayout(null);
            }
        }

        Collection<VirtualRepoDescriptor> virtualRepoDescriptors = virtualRepositoriesMap.values();
        for (VirtualRepoDescriptor virtualRepoDescriptor : virtualRepoDescriptors) {
            if (repoLayout.equals(virtualRepoDescriptor.getRepoLayout())) {
                virtualRepoDescriptor.setRepoLayout(null);
            }
        }

        return repoLayout;
    }

    @Override
    public RepoLayout getRepoLayout(String repoLayoutName) {
        for (RepoLayout repoLayout : repoLayouts) {
            if (repoLayout.getName().equals(repoLayoutName)) {
                return repoLayout;
            }
        }

        return null;
    }

    @Override
    public boolean isRemoteReplicationExists(RemoteReplicationDescriptor descriptor) {
        return remoteReplications.contains(descriptor);
    }

    @Override
    public boolean isLocalReplicationExists(LocalReplicationDescriptor descriptor) {
        return localReplications.contains(descriptor);
    }

    @Override
    public List<RemoteReplicationDescriptor> getRemoteReplications() {
        return remoteReplications;
    }

    @Override
    public void setRemoteReplications(List<RemoteReplicationDescriptor> replicationDescriptors) {
        remoteReplications = replicationDescriptors;
    }

    @Override
    public List<LocalReplicationDescriptor> getLocalReplications() {
        return localReplications;
    }

    @Override
    public void setLocalReplications(List<LocalReplicationDescriptor> localReplications) {
        this.localReplications = localReplications;
    }

    @Override
    public RemoteReplicationDescriptor getRemoteReplication(String replicatedRepoKey) {

        return getReplication(replicatedRepoKey, remoteReplications);
    }

    @Override
    public LocalReplicationDescriptor getLocalReplication(String replicatedRepoKey) {
        return getReplication(replicatedRepoKey, localReplications);
    }

    @Override
    public LocalReplicationDescriptor getEnabledLocalReplication(String replicatedRepoKey) {
        return get1stEnableLocalReplication(replicatedRepoKey, localReplications);
    }

    @Override
    public int getTotalNumOfActiveLocalReplication(String replicatedRepoKey) {
        return getNumOfActiveLocalReplication(replicatedRepoKey, localReplications);
    }

    @Override
    public boolean isMultiPushConfigureForThisRepo(String repoKey){
        return getNumOfActiveLocalReplication(repoKey, localReplications) > 1;
    }


    @Override
    public LocalReplicationDescriptor getLocalReplication(String replicatedRepoKey, String replicateRepoUrl) {
        return getSpecificLocalReplication(replicatedRepoKey, replicateRepoUrl, localReplications);
    }

    @Override
    public void addRemoteReplication(RemoteReplicationDescriptor replicationDescriptor) {
        addReplication(replicationDescriptor, remoteReplications);
    }

    @Override
    public void addLocalReplication(LocalReplicationDescriptor replicationDescriptor) {
        addLocalReplication(replicationDescriptor, localReplications);
    }

    @Override
    public void removeRemoteReplication(RemoteReplicationDescriptor replicationDescriptor) {
        removeReplication(replicationDescriptor, remoteReplications);
    }

    @Override
    public void removeLocalReplication(LocalReplicationDescriptor replicationDescriptor) {
        removeReplication(replicationDescriptor, localReplications);
    }

    @Override
    public String getServerUrlForEmail() {
        String serverUrl = "";
        if (mailServer != null) {
            String artifactoryUrl = mailServer.getArtifactoryUrl();
            if (StringUtils.isNotBlank(artifactoryUrl)) {
                serverUrl = artifactoryUrl;
            }
        }

        if (StringUtils.isBlank(serverUrl) && StringUtils.isNotBlank(urlBase)) {
            serverUrl = urlBase;
        }

        if (StringUtils.isNotBlank(serverUrl) && !serverUrl.endsWith("/")) {
            serverUrl += "/";
        }

        return serverUrl;
    }

    @Override
    public GcConfigDescriptor getGcConfig() {
        return gcConfig;
    }

    @Override
    public void setGcConfig(GcConfigDescriptor gcConfig) {
        this.gcConfig = gcConfig;
    }

    @Override
    public CleanupConfigDescriptor getCleanupConfig() {
        return cleanupConfig;
    }

    @Override
    public void setCleanupConfig(CleanupConfigDescriptor cleanupConfigDescriptor) {
        this.cleanupConfig = cleanupConfigDescriptor;
    }

    @Override
    public QuotaConfigDescriptor getQuotaConfig() {
        return quotaConfig;
    }

    @Override
    public void setQuotaConfig(QuotaConfigDescriptor descriptor) {
        this.quotaConfig = descriptor;
    }

    @Override
    public Map<String, LocalReplicationDescriptor> getLocalReplicationsMap() {
        Map<String, LocalReplicationDescriptor> localReplicationsMap = Maps.newHashMap();
        for (LocalReplicationDescriptor localReplication : localReplications) {
            localReplicationsMap.put(localReplication.getRepoKey(), localReplication);
        }

        return localReplicationsMap;
    }

    @Override
    public Map<String, LocalReplicationDescriptor> getSingleReplicationPerRepoMap() {
        Map<String, LocalReplicationDescriptor> localReplicationsMap = Maps.newHashMap();
        for (LocalReplicationDescriptor localReplication : localReplications) {
                localReplicationsMap.put(localReplication.getRepoKey(), localReplication);
        }
        return localReplicationsMap;
    }

    @Override
    public Map<String, LocalReplicationDescriptor> getLocalReplicationsPerRepoMap(String repoName) {
        Map<String, LocalReplicationDescriptor> localReplicationsMap = new HashMap();
        for (LocalReplicationDescriptor localReplication : localReplications) {
            if (localReplication.getRepoKey().equals(repoName)) {
                localReplicationsMap.put(localReplication.getUrl(), localReplication);
            }
        }
        return localReplicationsMap;
    }

    @Override
    public List<String> getLocalReplicationsUniqueKeyForProperty(String repoName) {
        List<String> localReplicationsList = new ArrayList<>();
        for (LocalReplicationDescriptor localReplication : localReplications) {
            if (localReplication.getRepoKey().equals(repoName)) {
                String uniqueKey = localReplication.getUrl().replaceAll("^(http|https)://", "_").replaceAll("/|:", "_");
                localReplicationsList.add(uniqueKey);
            }
        }
        return localReplicationsList;
    }

    @Override
    public CleanupConfigDescriptor getVirtualCacheCleanupConfig() {
        return virtualCacheCleanupConfig;
    }

    @Override
    public void setVirtualCacheCleanupConfig(CleanupConfigDescriptor virtualCacheCleanupConfig) {
        this.virtualCacheCleanupConfig = virtualCacheCleanupConfig;
    }

    private <T extends ReplicationBaseDescriptor> void addReplication(T replicationDescriptor,
            List<T> replications) {
        if (replications.contains(replicationDescriptor)) {
            throw new AlreadyExistsException("Replication for '" + replicationDescriptor.getRepoKey() +
                    "' already exists");
        }
        replications.add(replicationDescriptor);
    }

    /**
     * update if exist / add (new) local replication descriptor
     *
     * @param replicationDescriptor - new or update local replication descriptor
     * @param replications          - all replication descriptors
     * @param <T>
     */
    private <T extends ReplicationBaseDescriptor> void addLocalReplication(T replicationDescriptor,
            List<T> replications) {
        if (replications.contains(replicationDescriptor)) {
            replications.remove(replicationDescriptor);
        }
        replications.add(replicationDescriptor);
    }

    private <T extends ReplicationBaseDescriptor> T getReplication(String replicatedRepoKey, List<T> replications) {
        if (StringUtils.isNotBlank(replicatedRepoKey)) {
            for (T replication : replications) {

                if (replicatedRepoKey.equals(replication.getRepoKey())) {
                    return replication;
                }
            }
        }

        return null;
    }

    /**
     * get specific Local Replication based on replicate repo key and repo url
     *
     * @param replicatedRepoKey - repository key
     * @param replicateRepoUrl  - repository url
     * @param replications      - all replication in artifactory
     * @param <T>
     * @return
     */
    private <T extends ReplicationBaseDescriptor> T getSpecificLocalReplication(String replicatedRepoKey,
            String replicateRepoUrl, List<T> replications) {
        if (StringUtils.isNotBlank(replicatedRepoKey)) {
            for (T replication : replications) {

                if (replicatedRepoKey.equals(replication.getRepoKey()) && replicateRepoUrl.equals(
                        ((LocalReplicationDescriptor) replication).getUrl())) {
                    return replication;
                }
            }
        }

        return null;
    }

    /**
     * get specific Local Replication based on replicate repo key and repo url
     *
     * @param replicatedRepoKey - repository key
     * @param replications      - all replication in artifactory
     * @return
     */
    private <T extends ReplicationBaseDescriptor> T get1stEnableLocalReplication(String replicatedRepoKey,
            List<T> replications) {
        if (StringUtils.isNotBlank(replicatedRepoKey)) {
            for (T replication : replications) {
                if (replicatedRepoKey.equals(replication.getRepoKey()) && replication.isEnabled()) {
                    return replication;
                }
            }
        }
        return null;
    }
    /**
     * get specific Local Replication based on replicate repo key and repo url
     *
     * @param replicatedRepoKey - repository key
     * @param replications      - all replication in artifactory
     * @return
     */
    private <T extends ReplicationBaseDescriptor> int getNumOfActiveLocalReplication(String replicatedRepoKey,
            List<T> replications) {
        int replicationCounter = 0;
        if (StringUtils.isNotBlank(replicatedRepoKey)) {
            for (T replication : replications) {
                if (replicatedRepoKey.equals(replication.getRepoKey()) && replication.isEnabled()) {
                    replicationCounter++;
                }
            }
        }
        return replicationCounter;
    }
    private <T extends ReplicationBaseDescriptor> void removeReplication(T replicationDescriptor,
            List<T> replications) {
        replications.remove(replicationDescriptor);
    }

    @Override
    public ExternalProvidersDescriptor getExternalProvidersDescriptor() {
        return externalProviders;
    }

    @Override
    public void setExternalProvidersDescriptor(ExternalProvidersDescriptor externalProvidersDescriptor) {
        this.externalProviders = externalProvidersDescriptor;
    }

    @Override
    public BintrayConfigDescriptor getBintrayConfig() {
        return bintrayConfig;
    }

    @Override
    public void setBintrayConfig(BintrayConfigDescriptor bintrayConfigDescriptor) {
        this.bintrayConfig = bintrayConfigDescriptor;
    }

    @Override
    public List<LocalReplicationDescriptor> getMultiLocalReplications(String repoKey) {
        return getMultiLocalReplications(repoKey,localReplications);
    }

    private <T extends ReplicationBaseDescriptor> List<T> getMultiLocalReplications(String replicatedRepoKey,List<T> replications) {
        List<T> localReplicationList = new ArrayList<>();
        if (StringUtils.isNotBlank(replicatedRepoKey)) {
            for (T replication : replications) {
                if (replicatedRepoKey.equals(replication.getRepoKey()))
                localReplicationList.add(replication);
            }
        }
        return localReplicationList;
    }
}