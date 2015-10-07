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
public class GarbageCollectionService implements RestService {
    @Autowired
    org.artifactory.storage.StorageService storageService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        AolUtils.assertNotAol("GarbageCollection");
        // run now GC
        runNowGc(response);
    }

    /**
     * run now garbage collection
     *
     * @param artifactoryResponse - encapsulate data rellaateed to response
     */
    private void runNowGc(RestResponse artifactoryResponse) {
        BasicStatusHolder statusHolder = new BasicStatusHolder();
        storageService.callManualGarbageCollect(statusHolder);
        if (statusHolder.isError()) {
            artifactoryResponse.error(
                    "Could not run the garbage collector: " + statusHolder.getLastError().getMessage() + ".");
        } else {
            artifactoryResponse.info("Garbage collector was successfully scheduled to run in the background.");
        }
    }
}
