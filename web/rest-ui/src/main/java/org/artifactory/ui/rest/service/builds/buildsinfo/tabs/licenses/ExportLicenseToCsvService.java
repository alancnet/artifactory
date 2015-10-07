package org.artifactory.ui.rest.service.builds.buildsinfo.tabs.licenses;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.license.LicensesAddon;
import org.artifactory.api.build.BuildService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.license.ModuleLicenseModel;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.rest.common.service.StreamRestResponse;
import org.artifactory.ui.rest.model.admin.configuration.licenses.ExportLicense;
import org.artifactory.ui.rest.model.builds.BuildLicenseModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ExportLicenseToCsvService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(ExportLicenseToCsvService.class);

    @Autowired
    BuildService buildService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {

            AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
            LicensesAddon licensesAddon = addonsManager.addonByType(LicensesAddon.class);
        Collection<ModuleLicenseModel> models = ((BuildLicenseModel) request.getImodel()).getLicenses();
        if (models != null && !models.isEmpty()) {
                String licenseCsv = licensesAddon.generateLicenseCsv(models);
                ((StreamRestResponse) response).setDownloadFile("licenses.csv");
                ((StreamRestResponse) response).setDownload(true);
                ExportLicense exportLicense = new ExportLicense(licenseCsv);
            response.iModel(exportLicense);
            }
    }
}
