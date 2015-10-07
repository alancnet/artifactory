package org.artifactory.ui.rest.service.admin.advanced.maintenance;

import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
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
public class PruneUnReferenceDataService implements RestService {

    @Autowired
    org.artifactory.storage.StorageService storageService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        AolUtils.assertNotAol("PruneUnReferenceData");
        // prune Unreferenced files in data
        pruneUnreferencedFileInData(response);
    }

    /**
     * prune Unreferenced FileIn Data
     *
     * @param artifactoryResponse
     */
    private void pruneUnreferencedFileInData(RestResponse artifactoryResponse) {
        BasicStatusHolder statusHolder = new BasicStatusHolder();
        storageService.pruneUnreferencedFileInDataStore(statusHolder);
        if (statusHolder.isError()) {
            artifactoryResponse.error("Pruning unreferenced data completed with an error:\n" +
                    statusHolder.getLastError().getMessage() + ".");
        } else {
            artifactoryResponse.info(
                    "Pruning unreferenced data completed successfully!\n" + statusHolder.getStatusMsg());
        }
    }
}
