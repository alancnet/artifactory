package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.general.licenses;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.license.LicensesAddon;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.licenses.GeneralTabLicenseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetAllAvailableLicensesService implements RestService<GeneralTabLicenseModel> {

    @Autowired
    private AddonsManager addonsManager;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        response.iModel(addonsManager.addonByType(LicensesAddon.class).getArtifactsLicensesInfo()
                .getLicenses().stream()
                .parallel()
                .map(licenseInfo -> new GeneralTabLicenseModel(licenseInfo.getName()))
                .collect(Collectors.toList()));
    }
}
