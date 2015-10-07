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

import java.util.List;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetKeyStoreService implements RestService {

    @Autowired
    CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        // check if key store exist , if so the it return key store pairs
        getKeyStoreKeyPair(addonsManager, response);
    }

    /**
     * check if key store exist , if so the it return key store pairs
     *
     * @param addonsManager
     */
    private void getKeyStoreKeyPair(AddonsManager addonsManager, RestResponse response) {
        ArtifactWebstartAddon artifactWebstartAddon = addonsManager.addonByType(ArtifactWebstartAddon.class);
        boolean keyStoreExist = artifactWebstartAddon.keyStoreExist();
        KeyStore keyStore = new KeyStore();
        keyStore.setKeyStoreExist(keyStoreExist);
        if (keyStoreExist) {
            List<String> keyPairNames = artifactWebstartAddon.getKeyPairNames();
            keyStore.setKeyStorePairNames(keyPairNames);
            response.iModel(keyStore);
        }
    }
}
