package org.artifactory.ui.rest.service.admin.security.signingkeys.keystore;

import org.artifactory.api.context.ContextHelper;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.security.signingkey.KeyStore;
import org.artifactory.util.Files;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CancelKeyPairService implements RestService {

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        KeyStore keyStore = (KeyStore) request.getImodel();
        // remove keyStore from temp folder
        String fileName = keyStore.getFileName();
        String uploadDir = ContextHelper.get().getArtifactoryHome().getTempUploadDir().getAbsolutePath();
        File file = new File(uploadDir, fileName);
        Files.removeFile(file);

    }
}
