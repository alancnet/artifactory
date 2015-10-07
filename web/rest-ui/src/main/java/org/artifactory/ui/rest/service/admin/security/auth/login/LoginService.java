package org.artifactory.ui.rest.service.admin.security.auth.login;

import org.artifactory.UiAuthenticationDetails;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.OssAddonsManager;
import org.artifactory.addon.plugin.PluginsAddon;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.security.AclService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.security.SecurityService;
import org.artifactory.common.ConstantValues;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.security.AccessLogger;
import org.artifactory.security.ArtifactoryPermission;
import org.artifactory.security.AuthenticationHelper;
import org.artifactory.security.HttpAuthenticationDetails;
import org.artifactory.security.UserInfo;
import org.artifactory.ui.rest.model.admin.security.general.SecurityConfig;
import org.artifactory.ui.rest.model.admin.security.login.UserLogin;
import org.artifactory.ui.rest.model.admin.security.user.BaseUser;
import org.artifactory.ui.rest.service.admin.security.general.GetSecurityConfigService;
import org.artifactory.util.UiRequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class LoginService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(LoginService.class);

    @Autowired
    AddonsManager addonsManager;
    @Autowired
    protected AuthorizationService authorizationService;

    @Autowired
    private AclService aclService;

    @Autowired
    GetSecurityConfigService getSecurityConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        UserLogin userLogin = (UserLogin) request.getImodel();
        ArtifactoryContext artifactoryContext = ContextHelper.get();
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userLogin.getUser(), userLogin.getPassword());
        // authenticate credential against security providers
        try {
            Authentication authentication = authenticateCredential(authenticationToken, artifactoryContext,
                    request, response);
            if (authentication != null) {
                getSecurityConfigService.execute(request, response);
                SecurityConfig securityConfig = (SecurityConfig) response.getIModel();
                // update session and remember me service with login data
                updateSessionAndRememberMeServiceWithLoginData(request, response, userLogin,
                        artifactoryContext, authenticationToken, authentication);
                //update response with user login data
                updateResponseWithLoginUser(response, userLogin, artifactoryContext, securityConfig);
                AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
                addonsManager.addonByType(PluginsAddon.class).executeAdditiveRealmPlugins();
                // TODO: [chenk] fix this ^^^, not reachable without dependency to artifactory-core
            }
        } catch (AuthenticationException e) {
            log.debug("Username or password are incorrect");
            response.error("Username or password are incorrect");
            response.responseCode(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    /**
     * update session and remember me service with user login data
     *
     * @param artifactoryRequest  - encapsulate all data related for request
     * @param artifactoryResponse - encapsulate all data require for response
     * @param userLogin           - user login data
     * @param artifactoryContext  - artifactory application context
     * @param authenticationToken - authentication token created with username and password
     * @param authentication      - authentication created after authenticating the token against providers
     */
    private void updateSessionAndRememberMeServiceWithLoginData(ArtifactoryRestRequest artifactoryRequest,
            RestResponse artifactoryResponse, UserLogin userLogin, ArtifactoryContext artifactoryContext,
            UsernamePasswordAuthenticationToken authenticationToken, Authentication authentication) {
        // update authentication data to session and DB
        boolean isUpdateSucceeded = updateSessionAndDB(artifactoryContext, userLogin.getUser(),
                authenticationToken, authentication, artifactoryRequest);
        // update remember me service if session update succeeded
        updateRememberMeService(artifactoryContext, isUpdateSucceeded, artifactoryRequest, artifactoryResponse);
    }

    /**
     * update response with Login User data
     *  @param artifactoryResponse - encapsulate all data require for response
     * @param userLogin           - user login data
     * @param artifactoryContext  - artifactory application context
     * @param securityConfig
     */
    private void updateResponseWithLoginUser(RestResponse artifactoryResponse, UserLogin userLogin,
                                             ArtifactoryContext artifactoryContext, SecurityConfig securityConfig) {
        RestModel responseModel = getResponseModel(artifactoryContext, userLogin, securityConfig);
        artifactoryResponse.iModel(responseModel);
    }

    /**
     * update response data with user login model data
     * @param artifactoryContext - artifactory web context
     * @param userLogin - user login nae
     * @param securityConfig
     */
    private RestModel getResponseModel(ArtifactoryContext artifactoryContext, UserLogin userLogin, SecurityConfig securityConfig) {
        boolean proWithoutLicense=false;
        if ( ! (addonsManager instanceof OssAddonsManager) && ! addonsManager.isLicenseInstalled()){
            proWithoutLicense=true;
        }
        boolean offlineMode=true;
        CentralConfigDescriptor descriptor = ContextHelper.get().getCentralConfig().getDescriptor();
        if (ConstantValues.versionQueryEnabled.getBoolean() && !descriptor.isOfflineMode()) {
            offlineMode=false;
        }
        AuthorizationService authService = artifactoryContext.beanForType(AuthorizationService.class);
        boolean isAdmin = authService.isAdmin();
        boolean canDeploy = !aclService.getPermissionTargets(
                ArtifactoryPermission.DEPLOY).isEmpty();
        boolean canManage = !aclService.getPermissionTargets(
                ArtifactoryPermission.MANAGE).isEmpty();
        BaseUser baseUser = new BaseUser(userLogin.getUser(), isAdmin);
        baseUser.setCanDeploy(canDeploy);
        baseUser.setCanManage(canManage);
        baseUser.setPreventAnonAccessBuild(securityConfig.isAnonAccessToBuildInfosDisabled());
        baseUser.setProWithoutLicense(proWithoutLicense);
        baseUser.setOfflineMode(offlineMode);
        return baseUser;
    }


    /**
     * update spring remember me service with login status
     * @param artifactoryContext - artifactory web context
     * @param isUpdateSucceeded - if true authentication has been updated successfully
     * @param artifactoryRestRequest - encapsulate data related to request
     * @param artifactoryRestResponse - encapsulate data needed for response
     */
    private void updateRememberMeService(ArtifactoryContext artifactoryContext,
            boolean isUpdateSucceeded,ArtifactoryRestRequest artifactoryRestRequest,
            RestResponse artifactoryRestResponse) {
        HttpServletRequest servletRequest = artifactoryRestRequest.getServletRequest();
        HttpServletResponse servletResponse = artifactoryRestResponse.getServletResponse();
        if (isUpdateSucceeded) {
            RememberMeServices rememberMeServices = (RememberMeServices) artifactoryContext.getBean(
                    "rememberMeServices");
            if (!ConstantValues.securityDisableRememberMe.getBoolean()) {
                try {
                    rememberMeServices.loginSuccess(servletRequest, servletResponse,
                            AuthenticationHelper.getAuthentication());
                } catch (UsernameNotFoundException e) {
                    log.warn("Remember Me service is not supported for transient external users.");
                }
            } else {
                if (!ConstantValues.securityDisableRememberMe.getBoolean()) {
                    rememberMeServices.loginFail(servletRequest, servletResponse);
                }
            }
        }
    }

    /**
     * authenticate credential against Security providers (Artifactory,Ldap , crown and etc)
     * @param authenticationToken - user credentials
     * @param artifactoryContext - artifactory web context
     * @param artifactoryRestRequest - encapsulate data related to request
     * @param artifactoryRestResponse - encapsulate data related to response
     * @return Authentication Data
     */
    private Authentication authenticateCredential(UsernamePasswordAuthenticationToken authenticationToken,
            ArtifactoryContext artifactoryContext,ArtifactoryRestRequest artifactoryRestRequest,
            RestResponse artifactoryRestResponse) {
        WebAuthenticationDetails details = new UiAuthenticationDetails(artifactoryRestRequest.getServletRequest(),
                artifactoryRestResponse.getServletResponse());
        authenticationToken.setDetails(details);
        AuthenticationManager authenticationManager = (AuthenticationManager) artifactoryContext.getBean("authenticationManager");
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        return authentication;
    }

    /**
     * update session and DB with authentication data
     * @param artifactoryContext - artifactory web context
     * @param userName - login user name
     * @param authenticationToken - login authentication token
     * @param authentication - spring authentication
     * @param artifactoryRestRequest - encapsulate data related to request
     * @return if true  data save successfully
     */
    private boolean updateSessionAndDB(ArtifactoryContext artifactoryContext, String userName,
            UsernamePasswordAuthenticationToken authenticationToken, Authentication authentication,
            ArtifactoryRestRequest artifactoryRestRequest) {
        boolean isAuthenticate = true;
        try{
            if (authentication.isAuthenticated()) {
                SecurityContext securityContext = SecurityContextHolder.getContext();
                securityContext.setAuthentication(authenticationToken);
                setLoginDataToSessionAndDB(securityContext, userName, artifactoryContext, authentication,
                        artifactoryRestRequest.getServletRequest());
            }
        } catch (AuthenticationException e) {
            isAuthenticate = false;
            AccessLogger.loginDenied(authenticationToken);
            if (log.isDebugEnabled()) {
                log.debug("Failed to authenticate " + userName, e);
            }
        }
        return isAuthenticate;
    }

    /**
     * set login data to session and db if succeeded
     * @param securityContext - spring security context
     * @param userName - user name
     * @param context
     * @param authentication
     */
    private void setLoginDataToSessionAndDB(SecurityContext securityContext, String userName,
            ArtifactoryContext context, Authentication authentication,HttpServletRequest servletRequest) {
        setAuthentication(authentication,securityContext,servletRequest);
        if (isNotBlank(userName) && (!userName.equals(UserInfo.ANONYMOUS))) {
            SecurityService securityService = context.beanForType(SecurityService.class);
            String remoteAddress = new HttpAuthenticationDetails(servletRequest).getRemoteAddress();
            securityService.updateUserLastLogin(userName, remoteAddress, System.currentTimeMillis());
        }
    }

    /**
     * set session with authentication data
     * @param authentication - spring authentication
     * @param securityContext - spring security context
     * @param servletRequest - http servlet request
     */
    void setAuthentication(Authentication authentication,SecurityContext securityContext,HttpServletRequest servletRequest) {
        if (authentication.isAuthenticated()) {
            //Log authentication if not anonymous
            if (!isAnonymous(authentication)) {
                AccessLogger.loggedIn(authentication);
            }
            //Set a http session token so that we can reuse the login in direct repo browsing
            UiRequestUtils.setAuthentication(servletRequest, authentication, true);
            //Update the spring  security context
            bindAuthentication(securityContext,authentication);
        }
    }

    /**
     * @return True is anonymous user is logged in to this session.
     */
    boolean isAnonymous(Authentication authentication) {
        return authentication != null && UserInfo.ANONYMOUS.equals(authentication.getPrincipal().toString());
    }

    /**
     * bind authentication to spring security context
     * @param securityContext
     */
    void bindAuthentication(SecurityContext securityContext,Authentication authentication) {
        securityContext.setAuthentication(authentication);
    }
}
