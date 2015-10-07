package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.blackduck;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.blackduck.BlackDuckAddon;
import org.artifactory.addon.blackduck.BlackduckInfo;
import org.artifactory.addon.blackduck.ExternalComponentInfo;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.configuration.blackduck.BlackDuck;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.blackduck.BlackDuckArtifactInfo;
import org.artifactory.ui.rest.service.admin.configuration.blackduck.GetBlackDuckService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPFaultException;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetBlackDuckArtifactService implements RestService {
    public static final String N_A = "Not Found";
    private static final Logger log = LoggerFactory.getLogger(GetBlackDuckArtifactService.class);

    @Autowired
    GetBlackDuckService getBlackDuckService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        BlackDuckArtifactInfo duckArtifactInfo = (BlackDuckArtifactInfo) request.getImodel();
        String path = duckArtifactInfo.getPath();
        String repoKey = duckArtifactInfo.getRepoKey();
        RepoPath repoPath = InternalRepoPathFactory.create(repoKey, path);
        BlackDuck blackDuck = getBlackDuckConfig(request, response);
        boolean isBlackduckEnable = blackDuck.isEnableIntegration();
        // if black duck enable get ext component info
        fetchArtifactBlackDuckInfo(response, repoPath, blackDuck, isBlackduckEnable);
    }

    /**
     * @param restRequest  - encapsulate data related to request
     * @param restResponse - encapsulate data reqquire for response
     * @return - black duck config model
     */
    private BlackDuck getBlackDuckConfig(ArtifactoryRestRequest restRequest, RestResponse<? extends RestModel> restResponse) {
        // get black duck config and check if enable
        getBlackDuckService.execute(restRequest, restResponse);
        RestModel restModel = restResponse.getIModel();
        BlackDuck blackDuck;
        if (restModel instanceof BlackDuck) {
            blackDuck = (BlackDuck) restModel;
        } else {
            blackDuck = new BlackDuck();
            blackDuck.setEnableIntegration(false);
        }
        return blackDuck;
    }

    /**
     * fetch artifact info from black duck
     *
     * @param artifactoryResponse - encapsulate data related to response
     * @param repoPath            - repo path
     * @param blackDuck           - black duck data model
     * @param isBlackduckEnable   - if true - black duck enable
     */
    private void fetchArtifactBlackDuckInfo(RestResponse artifactoryResponse, RepoPath repoPath, BlackDuck blackDuck, boolean isBlackduckEnable) {
        if (isBlackduckEnable) {
            // get black duck addon
            BlackDuckAddon blackDuckAddon = ContextHelper.get().beanForType(AddonsManager.class).addonByType(
                    BlackDuckAddon.class);
            if (blackDuck != null) {
                // build black duck artifact info
                BlackDuckArtifactInfo blackDuckArtifactInfo = buildComponentInfo(isBlackduckEnable, artifactoryResponse,
                        repoPath, blackDuckAddon);
                // update response with model
                artifactoryResponse.iModel(blackDuckArtifactInfo);
            }
        } else {
            artifactoryResponse.iModel(null);
                artifactoryResponse.warn("Governance integration is not enabled. Check the configuration on the Admin tab to enable.");
            }
    }

    /**
     * @param online -  if true black suck online
     * @param response - encapsulate data related to response
     * @param repoPath - repo path
     * @return - black duck model
     */
    private BlackDuckArtifactInfo buildComponentInfo(boolean online, RestResponse response, RepoPath repoPath,
            BlackDuckAddon blackDuckAddon) {
        ExternalComponentInfo info = null;
        BlackDuckArtifactInfo blackDuckArtifactInfo = null;
        //prepare info
        if (online) {
            try {
                info = getComponentInfoAndWarnIfNeeded(response, repoPath, blackDuckAddon);
                blackDuckArtifactInfo = new BlackDuckArtifactInfo();
                // update black duck artifact info model
                updateBlackDuckInfoModel(info, blackDuckArtifactInfo);
            } catch (Exception e) {
                if (e instanceof WebServiceException) {
                    if (e instanceof SOAPFaultException) {
                        response.error(
                                "Could not connect to Black Duck Code Center. Check the configuration on the Admin tab.");
                        return new BlackDuckArtifactInfo();
                    } else {
                        handleConnectionError(e, response);
                    }
                } else {
                    handleBDSdkError(e, response);
                }
            }
        } else {
            blackDuckArtifactInfo = new BlackDuckArtifactInfo();
        }
        //component not found
        if (info == null) {
            blackDuckArtifactInfo = handleComponentNotFound(response, repoPath, blackDuckAddon);
        }
        return blackDuckArtifactInfo;
    }

    /**
     * update black suck model
     *
     * @param info                  - info from black duck
     * @param blackDuckArtifactInfo - black duck model
     */
    private void updateBlackDuckInfoModel(ExternalComponentInfo info, BlackDuckArtifactInfo blackDuckArtifactInfo) {
        if (info.getInfo() != null) {
            blackDuckArtifactInfo.setInfo(info.getInfo());
        }
        if (info.getVulnerabilities() != null && !info.getVulnerabilities().isEmpty()) {
            blackDuckArtifactInfo.setVulnerabilities(info.getVulnerabilities());
        }
        if (info.getLicense() != null && !info.getLicense().isEmpty()) {
            blackDuckArtifactInfo.setLicense(info.getLicense());
        }
    }

    /**
     * handle BDSdk error response
     * @param e - exception
     * @param response - encapsulate data related to response
     */
    private void handleBDSdkError(Exception e, RestResponse response) {
        String msg = "Error getting component info";
        log.debug(msg, e);
        response.error(msg + ": " + e.getMessage());
    }

    /**
     * handle connection error response
     * @param e - exception
     * @param response - encapsulate data related to response
     */
    private void handleConnectionError(Exception e, RestResponse response) {
        String msg = "Could not connect to Black Duck Code Center. Check the configuration on the Admin tab.";
        log.debug(msg, e);
        response.error(msg);
    }

    /**
     * @param response - encapsulate data related to response
     * @param repoPath - repo path
     * @return
     */
    private ExternalComponentInfo getComponentInfoAndWarnIfNeeded(RestResponse response, RepoPath repoPath,
            BlackDuckAddon blackDuckAddon) {
        ExternalComponentInfo info;
        info = blackDuckAddon.getBlackduckInfo(repoPath);
        String componentExternalIdFromProperty = blackDuckAddon.getComponentExternalIdFromProperty(repoPath);
        if (componentExternalIdFromProperty != null && componentExternalIdFromProperty.length() > 0) {
            info.getInfo().setExtComponentId(componentExternalIdFromProperty);
        } else {
            info.getInfo().setExtComponentId("");
        }
        if (info != null) {
            if (!info.getInfo().isCatalogComponent()) {
                response.warn(
                        "Could not find artifact in Catalog. Information displayed from Knowledge base component.");
            } else if (blackDuckAddon.getComponentExternalIdFromProperty(repoPath) != null) {
                response.warn("Component ID overridden by manual edit to " + info.getInfo().getComponentId());
            }
        }
        return info;
    }

    /**
     * if component was not found return relevant response
     *
     * @param response - encapsulate data require for response
     * @param repoPath - repo path
     * @return
     */
    private BlackDuckArtifactInfo handleComponentNotFound(RestResponse response, RepoPath repoPath,
            BlackDuckAddon blackDuckAddon) {
        BlackDuckArtifactInfo info = new BlackDuckArtifactInfo();
        info.setName(N_A);
        String componentId = blackDuckAddon.getComponentIdFromProperty(repoPath);
        if (componentId != null) {
            if (info.getInfo() == null) {
                info.setInfo(new BlackduckInfo());
            }
            info.getInfo().setName(componentId);
            info.getInfo().setDescription("A component with the ID " + componentId + " was not found.");
            response.warn("No Component was found with ID " + componentId + ".");
        } else {
            info.setInfo(new BlackduckInfo());
            info.getInfo().setComponentId(N_A); //case: on error
            info.getInfo().setDescription("No component found for " + repoPath.getName());
            response.error("No component found for " + repoPath.getName() + ".");
        }
        return info;
    }
}
