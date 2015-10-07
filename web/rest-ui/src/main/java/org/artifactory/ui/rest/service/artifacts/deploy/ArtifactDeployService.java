package org.artifactory.ui.rest.service.artifacts.deploy;

import org.artifactory.api.artifact.UnitInfo;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.maven.MavenArtifactInfo;
import org.artifactory.api.repo.DeployService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.repo.exception.RepoRejectException;
import org.artifactory.api.request.ArtifactoryRequestBase;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.md.Properties;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
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
public class ArtifactDeployService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(ArtifactDeployService.class);

    @Autowired
    DeployService deployService;

    @Autowired
    RepositoryService repositoryService;

    @Autowired
    CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String uploadDir = ContextHelper.get().getArtifactoryHome().getTempUploadDir().getAbsolutePath();
        // get model data
        UploadArtifactInfo uploadArtifactInfo = (UploadArtifactInfo) request.getImodel();
        //deploy file
        deploy(uploadArtifactInfo, uploadDir, response);
    }


    /**
     * deploy file to repository
     *
     * @param uploadArtifactInfo  - upload artifact info
     * @param uploadDir           - upload temp folder
     * @param artifactoryResponse - encapsulate data require for response
     */
    private void deploy(UploadArtifactInfo uploadArtifactInfo, String uploadDir, RestResponse artifactoryResponse) {
        String fileName = uploadArtifactInfo.getFileName();
        UnitInfo unitInfo = uploadArtifactInfo.getUnitInfo();
        Properties properties = parseMatrixParams(unitInfo);
        String repoKey = uploadArtifactInfo.getRepoKey();
        LocalRepoDescriptor localRepoDescriptor = repositoryService.localOrCachedRepoDescriptorByKey(repoKey);
        try {
            File file = new File(uploadDir, fileName);

            // deploy file with pom
            if (uploadArtifactInfo.isPublishUnitConfigFile()) {
                deployService.deploy(localRepoDescriptor, unitInfo, file, uploadArtifactInfo.getUnitConfigFileContent(),
                        true, false, properties);

                deletePomFile(unitInfo, localRepoDescriptor);
            } else {
                deployService.deploy(localRepoDescriptor, unitInfo, file, properties);
            }

            // delete tmp file
            Files.removeFile(file);
            String artifactPath = uploadArtifactInfo.getUnitInfo().getPath();
            UploadedArtifactInfo uploadedArtifactInfo = new UploadedArtifactInfo(TreeUtils.shouldProvideTreeLink(localRepoDescriptor, artifactPath),
                    localRepoDescriptor.getKey(), artifactPath);
            artifactoryResponse.iModel(uploadedArtifactInfo);
        } catch (RepoRejectException e) {
            log.error(e.toString());
            artifactoryResponse.error("Failed to deploy file:" + fileName + " to Repository: " + repoKey +
                    ". Please check the log file for more details");
        }
    }

    /**
     * get uploaded file Properties
     *
     * @param unitInfo - unit info - debian / artifact
     * @return
     */
    private Properties parseMatrixParams(UnitInfo unitInfo) {
        Properties props;
        props = (Properties) InfoFactoryHolder.get().createProperties();
        String targetPathFieldValue = unitInfo.getPath();
        //  targetPathFieldValue = updateTargetPathValue(unitInfo, targetPathFieldValue);
        int matrixParamStart = targetPathFieldValue.indexOf(Properties.MATRIX_PARAMS_SEP);
        if (matrixParamStart > 0) {
            ArtifactoryRequestBase.processMatrixParams(props, targetPathFieldValue.substring(matrixParamStart));
            updateUnitInfo(unitInfo);
        }
        return props;
    }

    /**
     * update target path value
     *
     * @param unitInfo             - unit  info
     * @param targetPathFieldValue - target path value
     * @return
     */
    private String updateTargetPathValue(UnitInfo unitInfo, String targetPathFieldValue) {
        if (unitInfo.getPath().endsWith("/")) {
            targetPathFieldValue = unitInfo.getPath().substring(0, unitInfo.getPath().length() - 1);
            unitInfo.setPath(targetPathFieldValue);
        }
        return targetPathFieldValue;
    }

    /**
     * update unit info path after removing path param
     *
     * @param unitInfo
     */
    private void updateUnitInfo(UnitInfo unitInfo) {
        String[] splitedPath = unitInfo.getPath().split(";");
        if (splitedPath.length > 0) {
            unitInfo.setPath(splitedPath[0]);
        }
    }

    private void deletePomFile(UnitInfo unitInfo, LocalRepoDescriptor localRepoDescriptor) {
        RepoPath repoPath = InternalRepoPathFactory.create(localRepoDescriptor.getKey(), unitInfo.getPath());
        MavenArtifactInfo mavenArtifactInfo = (MavenArtifactInfo) unitInfo;
        RepoPath pomPath = InternalRepoPathFactory.create(repoPath.getParent(),
                mavenArtifactInfo.getArtifactId() + "-" + mavenArtifactInfo.getVersion() + ".pom");
        Files.removeFile(new File(pomPath.getPath()));
    }
}