package org.artifactory.ui.rest.service.admin.advanced.maintenance;

import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.service.utils.AolUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CompressInternalDataService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(CompressInternalDataService.class);


    @Autowired
    org.artifactory.storage.StorageService storageService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        AolUtils.assertNotAol("CompressInternalData");
        // compress internal data
        compressInternalData(response);
    }

    /**
     * compress internal data
     *
     * @param artifactoryResponse - encapsulate data require for response
     */
    private void compressInternalData(RestResponse artifactoryResponse) {
        BasicStatusHolder statusHolder = new BasicStatusHolder();
        try {
            storageService.compress(statusHolder);
        } catch (Exception e) {
            statusHolder.error(e.getMessage(), log);
        } finally {
            if (statusHolder.isError()) {
                artifactoryResponse.error("Failed to compress database: " + statusHolder.getLastError().getMessage());
            } else {
                artifactoryResponse.info("Database successfully compressed.");
            }
        }
    }
}
