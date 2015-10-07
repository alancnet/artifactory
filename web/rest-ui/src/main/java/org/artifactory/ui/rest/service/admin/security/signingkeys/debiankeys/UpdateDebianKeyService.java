package org.artifactory.ui.rest.service.admin.security.signingkeys.debiankeys;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.debian.DebianAddon;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.security.crypto.CryptoHelper;
import org.artifactory.ui.rest.model.admin.security.signingkey.SignKey;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Shay Yaakov
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UpdateDebianKeyService implements RestService {

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        DebianAddon debianAddon = addonsManager.addonByType(DebianAddon.class);
        SignKey signKey = (SignKey) request.getImodel();
        String passPhrase = CryptoHelper.decryptIfNeeded(signKey.getPassPhrase());
        updatePassPhrase(response, debianAddon, passPhrase);
    }

    private void updatePassPhrase(RestResponse artifactoryResponse, DebianAddon debianAddon, String passPhrase) {
        debianAddon.savePassPhrase(passPhrase);
        artifactoryResponse.info("Successfully updated signing pass-phrase");
    }
}
