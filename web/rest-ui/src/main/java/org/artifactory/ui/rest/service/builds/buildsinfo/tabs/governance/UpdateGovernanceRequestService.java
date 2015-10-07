package org.artifactory.ui.rest.service.builds.buildsinfo.tabs.governance;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.blackduck.BlackDuckAddon;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.governance.GovernanceRequestInfo;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.ui.rest.model.builds.BuildGovernanceInfo;
import org.artifactory.ui.utils.DateUtils;
import org.jfrog.build.api.Build;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.text.ParseException;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UpdateGovernanceRequestService extends AbstractBuildService {
    private static final Logger log = LoggerFactory.getLogger(AbstractBuildService.class);


    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        try {
            String name = request.getPathParamByKey("name");
            String buildNumber = request.getPathParamByKey("number");
            String buildStarted = DateUtils.formatBuildDate(Long.parseLong(request.getPathParamByKey("date")));
            updateRequestAndResponse(request, response, name, buildNumber, buildStarted);
        } catch (ParseException e) {
            log.error("error updating request");
            response.error("error updating request");
        }
    }

    /**
     * update request and return status
     *
     * @param artifactoryRequest  - encapsulate data related to request
     * @param artifactoryResponse - encapsulate data related to response
     * @param name                - build name
     * @param buildNumber         - build number
     * @param buildStarted        - build start time
     */
    private void updateRequestAndResponse(ArtifactoryRestRequest artifactoryRequest, RestResponse artifactoryResponse, String name, String buildNumber, String buildStarted) {
        Build build = getBuild(name, buildNumber, buildStarted, artifactoryResponse);
        BuildGovernanceInfo buildGovernanceInfo = (BuildGovernanceInfo) artifactoryRequest.getImodel();
        GovernanceRequestInfo governanceRequestInfo = buildGovernanceInfo.getComponents().get(0);
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        BlackDuckAddon blackDuckAddon = addonsManager.addonByType(BlackDuckAddon.class);
        String status = blackDuckAddon.updateRequest(build, governanceRequestInfo);

        if (status.equals("Update request was successful")) {
            artifactoryResponse.info(status);
        } else {
            artifactoryResponse.error(status);
        }
    }
}
