package org.artifactory.ui.rest.service.admin.security.auth.forgotpassword;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.saml.SamlSsoAddon;
import org.artifactory.addon.oauth.OAuthSsoAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.mail.MailServerDescriptor;
import org.artifactory.descriptor.security.sso.SamlSettings;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.security.login.UserLogin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class LoginRelatedDataService implements RestService {

    @Autowired
    private CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        CentralConfigDescriptor descriptor = centralConfigService.getDescriptor();
        UserLogin userLogin = new UserLogin();
        // update saml provider link
        updateSamlLinkIfEnable(request, descriptor, userLogin);
        // update oauth provider link
        updateOAuthLinkIfEnable(request, userLogin);
        // update display forgot password flag
        updateDisplayForgotFlag(descriptor, userLogin);
        response.iModel(userLogin);
    }

    /**
     * update display forgot password flag
     *
     * @param descriptor - config descriptor
     * @param userLogin  - user details
     */
    private void updateDisplayForgotFlag(CentralConfigDescriptor descriptor, UserLogin userLogin) {
        MailServerDescriptor mailServer = descriptor.getMailServer();
        boolean isMailServerEnable = (mailServer != null && mailServer.isEnabled()) ? true : false;
        if (isMailServerEnable) {
            userLogin.setForgotPassword(true);
        } else {
            userLogin.setForgotPassword(false);
        }
    }

    /**
     * @param request    - encapsulate data related to request
     * @param descriptor - config descriptor
     * @param userLogin  - user login details
     */
    private void updateSamlLinkIfEnable(ArtifactoryRestRequest request, CentralConfigDescriptor descriptor,
            UserLogin userLogin) {
        SamlSettings samlSettings = descriptor.getSecurity().getSamlSettings();
        if (samlSettings != null && samlSettings.isEnableIntegration()) {
            AddonsManager addons = ContextHelper.get().beanForType(AddonsManager.class);
            SamlSsoAddon samlSsoAddon = addons.addonByType(SamlSsoAddon.class);
            String samlLoginIdentityProviderUrl = samlSsoAddon.getSamlLoginIdentityProviderUrl(
                    request.getServletRequest());
            // add sso lin if available
            userLogin.setSsoProviderLink(samlLoginIdentityProviderUrl);
        }
    }

    /**
     * @param request    - encapsulate data related to request
     * @param userLogin  - user login details
     */
    private void updateOAuthLinkIfEnable(ArtifactoryRestRequest request, UserLogin userLogin) {
        AddonsManager addons = ContextHelper.get().beanForType(AddonsManager.class);
        OAuthSsoAddon oauthSsoAddon = addons.addonByType(OAuthSsoAddon.class);
        String oauthLoginPageUrl = oauthSsoAddon.getOAuthLoginPageUrl(request.getServletRequest());
        if (oauthLoginPageUrl != null) userLogin.setOauthProviderLink(oauthLoginPageUrl);
    }
}
