package org.artifactory.ui.rest.service.admin.configuration.licenses;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.license.LicensesAddon;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.license.LicenseInfo;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.common.ConfigModelPopulator;
import org.artifactory.ui.rest.model.admin.configuration.licenses.License;
import org.artifactory.ui.rest.model.empty.EmptyModel;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetArtifactLicenseService implements RestService {
    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        fetchSingleOrMultiArtifactLicense(response, request);
    }

    /**
     * fetch single or multi license info objects
     *
     * @param response - encapsulate all data require for response
     * @param request  - encapsulate data related to request
     */
    private void fetchSingleOrMultiArtifactLicense(RestResponse response, ArtifactoryRestRequest request) {
        String licenseName = request.getPathParamByKey("id");
        // get license addon
        LicensesAddon licensesAddon = ContextHelper.get().beanForType(AddonsManager.class).addonByType(
                LicensesAddon.class);
        if (isMultiLicense(licenseName)) {
            List<LicenseInfo> licenseInfos = licensesAddon.getArtifactsLicensesInfo().getLicenses();
            // update response with license info data
            updateResponseWithMultiLicensesInfo(response, licenseInfos);
        } else {
            updateResponseWithSingleArtifactLicenseInfo(response, licenseName, licensesAddon);
        }
    }

    /**
     * get Single license info and update response
     *
     * @param artifactoryResponse - encapsulate Data require for response
     * @param licenseName         - license name from path param
     */
    private void updateResponseWithSingleArtifactLicenseInfo(RestResponse artifactoryResponse, String licenseName,
            LicensesAddon licensesAddon) {
        RestModel license = getSingleLicense(licenseName, licensesAddon);
        if (license == null) {
            license = new EmptyModel();
        }
        artifactoryResponse.iModel(license);
    }

    /**
     * get Multi artifact license info and update response
     *
     * @param artifactoryResponse - encapsulate Data require for response
     * @param licenseInfos        - list of all artifact license Found in DB
     */
    private void updateResponseWithMultiLicensesInfo(RestResponse artifactoryResponse,
            List<LicenseInfo> licenseInfos) {
        List<RestModel> licenseInfoList = new ArrayList<>();
        // populate artifact license info data to license model
        licenseInfos.stream().forEach(
                licenseInfo -> licenseInfoList.add(ConfigModelPopulator.populateLicenseInfo(licenseInfo)));
        // update response with artifact license model data
        artifactoryResponse.iModelList(licenseInfoList);
    }

    /**
     * check if  single or multi license is require based on path param data
     *
     * @param licenseName - path param
     * @return if true require multi license
     */
    private boolean isMultiLicense(String licenseName) {
        return licenseName == null || licenseName.length() == 0;
    }

    /**
     * get license from addon by name
     *
     * @param licenseName - license name
     * @return - license info instance for specific license name
     */
    private License getSingleLicense(String licenseName, LicensesAddon licensesAddon) {
        License license = null;
        LicenseInfo licenseInfo = licensesAddon.getLicenseByName(licenseName);
        if (!licenseInfo.getName().equals("Not Found")) {
            license = ConfigModelPopulator.populateLicenseInfo(licenseInfo);
        }
        return license;
    }
}
