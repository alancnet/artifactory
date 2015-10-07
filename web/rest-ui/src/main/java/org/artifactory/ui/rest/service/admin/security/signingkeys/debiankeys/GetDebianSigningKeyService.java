package org.artifactory.ui.rest.service.admin.security.signingkeys.debiankeys;

import com.google.common.base.Joiner;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.debian.DebianAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.descriptor.security.debian.DebianSettings;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.security.crypto.CryptoHelper;
import org.artifactory.ui.rest.model.admin.security.signingkey.SignKey;
import org.artifactory.util.HttpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetDebianSigningKeyService implements RestService {

    @Autowired
    CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        DebianAddon debianAddon = addonsManager.addonByType(DebianAddon.class);
        SignKey signKey = new SignKey();
        signKey.setPrivateKeyInstalled(debianAddon.hasPrivateKey());
        signKey.setPassPhrase(getPassPhrase());
        boolean publicKeyInstalled = debianAddon.hasPublicKey();
        if (publicKeyInstalled) {
            String link = getKeyLink(request.getServletRequest());
            signKey.setPublicKeyInstalled(true);
            signKey.setPublicKeyLink(link);
        }
        response.iModel(signKey);
    }

    private String getKeyLink(HttpServletRequest request) {
        return Joiner.on('/').join(HttpUtils.getServletContextUrl(request),
                "api", "gpg", "key/public");
    }

    public String getPassPhrase() {
        DebianSettings debianSettings = centralConfigService.getDescriptor().getSecurity().getDebianSettings();
        return debianSettings != null ? CryptoHelper.decryptIfNeeded(debianSettings.getPassphrase()) : null;
    }
}
