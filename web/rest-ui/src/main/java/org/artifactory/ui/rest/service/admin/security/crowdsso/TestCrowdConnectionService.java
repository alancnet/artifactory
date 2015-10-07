package org.artifactory.ui.rest.service.admin.security.crowdsso;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.crowd.CrowdAddon;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.descriptor.security.sso.CrowdSettings;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.security.crypto.CryptoHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class TestCrowdConnectionService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(TestCrowdConnectionService.class);

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        CrowdSettings crowdSettings = (CrowdSettings) request.getImodel();
        // test crowd connection
        TestCrowdConnection(response, crowdSettings);
    }

    /**
     * test crowd connection to server
     *
     * @param artifactoryResponse - encapsulate data require to response
     * @param crowdSettings       - crowd settings
     */
    private void TestCrowdConnection(RestResponse artifactoryResponse, CrowdSettings crowdSettings) {
        crowdSettings.setPassword(CryptoHelper.encryptIfNeeded(crowdSettings.getPassword()));
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        CrowdAddon crowdAddon = addonsManager.addonByType(CrowdAddon.class);
        try {
            if (crowdSettings.isEnableIntegration()) {
                crowdAddon.testCrowdConnection(crowdSettings);
                artifactoryResponse.info("Successfully connected to Atlassian Crowd.");
            }
        }
        catch (Exception e) {
            if(e.getMessage().contains("java.net.UnknownHostException")){
                artifactoryResponse.error("An error occurred while testing the new settings: " +
                        "Host \"" + crowdSettings.getServerUrl() + "\" not found");
            }
            else {
                artifactoryResponse.error("An error occurred while testing the new settings: " +
                        e.getClass().getSimpleName() + " - " + e.getMessage());
            }
            log.error("An error occurred while testing the new Crowd SSO settings", e);
        }
    }
}
