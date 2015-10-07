package org.artifactory.ui.rest.service.admin.configuration.licenses;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.license.LicensesAddon;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.license.ArtifactLicenseModel;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UpdateArtifactLicenseService implements RestService {
    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        // get license addon
        LicensesAddon licensesAddon = ContextHelper.get().beanForType(AddonsManager.class).addonByType(
                LicensesAddon.class);
        // update artifact license
        ArtifactLicenseModel artifactLicenseModel = (ArtifactLicenseModel) request.getImodel();
        licensesAddon.updateLicenseInfo(artifactLicenseModel.buildLicenseInfo());
        response.info("License " + artifactLicenseModel.getName() + " updated successfully");
    }
}
