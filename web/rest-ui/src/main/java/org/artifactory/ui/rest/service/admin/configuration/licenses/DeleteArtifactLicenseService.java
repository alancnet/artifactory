package org.artifactory.ui.rest.service.admin.configuration.licenses;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.license.LicensesAddon;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.license.LicenseInfo;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.configuration.licenses.DeleteLicensesModel;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DeleteArtifactLicenseService<T extends DeleteLicensesModel> implements RestService<T> {
    @Override
    public void execute(ArtifactoryRestRequest<T> request, RestResponse response) {
        T model = request.getImodel();
        for (String licenseId : model.getLicenseskeys()) {
            // get license addon
            LicensesAddon licensesAddon = ContextHelper.get().beanForType(AddonsManager.class).addonByType(
                    LicensesAddon.class);
            // delete artifact license
            LicenseInfo licenseInfo = licensesAddon.getLicenseByName(licenseId);
            licensesAddon.deleteLicenseInfo(licenseInfo);
        }
        if(model.getLicenseskeys().size()>1){
            response.info("Successfully removed "+model.getLicenseskeys().size()+" licenses");
        }else if(model.getLicenseskeys().size()==1){
            response.info("Successfully removed license '" + model.getLicenseskeys().get(0) + "'");
        }
    }
}
