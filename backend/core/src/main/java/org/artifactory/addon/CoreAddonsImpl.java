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

package org.artifactory.addon;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.http.HttpStatus;
import org.artifactory.addon.blackduck.BlackDuckAddon;
import org.artifactory.addon.blackduck.ExternalComponentInfo;
import org.artifactory.addon.bower.BowerAddon;
import org.artifactory.addon.bower.BowerMetadataInfo;
import org.artifactory.addon.build.ArtifactBuildAddon;
import org.artifactory.addon.crowd.CrowdAddon;
import org.artifactory.addon.crowd.CrowdExtGroup;
import org.artifactory.addon.debian.DebianAddon;
import org.artifactory.addon.filteredresources.FilteredResourcesAddon;
import org.artifactory.addon.gems.ArtifactGemsInfo;
import org.artifactory.addon.gems.GemsAddon;
import org.artifactory.addon.ha.HaCommonAddon;
import org.artifactory.addon.ha.message.HaMessage;
import org.artifactory.addon.ha.message.HaMessageTopic;
import org.artifactory.addon.ha.semaphore.JVMSemaphoreWrapper;
import org.artifactory.addon.ha.semaphore.SemaphoreWrapper;
import org.artifactory.addon.ldapgroup.LdapUserGroupAddon;
import org.artifactory.addon.license.LicenseStatus;
import org.artifactory.addon.license.LicensesAddon;
import org.artifactory.addon.npm.NpmAddon;
import org.artifactory.addon.npm.NpmMetadataInfo;
import org.artifactory.addon.nuget.UiNuGetAddon;
import org.artifactory.addon.oauth.OAuthSsoAddon;
import org.artifactory.addon.pypi.PypiAddon;
import org.artifactory.addon.pypi.PypiPkgMetadata;
import org.artifactory.addon.replication.LocalReplicationSettings;
import org.artifactory.addon.replication.RemoteReplicationSettings;
import org.artifactory.addon.replication.ReplicationAddon;
import org.artifactory.addon.saml.SamlSsoAddon;
import org.artifactory.addon.search.ArtifactSearchAddon;
import org.artifactory.addon.smartrepo.SmartRepoAddon;
import org.artifactory.addon.watch.ArtifactWatchAddon;
import org.artifactory.addon.webstart.ArtifactWebstartAddon;
import org.artifactory.addon.yum.ArtifactRpmMetadata;
import org.artifactory.addon.yum.YumAddon;
import org.artifactory.api.bintray.docker.BintrayPushRequest;
import org.artifactory.api.build.GeneralBuild;
import org.artifactory.api.build.ModuleArtifact;
import org.artifactory.api.build.ModuleDependency;
import org.artifactory.api.build.PublishedModule;
import org.artifactory.api.build.diff.BuildsDiffBaseFileModel;
import org.artifactory.api.build.diff.BuildsDiffPropertyModel;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.common.MoveMultiStatusHolder;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.governance.BlackDuckApplicationInfo;
import org.artifactory.api.governance.GovernanceRequestInfo;
import org.artifactory.api.license.LicenseInfo;
import org.artifactory.api.license.LicensesInfo;
import org.artifactory.api.license.ModuleLicenseModel;
import org.artifactory.api.request.ArtifactoryResponse;
import org.artifactory.api.rest.build.diff.BuildsDiff;
import org.artifactory.api.rest.compliance.FileComplianceInfo;
import org.artifactory.api.rest.replication.ReplicationStatus;
import org.artifactory.api.search.ItemSearchResult;
import org.artifactory.api.search.SavedSearchResults;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.build.ArtifactoryBuildArtifact;
import org.artifactory.build.BuildRun;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.MutableStatusHolder;
import org.artifactory.config.ConfigurationException;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.external.BlackDuckSettingsDescriptor;
import org.artifactory.descriptor.property.Property;
import org.artifactory.descriptor.property.PropertySet;
import org.artifactory.descriptor.replication.LocalReplicationDescriptor;
import org.artifactory.descriptor.replication.RemoteReplicationDescriptor;
import org.artifactory.descriptor.replication.ReplicationBaseDescriptor;
import org.artifactory.descriptor.repo.HttpRepoDescriptor;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RealRepoDescriptor;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.descriptor.repo.RepoLayout;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;
import org.artifactory.descriptor.security.ldap.LdapSetting;
import org.artifactory.descriptor.security.ldap.SearchPattern;
import org.artifactory.descriptor.security.ldap.group.LdapGroupPopulatorStrategies;
import org.artifactory.descriptor.security.ldap.group.LdapGroupSetting;
import org.artifactory.descriptor.security.sso.CrowdSettings;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.fs.FileInfo;
import org.artifactory.fs.RepoResource;
import org.artifactory.fs.StatsInfo;
import org.artifactory.fs.WatchersInfo;
import org.artifactory.md.Properties;
import org.artifactory.nuget.NuMetaData;
import org.artifactory.repo.HttpRepo;
import org.artifactory.repo.LocalRepo;
import org.artifactory.repo.RemoteRepo;
import org.artifactory.repo.Repo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.repo.service.mover.MoverConfig;
import org.artifactory.repo.virtual.VirtualRepo;
import org.artifactory.request.ArtifactoryRequest;
import org.artifactory.request.InternalRequestContext;
import org.artifactory.request.Request;
import org.artifactory.resource.FileResource;
import org.artifactory.resource.ResourceStreamHandle;
import org.artifactory.resource.UnfoundRepoResource;
import org.artifactory.sapi.common.ExportSettings;
import org.artifactory.sapi.common.ImportSettings;
import org.artifactory.sapi.fs.VfsItem;
import org.artifactory.schedule.Task;
import org.artifactory.security.MutableUserInfo;
import org.artifactory.security.UserGroupInfo;
import org.artifactory.storage.db.servers.model.ArtifactoryServer;
import org.artifactory.storage.fs.lock.FsItemsVault;
import org.artifactory.storage.fs.lock.FsItemsVaultCacheImpl;
import org.artifactory.storage.fs.lock.map.JVMLockingMap;
import org.artifactory.storage.fs.lock.map.LockingMap;
import org.artifactory.storage.fs.lock.provider.JVMLockProvider;
import org.artifactory.storage.fs.lock.provider.LockProvider;
import org.artifactory.util.HttpUtils;
import org.artifactory.util.Pair;
import org.artifactory.util.RepoLayoutUtils;
import org.jfrog.build.api.Build;
import org.jfrog.build.api.Dependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.security.Key;
import java.security.KeyStore;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

