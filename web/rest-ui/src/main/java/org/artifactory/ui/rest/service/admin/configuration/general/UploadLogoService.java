package org.artifactory.ui.rest.service.admin.configuration.general;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.utils.FileUpload;
import org.artifactory.ui.utils.MultiPartUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UploadLogoService implements RestService {

    @Autowired
    CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        try {
            String logoDir = ContextHelper.get().getArtifactoryHome().getLogoDir().getAbsolutePath();
            // save file to logo folder
            saveFileToLogoFolder(request, logoDir);
            response.info("Logo Uploaded Successfully.");
        }catch (Exception e){
            response.error("error uploading file to server");
        }
    }

    /**
     * save logo to logo folder
     * @param artifactoryRequest - encapsulate data related to request
     */
    private void saveFileToLogoFolder(ArtifactoryRestRequest artifactoryRequest,String logoDir) {
        FileUpload fileUpload = (FileUpload) artifactoryRequest.getImodel();
        MultiPartUtils.saveSpecificFile(centralConfigService, fileUpload.getFormDataMultiPart(), logoDir,
                "logo");
    }
}
