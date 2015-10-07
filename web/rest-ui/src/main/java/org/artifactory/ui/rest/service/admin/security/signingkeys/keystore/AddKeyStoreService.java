package org.artifactory.ui.rest.service.admin.security.signingkeys.keystore;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.webstart.ArtifactWebstartAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.security.signingkey.KeyStore;
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
public class AddKeyStoreService implements RestService {

    @Autowired
    CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        List<String> fileNames = new ArrayList<>();
        String password = request.getQueryParamByKey("pass");
        // save keystore to temp folder
        saveKeyFileToTempFolder(request, fileNames, response);
        // load keystore to server
        loadKeyStore(response, fileNames, password);
    }

    /**
     * load KeyStore
     *
     * @param artifactoryResponse - encapsulate data related to response
     * @param fileNames           - key store name
     * @param password            - key store pass
     */
    private void loadKeyStore(RestResponse artifactoryResponse, List<String> fileNames, String password) {
        String uploadDir = ContextHelper.get().getArtifactoryHome().getTempUploadDir().getAbsolutePath();
        String keyName = fileNames.get(0);
        File file = new File(uploadDir, keyName);
        if (validateKeyStore(file, password, artifactoryResponse)) {
            int endIndex = keyName.indexOf(".");
            String alias;
            if (endIndex == -1) {
                alias = keyName;
            } else {
                alias = keyName.substring(0, endIndex);
            }
            KeyStore key = new KeyStore(true, alias, keyName, password);
            // delete keystore on temp folder
            artifactoryResponse.iModel(key);
            artifactoryResponse.info("Key Pair uploaded successfully");
        }
    }

    /**
     * save key file to temp folder
     *
     * @param artifactoryRequest - encapsulate data related to request
     * @param fileNames          - file names
     */
    private void saveKeyFileToTempFolder(ArtifactoryRestRequest artifactoryRequest, List<String> fileNames, RestResponse response) {
        FileUpload uploadFile = (FileUpload) artifactoryRequest.getImodel();
        String uploadDir = ContextHelper.get().getArtifactoryHome().getTempUploadDir().getAbsolutePath();
        MultiPartUtils.createTempFolderIfNotExist(uploadDir);
        // save file to temp folder
        try {
            MultiPartUtils.saveFileDataToTemp(centralConfigService, uploadFile.getFormDataMultiPart(), uploadDir,
                    fileNames, false);
        }catch (Exception e){
            response.error(e.getMessage());
        }
    }

    /**
     * @param file     - key store
     * @param password - key store password
     * @param response - encapsulate data relate to response
     * @return
     */
    public boolean validateKeyStore(File file, String password, RestResponse response) {
        try {
            AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
            ArtifactWebstartAddon artifactWebstartAddon = addonsManager.addonByType(ArtifactWebstartAddon.class);
            artifactWebstartAddon.loadKeyStore(file, password);
        } catch (Exception e) {
            response.error(e.getMessage());
            return false;
        }
        return true;
    }
}
