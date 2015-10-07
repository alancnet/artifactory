package org.artifactory.ui.rest.service.admin.security.auth.forgotpassword;

import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.CoreAddons;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.security.login.UserLogin;
import org.artifactory.util.HttpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ForgotPasswordService implements RestService {

    @Autowired
    UserGroupService userGroupService;
    @Autowired
    CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        UserLogin userLogin = (UserLogin) request.getImodel();
        String resetPasswordUrl = getResetPasswordPageUrl();
        String username = userLogin.getUser();
        //Check if username is valid
        if (StringUtils.isEmpty(username)) {
            response.error(
                    "We have sent you via email a link for resetting your password. Please check your inbox.");
            response.responseCode(500);
            return;
        }
        // rest user password
        resetPassword(request, response, username,resetPasswordUrl);
    }

    /**
     * reset user password
     * @param artifactoryRequest - encapsulate data require for request
     * @param artifactoryResponse - encapsulate data related to response
     * @param username - username
     */
    private void resetPassword(ArtifactoryRestRequest artifactoryRequest, RestResponse artifactoryResponse,
            String username,String resetPasswordUrl) {
        // if in aol mode then have to go to the dashboard to reset password
        String remoteAddress = HttpUtils.getRemoteClientAddress(artifactoryRequest.getServletRequest());
         try {
            String status = userGroupService.resetPassword(username, remoteAddress, resetPasswordUrl);
            artifactoryResponse.info(status);
        } catch (Exception e) {
            artifactoryResponse.error(e.getMessage());
        }
    }

    /**
     * Get the bookmarkable URL of the reset password page
     *
     * @return String - URL to reset password page
     */

    private String getResetPasswordPageUrl() {
        CoreAddons addon = ContextHelper.get().beanForType(AddonsManager.class).addonByType(CoreAddons.class);
        String resetPageUrl = addon.getArtifactoryUrl();
        if (resetPageUrl != null &&  StringUtils.isNotBlank(resetPageUrl)) {
            if (!resetPageUrl.endsWith("/")) {
                resetPageUrl += "/";
            }
            if (addon.isAol()) {
                resetPageUrl += "#/resetpassword";
            } else {
                resetPageUrl += HttpUtils.ANGULAR_WEBAPP + "/#/resetpassword";
            }
        }
        return resetPageUrl;
    }
}
