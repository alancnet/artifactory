package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.general.licenses;

import org.apache.http.HttpStatus;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.blackduck.BlackDuckAddon;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.licenses.GeneralTabLicenseModel;
import org.artifactory.ui.utils.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class QueryCodeCenterService implements RestService<GeneralTabLicenseModel> {

    @Autowired
    private AddonsManager addonsManager;

    @Autowired
    private AuthorizationService authService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        BlackDuckAddon blackDuckAddon = addonsManager.addonByType(BlackDuckAddon.class);
        RepoPath path = RequestUtils.getPathFromRequest(request);
        if (!blackDuckAddon.isEnableIntegration()) {
            response.error("Governance integration is not enabled").responseCode(HttpStatus.SC_BAD_REQUEST);
            return;
        } else if (!authService.canAnnotate(path)) {
            response.error("Insufficient permissions for operation").responseCode(HttpStatus.SC_UNAUTHORIZED);
            return;
        }
        if (!blackDuckAddon.queryCodeCenterForPath(path)) {
            response.error("Can't find component info, Check the log for additional information")
                    .responseCode(HttpStatus.SC_METHOD_FAILURE); //4:20 !
        } else {
            response.info("Governance information retrieved successfully");
        }
    }
}
