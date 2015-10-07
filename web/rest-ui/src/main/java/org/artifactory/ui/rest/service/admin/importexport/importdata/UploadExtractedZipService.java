package org.artifactory.ui.rest.service.admin.importexport.importdata;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.importexport.ImportExportSettings;
import org.artifactory.ui.rest.model.utils.FileUpload;
import org.artifactory.ui.utils.MultiPartUtils;
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
public class UploadExtractedZipService implements RestService {


    @Autowired
    CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        List<String> fileNames = new ArrayList<>();
        FileUpload uploadFile = (FileUpload) request.getImodel();
        try {
            String uploadDir = ContextHelper.get().getArtifactoryHome().getTempUploadDir().getAbsolutePath();
            MultiPartUtils.createTempFolderIfNotExist(uploadDir);
            // save file to temp folder
            MultiPartUtils.saveFileDataToTemp(centralConfigService, uploadFile.getFormDataMultiPart(), uploadDir,
                    fileNames, false);
            // extract file to temp folder
            MultiPartUtils.saveUploadFileAsExtracted(new File(uploadDir, fileNames.get(0)), response);
            ImportExportSettings importExportSettings = new ImportExportSettings(fileNames.get(0) + "_extract");
            response.iModel(importExportSettings);
        } catch (Exception e) {
            response.error(e.getMessage());
        }
    }
}
