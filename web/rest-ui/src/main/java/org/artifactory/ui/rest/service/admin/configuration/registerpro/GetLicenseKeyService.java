package org.artifactory.ui.rest.service.admin.configuration.registerpro;

import org.artifactory.addon.AddonsManager;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.configuration.registerpro.ProLicense;
import org.artifactory.ui.rest.service.utils.AolUtils;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
public class GetLicenseKeyService implements RestService {
    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        AolUtils.assertNotAol("GetLicenseKey");
        AddonsManager addonsManager = getAddonsManager();
        String[] licenseDetails = null;
        if (addonsManager.isLicenseInstalled()) {
            licenseDetails = addonsManager.getLicenseDetails();
        }
        // update response with license details
        updateResponseWithLicenseDetails(response, addonsManager, licenseDetails);
    }

    /**
     * update response with license details
     *
     * @param artifactoryResponse - encapsulate all data require for response
     * @param addonsManager       - add on manager
     * @param licenseDetails      - license details array
     */
    private void updateResponseWithLicenseDetails(RestResponse artifactoryResponse, AddonsManager addonsManager,
            String[] licenseDetails) {
        ProLicense proLicense = new ProLicense(licenseDetails, addonsManager.getLicenseKey());
        artifactoryResponse.iModel(proLicense);
    }

    /**
     * get addon manager from application context
     *
     * @return addon manager
     */
    private AddonsManager getAddonsManager() {
        ArtifactoryContext artifactoryContext = ContextHelper.get();
        return artifactoryContext.beanForType(AddonsManager.class);
    }
}
