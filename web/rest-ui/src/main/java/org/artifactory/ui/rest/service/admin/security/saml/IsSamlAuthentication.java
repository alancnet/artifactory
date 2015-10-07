package org.artifactory.ui.rest.service.admin.security.saml;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.saml.SamlException;
import org.artifactory.addon.saml.SamlSsoAddon;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Gidi Shabat
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class IsSamlAuthentication implements RestService {
    private static final Logger log = LoggerFactory.getLogger(IsSamlAuthentication.class);


    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        try {
            AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
            Boolean saml = addonsManager.addonByType(SamlSsoAddon.class).isSamlAuthentication(
                    request.getServletRequest(),
                    response.getServletResponse());
            response.iModel(saml.toString());
        } catch (SamlException e) {
            log.error(e.getMessage(), e);
            response.error(e.getMessage());
        }
    }
}
