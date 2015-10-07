package org.artifactory.rest.util;

import com.sun.jersey.spi.container.ContainerResponse;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.util.SessionUtils;
import org.artifactory.util.UiRequestUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

/**
 * @author Chen Keinan
 */
public class AuthUtils {

    /**
     * validate that session is still active , if not try to restore if from remember me cookie if exist
     */
    public static void validateSession(HttpServletRequest request,UriInfo uriInfo,HttpServletResponse response) {
        if (UiRequestUtils.isUiRestRequest(request)) {
            // get existing session
            HttpSession session = request.getSession(false);
            if (session == null) {
                // try to restore auth if remember me cookie exist
                restoreRememberMeAuth(request,response);
            }
        }
    }

    /**
     * try to restore remember me authentication and create valid session of it
     */
    private static void restoreRememberMeAuth(HttpServletRequest request,HttpServletResponse response) {
        TokenBasedRememberMeServices tokenBasedRememberMeServices = ContextHelper.get().beanForType(
                TokenBasedRememberMeServices.class);
        Authentication authentication = tokenBasedRememberMeServices.autoLogin(request, response);
        if (authentication != null) {
            boolean sessionCreated = SessionUtils.setAuthentication(request, authentication, true);
            if (sessionCreated) {
                bindAuthentication(authentication);
            }
        }
    }

    /**
     * bind authentication to security context
     *
     * @param authentication - authentication
     */
    private static void bindAuthentication(Authentication authentication) {
        org.springframework.security.core.context.SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(authentication);
    }

    /**
     * add session valid header to response in case  request sent to UI
     *
     * @param response
     */
    public static  void addSessionStatusToHeaders(ContainerResponse response,UriInfo uriInfo,HttpServletRequest request) {
        if (UiRequestUtils.isUiRestRequest(request) && !uriInfo.getPath().equals("auth/login")) {
            MultivaluedMap<String, Object> metadata = response.getResponse().getMetadata();
            HttpSession session = request.getSession(false);
            if (session != null) {
                metadata.add("SessionValid", "true");
            } else {
                metadata.add("SessionValid", "false");
            }
        }
    }
}
