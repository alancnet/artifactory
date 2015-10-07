package org.artifactory.ui.rest.service.artifacts.deploy;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.maven.MavenService;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.deploy.UploadArtifactInfo;
import org.artifactory.ui.utils.MultiPartUtils;
import org.artifactory.ui.utils.UnitUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ArtifactUploadService implements RestService {

    @Autowired
    CentralConfigService centralConfigService;

    @Autowired
    private MavenService mavenService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String uploadDir = ContextHelper.get().getArtifactoryHome().getTempUploadDir().getAbsolutePath();
        try {
            MultiPartUtils.createTempFolderIfNotExist(uploadDir);
            // get upload model
            UploadArtifactInfo uploadArtifactInfo = (UploadArtifactInfo) request.getImodel();
            // save file data tto temp
            List<String> fileNames = new ArrayList<>();
            MultiPartUtils.saveFileDataToTemp(centralConfigService, uploadArtifactInfo.fetchFormDataMultiPart(),
                    uploadDir, fileNames, false);

            File file = new File(uploadDir, fileNames.get(0));
            // get artifact info
            uploadArtifactInfo = UnitUtils.getUnitInfo(file, uploadArtifactInfo, mavenService);
            response.iModel(uploadArtifactInfo);
        }catch (Exception e){
            response.error(e.getMessage());
        }
    }
}
