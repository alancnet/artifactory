package org.artifactory.ui.rest.service.admin.configuration.repositories.util.validator;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.webstart.ArtifactWebstartAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;
import org.artifactory.descriptor.repo.vcs.VcsGitProvider;
import org.artifactory.ui.rest.model.admin.configuration.propertysets.PropertySetNameModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.AdvancedRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.BasicRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.RepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.local.LocalAdvancedRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.local.LocalBasicRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.local.LocalRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteAdvancedRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteBasicRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteCacheRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteNetworkRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.typespecific.*;
import org.artifactory.ui.rest.model.admin.configuration.repository.virtual.VirtualBasicRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.virtual.VirtualRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.virtual.VirtualSelectedRepository;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.exception.RepoConfigException;
import org.artifactory.util.CollectionUtils;
import org.artifactory.util.UiRequestUtils;
import org.artifactory.util.UrlValidator;
import org.jdom2.Verifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.apache.http.HttpStatus.*;
import static org.artifactory.ui.rest.model.admin.configuration.repository.RepoConfigDefaultValues.*;

/**
 * Service validates values in the model and sets default values as needed.
 *
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class RepoConfigValidator {

    private static final int REPO_KEY_MAX_LENGTH = 64;
    private static final List<Character> forbiddenChars = Lists.newArrayList('/', '\\', ':', '|', '?', '*', '"', '<',
            '>');
    private final transient UrlValidator urlValidator = new UrlValidator("http", "https");
    @Autowired
    CentralConfigService centralConfig;
    @Autowired
    RepositoryService repoService;
    @Autowired
    AddonsManager addonsManager;

    public void validateLocal(LocalRepositoryConfigModel model) throws RepoConfigException {
        verifyAllSectionsExist(model);
        LocalBasicRepositoryConfigModel basic = model.getBasic();
        LocalAdvancedRepositoryConfigModel advanced = model.getAdvanced();
        TypeSpecificConfigModel typeSpecific = model.getTypeSpecific();

        //basic
        validateSharedBasic(basic);

        //advanced
        validateSharedAdvanced(advanced);

        //type specific
        validateSharedTypeSpecific(typeSpecific);
        validateLocalTypeSpecific(typeSpecific);
    }

    public void validateRemote(RemoteRepositoryConfigModel model) throws RepoConfigException {
        verifyAllSectionsExist(model);
        RemoteBasicRepositoryConfigModel basic = model.getBasic();
        RemoteAdvancedRepositoryConfigModel advanced = model.getAdvanced();
        TypeSpecificConfigModel typeSpecific = model.getTypeSpecific();

        //basic
        validateSharedBasic(basic);
        if (StringUtils.isBlank(basic.getUrl())) {
            throw new RepoConfigException("URL cannot be empty", SC_BAD_REQUEST);
        }
        try {
            urlValidator.validate(basic.getUrl());
        } catch (UrlValidator.UrlValidationException e) {
            throw new RepoConfigException("Invalid URL: " + e.getMessage(), SC_BAD_REQUEST);
        }
        if (basic.getRemoteLayoutMapping() != null
                && centralConfig.getDescriptor().getRepoLayout(basic.getLayout()) == null) {
            throw new RepoConfigException("Invalid remote repository layout", SC_BAD_REQUEST);
        }
        basic.setOffline(Optional.ofNullable(basic.isOffline()).orElse(DEFAULT_OFFLINE));

        //advanced
        validateSharedAdvanced(advanced);
        advanced.setHardFail(Optional.ofNullable(advanced.getHardFail()).orElse(DEFAULT_HARD_FAIL));
        advanced.setStoreArtifactsLocally(
                Optional.ofNullable(advanced.isStoreArtifactsLocally()).orElse(DEFAULT_STORE_ARTIFACTS_LOCALLY));
        advanced.setSynchronizeArtifactProperties(
                Optional.ofNullable(advanced.getSynchronizeArtifactProperties()).orElse(DEFAULT_SYNC_PROPERTIES));
        advanced.setShareConfiguration(
                Optional.ofNullable(advanced.isShareConfiguration()).orElse(DEFAULT_SHARE_CONFIG));

        //network
        RemoteNetworkRepositoryConfigModel network = advanced.getNetwork();
        if (network != null) {
            if (StringUtils.isNotBlank(network.getProxy()) && centralConfig.getDescriptor().getProxy(network.getProxy()) == null) {
                throw new RepoConfigException("Invalid proxy configuration", SC_BAD_REQUEST);
            }
            network.setSocketTimeout(Optional.ofNullable(network.getSocketTimeout()).orElse(DEFAULT_SOCKET_TIMEOUT));
            network.setSyncProperties(Optional.ofNullable(network.isSyncProperties()).orElse(DEFAULT_SYNC_PROPERTIES));
            network.setCookieManagement(
                    Optional.ofNullable(network.getCookieManagement()).orElse(DEFAULT_COOKIE_MANAGEMENT));
            network.setLenientHostAuth(
                    Optional.ofNullable(network.getLenientHostAuth()).orElse(DEFAULT_LENIENENT_HOST_AUTH));
        }

        //cache
        RemoteCacheRepositoryConfigModel cache = advanced.getCache();
        if (cache != null) {

            cache.setKeepUnusedArtifactsHours(
                    Optional.ofNullable(cache.getKeepUnusedArtifactsHours()).orElse(DEFAULT_KEEP_UNUSED_ARTIFACTS));
            cache.setRetrievalCachePeriodSecs(
                    Optional.ofNullable(cache.getRetrievalCachePeriodSecs()).orElse(DEFAULT_RETRIEVAL_CACHE_PERIOD));
            cache.setAssumedOfflineLimitSecs(
                    Optional.ofNullable(cache.getAssumedOfflineLimitSecs()).orElse(DEFAULT_ASSUMED_OFFLINE));
            cache.setMissedRetrievalCachePeriodSecs(
                    Optional.ofNullable(cache.getMissedRetrievalCachePeriodSecs()).orElse(
                            DEFAULT_MISSED_RETRIEVAL_PERIOD));
        }
        //type specific
        validateSharedTypeSpecific(typeSpecific);
        validateRemoteTypeSpecific(typeSpecific);
    }

    public void validateVirtual(VirtualRepositoryConfigModel model) throws RepoConfigException {
        //Sections and aggregated repos validation
        verifyAllSectionsExist(model);
        validateAggregatedReposExistAndTypesMatch(model);

        //basic
        VirtualBasicRepositoryConfigModel basic = model.getBasic();
        basic.setIncludesPattern(Optional.ofNullable(model.getBasic().getIncludesPattern())
                .orElse(DEFAULT_INCLUDES_PATTERN));

        //advanced
        model.getAdvanced().setRetrieveRemoteArtifacts(Optional.ofNullable(model.getAdvanced()
                .getRetrieveRemoteArtifacts()).orElse(DEFAULT_VIRTUAL_CAN_RETRIEVE_FROM_REMOTE));

        //type specific
        validateVirtualTypeSpecific(model.getTypeSpecific());
    }

    /**
     * Validates all given repo keys exist - throws an error for the first not found one.
     *
     * @param repoKeys - Keys to check if existing
     * @throws RepoConfigException
     */
    public void validateSelectedReposInVirtualExist(List<VirtualSelectedRepository> repoKeys)
            throws RepoConfigException {
        Set<String> allRepoKeys = repoService.getAllRepoKeys();
        String nonExistentKey = repoKeys.stream()
                .map(VirtualSelectedRepository::getRepoName)
                .filter(repoKey -> !allRepoKeys.contains(repoKey)).findAny().orElse(null);
        if (StringUtils.isNotBlank(nonExistentKey)) {
            throw new RepoConfigException("Repository '" + nonExistentKey + "' does not exist", SC_NOT_FOUND);
        }
    }

    private void validateAggregatedReposExistAndTypesMatch(VirtualRepositoryConfigModel model)
            throws RepoConfigException {
        List<VirtualSelectedRepository> repoKeys = Optional.ofNullable(model.getBasic().getSelectedRepositories())
                .orElse(Lists.newArrayList());
        if (CollectionUtils.isNullOrEmpty(repoKeys)) {
            model.getBasic().setSelectedRepositories(repoKeys);
        } else {
            validateSelectedReposInVirtualExist(repoKeys);
            RepoDescriptor invalidTypeDescriptor = repoKeys.stream()
                    .map(this::mapRepoKeyToDescriptor)
                    .filter(repoDescriptor -> !filterByType(model.getTypeSpecific().getRepoType(), repoDescriptor))
                    .findAny().orElse(null);
            if (invalidTypeDescriptor != null) {
                throw new RepoConfigException("Repository '" + model.getGeneral().getRepoKey()
                        + "' aggregates another repository '" + invalidTypeDescriptor.getKey() + "' that has a "
                        + "mismatching package type " + invalidTypeDescriptor.getType().name(), SC_FORBIDDEN);
            }
        }
    }

    private boolean filterByType(RepoType type, RepoDescriptor repo) {
        return repo != null && (type.equals(RepoType.Generic) ||
                (type.isMavenGroup() ? repo.getType().isMavenGroup() : repo.getType().equals(type)));
    }

    //(RTFACT-4891) and no time to dilly-dally on this
    public RepoDescriptor mapRepoKeyToDescriptor(VirtualSelectedRepository repository) {
        String repoKey = repository.getRepoName();
        RepoDescriptor descriptor = repoService.localRepoDescriptorByKey(repoKey);
        if (descriptor == null) {
            descriptor = repoService.remoteRepoDescriptorByKey(repoKey);
        }
        if (descriptor == null) {
            descriptor = repoService.virtualRepoDescriptorByKey(repoKey);
        }
        return descriptor;
    }


    private void validateSharedBasic(BasicRepositoryConfigModel basic) throws RepoConfigException {
        //basic
        basic.setIncludesPattern(Optional.ofNullable(basic.getIncludesPattern()).orElse(DEFAULT_INCLUDES_PATTERN));
        if (basic.getLayout() == null || centralConfig.getDescriptor().getRepoLayout(basic.getLayout()) == null) {
            throw new RepoConfigException("Invalid repository layout", SC_BAD_REQUEST);
        }
    }

    private void validateSharedAdvanced(AdvancedRepositoryConfigModel model) throws RepoConfigException {
        if (model.getPropertySets() == null) {
            return;
        }
        String invalidPropSet = model.getPropertySets().stream()
                .map(PropertySetNameModel::getName)
                .filter(propSetName -> !centralConfig.getMutableDescriptor().isPropertySetExists(propSetName))
                .findAny().orElse(null);
        if (StringUtils.isNotBlank(invalidPropSet)) {
            throw new RepoConfigException("Property set " + invalidPropSet + " doesn't exist", SC_NOT_FOUND);
        }
        model.setAllowContentBrowsing(
                Optional.ofNullable(model.getAllowContentBrowsing()).orElse(DEFAULT_ALLOW_CONTENT_BROWSING));
        model.setBlackedOut(Optional.ofNullable(model.isBlackedOut()).orElse(DEFAULT_BLACKED_OUT));
    }

    private void validateSharedTypeSpecific(TypeSpecificConfigModel model) {
        switch (model.getRepoType()) {
            case Gradle:
            case Ivy:
            case SBT:
                //Maven types suppress pom checks by default, maven does not
                MavenTypeSpecificConfigModel mavenTypes = (MavenTypeSpecificConfigModel) model;
                mavenTypes.setSuppressPomConsistencyChecks(Optional.ofNullable(mavenTypes.getSuppressPomConsistencyChecks())
                        .orElse(DEFAULT_SUPPRESS_POM_CHECKS));
            case Maven:
                MavenTypeSpecificConfigModel maven = (MavenTypeSpecificConfigModel) model;
                maven.setMaxUniqueSnapshots(
                        Optional.ofNullable(maven.getMaxUniqueSnapshots()).orElse(DEFAULT_MAX_UNIQUE_SNAPSHOTS));
                maven.setHandleReleases(Optional.ofNullable(maven.getHandleReleases()).orElse(DEFAULT_HANDLE_RELEASES));
                maven.setHandleSnapshots(
                        Optional.ofNullable(maven.getHandleSnapshots()).orElse(DEFAULT_HANDLE_SNAPSHOTS));
                maven.setSuppressPomConsistencyChecks(Optional.ofNullable(maven.getSuppressPomConsistencyChecks())
                        .orElse(DEFAULT_SUPPRESS_POM_CHECKS));
                break;
            case NuGet:
                NugetTypeSpecificConfigModel nuget = ((NugetTypeSpecificConfigModel) model);
                nuget.setForceNugetAuthentication(
                        Optional.ofNullable(nuget.isForceNugetAuthentication()).orElse(DEFAULT_FORCE_NUGET_AUTH));
                break;
        }
    }

    private void validateLocalTypeSpecific(TypeSpecificConfigModel model) throws RepoConfigException {
        validateSharedTypeSpecific(model);
        switch (model.getRepoType()) {
            case YUM:
                YumTypeSpecificConfigModel yum = (YumTypeSpecificConfigModel) model;
                yum.setGroupFileNames(Optional.ofNullable(yum.getGroupFileNames()).orElse(DEFAULT_YUM_GROUPFILE_NAME));
                yum.setMetadataFolderDepth(
                        Optional.ofNullable(yum.getMetadataFolderDepth()).orElse(DEFAULT_YUM_METADATA_DEPTH));
                yum.setAutoCalculateYumMetadata(
                        Optional.ofNullable(yum.isAutoCalculateYumMetadata()).orElse(DEFAULT_YUM_AUTO_CALCULATE));
                break;
            case Debian:
                DebTypeSpecificConfigModel deb = (DebTypeSpecificConfigModel) model;
                deb.setListRemoteFolderItems(
                        Optional.ofNullable(deb.isListRemoteFolderItems()).orElse(
                                DEFAULT_LIST_REMOTE_ITEMS_SUPPORTED_TYPE));
                deb.setTrivialLayout(Optional.ofNullable(deb.getTrivialLayout()).orElse(DEFAULT_DEB_TRIVIAL_LAYOUT));
                break;
            case Docker:
                DockerTypeSpecificConfigModel docker = (DockerTypeSpecificConfigModel) model;
                docker.setDockerApiVersion(
                        Optional.ofNullable(docker.getDockerApiVersion()).orElse(DEFAULT_DOCKER_API_VER));
                docker.setForceDockerAuthentication(
                        Optional.ofNullable(docker.isForceDockerAuthentication()).orElse(DEFAULT_FORCE_DOCKER_AUTH));
                break;
            case NuGet:
                NugetTypeSpecificConfigModel nuget = (NugetTypeSpecificConfigModel) model;
                nuget.setMaxUniqueSnapshots(
                        Optional.ofNullable(nuget.getMaxUniqueSnapshots()).orElse(DEFAULT_MAX_UNIQUE_SNAPSHOTS));
                break;
            case VCS:
                //Don't fail on bower local
                if (model.getRepoType().equals(RepoType.Bower)) {
                    break;
                }
            case P2:
                throw new RepoConfigException("Package type " + model.getRepoType().name()
                        + " is unsupported in local repositories", SC_BAD_REQUEST);
        }
    }

    private void validateRemoteTypeSpecific(TypeSpecificConfigModel model) throws RepoConfigException {
        validateSharedTypeSpecific(model);
        switch (model.getRepoType()) {
            case P2:
                P2TypeSpecificConfigModel p2 = (P2TypeSpecificConfigModel) model;
                p2.setSuppressPomConsistencyChecks(Optional.ofNullable(p2.getSuppressPomConsistencyChecks())
                        .orElse(DEFAULT_SUPPRESS_POM_CHECKS));
            case Maven:
            case Gradle:
            case Ivy:
            case SBT:
                MavenTypeSpecificConfigModel maven = (MavenTypeSpecificConfigModel) model;
                maven.setEagerlyFetchJars(
                        Optional.ofNullable(maven.getEagerlyFetchJars()).orElse(DEFAULT_EAGERLY_FETCH_JARS));
                maven.setEagerlyFetchSources(
                        Optional.ofNullable(maven.getEagerlyFetchSources()).orElse(DEFAULT_EAGERLY_FETCH_SOURCES));
                maven.setListRemoteFolderItems(Optional.ofNullable(maven.isListRemoteFolderItems())
                                .orElse(DEFAULT_LIST_REMOTE_ITEMS_SUPPORTED_TYPE));
                break;
            case Docker:
                DockerTypeSpecificConfigModel docker = (DockerTypeSpecificConfigModel) model;
                docker.setEnableTokenAuthentication(
                        Optional.ofNullable(docker.isEnableTokenAuthentication()).orElse(DEFAULT_TOKEN_AUTH));
                docker.setForceDockerAuthentication(
                        Optional.ofNullable(docker.isForceDockerAuthentication()).orElse(DEFAULT_FORCE_DOCKER_AUTH));
                docker.setListRemoteFolderItems(Optional.ofNullable(docker.isListRemoteFolderItems())
                        .orElse(DEFAULT_LIST_REMOTE_ITEMS_UNSUPPORTED_TYPE));
                break;
            case Bower:
                BowerTypeSpecificConfigModel bower = (BowerTypeSpecificConfigModel) model;
                bower.setRegistryUrl(Optional.ofNullable(bower.getRegistryUrl()).orElse(DEFAULT_BOWER_REGISTRY));
            case VCS: //also for bower
                VcsTypeSpecificConfigModel vcs = (VcsTypeSpecificConfigModel) model;
                validateVcsConfig(vcs);
                break;
            case NuGet:
                NugetTypeSpecificConfigModel nuGet = (NugetTypeSpecificConfigModel) model;
                nuGet.setDownloadContextPath(
                        Optional.ofNullable(nuGet.getDownloadContextPath()).orElse(DEFAULT_NUGET_DOWNLOAD_PATH));
                nuGet.setFeedContextPath(
                        Optional.ofNullable(nuGet.getFeedContextPath()).orElse(DEFAULT_NUGET_FEED_PATH));
                nuGet.setListRemoteFolderItems(Optional.ofNullable(nuGet.isListRemoteFolderItems())
                        .orElse(DEFAULT_LIST_REMOTE_ITEMS_UNSUPPORTED_TYPE));
                break;
            case Debian:
                DebTypeSpecificConfigModel deb = (DebTypeSpecificConfigModel) model;
                deb.setListRemoteFolderItems(Optional.ofNullable(deb.isListRemoteFolderItems())
                        .orElse(DEFAULT_LIST_REMOTE_ITEMS_SUPPORTED_TYPE));
                break;
            case YUM:
                YumTypeSpecificConfigModel yum = (YumTypeSpecificConfigModel) model;
                yum.setListRemoteFolderItems(Optional.ofNullable(yum.isListRemoteFolderItems())
                        .orElse(DEFAULT_LIST_REMOTE_ITEMS_SUPPORTED_TYPE));
                break;
            case Npm:
                NpmTypeSpecificConfigModel npm = (NpmTypeSpecificConfigModel) model;
                npm.setListRemoteFolderItems(Optional.ofNullable(npm.isListRemoteFolderItems())
                        .orElse(DEFAULT_LIST_REMOTE_ITEMS_SUPPORTED_TYPE));
                break;
            case Generic:
                GenericTypeSpecificConfigModel generic = (GenericTypeSpecificConfigModel) model;
                generic.setListRemoteFolderItems(Optional.ofNullable(generic.isListRemoteFolderItems())
                        .orElse(DEFAULT_LIST_REMOTE_ITEMS_SUPPORTED_TYPE));
                break;
            case Gems:
                GemsTypeSpecificConfigModel gems = (GemsTypeSpecificConfigModel) model;
                gems.setListRemoteFolderItems(Optional.ofNullable(gems.isListRemoteFolderItems())
                        .orElse(DEFAULT_LIST_REMOTE_ITEMS_SUPPORTED_TYPE));
                break;
            case Pypi:
                PypiTypeSpecificConfigModel pypi = (PypiTypeSpecificConfigModel) model;
                pypi.setListRemoteFolderItems(Optional.ofNullable(pypi.isListRemoteFolderItems())
                        .orElse(DEFAULT_LIST_REMOTE_ITEMS_SUPPORTED_TYPE));
                break;
            case Vagrant:
            case GitLfs:
                throw new RepoConfigException("Package type " + model.getRepoType().name()
                        + " is unsupported in remote repositories", SC_BAD_REQUEST);

        }
    }

    private void validateVirtualTypeSpecific(TypeSpecificConfigModel model) throws RepoConfigException {
        switch (model.getRepoType()) {
            case P2:
                P2TypeSpecificConfigModel p2 = (P2TypeSpecificConfigModel) model;
                p2.setP2Repos(Optional.ofNullable(p2.getP2Repos()).orElse(Lists.newArrayList()));
            case Maven:
            case Gradle:
            case Ivy:
            case SBT:
                MavenTypeSpecificConfigModel maven = (MavenTypeSpecificConfigModel) model;
                maven.setPomCleanupPolicy(
                        Optional.ofNullable(maven.getPomCleanupPolicy()).orElse(DEFAULT_POM_CLEANUP_POLICY));
                if (maven.getKeyPair() != null && !addonsManager.addonByType(ArtifactWebstartAddon.class)
                        .getKeyPairNames().contains(maven.getKeyPair())) {
                    throw new RepoConfigException("Keypair '" + maven.getKeyPair() + "' doesn't exist", SC_NOT_FOUND);
                }
                break;
            case VCS:
                //Don't fail on bower virtual
                if (model.getRepoType().equals(RepoType.Bower)) {
                    break;
                }
            case Debian:
            case Vagrant:
            case GitLfs:
            case YUM:
                throw new RepoConfigException("Package type " + model.getRepoType().name()
                        + " is unsupported in virtual repositories", SC_BAD_REQUEST);
        }
    }


    private void verifyAllSectionsExist(RepositoryConfigModel model) throws RepoConfigException {
        if (model.getGeneral() == null) {
            throw new RepoConfigException("Repository Key cannot be empty", SC_BAD_REQUEST);
        } else if (model.getBasic() == null) {
            throw new RepoConfigException("Basic configuration cannot be empty", SC_BAD_REQUEST);
        }
        if (model.getAdvanced() == null) {
            throw new RepoConfigException("Advanced configuration cannot be empty", SC_BAD_REQUEST);
        }
        if (model.getTypeSpecific() == null) {
            throw new RepoConfigException("Package type configuration cannot be empty", SC_BAD_REQUEST);
        }
    }

    public void validateRepoName(String repoKey) throws RepoConfigException {
        if (StringUtils.isBlank(repoKey)) {
            throw new RepoConfigException("Repository key cannot be empty", SC_BAD_REQUEST);
        }
        if (StringUtils.length(repoKey) > REPO_KEY_MAX_LENGTH) {
            throw new RepoConfigException("Repository key '" + repoKey + "' exceed maximum length", SC_BAD_REQUEST);
        }
        if (UiRequestUtils.isReservedName(repoKey)
                || VirtualRepoDescriptor.GLOBAL_VIRTUAL_REPO_KEY.equalsIgnoreCase(repoKey)) {
            throw new RepoConfigException("Repository key '" + repoKey + "' is a reserved name", SC_BAD_REQUEST);
        }
        if (repoKey.equals(".") || repoKey.equals("..") || repoKey.equals("&")) {
            throw new RepoConfigException("Invalid Repository name", SC_BAD_REQUEST);
        }
        // TODO: [by dan] make this stream-ey
        char[] nameChars = repoKey.toCharArray();
        for (char c : nameChars) {
            for (char fc : forbiddenChars) {
                if (c == fc) {
                    throw new RepoConfigException("Illegal character: '" + c + "'", SC_BAD_REQUEST);
                }
            }
        }
        String error = Verifier.checkXMLName(repoKey);
        if (StringUtils.isNotBlank(error)) {
            throw new RepoConfigException("Invalid Repository name: " + error, SC_BAD_REQUEST);
        }

        if (!centralConfig.getMutableDescriptor().isKeyAvailable(repoKey)) {
            throw new RepoConfigException("Repository name is already in use", SC_BAD_REQUEST);
        }
    }

    private void validateVcsConfig(VcsTypeSpecificConfigModel vcs) throws RepoConfigException {
        vcs.setVcsType(Optional.ofNullable(vcs.getVcsType()).orElse(DEFAULT_VCS_TYPE));
        vcs.setGitProvider(Optional.ofNullable(vcs.getGitProvider()).orElse(DEFAULT_GIT_PROVIDER));
        if (vcs.getGitProvider().equals(VcsGitProvider.CUSTOM)) {
            if (StringUtils.isBlank(vcs.getDownloadUrl())) {
                throw new RepoConfigException("Git Download URL is required for custom Git providers", SC_BAD_REQUEST);
            }
        } else if (StringUtils.isNotBlank(vcs.getDownloadUrl())) {
        //custom url is sent in model but not for custom provider
            vcs.setDownloadUrl(null);
        }
        vcs.setMaxUniqueSnapshots(
                Optional.ofNullable(vcs.getMaxUniqueSnapshots()).orElse(DEFAULT_MAX_UNIQUE_SNAPSHOTS));
        vcs.setListRemoteFolderItems(
                Optional.ofNullable(vcs.isListRemoteFolderItems()).orElse(DEFAULT_LIST_REMOTE_ITEMS_UNSUPPORTED_TYPE));
    }
}
