package org.artifactory.ui.rest.service.setmeup;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.RepoLayout;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.setmeup.GradleSettingModel;
import org.artifactory.ui.rest.service.utils.setMeUp.SettingsHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author chen Keinan
 * @author Lior Hasson
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GradleSettingGeneratorService implements RestService {

    private final String JCENTER_DUMMY_REPO_KEY = "Bintray -> jcenter";

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
        List<RepoDescriptor> readableLocalRepoDescriptors = SettingsHelper.getReadableLocalRepoDescriptors(
                repositoryService, authorizationService);


        List<RepoDescriptor> readableVirtualAndRemoteRepoDesc = new LinkedList<>();
        readableVirtualAndRemoteRepoDesc.addAll(readableVirtualRepoDescriptors);

        GradleSettingModel gradleSettingModel = new GradleSettingModel();
        gradleSettingModel.getPluginResolver().add(JCENTER_DUMMY_REPO_KEY);

        //add repos to publisher
        gradleSettingModel.getLibsPublisher().addAll(
                readableLocalRepoDescriptors
                        .stream().map(RepoDescriptor::getKey)
                        .collect(Collectors.toList()));

        //add repos to resolvers and plugin resolvers
        gradleSettingModel.getLibsResolver().addAll(
                readableVirtualAndRemoteRepoDesc
                        .stream().map(RepoDescriptor::getKey)
                        .collect(Collectors.toList()));

        //add repos to plugin resolvers
        gradleSettingModel.getPluginResolver().addAll(
                readableVirtualAndRemoteRepoDesc
                        .stream().map(RepoDescriptor::getKey)
                        .collect(Collectors.toList()));

        List<RepoLayout> repoLayouts = centralConfigService.getDescriptor().getRepoLayouts();
        repoLayouts.forEach(repoLayout -> gradleSettingModel.getLayouts().add(repoLayout.getName()));
        response.iModel(gradleSettingModel);
    }
}
