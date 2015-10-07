package org.artifactory.ui.rest.service.admin.security.signingkeys.keystore;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.webstart.ArtifactWebstartAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.security.signingkey.KeyStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ChangeKeyStorePasswordService implements RestService {

    @Autowired
    CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        KeyStore keyStore = (KeyStore) request.getImodel();
        //update key store password
        updateKeyStorePassword(response, keyStore);
    }

    /**
     * update keyStore password
     *
     * @param artifactoryResponse - encapsulate data related to response
     * @param keyStore            - key store model
     */
    private void updateKeyStorePassword(RestResponse artifactoryResponse, KeyStore keyStore) {
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        ArtifactWebstartAddon artifactWebstartAddon = addonsManager.addonByType(ArtifactWebstartAddon.class);
        String password = keyStore.getPassword();
        try {
            artifactWebstartAddon.setKeyStorePassword(password);
            artifactoryResponse.info("Successfully updated Key Store password");
        } catch (Exception e) {
            artifactoryResponse.error(e.getMessage());
        }
    }
}
