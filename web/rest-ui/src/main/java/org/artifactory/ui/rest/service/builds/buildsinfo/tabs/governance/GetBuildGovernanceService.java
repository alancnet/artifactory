package org.artifactory.ui.rest.service.builds.buildsinfo.tabs.governance;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.blackduck.BlackDuckAddon;
import org.artifactory.api.build.BuildService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.governance.BlackDuckApplicationInfo;
import org.artifactory.api.governance.GovernanceRequestInfo;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.ui.rest.model.builds.BuildGovernanceInfo;
import org.artifactory.ui.utils.DateUtils;
import org.jfrog.build.api.BlackDuckProperties;
import org.jfrog.build.api.Build;
import org.jfrog.build.api.Dependency;
import org.jfrog.build.api.Governance;
import org.jfrog.build.api.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetBuildGovernanceService extends AbstractBuildService {
    private static final Logger log = LoggerFactory.getLogger(GetBuildGovernanceService.class);

    @Autowired
    BuildService buildService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        try {
            String name = request.getPathParamByKey("name");
            String buildNumber = request.getPathParamByKey("number");
            String buildStarted = DateUtils.formatBuildDate(Long.parseLong(request.getPathParamByKey("date")));
            BlackDuckAddon blackDuckAddon = ContextHelper.get().beanForType(AddonsManager.class).addonByType(
                    BlackDuckAddon.class);
            boolean enableIntegration = blackDuckAddon.isEnableIntegration();
            if (!enableIntegration) {
                response.responseCode(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            // get current build
            Build build = getBuild(name, buildNumber, buildStarted, response);
            // get governance
            Governance governance = build.getGovernance();
            // fetch build info from black duck info
            fetchBlackDuckInfo(response, name, buildNumber, buildStarted, build, governance, enableIntegration);
        } catch (ParseException e) {
            log.error(e.toString());
        }
    }

    /**
     * fetch black duck info
     *
     * @param artifactoryResponse - encapsulated data related to response
     * @param name                - build name
     * @param buildNumber         - build number
     * @param buildStarted        - build started
     * @param build               - current  build model
     * @param governance          - governance model
     */
    private void fetchBlackDuckInfo(RestResponse artifactoryResponse, String name, String buildNumber,
            String buildStarted, Build build, Governance governance, boolean enableIntegration) {
        Set<String> defaultSelectedScopes;
        try {
            if (governance == null) {
                artifactoryResponse.iModel(new BuildGovernanceInfo());
                artifactoryResponse.warn("No Code Center application name or version found for this build.");
                return;
            }
            AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
            BlackDuckAddon blackDuckAddon = addonsManager.addonByType(BlackDuckAddon.class);
            BlackDuckProperties blackDuckProperties = governance.getBlackDuckProperties();
            if (blackDuckProperties != null) {
                String appName = blackDuckProperties.getAppName();
                String appVersion = blackDuckProperties.getAppVersion();
                defaultSelectedScopes = findScopes(build);
                if (appName == null) {
                    artifactoryResponse.iModel(new BuildGovernanceInfo());
                    artifactoryResponse.warn("No Code Center application name or version found for this build.");
                    return;
                }
                //fetch governance build info
                Collection<GovernanceRequestInfo> governanceRequestInfos = fetchBuildGovernanceInfo(artifactoryResponse,
                        name,
                        buildNumber, buildStarted, appName, appVersion, defaultSelectedScopes, blackDuckAddon,
                        enableIntegration);
                List<GovernanceRequestInfo> components = new ArrayList();
                List<GovernanceRequestInfo> publishedArtifacts = new ArrayList<>();
                if (governanceRequestInfos != null) {
                    filterRequests(governanceRequestInfos, components, publishedArtifacts);
                }
                // get black duck application info
                BlackDuckApplicationInfo blackDuckApplicationInfo = buildAppInfo(artifactoryResponse,
                        blackDuckAddon, appName, appVersion);
                if (blackDuckApplicationInfo == null) {
                    artifactoryResponse.warn("No Code Center application name or version found for this build.");
                }
                BuildGovernanceInfo buildGovernanceInfo = new BuildGovernanceInfo(components, publishedArtifacts,
                        defaultSelectedScopes, blackDuckApplicationInfo);
                artifactoryResponse.iModel(buildGovernanceInfo);
            }
        } catch (Exception e) {
            log.error(e.getLocalizedMessage().toString());
            artifactoryResponse.error(e.getCause().getMessage());
        }
    }

    /**
     * filter published and non published requests
     *
     * @param governanceRequestInfos - all black duck requests
     * @param components             - non published requests
     * @param publishedArtifacts     - published requests
     */
    private void filterRequests(Collection<GovernanceRequestInfo> governanceRequestInfos, List<GovernanceRequestInfo> components, List<GovernanceRequestInfo> publishedArtifacts) {
        governanceRequestInfos.forEach(request -> {
            if (request.isPublished()) {
                publishedArtifacts.add(request);
            } else {
                components.add(request);
            }
        });
    }

    /**
     * find configured scopes for this build
     *
     * @param build - current build
     * @return set of scopes
     */
    public Set<String> findScopes(Build build) {
        final Set<String> scopes = Sets.newHashSet();
        if (build.getModules() != null) {
            for (Module module : build.getModules()) {
                if (module.getDependencies() != null) {
                    for (Dependency dependency : module.getDependencies()) {
                        Set<String> dependencyScopes = dependency.getScopes();
                        if (dependencyScopes != null) {
                            for (String dependencyScope : dependencyScopes) {
                                if (StringUtils.isBlank(dependencyScope)) {
                                    scopes.add("unspecified");
                                } else {
                                    scopes.add(dependencyScope);
                                }
                            }
                        }
                    }
                }
            }
        }
        return scopes;
    }

    /**
     * @param artifactoryResponse - encapsulate data related to response
     * @param name                - build name - build name
     * @param buildNumber         - build number - build number
     * @param buildStarted        - build date - build date
     */
    private Collection<GovernanceRequestInfo> fetchBuildGovernanceInfo(RestResponse artifactoryResponse, String name,
                                                                       String buildNumber, String buildStarted, String appName, String appVersion,
            Set<String> scopes, BlackDuckAddon blackDuckAddon, boolean enableIntegration) {
        Build build = getBuild(name, buildNumber, buildStarted, artifactoryResponse);
        Collection<GovernanceRequestInfo> buildRequests;
        if (enableIntegration) {
            buildRequests = blackDuckAddon.getGovernanceRequestInfos(build, appName, appVersion, scopes);
            if (buildRequests == null) {
                artifactoryResponse.warn("No Code Center application name or version found for this build.");
            }
        } else {
            buildRequests = Lists.newArrayList();
        }
        return buildRequests;
    }


    /**
     * build application info
     *
     * @param response       - encapsulate data related to response
     * @param blackDuckAddon - black duck addon
     * @param appName        - application name
     * @param appVersion     - application version
     * @return black suck application info details
     */
    private BlackDuckApplicationInfo buildAppInfo(RestResponse response, BlackDuckAddon blackDuckAddon,
                                                  String appName, String appVersion) {
        BlackDuckApplicationInfo appInfo;
        try {
            appInfo = blackDuckAddon.blackDuckApplicationInfo(appName, appVersion);
            if (appInfo.getVersion() == null) {
                appInfo.setVersion(appVersion);
            }
            if (appInfo == null) {
                response.error("Could not get Code Center application " + appName + " : " + appVersion);
                return null;
            }
        } catch (Exception e) {
            appInfo = new BlackDuckApplicationInfo(appName, appVersion); //empty
            response.error("Could not get Code Center application " + appName + " : " + appVersion);
            log.error("Could not get Code Center application " + appName + " : " + appVersion, e);
        }
        if (appInfo == null) {
            appInfo = new BlackDuckApplicationInfo(appName, appVersion);
        }
        return appInfo;
    }
}
