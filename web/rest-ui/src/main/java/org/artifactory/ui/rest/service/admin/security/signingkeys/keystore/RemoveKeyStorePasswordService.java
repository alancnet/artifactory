package org.artifactory.ui.rest.service.admin.security.signingkeys.keystore;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.webstart.ArtifactWebstartAddon;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Aviad Shikloshi
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class RemoveKeyStorePasswordService implements RestService {

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        ArtifactWebstartAddon artifactWebstartAddon = addonsManager.addonByType(ArtifactWebstartAddon.class);
        try {
            artifactWebstartAddon.removeKeyStorePassword();
            response.info("Successfully removed Key Store");
        } catch (Exception e) {
            response.error(e.getMessage());
        }
    }
}
