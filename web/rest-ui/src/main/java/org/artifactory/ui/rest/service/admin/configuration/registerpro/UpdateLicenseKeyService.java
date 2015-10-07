package org.artifactory.ui.rest.service.admin.configuration.registerpro;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.license.VerificationResult;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.configuration.registerpro.ProLicense;
import org.artifactory.ui.rest.service.utils.AolUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
public class UpdateLicenseKeyService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(UpdateLicenseKeyService.class);

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        AolUtils.assertNotAol("UpdateLicenseKey");
        AddonsManager addonsManager = getAddonsManager();
        boolean hasLicenseAlready = addonsManager.isLicenseInstalled();
        try {
            ProLicense proLicense = (ProLicense) request.getImodel();
            // try to install license
            VerificationResult verificationResult = addonsManager.installLicense(proLicense.getKey());
            // update response with license validation result
            updateResponseWithLicenseInstallResult(response, verificationResult, addonsManager,
                    hasLicenseAlready);
        } catch (Exception e) {
            response.error("The license key is not valid");
            log.error(e.toString());
        }
    }

    /**
     * update response with license install result
     *
     * @param artifactoryResponse - encapsulate data require for response
     * @param verificationResult  - license install validation result
     */
    private void updateResponseWithLicenseInstallResult(RestResponse artifactoryResponse,
            VerificationResult verificationResult, AddonsManager addonsManager, boolean hasLicenseAlready) {
        String installResult = verificationResult.showMassage();
        if (verificationResult.isValid()) {
            updateFeedbackMessage(artifactoryResponse, addonsManager, hasLicenseAlready);
        } else {
            artifactoryResponse.error(installResult);
        }
    }

    /**
     * update feedback message for new license or update license
     *
     * @param artifactoryResponse - encapsulate data require for response
     * @param addonsManager       - add on manager
     * @param hasLicenseAlready   - if true license installed already
     */
    private void updateFeedbackMessage(RestResponse artifactoryResponse, AddonsManager addonsManager,
            boolean hasLicenseAlready) {
        String licenseType=addonsManager.getProductName();
        if (addonsManager.isLicenseInstalled()) {
            String[] licenseDetails = addonsManager.getLicenseDetails();
            licenseType=licenseDetails[2];
        }
        if (hasLicenseAlready) {
            artifactoryResponse.info("Successfully updated " + licenseType + " license");
        } else {
            artifactoryResponse.info("Successfully created " + licenseType + " license");
        }
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
