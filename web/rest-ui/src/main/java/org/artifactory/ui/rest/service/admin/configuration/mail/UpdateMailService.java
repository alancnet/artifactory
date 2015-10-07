package org.artifactory.ui.rest.service.admin.configuration.mail;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.configuration.mail.MailServer;
import org.artifactory.ui.rest.service.utils.AolUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UpdateMailService implements RestService {

    @Autowired
    private CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        AolUtils.assertNotAol("UpdateMail");
        MutableCentralConfigDescriptor configDescriptor = centralConfigService.getMutableDescriptor();
        saveMailChangesToDescriptor(request, configDescriptor, response);
    }

    /**
     * save mail changes to descriptor
     *
     * @param artifactoryRequest - encapsulate artifactory data related  to request
     * @param configDescriptor   - config descriptor without new maul changes
     */
    private void saveMailChangesToDescriptor(ArtifactoryRestRequest artifactoryRequest,
            MutableCentralConfigDescriptor configDescriptor, RestResponse artifactoryResponse) {
        MailServer mailServer = (MailServer) artifactoryRequest.getImodel();
        configDescriptor.setMailServer(mailServer);
        centralConfigService.saveEditedDescriptorAndReload(configDescriptor);
        artifactoryResponse.info("Successfully updated Mail server settings");
    }
}
