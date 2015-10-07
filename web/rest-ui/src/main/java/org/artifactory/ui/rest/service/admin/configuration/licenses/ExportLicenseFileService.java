package org.artifactory.ui.rest.service.admin.configuration.licenses;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.license.LicensesAddon;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.license.LicensesInfo;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.rest.common.service.StreamRestResponse;
import org.artifactory.ui.rest.model.admin.configuration.licenses.ExportLicense;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinann
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ExportLicenseFileService implements RestService {

    String LICENSES_FILE_NAME = "licenses.xml";

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        LicensesAddon licensesAddon = ContextHelper.get().beanForType(AddonsManager.class).addonByType(
                LicensesAddon.class);
        LicensesInfo artifactLicensesInfo = licensesAddon.getArtifactsLicensesInfo();
        updateResponseWithLicenseFile(response, licensesAddon, artifactLicensesInfo);
    }

    /**
     * update response with license data
     *
     * @param artifactoryResponse  - encapsulate data related to response
     * @param licensesAddon        - license add on
     * @param artifactLicensesInfo - artifacts license info
     */
    private void updateResponseWithLicenseFile(RestResponse artifactoryResponse, LicensesAddon licensesAddon,
            LicensesInfo artifactLicensesInfo) {
        if (artifactLicensesInfo != null) {
            String licenseXml = licensesAddon.writeLicenseXML(artifactLicensesInfo);
            ((StreamRestResponse) artifactoryResponse).setDownloadFile(LICENSES_FILE_NAME);
            ((StreamRestResponse) artifactoryResponse).setDownload(true);
            ExportLicense exportLicense = new ExportLicense(licenseXml);
            artifactoryResponse.iModel(exportLicense);
        }
    }
}
