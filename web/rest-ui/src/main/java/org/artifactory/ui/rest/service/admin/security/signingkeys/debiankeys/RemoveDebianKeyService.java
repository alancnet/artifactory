package org.artifactory.ui.rest.service.admin.security.signingkeys.debiankeys;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.debian.DebianAddon;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class RemoveDebianKeyService implements RestService {

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        boolean isPublic = Boolean.valueOf(request.getQueryParamByKey("public"));
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        DebianAddon debianAddon = addonsManager.addonByType(DebianAddon.class);
        debianAddon.removeKey(isPublic);
        response.info("Key was removed");
    }
}
