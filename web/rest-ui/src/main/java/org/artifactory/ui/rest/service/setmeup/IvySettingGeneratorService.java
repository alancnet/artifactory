package org.artifactory.ui.rest.service.setmeup;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.RepoLayout;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.setmeup.IvySettingModel;
import org.artifactory.ui.rest.service.utils.setMeUp.SettingsHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author chen Keinan
 * @author Lior Hasson
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class IvySettingGeneratorService implements RestService {

    @Autowired
    AuthorizationService authorizationService;

    @Autowired
    CentralConfigService centralConfigService;

    @Autowired
    RepositoryService repositoryService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        List<RepoDescriptor> readableVirtualRepoDescriptors = SettingsHelper.getReadableVirtualRepoDescriptors(
                repositoryService, authorizationService);
        IvySettingModel ivySettingModel = new IvySettingModel();
         readableVirtualRepoDescriptors.forEach(repoDescriptor ->
                    ivySettingModel.getLibsRepository().add(repoDescriptor.getKey())
        );
        List<RepoLayout> repoLayouts = centralConfigService.getDescriptor().getRepoLayouts();
        repoLayouts.forEach(repoLayout -> ivySettingModel.getLibsRepositoryLayout().add(repoLayout.getName()));
        response.iModel(ivySettingModel);
    }
}
