package org.artifactory.ui.rest.service.artifacts.deploy;

import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.DeployService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.deploy.UploadArtifactInfo;
import org.artifactory.ui.rest.model.artifacts.deploy.UploadedArtifactInfo;
import org.artifactory.ui.utils.TreeUtils;
import org.artifactory.util.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ArtifactDeployBundleService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(ArtifactDeployBundleService.class);

    @Autowired
    DeployService deployService;
    @Autowired
    CentralConfigService centralConfigService;
    @Autowired
    RepositoryService repositoryService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String uploadDir = ContextHelper.get().getArtifactoryHome().getTempUploadDir().getAbsolutePath();
        // get upload model
        UploadArtifactInfo uploadArtifactInfo = (UploadArtifactInfo) request.getImodel();
        String repoKey = uploadArtifactInfo.getRepoKey();
        // deploy bundle
        deployBundle(response, uploadDir, uploadArtifactInfo, repoKey);
    }

    /**
     * deploy bundle and update response
     *
     * @param artifactoryResponse - encapsulate data related to response
     * @param uploadDir           - temp folder
     * @param uploadArtifactInfo  - upload artifact info
     * @param repoKey             - repo key
     */
    private void deployBundle(RestResponse artifactoryResponse, String uploadDir, UploadArtifactInfo uploadArtifactInfo,
            String repoKey) {
        try {
            LocalRepoDescriptor localRepoDescriptor = repositoryService.localOrCachedRepoDescriptorByKey(repoKey);
            BasicStatusHolder statusHolder = new BasicStatusHolder();
            // deploy file to repository
            File bundleFile = new File(uploadDir, uploadArtifactInfo.getFileName());
            deployService.deployBundle(bundleFile, localRepoDescriptor, statusHolder, false);
            // update feedback message
            updateFeedbackMsg(artifactoryResponse, statusHolder);
            // delete tmp file
            Files.removeFile(bundleFile);
            String artifactPath = uploadArtifactInfo.getUnitInfo().getPath();
            UploadedArtifactInfo uploadedArtifactInfo = new UploadedArtifactInfo(
                    TreeUtils.shouldProvideTreeLink(localRepoDescriptor, artifactPath),
                    localRepoDescriptor.getKey(), artifactPath);
            artifactoryResponse.iModel(uploadedArtifactInfo);

        } catch (Exception e) {
            artifactoryResponse.error(e.getMessage());
            log.error(e.toString());
        }
    }

    /**
     * update error and warn feedback msg
     *
     * @param artifactoryResponse - encapsulate data related to response
     * @param statusHolder        - msg status holder
     */
    private void updateFeedbackMsg(RestResponse artifactoryResponse, BasicStatusHolder statusHolder) {
        if (statusHolder.hasErrors()) {
            artifactoryResponse.error(statusHolder.getErrors().get(0).getMessage());
        } else if (statusHolder.hasWarnings()) {
            artifactoryResponse.warn(statusHolder.getWarnings().get(0).getMessage());
        } else {
            artifactoryResponse.info(statusHolder.getStatusMsg());
        }
    }
}
