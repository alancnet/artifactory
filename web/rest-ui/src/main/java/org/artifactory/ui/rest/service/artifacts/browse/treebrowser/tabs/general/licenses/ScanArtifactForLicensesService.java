package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.general.licenses;

import com.google.common.collect.Lists;
import org.apache.http.HttpStatus;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.license.LicensesAddon;
import org.artifactory.api.license.LicenseInfo;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.licenses.GeneralTabLicenseModel;
import org.artifactory.ui.utils.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ScanArtifactForLicensesService implements RestService<GeneralTabLicenseModel> {

    @Autowired
    private AddonsManager addonsManager;

    @Autowired
    AuthorizationService authService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        RepoPath path = RequestUtils.getPathFromRequest(request);
        if (!authService.canAnnotate(path)) {
            response.error("Insufficient permissions for operation").responseCode(HttpStatus.SC_UNAUTHORIZED);
            return;
        }
        Set<LicenseInfo> foundLicenses = addonsManager.addonByType(LicensesAddon.class).scanPathForLicenses(path);
        if (foundLicenses.isEmpty() ||
                (foundLicenses.size() == 1 && foundLicenses.iterator().next().equals(LicenseInfo.NOT_FOUND_LICENSE))) {
            //Don't send "not found" object - UI gets empty array and handles
            response.iModelList(Lists.newArrayList());
        } else {
            response.iModel(foundLicenses.stream()
                    .map(GeneralTabLicenseModel::new)
                    .collect(Collectors.toList()));
        }
    }
}
