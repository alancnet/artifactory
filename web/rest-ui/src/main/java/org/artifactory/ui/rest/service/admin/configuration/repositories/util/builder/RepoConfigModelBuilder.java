package org.artifactory.ui.rest.service.admin.configuration.repositories.util.builder;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.p2.P2Repo;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.descriptor.property.PropertySet;
import org.artifactory.descriptor.replication.LocalReplicationDescriptor;
import org.artifactory.descriptor.replication.RemoteReplicationDescriptor;
import org.artifactory.descriptor.repo.*;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.ui.rest.model.admin.configuration.propertysets.PropertySetNameModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.BasicRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.GeneralRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.local.LocalAdvancedRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.local.LocalBasicRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.local.LocalRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteAdvancedRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteBasicRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteCacheRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteNetworkRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.replication.local.LocalReplicationConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.replication.remote.RemoteReplicationConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.typespecific.*;
import org.artifactory.ui.rest.model.admin.configuration.repository.virtual.VirtualAdvancedRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.virtual.VirtualBasicRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.virtual.VirtualRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.virtual.VirtualSelectedRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * Utility class for converting descriptor to model
 *
 * @author Aviad Shikloshi
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class
        RepoConfigModelBuilder {

    @Autowired
    private CentralConfigService centralConfig;

    @Autowired
    RepositoryService repoService;

    @Autowired
    AddonsManager addonsManager;

    /**
     * Populate model configuration from local repository descriptor
     */
    public void populateLocalDescriptorValuesToModel(LocalRepoDescriptor descriptor, LocalRepositoryConfigModel model) {
        GeneralRepositoryConfigModel general = createGeneralConfig(descriptor);
        LocalBasicRepositoryConfigModel basic = createLocalBasicConfig(descriptor);
        LocalAdvancedRepositoryConfigModel advanced = createLocalAdvancedConfig(descriptor);
        TypeSpecificConfigModel typeSpecific = createLocalTypeSpecific(descriptor.getType(), descriptor);

        List<LocalReplicationDescriptor> replicationDescriptors = centralConfig.getDescriptor()
                .getMultiLocalReplications(descriptor.getKey());
        List<LocalReplicationConfigModel> replications = replicationDescriptors.stream()
                .map(this::createLocalReplicationConfig)
                .collect(Collectors.toList());

        model.setGeneral(general);
        model.setBasic(basic);
        model.setAdvanced(advanced);
        model.setTypeSpecific(typeSpecific);
        model.setReplications(replications);
    }

    /**
     * Populate model configuration from remote repository descriptor
     */
    public void populateRemoteRepositoryConfigValuesToModel(HttpRepoDescriptor descriptor,
            RemoteRepositoryConfigModel model) {
        GeneralRepositoryConfigModel general = createGeneralConfig(descriptor);
        model.setGeneral(general);

        RemoteBasicRepositoryConfigModel basic = createRemoteBasicConfig(descriptor);
        model.setBasic(basic);

        RemoteReplicationDescriptor replicationDescriptor = centralConfig.getDescriptor().getRemoteReplication(
                descriptor.getKey());
        if (replicationDescriptor != null) {
            RemoteReplicationConfigModel replication = createRemoteReplicationConfigModel(replicationDescriptor);
            model.setReplications(Lists.newArrayList(replication));
        }
        RemoteAdvancedRepositoryConfigModel advanced = createRemoteAdvancedConfig(descriptor);
        model.setAdvanced(advanced);
        TypeSpecificConfigModel typeSpecific = createRemoteTypeSpecific(descriptor.getType(), descriptor);
        model.setTypeSpecific(typeSpecific);
    }

    /**
     * Populate model configuration from virtual repository descriptor
     */
    public void populateVirtualRepositoryConfigValuesToModel(VirtualRepoDescriptor descriptor,
            VirtualRepositoryConfigModel model) {
        // General
        GeneralRepositoryConfigModel general = new GeneralRepositoryConfigModel();
        general.setRepoKey(descriptor.getKey());
        model.setGeneral(general);

        // Basic
        VirtualBasicRepositoryConfigModel basic = new VirtualBasicRepositoryConfigModel();
        Optional.ofNullable(descriptor.getRepoLayout()).ifPresent(layout -> basic.setLayout(layout.getName()));
        basic.setPublicDescription(descriptor.getDescription());
        basic.setInternalDescription(descriptor.getNotes());
        basic.setExcludesPattern(descriptor.getExcludesPattern());
        basic.setIncludesPattern(descriptor.getIncludesPattern());
        List<RepoDescriptor> repositories = descriptor.getRepositories();
        VirtualRepoResolver resolver = new VirtualRepoResolver(descriptor);
        List<String> resolvedDesc = resolver.getOrderedRepos().stream().map(RealRepoDescriptor::getKey).collect(
                Collectors.toList());
        basic.setResolvedRepositories(resolvedDesc);
        basic.setSelectedRepositories(repositories.stream().map(VirtualSelectedRepository::new).collect(Collectors.toList()));
        model.setBasic(basic);

        // Advanced
        VirtualAdvancedRepositoryConfigModel advanced = new VirtualAdvancedRepositoryConfigModel();
        advanced.setRetrieveRemoteArtifacts(descriptor.isArtifactoryRequestsCanRetrieveRemoteArtifacts());
        model.setAdvanced(advanced);

        // Type specific
        TypeSpecificConfigModel typeSpecific = createVirtualTypeSpecific(descriptor.getType(), descriptor);
        model.setTypeSpecific(typeSpecific);
    }

    private LocalReplicationConfigModel createLocalReplicationConfig(LocalReplicationDescriptor replicationDescriptor) {
        LocalReplicationConfigModel replication = new LocalReplicationConfigModel();
        replication.setUrl(replicationDescriptor.getUrl());
        replication.setCronExp(replicationDescriptor.getCronExp());
        replication.setEnableEventReplication(replicationDescriptor.isEnableEventReplication());
        replication.setEnabled(replicationDescriptor.isEnabled());

        ProxyDescriptor proxyDescriptor = replicationDescriptor.getProxy();
        if (proxyDescriptor != null) {
            replication.setProxy(proxyDescriptor.getKey());
        }
        replication.setSocketTimeout(replicationDescriptor.getSocketTimeoutMillis());
        replication.setSyncDeletes(replicationDescriptor.isSyncDeletes());
        replication.setSyncProperties(replicationDescriptor.isSyncProperties());
        replication.setUsername(replicationDescriptor.getUsername());
        replication.setPassword(replicationDescriptor.getPassword());
        return replication;
    }

    private RemoteReplicationConfigModel createRemoteReplicationConfigModel(
            RemoteReplicationDescriptor replicationDescriptor) {
        RemoteReplicationConfigModel replication = new RemoteReplicationConfigModel();
        replication.setCronExp(replicationDescriptor.getCronExp());
        replication.setEnabled(replicationDescriptor.isEnabled());
        replication.setSyncDeletes(replicationDescriptor.isSyncDeletes());
        replication.setSyncProperties(replicationDescriptor.isSyncProperties());
        return replication;
    }

    private List<PropertySetNameModel> collectPropertySets(List<PropertySet> propertySetsList) {
        return propertySetsList.stream()
                .map(PropertySetNameModel::new)
                .collect(Collectors.toList());
    }

    private GeneralRepositoryConfigModel createGeneralConfig(RepoBaseDescriptor descriptor) {
        GeneralRepositoryConfigModel general = new GeneralRepositoryConfigModel();
        general.setRepoKey(descriptor.getKey());
        return general;
    }

    private LocalBasicRepositoryConfigModel createLocalBasicConfig(LocalRepoDescriptor descriptor) {
        LocalBasicRepositoryConfigModel basic = new LocalBasicRepositoryConfigModel();
        addSharedBasicConfigModel(basic, descriptor);
        return basic;
    }

    private void addSharedBasicConfigModel(BasicRepositoryConfigModel basic, RepoDescriptor descriptor) {
        basic.setPublicDescription(descriptor.getDescription());
        basic.setInternalDescription(descriptor.getNotes());
        basic.setIncludesPattern(descriptor.getIncludesPattern());
        basic.setExcludesPattern(descriptor.getExcludesPattern());
        Optional.ofNullable(descriptor.getRepoLayout()).ifPresent(repoLayout -> basic.setLayout(repoLayout.getName()));
    }

    private RemoteBasicRepositoryConfigModel createRemoteBasicConfig(HttpRepoDescriptor descriptor) {
        RemoteBasicRepositoryConfigModel basic = new RemoteBasicRepositoryConfigModel();
        addSharedBasicConfigModel(basic, descriptor);
        basic.setUrl(descriptor.getUrl());
        basic.setContentSynchronisation(descriptor.getContentSynchronisation());
        RepoLayout remoteRepoLayout = descriptor.getRemoteRepoLayout();
        if (remoteRepoLayout != null) {
            basic.setRemoteLayoutMapping(remoteRepoLayout.getName());
        }
        basic.setOffline(descriptor.isOffline());
        return basic;
    }

    private LocalAdvancedRepositoryConfigModel createLocalAdvancedConfig(LocalRepoDescriptor descriptor) {
        LocalAdvancedRepositoryConfigModel advanced = new LocalAdvancedRepositoryConfigModel();
        advanced.setAllowContentBrowsing(descriptor.isArchiveBrowsingEnabled());
        advanced.setBlackedOut(descriptor.isBlackedOut());

        List<PropertySet> propertySetsList = descriptor.getPropertySets();
        List<PropertySetNameModel> propertySetNameModelList = collectPropertySets(propertySetsList);
        advanced.setPropertySets(propertySetNameModelList);
        return advanced;
    }

    private RemoteAdvancedRepositoryConfigModel createRemoteAdvancedConfig(HttpRepoDescriptor descriptor) {
        RemoteAdvancedRepositoryConfigModel advanced = new RemoteAdvancedRepositoryConfigModel();
        advanced.setBlackedOut(descriptor.isBlackedOut());
        List<PropertySet> propertySetsList = descriptor.getPropertySets();
        List<PropertySetNameModel> propertySetNameModelList = collectPropertySets(propertySetsList);
        advanced.setPropertySets(propertySetNameModelList);

        RemoteNetworkRepositoryConfigModel networkModel = createNetworkConfig(descriptor);
        advanced.setNetwork(networkModel);

        RemoteCacheRepositoryConfigModel cacheConfig = createCacheConfig(descriptor);
        advanced.setCache(cacheConfig);

        advanced.setAllowContentBrowsing(descriptor.isArchiveBrowsingEnabled());
        advanced.setStoreArtifactsLocally(descriptor.isStoreArtifactsLocally());
        advanced.setSynchronizeArtifactProperties(descriptor.isSynchronizeProperties());
        advanced.setHardFail(descriptor.isHardFail());
        advanced.setQueryParams(descriptor.getQueryParams());
        advanced.setShareConfiguration(descriptor.isShareConfiguration());
        return advanced;
    }

    private RemoteCacheRepositoryConfigModel createCacheConfig(HttpRepoDescriptor descriptor) {
        RemoteCacheRepositoryConfigModel cacheConfig = new RemoteCacheRepositoryConfigModel();
        cacheConfig.setKeepUnusedArtifactsHours(descriptor.getUnusedArtifactsCleanupPeriodHours());
        cacheConfig.setRetrievalCachePeriodSecs(descriptor.getRetrievalCachePeriodSecs());
        cacheConfig.setAssumedOfflineLimitSecs(descriptor.getAssumedOfflinePeriodSecs());
        cacheConfig.setMissedRetrievalCachePeriodSecs(descriptor.getMissedRetrievalCachePeriodSecs());
        return cacheConfig;
    }

    private RemoteNetworkRepositoryConfigModel createNetworkConfig(HttpRepoDescriptor descriptor) {
        RemoteNetworkRepositoryConfigModel networkModel = new RemoteNetworkRepositoryConfigModel();
        if (descriptor.getProxy() != null) {
            networkModel.setProxy(descriptor.getProxy().getKey());
        }
        networkModel.setLocalAddress(descriptor.getLocalAddress());
        networkModel.setUsername(descriptor.getUsername());
        networkModel.setPassword(descriptor.getPassword());
        networkModel.setSocketTimeout(descriptor.getSocketTimeoutMillis());
        networkModel.setLenientHostAuth(descriptor.isAllowAnyHostAuth());
        networkModel.setCookieManagement(descriptor.isEnableCookieManagement());
        networkModel.setSyncProperties(descriptor.isSynchronizeProperties());
        return networkModel;
    }

    private TypeSpecificConfigModel createLocalTypeSpecific(RepoType type, LocalRepoDescriptor descriptor) {
        MavenTypeSpecificConfigModel mavenModel = null;
        TypeSpecificConfigModel model = null;
        switch (type) {
            case Bower:
                model = new BowerTypeSpecificConfigModel();
                break;
            case Docker:
                DockerTypeSpecificConfigModel dockerType = new DockerTypeSpecificConfigModel();
                DockerApiVersion dockerApiVersion = descriptor.getDockerApiVersion();
                if (dockerApiVersion != null) {
                    dockerType.setDockerApiVersion(dockerApiVersion);
                }
                dockerType.setForceDockerAuthentication(descriptor.isForceDockerAuthentication());
                model = dockerType;
                break;
            case NuGet:
                NugetTypeSpecificConfigModel nugetType = new NugetTypeSpecificConfigModel();
                populateSharedNuGetValues(nugetType, descriptor);
                nugetType.setMaxUniqueSnapshots(descriptor.getMaxUniqueSnapshots());
                model = nugetType;
                break;
            case Npm:
                model = new NpmTypeSpecificConfigModel();
                break;
            case Pypi:
                model = new PypiTypeSpecificConfigModel();
                break;
            case Vagrant:
                model = new VagrantTypeSpecificConfigModel();
                break;
            case GitLfs:
                model = new GitLfsTypeSpecificConfigModel();
                break;
            case Debian:
                DebTypeSpecificConfigModel debType = new DebTypeSpecificConfigModel();
                debType.setTrivialLayout(descriptor.isDebianTrivialLayout());
                model = debType;
                break;
            case YUM:
                YumTypeSpecificConfigModel yumType = new YumTypeSpecificConfigModel();
                yumType.setMetadataFolderDepth(descriptor.getYumRootDepth());
                yumType.setGroupFileNames(descriptor.getYumGroupFileNames());
                yumType.setAutoCalculateYumMetadata(descriptor.isCalculateYumMetadata());
                model = yumType;
                break;
            case Gems:
                model = new GemsTypeSpecificConfigModel();
                break;
            case Generic:
                model = new GenericTypeSpecificConfigModel();
                break;
            case Maven:
                mavenModel = new MavenTypeSpecificConfigModel();
                break;
            case Gradle:
                mavenModel = new GradleTypeSpecificConfigModel();
                break;
            case Ivy:
                mavenModel = new IvyTypeSpecificConfigModel();
                break;
            case SBT:
                mavenModel = new SbtTypeSpecificConfigModel();
                break;
        }
        if (model != null) {
            return model;
        }
        // We will get here only if our model is maven / gradle / ivy / sbt and we populate the values
        populateMavenLocalValues(mavenModel, descriptor);
        return mavenModel;
    }

    private TypeSpecificConfigModel createRemoteTypeSpecific(RepoType type, HttpRepoDescriptor descriptor) {
        MavenTypeSpecificConfigModel mavenModel = null;
        TypeSpecificConfigModel model = null;
        switch (type) {
            case Maven:
                mavenModel = new MavenTypeSpecificConfigModel();
                break;
            case Gradle:
                mavenModel = new GradleTypeSpecificConfigModel();
                break;
            case Ivy:
                mavenModel = new IvyTypeSpecificConfigModel();
                break;
            case SBT:
                mavenModel = new SbtTypeSpecificConfigModel();
                break;
            case P2:
                mavenModel = new P2TypeSpecificConfigModel();
                break;
            case Debian:
                DebTypeSpecificConfigModel debType = new DebTypeSpecificConfigModel();
                debType.setListRemoteFolderItems(descriptor.isListRemoteFolderItems());
                model = debType;
                break;
            case Docker:
                DockerTypeSpecificConfigModel dockerType = new DockerTypeSpecificConfigModel();
                dockerType.setEnableTokenAuthentication(descriptor.isEnableTokenAuthentication());
                dockerType.setForceDockerAuthentication(descriptor.isForceDockerAuthentication());
                dockerType.setListRemoteFolderItems(descriptor.isListRemoteFolderItems());
                model = dockerType;
                break;
            case NuGet:
                NugetTypeSpecificConfigModel nugetType = new NugetTypeSpecificConfigModel();
                populateSharedNuGetValues(nugetType, descriptor);
                NuGetConfiguration nuget = descriptor.getNuget();
                if (nuget != null) {
                    nugetType.setDownloadContextPath(nuget.getDownloadContextPath());
                    nugetType.setFeedContextPath(nuget.getFeedContextPath());
                }
                nugetType.setListRemoteFolderItems(descriptor.isListRemoteFolderItems());
                model = nugetType;
                break;
            case Npm:
                NpmTypeSpecificConfigModel npmType = new NpmTypeSpecificConfigModel();
                npmType.setListRemoteFolderItems(descriptor.isListRemoteFolderItems());
                model = npmType;
                break;
            case Pypi:
                PypiTypeSpecificConfigModel pypiType = new PypiTypeSpecificConfigModel();
                pypiType.setListRemoteFolderItems(descriptor.isListRemoteFolderItems());
                model = pypiType;
                break;
            case VCS:
                VcsTypeSpecificConfigModel vcsType = new VcsTypeSpecificConfigModel();
                populateVcsValues(vcsType, descriptor);
                model = vcsType;
                break;
            case Bower:
                BowerTypeSpecificConfigModel bowerType = new BowerTypeSpecificConfigModel();
                populateVcsValues(bowerType, descriptor);
                BowerConfiguration bowerConfiguration = descriptor.getBower();
                if (bowerConfiguration != null) {
                    bowerType.setRegistryUrl(bowerConfiguration.getBowerRegistryUrl());
                }
                model = bowerType;
                break;
            case Gems:
                GemsTypeSpecificConfigModel gemsType = new GemsTypeSpecificConfigModel();
                gemsType.setListRemoteFolderItems(descriptor.isListRemoteFolderItems());
                model = gemsType;
                break;
            case Generic:
                GenericTypeSpecificConfigModel genericType = new GenericTypeSpecificConfigModel();
                genericType.setListRemoteFolderItems(descriptor.isListRemoteFolderItems());
                model = genericType;
                break;
            case YUM:
                YumTypeSpecificConfigModel yumType = new YumTypeSpecificConfigModel();
                yumType.setListRemoteFolderItems(descriptor.isListRemoteFolderItems());
                yumType.setAutoCalculateYumMetadata(null);
                yumType.setMetadataFolderDepth(null);
                yumType.setGroupFileNames(null);
                model = yumType;
                break;
        }
        if (model != null) {
            return model;
        }
        populateMavenRemoteValues(mavenModel, descriptor);
        return mavenModel;
    }

    private TypeSpecificConfigModel createVirtualTypeSpecific(RepoType type, VirtualRepoDescriptor descriptor) {
        TypeSpecificConfigModel typeSpecific = null;
        MavenTypeSpecificConfigModel mavenModel = null;
        switch (type) {
            case Maven:
                mavenModel = new MavenTypeSpecificConfigModel();
                break;
            case Gradle:
                mavenModel = new GradleTypeSpecificConfigModel();
                break;
            case Ivy:
                mavenModel = new IvyTypeSpecificConfigModel();
                break;
            case SBT:
                mavenModel = new SbtTypeSpecificConfigModel();
                break;
            case P2:
                P2TypeSpecificConfigModel p2 = new P2TypeSpecificConfigModel();
                populateVirtualP2Values(p2, descriptor);
                mavenModel = p2;
                break;
            case Gems:
                typeSpecific = new GemsTypeSpecificConfigModel();
                break;
            case Npm:
                typeSpecific = new NpmTypeSpecificConfigModel();
                break;
            case Bower:
                typeSpecific = new BowerTypeSpecificConfigModel();
                break;
            case NuGet:
                typeSpecific = new NugetTypeSpecificConfigModel();
                populateSharedNuGetValues((NugetTypeSpecificConfigModel) typeSpecific, descriptor);
                break;
            case Pypi:
                typeSpecific = new PypiTypeSpecificConfigModel();
                break;
            case Docker:
                DockerTypeSpecificConfigModel dockerType = new DockerTypeSpecificConfigModel();
                dockerType.setForceDockerAuthentication(descriptor.isForceDockerAuthentication());
                typeSpecific = dockerType;
                break;
            case Generic:
                typeSpecific = new GenericTypeSpecificConfigModel();
                break;
        }
        if (typeSpecific != null) {
            return typeSpecific;
        }
        populateMavenVirtualValues(mavenModel, descriptor);
        return mavenModel;
    }

    private void populateVcsValues(VcsTypeSpecificConfigModel vcsType, HttpRepoDescriptor descriptor) {
        vcsType.setMaxUniqueSnapshots(descriptor.getMaxUniqueSnapshots());
        VcsConfiguration vcsConf = descriptor.getVcs();
        vcsType.setListRemoteFolderItems(descriptor.isListRemoteFolderItems());
        if (vcsConf != null) {
            vcsType.setVcsType(vcsConf.getType());
            VcsGitConfiguration git = vcsConf.getGit();
            if (git != null) {
                vcsType.setGitProvider(git.getProvider());
                vcsType.setDownloadUrl(git.getDownloadUrl());
            }
        }
    }

    private void populateSharedMavenValues(MavenTypeSpecificConfigModel model, RealRepoDescriptor descriptor) {
        model.setMaxUniqueSnapshots(descriptor.getMaxUniqueSnapshots());
        model.setHandleReleases(descriptor.isHandleReleases());
        model.setHandleSnapshots(descriptor.isHandleSnapshots());
        model.setSuppressPomConsistencyChecks(descriptor.isSuppressPomConsistencyChecks());
    }

    private void populateMavenLocalValues(MavenTypeSpecificConfigModel model, LocalRepoDescriptor descriptor) {
        populateSharedMavenValues(model, descriptor);
        model.setSnapshotVersionBehavior(descriptor.getSnapshotVersionBehavior());
        model.setLocalChecksumPolicy(descriptor.getChecksumPolicyType());
    }

    private void populateMavenRemoteValues(MavenTypeSpecificConfigModel model, HttpRepoDescriptor descriptor) {
        populateSharedMavenValues(model, descriptor);
        model.setEagerlyFetchJars(descriptor.isFetchJarsEagerly());
        model.setEagerlyFetchSources(descriptor.isFetchSourcesEagerly());
        model.setRemoteChecksumPolicy(descriptor.getChecksumPolicyType());
        model.setListRemoteFolderItems(descriptor.isListRemoteFolderItems());
        model.setRejectInvalidJars(descriptor.isRejectInvalidJars());
    }

    private void populateMavenVirtualValues(MavenTypeSpecificConfigModel model, VirtualRepoDescriptor descriptor) {
        model.setPomCleanupPolicy(descriptor.getPomRepositoryReferencesCleanupPolicy());
        model.setKeyPair(descriptor.getKeyPair());
    }

    private void populateVirtualP2Values(P2TypeSpecificConfigModel model, VirtualRepoDescriptor descriptor) {
        if (descriptor.getP2() == null || descriptor.getP2().getUrls() == null) {
            return;
        }
        Map<String, String> urlToRepoKeyMap = getUrlToRepoKeyMapping(descriptor.getRepositories());
        List<P2Repo> p2Repos = Lists.newArrayList();
        descriptor.getP2().getUrls().stream().forEach(url -> {
            if (StringUtils.startsWith(url, "local://")) {
                Optional.ofNullable(resolveLocalP2RepoFromUrl(url)).ifPresent(p2Repos::add);
            } else {
                urlToRepoKeyMap.keySet().stream()
                        .map(RepoConfigDescriptorBuilder::getUrlWithoutSubpath)
                        .filter(p2Url -> RepoConfigDescriptorBuilder.getUrlWithoutSubpath(url).equals(p2Url))
                        .findAny()
                        .ifPresent(containingUrl ->
                                p2Repos.add(new P2Repo(null, urlToRepoKeyMap.get(containingUrl), url)));
            }
        });
        model.setP2Repos(p2Repos);
    }

    private P2Repo resolveLocalP2RepoFromUrl(String url) {
        String rpp = StringUtils.removeStart(url, "local://");
        //rpp = rpp.substring(0, rpp.indexOf('/'));
        RepoPath repoPath = RepoPathFactory.create(rpp);
        LocalRepoDescriptor localRepoDescriptor = centralConfig.getMutableDescriptor().getLocalRepositoriesMap()
                .get(repoPath.getRepoKey());
        if (localRepoDescriptor != null) {
            return new P2Repo(null, repoPath.getRepoKey(), url);
        }
        return null;
    }

    /**
     * Creates a mapping of url -> remote repo key to help build the P2 model (maps 'maven group' repos only)
     */
    private Map<String, String> getUrlToRepoKeyMapping(List<RepoDescriptor> descriptors) {
        ConcurrentMap<String, String> mapping = Maps.newConcurrentMap();
        descriptors.stream()
                .filter(repoDescriptor -> repoDescriptor instanceof HttpRepoDescriptor)
                .filter(repoDescriptor -> repoDescriptor.getType().isMavenGroup())
                .forEach(remoteDescriptor ->
                        mapping.put(((HttpRepoDescriptor) remoteDescriptor).getUrl(), remoteDescriptor.getKey()));
        return mapping;
    }

    private void populateSharedNuGetValues(NugetTypeSpecificConfigModel nuget, RepoBaseDescriptor descriptor) {
        nuget.setForceNugetAuthentication(descriptor.isForceNugetAuthentication());
    }
}
