package org.artifactory.ui.rest.service.admin.configuration.bintray;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.bintray.BintrayConfigDescriptor;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UpdateBintrayUIService implements RestService {
    @Autowired
    private CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        // update bintray setting
        updateBintraySetting(request);
        //send feedback msg
        response.info("Successfully updated Bintray settings");
    }

    /**
     * update bintray setting to config descriptor
     *
     * @param artifactoryRequest - encapsulate data related to request
     */
    private void updateBintraySetting(ArtifactoryRestRequest artifactoryRequest) {
        MutableCentralConfigDescriptor cc = centralConfigService.getMutableDescriptor();
        BintrayConfigDescriptor bintrayConfigDescriptor = (BintrayConfigDescriptor) artifactoryRequest.getImodel();
        cc.setBintrayConfig(bintrayConfigDescriptor);
        centralConfigService.saveEditedDescriptorAndReload(cc);
    }
}
