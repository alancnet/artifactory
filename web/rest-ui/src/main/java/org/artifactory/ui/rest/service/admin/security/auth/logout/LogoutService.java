package org.artifactory.ui.rest.service.admin.security.auth.logout;

import org.artifactory.api.context.ContextHelper;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.security.AuthenticationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class LogoutService implements RestService {

    private static final Logger log = LoggerFactory.getLogger(LogoutService.class);

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        try {
            Map<String, LogoutHandler> logoutHandlers = ContextHelper.get().beansForType(LogoutHandler.class);
            tryToLogoutFromProviders(request, response, logoutHandlers);
        }catch (Exception e){
            log.debug("failed to perform session logout" , e);
        }
    }

    /**
     * iterate security providers and try to logout
     *
     * @param artifactoryRequest  - encapsulate data related to request
     * @param artifactoryResponse - encapsulate data require for response
     * @param logoutHandlers      - map of logout handlers
     */
    private void tryToLogoutFromProviders(ArtifactoryRestRequest artifactoryRequest,
            RestResponse artifactoryResponse, Map<String, LogoutHandler> logoutHandlers) {
        HttpServletRequest servletRequest = artifactoryRequest.getServletRequest();
        HttpServletResponse servletResponse = artifactoryResponse.getServletResponse();
        Authentication authentication = AuthenticationHelper.getAuthentication();
        // logout from all providers
        for (LogoutHandler logoutHandler : logoutHandlers.values()) {
            logoutHandler.logout(servletRequest, servletResponse, authentication);
        }
    }
}
