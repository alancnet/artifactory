package org.artifactory.ui.rest.service.admin.configuration.blackduck;

import java.io.IOException;
import java.net.UnknownHostException;

import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.blackduck.BlackDuckAddon;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.descriptor.external.BlackDuckSettingsDescriptor;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.util.ExceptionUtils;
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
public class TestBlackDuckService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(TestBlackDuckService.class);

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {

        BlackDuckSettingsDescriptor blackDuckSettingsDescriptor = (BlackDuckSettingsDescriptor) request.getImodel();
        // get black duck addon
        BlackDuckAddon blackDuckAddon = ContextHelper.get().beanForType(AddonsManager.class).addonByType(
                BlackDuckAddon.class);
        // test connection to black duck
        testBlackDuckConnection(response, blackDuckSettingsDescriptor, blackDuckAddon);
    }

    /**
     * Test connection to black duck
     *
     * @param artifactoryResponse         - encapsulate data related to response
     * @param blackDuckSettingsDescriptor - black duck descriptor
     * @param blackDuckAddon              - black duck add on
     */
    private void testBlackDuckConnection(RestResponse artifactoryResponse,
            BlackDuckSettingsDescriptor blackDuckSettingsDescriptor, BlackDuckAddon blackDuckAddon) {
        try {
            blackDuckAddon.testConnection(blackDuckSettingsDescriptor);
            artifactoryResponse.info("Target is a valid Code Center instance.");
        } catch (Exception e) {
            log.error("Error testing connection to black duck URI: {}",
                    blackDuckSettingsDescriptor.getServerUri(), e);
            artifactoryResponse.error("Couldn't connect to code center: " + getErrorMessage(e));
        }
    }

    private String getErrorMessage(Exception e) {
        String message = e.getMessage(); // default message
        Throwable ioException = ExceptionUtils.getCauseOfTypes(e, IOException.class);
        if (ioException != null) {
            if (ioException instanceof UnknownHostException) {
                message = "Unknown host - " + ioException.getMessage();
            } else {
                message = ioException.getMessage();
            }
        }
        if (StringUtils.isBlank(message)) {
            message = ioException.getClass().toString();
        }
        return message;
    }
}
