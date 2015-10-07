package org.artifactory.ui.rest.service.setmeup;

import com.google.common.collect.Lists;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.setmeup.MavenSettingModel;
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
public class MavenSettingGeneratorService implements RestService {

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

        List<RepoDescriptor> readableVirtualAndRemote = Lists.newArrayList();
        readableVirtualAndRemote.addAll(readableVirtualRepoDescriptors);

        MavenSettingModel mavenSettingModel = new MavenSettingModel();
        readableVirtualAndRemote.forEach(repoDescriptor -> {
                    mavenSettingModel.getSnapshots().add(repoDescriptor.getKey());
                    mavenSettingModel.getPluginReleases().add(repoDescriptor.getKey());
                    mavenSettingModel.getPluginSnapshots().add(repoDescriptor.getKey());
                    mavenSettingModel.getAnyMirror().add(repoDescriptor.getKey());
                    mavenSettingModel.getReleases().add(repoDescriptor.getKey());
                }
        );
        response.iModel(mavenSettingModel);
    }
}
