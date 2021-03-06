package org.artifactory.ui.rest.service.admin.security.saml;

import org.artifactory.addon.saml.SamlHandler;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Gidi Shabat
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetSamlLoginResponseService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(GetSamlLoginResponseService.class);
    @Autowired
    private SamlHandler samlHandler;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        try {
            samlHandler.handleLoginResponse(request.getServletRequest(), response.getServletResponse());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            response.error(e.getMessage());
        }
    }
}
