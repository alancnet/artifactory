package org.artifactory.ui.rest.service.admin.configuration.repositories.util.builder;

import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.p2.P2Repo;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.descriptor.property.PropertySet;
import org.artifactory.descriptor.repo.*;
import org.artifactory.security.crypto.CryptoHelper;
import org.artifactory.ui.rest.model.admin.configuration.propertysets.PropertySetNameModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.AdvancedRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.BasicRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.GeneralRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.local.LocalRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteAdvancedRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteBasicRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteCacheRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteNetworkRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.typespecific.*;
import org.artifactory.ui.rest.model.admin.configuration.repository.virtual.VirtualAdvancedRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.virtual.VirtualBasicRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.virtual.VirtualRepositoryConfigModel;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.validator.RepoConfigValidator;
import org.artifactory.util.PathUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for converting model to descriptor
 *
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class RepoConfigDescriptorBuilder {

    @Autowired
    CentralConfigService centralConfig;

    @Autowired
    AddonsManager addonsManager;

    @Autowired
    RepositoryService repoService;

    @Autowired
    RepoConfigValidator configValidator;

    public LocalRepoDescriptor buildLocalDescriptor(LocalRepositoryConfigModel model) {
        LocalRepoDescriptor descriptor = new LocalRepoDescriptor();
        populateSharedGeneralDescriptorValues(model.getGeneral(), descriptor);
        populateSharedBasicDescriptorValues(model.getBasic(), descriptor);
        populateSharedAdvancedDescriptorValues(model.getAdvanced(), descriptor);
        populateSharedTypeSpecificDescriptorValues(model.getTypeSpecific(), descriptor);
        populateLocalTypeSpecificDescriptorValues(model.getTypeSpecific(), descriptor);
        return descriptor;
    }

    public HttpRepoDescriptor buildRemoteDescriptor(RemoteRepositoryConfigModel model) {
        HttpRepoDescriptor descriptor = new HttpRepoDescriptor();
        populateSharedGeneralDescriptorValues(model.getGeneral(), descriptor);
        populateSharedBasicDescriptorValues(model.getBasic(), descriptor);
        populateRemoteBasicDescriptorValues(model.getBasic(), descriptor);
        populateSharedAdvancedDescriptorValues(model.getAdvanced(), descriptor);
        populateRemoteAdvancedDescriptorValues(model.getAdvanced(), descriptor);
        populateSharedTypeSpecificDescriptorValues(model.getTypeSpecific(), descriptor);
        populateRemoteTypeSpecificDescriptorValues(model.getTypeSpecific(), descriptor);
        return descriptor;
    }

    public VirtualRepoDescriptor buildVirtualDescriptor(VirtualRepositoryConfigModel model) {
        VirtualRepoDescriptor descriptor = new VirtualRepoDescriptor();
        populateSharedGeneralDescriptorValues(model.getGeneral(), descriptor);
        populateSharedBasicDescriptorValues(model.getBasic(), descriptor);
        populateVirtualBasicDescriptorValues(model.getBasic(), descriptor);
        populateVirtualAdvancedDescriptorValues(model.getAdvanced(), descriptor);
        populateVirtualTypeSpecific(model.getTypeSpecific(), descriptor);
        return descriptor;
    }

    private void populateSharedGeneralDescriptorValues(GeneralRepositoryConfigModel model,
            RepoBaseDescriptor descriptor) {
        descriptor.setKey(model.getRepoKey());
    }

    /**
     * Populates basic descriptor values that are shared between local and remote repos
     */
    private void populateSharedBasicDescriptorValues(BasicRepositoryConfigModel model, RepoBaseDescriptor descriptor) {
        descriptor.setDescription(model.getPublicDescription());
        descriptor.setNotes(model.getInternalDescription());
        descriptor.setIncludesPattern(model.getIncludesPattern());
        descriptor.setExcludesPattern(model.getExcludesPattern());
        if (StringUtils.isNotBlank(model.getLayout())) { //Don't enforce on virtual
            descriptor.setRepoLayout(centralConfig.getDescriptor().getRepoLayout(model.getLayout()));
        }
    }

    /**
     * Populates remote basic descriptor values
     */
    private void populateRemoteBasicDescriptorValues(RemoteBasicRepositoryConfigModel model,
            HttpRepoDescriptor descriptor) {
        descriptor.setUrl(model.getUrl());
        if (model.getRemoteLayoutMapping() != null) {
            descriptor.setRemoteRepoLayout(centralConfig.getDescriptor().getRepoLayout(model.getRemoteLayoutMapping()));
        }
        descriptor.setOffline(model.isOffline());
        descriptor.setContentSynchronisation(model.getContentSynchronisation());
    }

    private void populateVirtualBasicDescriptorValues(VirtualBasicRepositoryConfigModel model,
            VirtualRepoDescriptor descriptor) {
        descriptor.setRepositories(model.getSelectedRepositories().stream()
                .map(configValidator::mapRepoKeyToDescriptor)
                .filter(selectedDescriptor -> selectedDescriptor != null)
                .collect(Collectors.toList()));
    }

    private void populateVirtualAdvancedDescriptorValues(VirtualAdvancedRepositoryConfigModel model,
            VirtualRepoDescriptor descriptor) {
        descriptor.setArtifactoryRequestsCanRetrieveRemoteArtifacts(model.getRetrieveRemoteArtifacts());
    }

    /**
     * Populates advanced descriptor values that are shared between local and remote repos
     */
    private void populateSharedAdvancedDescriptorValues(AdvancedRepositoryConfigModel model,
            RealRepoDescriptor descriptor) {
        descriptor.setBlackedOut(model.isBlackedOut());
        descriptor.setArchiveBrowsingEnabled(model.getAllowContentBrowsing());
        List<PropertySetNameModel> propertySets = model.getPropertySets();
        if (propertySets == null) {
            return;
        }
        descriptor.setPropertySets(model.getPropertySets().stream()
                .map(propSet -> getPropSetByName(propSet.getName()))
                .filter(propSet -> propSet != null)
                .collect(Collectors.toList()));
    }

    /**
     * Populates remote advanced descriptor values
     */
    private void populateRemoteAdvancedDescriptorValues(RemoteAdvancedRepositoryConfigModel model,
            HttpRepoDescriptor descriptor) {
        //network
        RemoteNetworkRepositoryConfigModel network = model.getNetwork();
        if (network != null) {
            if(StringUtils.isNotBlank(network.getProxy())) {
                descriptor.setProxy(centralConfig.getDescriptor().getProxy(network.getProxy()));
            }
            descriptor.setLocalAddress(network.getLocalAddress());
            descriptor.setUsername(network.getUsername());
            descriptor.setPassword(CryptoHelper.encryptIfNeeded(network.getPassword()));
            descriptor.setSocketTimeoutMillis(network.getSocketTimeout());
            descriptor.setAllowAnyHostAuth(network.getLenientHostAuth());
            descriptor.setEnableCookieManagement(network.getCookieManagement());
        }
        //cache
        RemoteCacheRepositoryConfigModel cache = model.getCache();
        if (cache != null) {
            descriptor.setUnusedArtifactsCleanupPeriodHours(cache.getKeepUnusedArtifactsHours());
            descriptor.setRetrievalCachePeriodSecs(cache.getRetrievalCachePeriodSecs());
            descriptor.setAssumedOfflinePeriodSecs(cache.getAssumedOfflineLimitSecs());
            descriptor.setMissedRetrievalCachePeriodSecs(cache.getMissedRetrievalCachePeriodSecs());
        }
        //other
        descriptor.setQueryParams(model.getQueryParams());
        descriptor.setHardFail(model.getHardFail());
        descriptor.setStoreArtifactsLocally(model.isStoreArtifactsLocally());
        descriptor.setSynchronizeProperties(model.getSynchronizeArtifactProperties());
        descriptor.setShareConfiguration(model.isShareConfiguration());
    }

    /**
     * Populates type specific values shared by local and remote repos
     */
    private void populateSharedTypeSpecificDescriptorValues(TypeSpecificConfigModel type,
            RealRepoDescriptor descriptor) {
        descriptor.setType(type.getRepoType());
        switch (type.getRepoType()) {
            case Maven:
            case Gradle:
            case Ivy:
            case SBT:
                MavenTypeSpecificConfigModel maven = (MavenTypeSpecificConfigModel) type;
                descriptor.setMaxUniqueSnapshots(maven.getMaxUniqueSnapshots());
                descriptor.setHandleReleases(maven.getHandleReleases());
                descriptor.setHandleSnapshots(maven.getHandleSnapshots());
                descriptor.setSuppressPomConsistencyChecks(maven.getSuppressPomConsistencyChecks());
                break;
            case NuGet:
                descriptor.setForceNugetAuthentication(((NugetTypeSpecificConfigModel) type).isForceNugetAuthentication());
                break;
        }
    }

    /**
     * Populates type specific values for local repos
     */
    private void populateLocalTypeSpecificDescriptorValues(TypeSpecificConfigModel type,
            LocalRepoDescriptor descriptor) {
        switch (type.getRepoType()) {
            case Maven:
            case Gradle:
            case Ivy:
            case SBT:
                MavenTypeSpecificConfigModel maven = (MavenTypeSpecificConfigModel) type;
                descriptor.setSnapshotVersionBehavior(maven.getSnapshotVersionBehavior());
                descriptor.setChecksumPolicyType(maven.getLocalChecksumPolicy());
                break;
            case YUM:
                YumTypeSpecificConfigModel yum = (YumTypeSpecificConfigModel) type;
                descriptor.setYumGroupFileNames(yum.getGroupFileNames());
                descriptor.setYumRootDepth(yum.getMetadataFolderDepth());
                descriptor.setCalculateYumMetadata(yum.isAutoCalculateYumMetadata());
                break;
            case Docker:
                DockerTypeSpecificConfigModel docker = (DockerTypeSpecificConfigModel) type;
                descriptor.setDockerApiVersion(docker.getDockerApiVersion().toString());
                descriptor.setForceDockerAuthentication(docker.isForceDockerAuthentication());
                break;
            case Debian:
                DebTypeSpecificConfigModel deb = (DebTypeSpecificConfigModel) type;
                descriptor.setDebianTrivialLayout(deb.getTrivialLayout());
                break;
            case NuGet:
                NugetTypeSpecificConfigModel nuget = (NugetTypeSpecificConfigModel) type;
                descriptor.setMaxUniqueSnapshots(nuget.getMaxUniqueSnapshots());
                break;
        }
    }

    /**
     * Populates type specific values for remote repos
     */
    private void populateRemoteTypeSpecificDescriptorValues(TypeSpecificConfigModel type,
            HttpRepoDescriptor descriptor) {
        descriptor.setType(type.getRepoType());
        switch (type.getRepoType()) {
            case P2:
            case Maven:
            case Gradle:
            case Ivy:
            case SBT:
                MavenTypeSpecificConfigModel maven = (MavenTypeSpecificConfigModel) type;
                descriptor.setFetchJarsEagerly(maven.getEagerlyFetchJars());
                descriptor.setFetchSourcesEagerly(maven.getEagerlyFetchSources());
                descriptor.setRejectInvalidJars(maven.getRejectInvalidJars());
                descriptor.setListRemoteFolderItems(maven.isListRemoteFolderItems());
                descriptor.setChecksumPolicyType(maven.getRemoteChecksumPolicy());

                //Set p2 url for supporting repos
                if (descriptor.getUrl() != null) { //Should always be true, but this is to avoid accidental nulls
                    descriptor.setP2OriginalUrl(getUrlWithoutSubpath(descriptor.getUrl()));
                }
                break;
            case Bower:
                BowerTypeSpecificConfigModel bower = (BowerTypeSpecificConfigModel) type;
                buildAndSetBowerConfig(descriptor, bower);
                descriptor.setListRemoteFolderItems(bower.isListRemoteFolderItems());
                break;
            case VCS:
                VcsTypeSpecificConfigModel vcs = (VcsTypeSpecificConfigModel) type;
                buildAndSetVcsConfig(descriptor, vcs);
                descriptor.setListRemoteFolderItems(vcs.isListRemoteFolderItems());
                break;
            case Docker:
                DockerTypeSpecificConfigModel docker = (DockerTypeSpecificConfigModel) type;
                descriptor.setEnableTokenAuthentication(docker.isEnableTokenAuthentication());
                descriptor.setForceDockerAuthentication(docker.isForceDockerAuthentication());
                descriptor.setListRemoteFolderItems(docker.isListRemoteFolderItems());
                break;
            case Debian:
                DebTypeSpecificConfigModel deb = (DebTypeSpecificConfigModel) type;
                descriptor.setListRemoteFolderItems(deb.isListRemoteFolderItems());
                break;
            case NuGet:
                NugetTypeSpecificConfigModel nuGet = (NugetTypeSpecificConfigModel) type;
                descriptor.setListRemoteFolderItems(nuGet.isListRemoteFolderItems());
                buildAndSetNugetConfig(descriptor, nuGet);
                break;
            case Generic:
                GenericTypeSpecificConfigModel generic = (GenericTypeSpecificConfigModel) type;
                descriptor.setListRemoteFolderItems(generic.isListRemoteFolderItems());
                break;
            case YUM:
                YumTypeSpecificConfigModel yum = (YumTypeSpecificConfigModel) type;
                descriptor.setListRemoteFolderItems(yum.isListRemoteFolderItems());
                break;
            case Npm:
                NpmTypeSpecificConfigModel npm = (NpmTypeSpecificConfigModel) type;
                descriptor.setListRemoteFolderItems(npm.isListRemoteFolderItems());
                break;
            case Gems:
                GemsTypeSpecificConfigModel gems = (GemsTypeSpecificConfigModel) type;
                descriptor.setListRemoteFolderItems(gems.isListRemoteFolderItems());
                break;
            case Pypi:
                PypiTypeSpecificConfigModel pypi = (PypiTypeSpecificConfigModel) type;
                descriptor.setListRemoteFolderItems(pypi.isListRemoteFolderItems());
                break;
        }
    }

    private void populateVirtualTypeSpecific(TypeSpecificConfigModel type, VirtualRepoDescriptor descriptor) {
        descriptor.setType(type.getRepoType());
        switch (type.getRepoType()) {
            case P2:
                buildAndSetVirtualP2Config(descriptor, (P2TypeSpecificConfigModel) type);
                break;
            case Docker:
                DockerTypeSpecificConfigModel docker = (DockerTypeSpecificConfigModel) type;
                descriptor.setForceDockerAuthentication(docker.isForceDockerAuthentication());
                break;
            case Maven: //P2 reaches maven also
            case Gradle:
            case Ivy:
            case SBT:
                MavenTypeSpecificConfigModel maven = (MavenTypeSpecificConfigModel) type;
                descriptor.setKeyPair(maven.getKeyPair());
                descriptor.setPomRepositoryReferencesCleanupPolicy(maven.getPomCleanupPolicy());
                break;
        }
    }

    private void buildAndSetNugetConfig(HttpRepoDescriptor descriptor, NugetTypeSpecificConfigModel nuGet) {
        NuGetConfiguration nugetConfig = new NuGetConfiguration();
        nugetConfig.setDownloadContextPath(nuGet.getDownloadContextPath());
        nugetConfig.setFeedContextPath(nuGet.getFeedContextPath());
        descriptor.setNuget(nugetConfig);
    }

    private void buildAndSetVcsConfig(HttpRepoDescriptor descriptor, VcsTypeSpecificConfigModel vcs) {
        VcsConfiguration vcsConfig = new VcsConfiguration();
        VcsGitConfiguration vcsGitConfig = new VcsGitConfiguration();
        vcsGitConfig.setProvider(vcs.getGitProvider());
        vcsGitConfig.setDownloadUrl(vcs.getDownloadUrl());
        vcsConfig.setType(vcs.getVcsType());
        vcsConfig.setGit(vcsGitConfig);
        descriptor.setVcs(vcsConfig);
        descriptor.setMaxUniqueSnapshots(vcs.getMaxUniqueSnapshots());
        descriptor.setListRemoteFolderItems(vcs.isListRemoteFolderItems());
    }

    private void buildAndSetBowerConfig(HttpRepoDescriptor descriptor, BowerTypeSpecificConfigModel bower) {
        buildAndSetVcsConfig(descriptor, bower);
        BowerConfiguration bowerConfig = new BowerConfiguration();
        bowerConfig.setBowerRegistryUrl(bower.getRegistryUrl());
        descriptor.setBower(bowerConfig);
    }

    private void buildAndSetVirtualP2Config(VirtualRepoDescriptor descriptor, P2TypeSpecificConfigModel p2) {
        P2Configuration p2Config = new P2Configuration();
        //This just adds the selected urls - creating repos etc is done at the service
        p2Config.setUrls(p2.getP2Repos().stream()
                .map(P2Repo::getRepoUrl)
                .distinct()
                .collect(Collectors.toList()));
        descriptor.setP2(p2Config);
    }

    private PropertySet getPropSetByName(String name) {
        return centralConfig.getDescriptor().getPropertySets().stream()
                .filter(propertySet -> propertySet.getName().equals(name))
                .findFirst()
                .orElseGet(null);
    }

    public static String getUrlWithoutSubpath(String url) {
        int slashslash = url.indexOf("//") + 2;
        int nextSlash = url.indexOf('/', slashslash);
        return nextSlash < 0 ? url : PathUtils.trimSlashes(url.substring(0, nextSlash)).toString();
    }
}
