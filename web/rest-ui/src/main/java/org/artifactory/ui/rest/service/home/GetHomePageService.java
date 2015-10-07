package org.artifactory.ui.rest.service.home;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.artifactory.addon.AddonInfo;
import org.artifactory.addon.AddonState;
import org.artifactory.addon.AddonType;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.CoreAddons;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.version.VersionHolder;
import org.artifactory.api.version.VersionInfoService;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.property.ArtifactorySystemProperties;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.home.AddonModel;
import org.artifactory.ui.rest.model.home.HomeModel;
import org.artifactory.ui.utils.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author chen keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetHomePageService implements RestService {
    private static final String ARTIFACTORY_ACCOUNT_MANAGEMENT_URL = "artifactory.accountManagement.url";
    public static final String DEFAULT_ACCOUNT_MANAGEMENT_URL = "http://localhost:8086/dashboard/webapp";

    private String accountManagementUrl = DEFAULT_ACCOUNT_MANAGEMENT_URL;

    @Autowired
    AuthorizationService authorizationService;

    @Autowired
    CentralConfigService centralConfigService;

    @Autowired
    RepositoryService repositoryService;


    @PostConstruct
    private void initialize() {
        ArtifactorySystemProperties artifactorySystemProperties = ArtifactoryHome.get().getArtifactoryProperties();
        accountManagementUrl = artifactorySystemProperties.getProperty(ARTIFACTORY_ACCOUNT_MANAGEMENT_URL, DEFAULT_ACCOUNT_MANAGEMENT_URL);
    }

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        List<AddonInfo> installedAddons = addonsManager.getInstalledAddons(null);

        HomeModel homeModel = new HomeModel();
        // update home addon model
        updateHomeModel(homeModel, request);
        HashMap<String, AddonInfo> addonInfoMap = new HashMap<>();
        installedAddons.forEach(addonInfo -> addonInfoMap.put(addonInfo.getAddonName(), addonInfo));
        List<AddonModel> addonModels = new ArrayList<>();
        // update addon data
        updateAddonList(addonInfoMap, addonModels);
        homeModel.setAddons(addonModels);
        homeModel.setUpTime(getUptime());
        homeModel.setAccountManagementLink(getAccountManagementLink());
        homeModel.setDisplayAccountManagementLink(displayAccountManagementLink());
        response.iModel(homeModel);
    }

    /**
     * return system up time
     *
     * @return up time as string
     */
    public String getUptime() {
        long uptime = ContextHelper.get().getUptime();
        String uptimeStr = DurationFormatUtils.formatDuration(uptime, "d'd' H'h' m'm' s's'");
        return uptimeStr;
    }


    /**
     * update home model data
     *
     * @param homeModel - home model object
     */
    private void updateHomeModel(HomeModel homeModel, ArtifactoryRestRequest request) {
        Map<String, String> headersMap = RequestUtils.getHeadersMap(request.getServletRequest());
        String currentVersion = ConstantValues.artifactoryVersion.getString();
        VersionInfoService versionInfoService = ContextHelper.get().beanForType(VersionInfoService.class);
        VersionHolder versionHolder = versionInfoService.getLatestVersion(headersMap, true);
        CentralConfigDescriptor configDescriptor = centralConfigService.getDescriptor();
        updaateLatestVersion(homeModel, versionHolder, configDescriptor);
        homeModel.setVersion(currentVersion);
        homeModel.setArtifacts(getArtifactsCount());

    }

    /**
     * update latest version data and link
     *
     * @param homeModel        - home model
     * @param versionHolder    - version holder
     * @param configDescriptor - config descriptor
     */
    private void updaateLatestVersion(HomeModel homeModel, VersionHolder versionHolder,
            CentralConfigDescriptor configDescriptor) {
        if (ConstantValues.versionQueryEnabled.getBoolean() && !configDescriptor.isOfflineMode()) {
            String latestVersion = versionHolder.getVersion();
            String latestVersionUrl = versionHolder.getDownloadUrl();
            if (latestVersion != null && !latestVersion.equals("NA")) {
                homeModel.setLatestRelease(latestVersion);
            }
            homeModel.setLatestReleaseLink(latestVersionUrl);
        }
    }

    /**
     * update addon list data
     *
     * @param addonInfoMap - addon info map
     * @param addonModels  - addons models
     */
    private void updateAddonList(HashMap<String, AddonInfo> addonInfoMap, List<AddonModel> addonModels) {
        if (!isAol()) {
            addonModels.add(new AddonModel(AddonType.HA, addonInfoMap.get("ha"), getAddonLearnMoreUrl("ha"),
                    getAddonConfigureUrl(AddonType.HA.getConfigureUrlSuffix())));
        }
        addonModels.add(new AddonModel(AddonType.BINTRAY_INTEGRATION,
                getAddonInfo(AddonType.BINTRAY_INTEGRATION, AddonState.ACTIVATED),
                getAddonLearnMoreUrl("bintrayIntegration"), getAddonConfigureUrl(
                AddonType.BINTRAY_INTEGRATION.getConfigureUrlSuffix())));
        addonModels.add(new AddonModel(AddonType.BUILD, addonInfoMap.get("build"), getAddonLearnMoreUrl("build"), getAddonConfigureUrl(AddonType.BUILD.getConfigureUrlSuffix())));
        addonModels.add(new AddonModel(AddonType.DOCKER, addonInfoMap.get("docker"), getAddonLearnMoreUrl("docker"), getAddonConfigureUrl(AddonType.DOCKER.getConfigureUrlSuffix())));
        addonModels.add(new AddonModel(AddonType.REPLICATION, addonInfoMap.get("replication"), getAddonLearnMoreUrl("replication"), getAddonConfigureUrl(AddonType.REPLICATION.getConfigureUrlSuffix())));
        addonModels.add(new AddonModel(AddonType.MULTIPUSH, getAddonInfo(AddonType.MULTIPUSH), String.format(ConstantValues.addonsInfoUrl.getString(), "replication"), getAddonConfigureUrl(AddonType.MULTIPUSH.getConfigureUrlSuffix())));
        addonModels.add(new AddonModel(AddonType.AQL, getAqlAddonInfo(), getAddonLearnMoreUrl("aql"), getAddonConfigureUrl(AddonType.AQL.getConfigureUrlSuffix())));
        addonModels.add(new AddonModel(AddonType.FILE_STORE, getAddonInfo(AddonType.FILE_STORE),
                getAddonLearnMoreUrl("filestore"), getAddonConfigureUrl(AddonType.FILE_STORE.getConfigureUrlSuffix())));
        AddonInfo aolAddonPlugin;
        aolAddonPlugin = getUserPluginAddonInfo(addonInfoMap);
        addonModels.add(new AddonModel(AddonType.PLUGINS, aolAddonPlugin, getAddonLearnMoreUrl("plugins"),
                getAddonConfigureUrl(AddonType.PLUGINS.getConfigureUrlSuffix())));
        addonModels.add(new AddonModel(AddonType.NUGET, addonInfoMap.get("nuget"), getAddonLearnMoreUrl("nuget"), getAddonConfigureUrl(AddonType.NUGET.getConfigureUrlSuffix())));
        addonModels.add(new AddonModel(AddonType.NPM, addonInfoMap.get("npm"), getAddonLearnMoreUrl("npm"), getAddonConfigureUrl(AddonType.NPM.getConfigureUrlSuffix())));
        addonModels.add(new AddonModel(AddonType.BOWER, addonInfoMap.get("bower"), getAddonLearnMoreUrl("bower"), getAddonConfigureUrl(AddonType.BOWER.getConfigureUrlSuffix())));
        addonModels.add(new AddonModel(AddonType.REST, addonInfoMap.get("rest"), getAddonLearnMoreUrl("rest"), getAddonConfigureUrl(AddonType.REST.getConfigureUrlSuffix())));
        addonModels.add(new AddonModel(AddonType.GITLFS, addonInfoMap.get("git-lfs"), getAddonLearnMoreUrl("git-lfs"), getAddonConfigureUrl(AddonType.GITLFS.getConfigureUrlSuffix())));
        addonModels.add(new AddonModel(AddonType.VAGRANT, addonInfoMap.get("vagrant"), getAddonLearnMoreUrl("vagrant"), getAddonConfigureUrl(AddonType.VAGRANT.getConfigureUrlSuffix())));
        addonModels.add(new AddonModel(AddonType.LDAP, addonInfoMap.get("ldap"), getAddonLearnMoreUrl("ldap"), getAddonConfigureUrl(AddonType.LDAP.getConfigureUrlSuffix())));
        addonModels.add(new AddonModel(AddonType.SSO, addonInfoMap.get("sso"), getAddonLearnMoreUrl("sso"), getAddonConfigureUrl(AddonType.SSO.getConfigureUrlSuffix())));
        addonModels.add(new AddonModel(AddonType.VCS, addonInfoMap.get("vcs"), getAddonLearnMoreUrl("vcs"), getAddonConfigureUrl(AddonType.VCS.getConfigureUrlSuffix())));
        addonModels.add(new AddonModel(AddonType.YUM, addonInfoMap.get("yum"), getAddonLearnMoreUrl("yum"), getAddonConfigureUrl(AddonType.YUM.getConfigureUrlSuffix())));
        addonModels.add(new AddonModel(AddonType.DEBIAN, addonInfoMap.get("debian"), getAddonLearnMoreUrl("debian"), getAddonConfigureUrl(AddonType.DEBIAN.getConfigureUrlSuffix())));
        addonModels.add(new AddonModel(AddonType.GEMS, addonInfoMap.get("gems"), getAddonLearnMoreUrl("gems"), getAddonConfigureUrl(AddonType.GEMS.getConfigureUrlSuffix())));
        addonModels.add(new AddonModel(AddonType.PYPI, addonInfoMap.get("pypi"), getAddonLearnMoreUrl("pypi"), getAddonConfigureUrl(AddonType.PYPI.getConfigureUrlSuffix())));
        addonModels.add(new AddonModel(AddonType.PROPERTIES, addonInfoMap.get("properties"), getAddonLearnMoreUrl("properties"), getAddonConfigureUrl(AddonType.PROPERTIES.getConfigureUrlSuffix())));
        addonModels.add(new AddonModel(AddonType.SEARCH, addonInfoMap.get("search"), getAddonLearnMoreUrl("search"), getAddonConfigureUrl(AddonType.SEARCH.getConfigureUrlSuffix())));
        addonModels.add(new AddonModel(AddonType.LAYOUTS, addonInfoMap.get("layouts"), getAddonLearnMoreUrl("layouts"), getAddonConfigureUrl(AddonType.LAYOUTS.getConfigureUrlSuffix())));
        addonModels.add(new AddonModel(AddonType.LICENSES, addonInfoMap.get("license"), getAddonLearnMoreUrl("license"), getAddonConfigureUrl(AddonType.LICENSES.getConfigureUrlSuffix())));
        addonModels.add(new AddonModel(AddonType.BLACKDUCK, addonInfoMap.get("blackduck"), getAddonLearnMoreUrl("blackduck"), getAddonConfigureUrl(AddonType.BLACKDUCK.getConfigureUrlSuffix())));
        addonModels.add(
                new AddonModel(AddonType.MAVEN_PLUGIN, getAddonInfo(AddonType.MAVEN_PLUGIN, AddonState.ACTIVATED),
                        getAddonLearnMoreUrl("mavenPlugin"),
                        getAddonConfigureUrl(AddonType.MAVEN_PLUGIN.getConfigureUrlSuffix())));
        addonModels.add(new AddonModel(AddonType.GRADLE_PLUGIN,
                getAddonInfo(AddonType.GRADLE_PLUGIN, AddonState.ACTIVATED),
                getAddonLearnMoreUrl("gradlePlugin"),
                getAddonConfigureUrl(AddonType.GRADLE_PLUGIN.getConfigureUrlSuffix())));
        addonModels.add(new AddonModel(AddonType.JENKINS_PLUGIN,
                getAddonInfo(AddonType.JENKINS_PLUGIN, AddonState.ACTIVATED),
                getAddonLearnMoreUrl("jenkinsPlugin"),
                getAddonConfigureUrl(AddonType.JENKINS_PLUGIN.getConfigureUrlSuffix())));
        addonModels.add(new AddonModel(AddonType.BAMBOO_PLUGIN,
                getAddonInfo(AddonType.BAMBOO_PLUGIN, AddonState.ACTIVATED ),
                getAddonLearnMoreUrl("bambooPlugin"),
                getAddonConfigureUrl(AddonType.BAMBOO_PLUGIN.getConfigureUrlSuffix())));
        addonModels.add(new AddonModel(AddonType.TC_PLUGIN,
                getAddonInfo(AddonType.TC_PLUGIN, AddonState.ACTIVATED),
                getAddonLearnMoreUrl("tcPlugin"),
                getAddonConfigureUrl(AddonType.TC_PLUGIN.getConfigureUrlSuffix())));
        addonModels.add(new AddonModel(AddonType.MSBUILD_PLUGIN,
                getAddonInfo(AddonType.MSBUILD_PLUGIN, AddonState.ACTIVATED ),
                getAddonLearnMoreUrl("tfs-integration"),
                getAddonConfigureUrl(AddonType.MSBUILD_PLUGIN.getConfigureUrlSuffix())));
        addonModels.add(new AddonModel(AddonType.FILTERED_RESOURCES, addonInfoMap.get("filtered-resources"), getAddonLearnMoreUrl("filtered-resources"), getAddonConfigureUrl(AddonType.FILTERED_RESOURCES.getConfigureUrlSuffix())));
        addonModels.add(new AddonModel(AddonType.P2, addonInfoMap.get("p2"), getAddonLearnMoreUrl("p2"), getAddonConfigureUrl(AddonType.P2.getConfigureUrlSuffix())));
        addonModels.add(new AddonModel(AddonType.WEBSTART, addonInfoMap.get("webstart"), getAddonLearnMoreUrl("webstart"), getAddonConfigureUrl(AddonType.WEBSTART.getConfigureUrlSuffix())));

    }

    /**
     * get User plugin for aol or pro
     * @param addonInfoMap - addon info map
     * @return
     */
    private AddonInfo getUserPluginAddonInfo(HashMap<String, AddonInfo> addonInfoMap) {
        AddonInfo aolAddonPlugin;
        if (isAol()) {
            aolAddonPlugin = getAddonInfo(AddonType.PLUGINS, AddonState.INACTIVATED);
        } else {
            aolAddonPlugin = addonInfoMap.get("plugins");
        }
        return aolAddonPlugin;
    }

    /**
     * @return global artifact count
     */
    private long getArtifactsCount() {
        long count = repositoryService.getArtifactCount();
        return count;
    }

    /**
     * return add on lean more url
     * @param addonId - addon id
     * @return
     */
    private String getAddonLearnMoreUrl(String addonId) {
        return String.format(ConstantValues.addonsInfoUrl.getString(), addonId);
    }

    /**
     * return add on configure more url
     * @param addonId - addon id
     * @return
     */
    private String getAddonConfigureUrl(String addonId) {
        return String.format(ConstantValues.addonsConfigureUrl.getString(), addonId);
    }

    private AddonInfo getAqlAddonInfo() {
        AddonInfo addonInfo = new AddonInfo(AddonType.AQL.getAddonName(),
                AddonType.AQL.getAddonDisplayName(), "", AddonState.ACTIVATED, new Properties(), 10);
        return addonInfo;
    }

    /**
     * get addon info
     * @param type - addon type
     * @return
     */
    private AddonInfo getAddonInfo(AddonType type) {
        boolean haLicensed = ContextHelper.get().beanForType(AddonsManager.class).isHaLicensed();
        AddonInfo addonInfo = new AddonInfo(type.getAddonName(),
                type.getAddonDisplayName(), "",
                (haLicensed) ? AddonState.ACTIVATED : AddonState.NOT_LICENSED, new Properties(), 10);
        return addonInfo;
    }

    /**
     * get addon info
     * @param type - addon type
     * @return
     */
    private AddonInfo getAddonInfo(AddonType type, AddonState state) {
        AddonInfo addonInfo = new AddonInfo(type.getAddonName(),
                type.getAddonDisplayName(), "", state, new Properties(), 10);
        return addonInfo;
    }

    /**
     * if true - aol license
     * @return
     */
    private boolean isAol() {
        return ContextHelper.get().beanForType(AddonsManager.class).addonByType(CoreAddons.class).isAol();
    }

    /**
     * display account managements link
     * @return
     */
    private boolean displayAccountManagementLink() {
        return isAol() && ConstantValues.aolDisplayAccountManagementLink.getBoolean();
    }

    /**
     * get account managements link
     * @return
     */
    private String getAccountManagementLink() {
        return accountManagementUrl;
    }
}
