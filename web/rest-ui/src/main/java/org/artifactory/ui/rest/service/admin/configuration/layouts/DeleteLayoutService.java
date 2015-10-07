package org.artifactory.ui.rest.service.admin.configuration.layouts;

import org.apache.http.HttpStatus;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Lior Hasson
 */
@Component
public class DeleteLayoutService implements RestService {
    @Autowired
    private CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String layoutKey = request.getPathParamByKey("layoutKey");
        MutableCentralConfigDescriptor mutableDescriptor = getMutableDescriptor();
        mutableDescriptor.removeRepoLayout(layoutKey);
        centralConfigService.saveEditedDescriptorAndReload(mutableDescriptor);

        String message = "Layout '" + layoutKey + "' successfully deleted.";
        response.info(message).responseCode(HttpStatus.SC_OK);
    }

    private MutableCentralConfigDescriptor getMutableDescriptor() {
        return centralConfigService.getMutableDescriptor();
    }
}