/**
 * Default implementation of the core-related addon factories.
 *
 * @author Yossi Shaul
 */
@Component
public class CoreAddonsImpl implements WebstartAddon, LdapGroupAddon, LicensesAddon, PropertiesAddon, LayoutsCoreAddon,
        FilteredResourcesAddon, ReplicationAddon, YumAddon, NuGetAddon, RestCoreAddon, CrowdAddon, BlackDuckAddon,
        GemsAddon, HaAddon, NpmAddon, BowerAddon, DebianAddon, PypiAddon, DockerAddon, VagrantAddon, GitLfsAddon,
        ArtifactWatchAddon, ArtifactBuildAddon, UiNuGetAddon, LdapUserGroupAddon,
        ArtifactWebstartAddon, ArtifactSearchAddon, SamlSsoAddon, OAuthSsoAddon, SmartRepoAddon {

    private static final Logger log = LoggerFactory.getLogger(CoreAddonsImpl.class);

    @Override
    public boolean isDefault() {
        return true;
    }

    @Override
    public VirtualRepo createVirtualRepo(InternalRepositoryService repoService, VirtualRepoDescriptor descriptor) {
        return new VirtualRepo(descriptor, repoService);
    }

    @Override
    public void importKeyStore(ImportSettings settings) {
        // do nothing
    }

    @Override
    public void exportKeyStore(ExportSettings exportSettings) {
        // do nothing
    }

    @Override
    public void addExternalGroups(String userName, Set<UserGroupInfo> groups) {
        // nop
    }

    @Override
    public Set<CrowdExtGroup> findCrowdExtGroups(String username, CrowdSettings currentCrowdSettings) {
        return null;
    }

    @Override
    public void testCrowdConnection(CrowdSettings crowdSettings) throws Exception {
    }

    @Override
    public void populateGroups(DirContextOperations dirContextOperations, MutableUserInfo userInfo) {
        // do nothing
    }

    @Override
    public void populateGroups(String dn, MutableUserInfo info) {
        // do nothing
    }

    @Override
    public List<LdapSetting> getEnabledLdapSettings() {
        CentralConfigDescriptor descriptor = ContextHelper.get().beanForType(
                CentralConfigService.class).getDescriptor();
        List<LdapSetting> enabledLdapSettings = descriptor.getSecurity().getEnabledLdapSettings();
        if (enabledLdapSettings != null && !enabledLdapSettings.isEmpty()) {
            return Lists.newArrayList(enabledLdapSettings.get(0));
        }
        return Lists.newArrayList();
    }

    @Override
    public List<FilterBasedLdapUserSearch> getLdapUserSearches(ContextSource ctx, LdapSetting settings) {
        SearchPattern searchPattern = settings.getSearch();
        String searchBase = searchPattern.getSearchBase();
        if (searchBase == null) {
            searchBase = "";
        }
        ArrayList<FilterBasedLdapUserSearch> result = new ArrayList<>();
        FilterBasedLdapUserSearch userSearch = new FilterBasedLdapUserSearch(searchBase,
                searchPattern.getSearchFilter(), (BaseLdapPathContextSource) ctx);
        userSearch.setSearchSubtree(searchPattern.isSearchSubTree());
        result.add(userSearch);
        return result;
    }

    @Override
    public void performOnBuildArtifacts(Build build) {
        // NOP
    }

    @Override
    public void addPropertySetToRepository(RealRepoDescriptor descriptor) {
        // NOP
    }

    @Override
    public void importLicenses(ImportSettings settings) {
        // NOP
    }

    @Override
    public void exportLicenses(ExportSettings exportSettings) {
        // nop
    }

    @Override
    public List<ModuleLicenseModel> findLicensesInRepos(Set<String> repoKeys, LicenseStatus status) {
        return Lists.newArrayList();
    }

    @Override
    public LicensesInfo getArtifactsLicensesInfo() {
        return null;
    }

    @Override
    public String writeLicenseXML(LicensesInfo licensesInfo) {
        return null;
    }

    @Override
    public void addLicenseInfo(LicenseInfo licensesInfo) {

    }

    @Override
    public void updateLicenseInfo(LicenseInfo licensesInfo) {
    }

    @Override
    public void deleteLicenseInfo(LicenseInfo licensesInfo) {

    }

    @Override
    public LicenseInfo getLicenseByName(String licenseName) {
        return new LicenseInfo();
    }

    @Override
    public void reloadLicensesCache() {
    }

    @Override
    public Multimap<RepoPath, ModuleLicenseModel> populateLicenseInfoSynchronously(Build build, boolean autoDiscover) {
        return HashMultimap.create();
    }

    @Override
    public String generateLicenseCsv(Collection<ModuleLicenseModel> models) {
        return null;
    }

    @Override
    public boolean setLicensePropsOnPath(RepoPath path, Set<LicenseInfo> licenses) {
        return false;
    }

    @Override
    public Set<LicenseInfo> scanPathForLicenses(RepoPath path) {
        return Sets.newHashSet();
    }

    @Override
    public Set<LicenseInfo> getPathLicensesByProps(RepoPath path) {
        return null;
    }

    @Override
    public Properties getProperties(RepoPath repoPath) {
        return (Properties) InfoFactoryHolder.get().createProperties();
    }

    @Override
    public Map<RepoPath, Properties> getProperties(Set<RepoPath> repoPaths) {
        return Maps.newHashMap();
    }

    @Override
    public void deleteProperty(RepoPath repoPath, String property) {
        // nop
    }

    @Override
    public void addProperty(RepoPath repoPath, PropertySet propertySet, Property property, String... values) {
        //nop
    }

    @Override
    public void setProperties(RepoPath repoPath, Properties properties) {
        //nop
    }

    @Override
    public RepoResource assembleDynamicMetadata(InternalRequestContext context, RepoPath metadataRepoPath) {
        return new FileResource(ContextHelper.get().getRepositoryService().getFileInfo(metadataRepoPath));
    }

    @Override
    public void updateRemoteProperties(Repo repo, RepoPath repoPath) {
        // nop
    }

    @Override
    public boolean isFilteredResourceFile(RepoPath repoPath) {
        return false;
    }

    @Override
    public boolean isFilteredResourceFile(RepoPath repoPath, Properties props) {
        return false;
    }

    @Override
    public RepoResource getFilteredResource(Request request, FileInfo fileInfo, InputStream fileInputStream) {
        return new UnfoundRepoResource(fileInfo.getRepoPath(),
                "Creation of a filtered resource requires the Properties add-on.", HttpStatus.SC_FORBIDDEN);
    }

    @Override
    public RepoResource getZipResource(Request request, FileInfo fileInfo, InputStream stream) {
        return new UnfoundRepoResource(fileInfo.getRepoPath(),
                "Direct resource download from zip requires the Filtered resources add-on.", HttpStatus.SC_FORBIDDEN);
    }

    @Override
    public ResourceStreamHandle getZipResourceHandle(RepoResource resource, InputStream stream) {
        throw new UnsupportedOperationException(
                "Direct resource download from zip requires the Filtered resources add-on.");
    }

    @Override
    public String getGeneratedSettingsUsernameTemplate() {
        return "${security.getCurrentUsername()}";
    }

    @Override
    public String getGeneratedSettingsUserCredentialsTemplate(boolean escape) {
        AuthorizationService authorizationService = ContextHelper.get().getAuthorizationService();

        if (authorizationService.isAnonymous() || authorizationService.isTransientUser()) {
            return "";
        }

        StringBuilder credentialsTemplateBuilder = new StringBuilder("${security.getE");
        if (escape) {
            credentialsTemplateBuilder.append("scapedE");
        }
        return credentialsTemplateBuilder.append(
                "ncryptedPassword()!\"*** Insert encrypted password here ***\"}").toString();
    }

    @Override
    public String filterResource(Request request, Properties contextProperties, Reader reader) throws Exception {
        try {
            return IOUtils.toString(reader);
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    @Override
    public void toggleResourceFilterState(RepoPath repoPath, boolean filtered) {
    }

    @Override
    public void assertLayoutConfigurationsBeforeSave(CentralConfigDescriptor newDescriptor) {
        List<RepoLayout> repoLayouts = newDescriptor.getRepoLayouts();
        if ((repoLayouts == null) || repoLayouts.isEmpty()) {
            throw new ConfigurationException("Could not find any repository layouts.");
        }

        if (repoLayouts.size() != 10) {
            throw new ConfigurationException("There should be 10 default repository layouts.");
        }

        assertLayoutsExistsAndEqual(repoLayouts, RepoLayoutUtils.MAVEN_2_DEFAULT, RepoLayoutUtils.IVY_DEFAULT,
                RepoLayoutUtils.GRADLE_DEFAULT, RepoLayoutUtils.MAVEN_1_DEFAULT);
    }

    @Override
    public boolean canCrossLayouts(RepoLayout source, RepoLayout target) {
        return false;
    }

    @Override
    public void performCrossLayoutMoveOrCopy(MoveMultiStatusHolder status, MoverConfig moverConfig,
                                             LocalRepo sourceRepo, LocalRepo targetLocalRepo, VfsItem sourceItem) {
        throw new UnsupportedOperationException(
                "Cross layout move or copy operations require the Repository Layouts addon.");
    }

    @Override
    public String translateArtifactPath(RepoLayout sourceRepoLayout, RepoLayout targetRepoLayout, String path) {
        return translateArtifactPath(sourceRepoLayout, targetRepoLayout, path, null);
    }

    @Override
    public String translateArtifactPath(RepoLayout sourceRepoLayout, RepoLayout targetRepoLayout, String path,
                                        @Nullable BasicStatusHolder multiStatusHolder) {
        return path;
    }

    private void assertLayoutsExistsAndEqual(List<RepoLayout> repoLayouts, RepoLayout... expectedLayouts) {
        for (RepoLayout expectedLayout : expectedLayouts) {
            assertLayoutExistsAndEqual(repoLayouts, expectedLayout);
        }
    }

    private void assertLayoutExistsAndEqual(List<RepoLayout> repoLayouts, RepoLayout expectedLayout) {

        if (!repoLayouts.contains(expectedLayout)) {
            throw new ConfigurationException("Could not find the default repository layout: " +
                    expectedLayout.getName());
        }

        RepoLayout existingLayoutConfig = repoLayouts.get(repoLayouts.indexOf(expectedLayout));
        if (!EqualsBuilder.reflectionEquals(existingLayoutConfig, expectedLayout)) {
            throw new ConfigurationException("The configured repository layout '" + expectedLayout.getName() +
                    "' is different from the default configuration.");
        }
    }

    @Override
    public BasicStatusHolder performRemoteReplication(RemoteReplicationSettings settings) {
        return getReplicationRequiredStatusHolder();
    }

    @Override
    public BasicStatusHolder performLocalReplication(LocalReplicationSettings settings) {
        return getReplicationRequiredStatusHolder();
    }

    @Override
    public void scheduleImmediateLocalReplicationTask(LocalReplicationDescriptor replicationDescriptor,
                                                      BasicStatusHolder statusHolder) {
        statusHolder.error("Error: the replication addon is required for this operation.", HttpStatus.SC_BAD_REQUEST,
                log);
    }

    @Override
    public void scheduleImmediateRemoteReplicationTask(RemoteReplicationDescriptor replicationDescriptor,
                                                       BasicStatusHolder statusHolder) {
        statusHolder.error("Error: the replication addon is required for this operation.", HttpStatus.SC_BAD_REQUEST,
                log);
    }

    @Override
    public ReplicationStatus getReplicationStatus(RepoPath repoPath) {
        return null;
    }

    @Override
    public void offerLocalReplicationDeploymentEvent(RepoPath repoPath) {
    }

    @Override
    public void offerLocalReplicationMkDirEvent(RepoPath repoPath) {
    }

    @Override
    public void offerLocalReplicationDeleteEvent(RepoPath repoPath) {
    }

    @Override
    public void offerLocalReplicationPropertiesChangeEvent(RepoPath repoPath) {
    }

    @Override
    public void validateTargetIsDifferentInstance(ReplicationBaseDescriptor descriptor,
                                                  RealRepoDescriptor repoDescriptor) throws IOException {
    }

    @Override
    public boolean isRepoExistInCache(RepoPath repoPath) {
        return false;
    }

    @Override
    public void validateTargetLicense(ReplicationBaseDescriptor descriptor, RealRepoDescriptor repoDescriptor,
                                      int numOfReplicationConfigured) {
    }

    @Override
    public void cleanupLocalReplicationProperties(LocalReplicationDescriptor replication) {

    }

    private BasicStatusHolder getReplicationRequiredStatusHolder() {
        BasicStatusHolder multiStatusHolder = new BasicStatusHolder();
        multiStatusHolder.error("Error: the replication addon is required for this operation.",
                HttpStatus.SC_BAD_REQUEST, log);
        return multiStatusHolder;
    }

    @Override
    public void requestAsyncRepositoryYumMetadataCalculation(RepoPath... repoPaths) {
    }

    @Override
    public void requestAsyncRepositoryYumMetadataCalculation(LocalRepoDescriptor repo) {
    }

    @Override
    public void requestYumMetadataCalculation(LocalRepoDescriptor repo) {
    }

    @Override
    public ArtifactRpmMetadata getRpmMetadata(FileInfo fileInfo) {
        return null;
    }

    @Override
    public void recalculateAll(LocalRepoDescriptor localRepoDescriptor, String password, boolean delayed) {
    }

    @Override
    public String getPublicKeyDownloadTarget() {
        return "";
    }

    @Override
    public void onInstallKey(String key, boolean isPublic) throws Exception {

    }

    @Override
    public void removeKey(boolean isPublic) {
    }

    @Override
    public boolean hasPrivateKey() {
        return false;
    }

    @Override
    public boolean hasPublicKey() {
        return false;
    }

    @Override
    public boolean verifyPassPhrase(String phrase) {
        return false;
    }

    @Override
    public void savePassPhrase(String password) {

    }

    @Override
    public void extractNuPkgInfo(FileInfo fileInfo, MutableStatusHolder statusHolder, boolean addToCache) {
    }

    @Override
    public void extractNuPkgInfoSynchronously(FileInfo file, MutableStatusHolder statusHolder) {
    }

    @Override
    public void addNuPkgToRepoCache(RepoPath repoPath, Properties properties) {
    }

    @Override
    public void addNuPkgToRepoCacheAsync(RepoPath repoPath, Properties properties) {

    }

    @Override
    public void removeNuPkgFromRepoCache(String repoKey, String packageId, String packageVersion) {
    }

    @Override
    public void internalAddNuPkgToRepoCache(RepoPath repoPath, Properties properties) {
    }

    @Override
    public void internalRemoveNuPkgFromRepoCache(String repoKey, String packageId, String packageVersion) {
    }

    @Nonnull
    @Override
    public RemoteRepo createRemoteRepo(InternalRepositoryService repoService, RemoteRepoDescriptor repoDescriptor,
                                       boolean offlineMode, RemoteRepo oldRemoteRepo) {
        return new HttpRepo((HttpRepoDescriptor) repoDescriptor, repoService, offlineMode, oldRemoteRepo);
    }

    @Override
    public void deployArchiveBundle(ArtifactoryRequest request, ArtifactoryResponse response, LocalRepo repo)
            throws IOException {
        response.sendError(HttpStatus.SC_BAD_REQUEST, "This REST API is available only in Artifactory Pro.", log);
    }

    @Override
    public InternalRequestContext getDynamicVersionContext(Repo repo, InternalRequestContext originalRequestContext,
                                                           boolean isRemote) {
        return originalRequestContext;
    }

    @Override
    public boolean isCrowdAuthenticationSupported(Class<?> authentication) {
        return false;
    }

    @Override
    public Authentication authenticateCrowd(Authentication authentication) {
        throw new UnsupportedOperationException("This feature requires the Crowd SSO addon.");
    }

    @Override
    public boolean findUser(String userName) {
        return false;
    }

    @Override
    public FileComplianceInfo getExternalInfoFromMetadata(RepoPath repoPath) {
        throw new UnsupportedOperationException("This feature requires the Black Duck addon.");
    }

    @Override
    public void performBlackDuckOnBuildArtifacts(Build build) {
        // NOP
    }

    @Override
    public void testConnection(BlackDuckSettingsDescriptor blackDuckSettingsDescriptor) throws Exception {

    }

    @Override
    public ExternalComponentInfo getBlackduckInfo(RepoPath repoPath) {
        return null;
    }

    @Override
    public boolean queryCodeCenterForPath(RepoPath path) {
        return false;
    }

    @Override
    public String getComponentExternalIdFromProperty(RepoPath repoPath) {
        return null;
    }

    @Override
    public String getComponentIdFromProperty(RepoPath repoPath) {
        return null;
    }

    @Override
    public void clearComponentIdProperty(RepoPath repoPath) {

    }

    @Override
    public void setComponentExternalIdProperty(RepoPath repoPath, String updatedComponentId) {

    }

    @Override
    public boolean isEnableIntegration() {
        return false;
    }

    @Override
    public Collection<GovernanceRequestInfo> getGovernanceRequestInfos(Build build, String appName, String appVersion, Set<String> scopes) {
        return null;
    }

    @Override
    public BlackDuckApplicationInfo blackDuckApplicationInfo(String appInfo, String versionInfo) {
        return null;
    }

    @Override
    public String updateRequest(Build build, GovernanceRequestInfo requestInfo) {
        return null;
    }

    @Override
    public boolean isSupportedPackageType(RepoPath path) {
        return false;
    }

    @Override
    public Set<String> getPathLicensesFromProperties(RepoPath path) {
        return null;
    }

    @Override
    public String getLicenseUrl(String license) {
        return "";
    }

    @Override
    public void reindexAsync(String repoKey) {

    }

    @Override
    public NpmMetadataInfo getNpmMetaDataInfo(FileInfo fileInfo) {
        return null;
    }

    @Override
    public void afterRepoInit(String repoKey) {
    }

    @Override
    public ArtifactGemsInfo getGemsInfo(String repoKey, String path) {
        return null;
    }

    @Override
    public void requestAsyncReindexNuPkgs(String repoKey) {
    }

    @Override
    public boolean isHaEnabled() {
        return false;
    }

    @Override
    public boolean isPrimary() {
        return true;
    }

    @Override
    public boolean isHaConfigured() {
        try {
            return ArtifactoryHome.get().isHaConfigured();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void notify(HaMessageTopic haMessageTopic, HaMessage haMessage) {
    }

    @Override
    public String getHostId() {
        return HttpUtils.getHostId();
    }

    @Override
    public void updateArtifactoryServerRole() {
    }

    @Override
    public boolean deleteArtifactoryServer(String serverId) {
        return false;
    }

    @Override
    public boolean artifactoryServerHasHeartbeat(ArtifactoryServer artifactoryServer) {
        return false;
    }

    @Override
    public void propagateTaskToPrimary(Task Task) {
    }

    @Override
    public LockingMap getLockingMap() {
        return new JVMLockingMap();
    }

    @Override
    public LockingMap getLockingMap(String mapName) {
        return new JVMLockingMap();
    }

    @Override
    public void init() {
    }

    @Override
    public FsItemsVault getFsItemVault() {
        LockProvider lockProvider = new JVMLockProvider();
        return new FsItemsVaultCacheImpl(lockProvider);
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
    public void addNpmPackage(FileInfo info) {

    }

    @Override
    public void handleAddAfterCommit(FileInfo info) {

    }

    @Override
    public void removeNpmPackage(FileInfo info) {

    }

    @Override
    public void reindex(LocalRepoDescriptor descriptor, boolean async) {

    }

    @Override
    public PypiPkgMetadata getPypiMetadata(RepoPath packagePath) {
        return null;
    }

    @Override
    public boolean isPypiFile(FileInfo fileInfo) {
        return false;
    }

    @Override
    public void addBowerPackage(FileInfo info) {

    }

    @Override
    public void removeBowerPackage(FileInfo info) {

    }

    @Override
    public boolean isBowerFile(String filePath) {
        return false;
    }

    @Override
    public void requestAsyncReindexBowerPackages(String repoKey) {

    }

    @Override
    public BowerMetadataInfo getBowerMetadata(FileInfo fileInfo) {
        return null;
    }

    @Override
    public void pushTagToBintray(String repoKey, BintrayPushRequest request) {
    }

    @Override
    public Map<RepoPath, WatchersInfo> getAllWatchers(RepoPath repoPath) {
        return null;
    }

    @Override
    public void removeWatcher(RepoPath repoPath, String watchUser) {

    }

    @Override
    public void addWatcher(RepoPath repoPath, String watcherUsername) {

    }

    @Override
    public boolean isUserWatchingRepo(RepoPath repoPath, String userName) {
        return false;
    }

    @Override
    public Pair<RepoPath, WatchersInfo> getNearestWatchDefinition(RepoPath repoPath, String userName) {
        return null;
    }

    @Override
    public WatchersInfo getWatchers(RepoPath repoPath) {
        return null;
    }

    @Override
    public Set<ArtifactoryBuildArtifact> getBuildArtifactsFileInfos(Build build) {
        return null;
    }

    @Override
    public Map<Dependency, FileInfo> getBuildDependenciesFileInfos(Build build) {
        return null;
    }

    @Override
    public Set<BuildRun> getLatestBuildsPaging(String offset, String orderBy, String direction, String limit) {
        return null;
    }

    @Override
    public List<GeneralBuild> getBuildForNamePaging(String buildName, String orderBy, String direction, String offset, String limit) throws SQLException {
        return null;
    }

    @Override
    public int getBuildForNameTotalCount(String buildName) throws SQLException {
        return 0;
    }

    @Override
    public Build getBuild(BuildRun buildRun) {
        return null;
    }

    @Override
    public BuildRun getBuildRun(String buildName, String buildNumber, String buildStarted) {
        return null;
    }

    @Override
    public Build getLatestBuildByNameAndNumber(String buildName, String BuildNumber) {
        return null;
    }

    @Override
    public List<PublishedModule> getPublishedModules(String buildName, String date, String orderBy, String direction, String offset, String limit) {
        return null;
    }

    @Override
    public int getPublishedModulesCounts(String buildName, String date) {
        return 0;
    }

    @Override
    public List<ModuleArtifact> getModuleArtifact(String buildName ,String buildNumber, String moduleId, String date, String orderBy, String direction, String offset, String limit) {
        return new ArrayList<>();
    }

    @Override
    public int getModuleArtifactCount(String buildNumber, String moduleId, String date) {
        return 0;
    }

    @Override
    public List<ModuleDependency> getModuleDependency(String buildNumber, String moduleId, String date, String orderBy, String direction, String offset, String limit) {
        return null;
    }

    @Override
    public int getModuleDependencyCount(String buildNumber, String moduleId, String date) {
        return 0;
    }

    @Override
    public void deleteAllBuilds(String name) {

    }

    @Override
    public BuildsDiff getBuildsDiff(Build firstBuild, Build secondBuild, String baseStorageInfoUri) {
        return null;
    }

    @Override
    public List<BuildsDiffBaseFileModel> compareArtifacts(Build build, Build secondBuild) {
        return null;
    }

    @Override
    public List<BuildsDiffBaseFileModel> compareDependencies(Build build, Build secondBuild) {
        return null;
    }

    @Override
    public List<BuildsDiffPropertyModel> compareProperties(Build build, Build secondBuild) {
        return null;
    }

    @Override
    public NuMetaData getNutSpecMetaData(RepoPath nuGetRepoPath) {
        return null;
    }

    @Override
    public Set refreshLdapGroups(String userName, LdapGroupSetting ldapGroupSetting, BasicStatusHolder statusHolder) {
        return null;
    }

    @Override
    public int importLdapGroupsToArtifactory(List ldapGroups, LdapGroupPopulatorStrategies strategy) {
        return 0;
    }

    @Override
    public String[] retrieveUserLdapGroups(String userName, LdapGroupSetting ldapGroupSetting) {
        return null;
    }

    @Override
    public KeyStore loadKeyStore(File keyStoreFile, String password) {
        return null;
    }

    @Override
    public Key getAliasKey(KeyStore keyStore, String alias, String password) {
        return null;
    }

    @Override
    public void addKeyPair(File file, String pairName, String keyStorePassword, String alias, String privateKeyPassword) throws IOException {

    }

    @Override
    public boolean keyStoreExist() {
        return false;
    }

    @Override
    public List<String> getKeyPairNames() {
        return null;
    }

    @Override
    public boolean removeKeyPair(String keyPairName) {
        return false;
    }

    @Override
    public void setKeyStorePassword(String password) {

    }

    @Override
    public void removeKeyStorePassword() {
    }

    @Override
    public SavedSearchResults getSearchResults(String name, List<? extends ItemSearchResult> itemSearchResults,
            boolean completeVersion) {
        return null;
    }

    @Override
    public String getSamlLoginIdentityProviderUrl(HttpServletRequest request) {
        return null;
    }

    @Override
    public String getOAuthLoginPageUrl(HttpServletRequest request) {
        return null;
    }

    @Override
    public void createCertificate(String certificate) throws Exception {

    }

    @Override
    public Boolean isSamlAuthentication(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        return false;
    }

    @Override
    public boolean supportRemoteStats() {
        return false;
    }

    @Override
    public void fileDownloadedRemotely(StatsInfo statsInfo, String remoteHost, RepoPath repoPath) {

    }
}

