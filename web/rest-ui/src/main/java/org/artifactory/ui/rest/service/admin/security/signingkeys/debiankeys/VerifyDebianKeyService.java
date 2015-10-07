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

import static org.apache.commons.lang.StringUtils.isEmpty;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class VerifyDebianKeyService implements RestService {

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {

        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        DebianAddon debianAddon = addonsManager.addonByType(DebianAddon.class);
        SignKey signKey = (SignKey) request.getImodel();
        String passPhrase = CryptoHelper.decryptIfNeeded(signKey.getPassPhrase());
        // verify signing key phrase
        verifySignKeyPhrase(response, debianAddon, passPhrase);
    }

    /**
     * verify signing key phrase
     *
     * @param artifactoryResponse - encapsulate data related to response
     * @param debianAddon         -- debian addon
     * @param passPhrase          = sign key pass phrase
     */
    private void verifySignKeyPhrase(RestResponse artifactoryResponse, DebianAddon debianAddon, String passPhrase) {
        boolean hasRequisites = true;
        if (isEmpty(passPhrase)) {
            artifactoryResponse.warn("No pass-phrase supplied");
            hasRequisites = false;
        }
        if (!debianAddon.hasPrivateKey()) {
            artifactoryResponse.error("No private key installed");
            hasRequisites = false;
        }
        if (!debianAddon.hasPublicKey()) {
            artifactoryResponse.error("No public key installed");
            hasRequisites = false;
        }
        if (hasRequisites) {
            if (debianAddon.verifyPassPhrase(passPhrase)) {
                artifactoryResponse.info("Successfully verified signing");
            } else {
                artifactoryResponse.error(
                        "Failed to sign and verify using the installed keys and supplied pass-phrase");
            }
        }
    }
}
