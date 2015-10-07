package org.artifactory.ui.rest.service.admin.security.signingkeys.keystore;

import org.apache.http.HttpStatus;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.webstart.ArtifactWebstartAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.security.signingkey.KeyStore;
import org.artifactory.util.Files;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.security.Key;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class SaveKeyStoreService implements RestService {

    @Autowired
    CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        KeyStore keyStore = (KeyStore) request.getImodel();
        // save key pair
        addKeyPair(response, keyStore);
    }

    /**
     * save key pair file
     *
     * @param artifactoryResponse - encapsulate data require for response
     * @param keyStore            - key store model
     */
    private void addKeyPair(RestResponse artifactoryResponse, KeyStore keyStore) {
        String uploadDir = ContextHelper.get().getArtifactoryHome().getTempUploadDir().getAbsolutePath();
        File file = new File(uploadDir, keyStore.getFileName());
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        ArtifactWebstartAddon artifactWebstartAddon = addonsManager.addonByType(ArtifactWebstartAddon.class);
        if (validateAliasPassword(artifactWebstartAddon, keyStore, artifactoryResponse, file)) {
            try {
                artifactWebstartAddon.addKeyPair(file, keyStore.getKeyPairName(), keyStore.getPassword(), keyStore.getAlias(), keyStore.getPrivateKeyPassword());
                artifactoryResponse.info("Saved Key-Pair '" + keyStore.getKeyPairName() + "'.");
                Files.removeFile(file);
            } catch (IOException e) {
                artifactoryResponse.error("Failed to save key-pair: " + e.getMessage() + ".");
            } catch (Exception e) {
                artifactoryResponse.error(e.getMessage().toString());
            }
        } else {
            artifactoryResponse.responseCode(HttpStatus.SC_BAD_REQUEST);
        }
    }

    /**
     * validate alias and private  key pass before saving key pair
     *
     * @param artifactWebstartAddon - web start addon
     * @param keyStoreModel         - key store model send from client
     * @param response              - encapsulate data require for response
     * @param file                  - key store file
     * @return - if true validation succeeded
     */
    private boolean validateAliasPassword(ArtifactWebstartAddon artifactWebstartAddon, KeyStore keyStoreModel, RestResponse response, File file) {
        java.security.KeyStore keyStore = artifactWebstartAddon.loadKeyStore(file, keyStoreModel.getPassword());
        try {
            Key aliasKey = artifactWebstartAddon.getAliasKey(keyStore, keyStoreModel.getAlias(), keyStoreModel.getPrivateKeyPassword());
            if (aliasKey == null) {
                // should not happen
                response.error("Alias doesn't exist in the key store.");
                return false;
            }
        } catch (Exception e) {
            response.error(e.getMessage());
            return false;
        }
        return true;
    }
}
