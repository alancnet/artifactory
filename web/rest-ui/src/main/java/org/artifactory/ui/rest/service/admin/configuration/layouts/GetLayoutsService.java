package org.artifactory.ui.rest.service.admin.configuration.layouts;

import org.artifactory.addon.AddonsManager;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.repo.RepoLayout;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.configuration.layouts.LayoutActionsModel;
import org.artifactory.ui.rest.model.admin.configuration.layouts.LayoutGridModel;
import org.artifactory.util.RepoLayoutUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lior Hasson
 */
@Component
public class GetLayoutsService implements RestService<LayoutGridModel> {

    @Autowired
    private CentralConfigService centralConfigService;

    @Autowired
    private AddonsManager addonsManager;

    @Override
    public void execute(ArtifactoryRestRequest<LayoutGridModel> artifactoryRequest,
                        RestResponse artifactoryResponse) {
        List<RepoLayout> repoLayouts = getMutableDescriptor().getRepoLayouts();
        List<LayoutGridModel> layoutModels = repoLayouts.stream()
                                                        .map(LayoutGridModel::new)
                                                        .collect(Collectors.toList());

        addActions(layoutModels);

        artifactoryResponse.iModelList(layoutModels);
    }

    private void addActions(List<LayoutGridModel> layoutModels) {
        layoutModels.forEach(layoutGridModel -> {
            LayoutActionsModel actions = new LayoutActionsModel();
            boolean licenseInstalled = addonsManager.isLicenseInstalled();
            boolean reservedName = RepoLayoutUtils.isReservedName(layoutGridModel.getName());
            actions.setDelete(!reservedName);
            actions.setEdit(licenseInstalled && !reservedName);
            actions.setCopy(licenseInstalled);
            layoutGridModel.setLayoutActions(actions);
        });
    }

    private MutableCentralConfigDescriptor getMutableDescriptor() {
        return centralConfigService.getMutableDescriptor();
    }
}
