package org.artifactory.ui.rest.service.admin.services.indexer;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.services.indexer.Indexer;
import org.artifactory.ui.rest.service.utils.AolUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
public class UpdateIndexerService implements RestService {

    @Autowired
    private CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        AolUtils.assertNotAol("UpdateIndexer");
        // update config descriptor indexer
        updateIndexerDescriptor(request, response);
    }

    /**
     * update indexer descriptor
     *
     * @param artifactoryRequest  - encapsulate data related to request
     * @param artifactoryResponse - encapsulate data require for response
     */
    private void updateIndexerDescriptor(ArtifactoryRestRequest artifactoryRequest, RestResponse artifactoryResponse) {
        Indexer indexer = (Indexer) artifactoryRequest.getImodel();
        MutableCentralConfigDescriptor configDescriptor = centralConfigService.getMutableDescriptor();
        configDescriptor.setIndexer(indexer.toDescriptor());
        centralConfigService.saveEditedDescriptorAndReload(configDescriptor);
        artifactoryResponse.info("Successfully updated Indexer service settings");
    }
}
