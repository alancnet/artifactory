package org.artifactory.ui.rest.service.admin.services.indexer;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.index.IndexerDescriptor;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.common.ServiceModelPopulator;
import org.artifactory.ui.rest.service.utils.AolUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
public class GetIndexerService implements RestService {

    @Autowired
    private CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        AolUtils.assertNotAol("GetIndexer");
        getIndexerDescriptor(response);
    }

    /**
     * get the indexer descriptor from config and populate data to indexer model
     *
     * @param artifactoryResponse - encapsulate data require for response
     */
    private void getIndexerDescriptor(RestResponse artifactoryResponse) {
        IndexerDescriptor indexerDescriptor = centralConfigService.getDescriptor().getIndexer();
        RestModel indexer = ServiceModelPopulator.populateIndexerConfiguration(indexerDescriptor);
        artifactoryResponse.iModel(indexer);
    }
}
