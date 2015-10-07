package org.artifactory.ui.rest.service.admin.security.auth.forgotpassword;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.security.SecurityService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.security.MutableUserInfo;
import org.artifactory.ui.rest.model.admin.security.login.UserLogin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ResetPasswordService<T extends UserLogin> implements RestService<T> {
    private static final Logger log = LoggerFactory.getLogger(ResetPasswordService.class);


    @Autowired
    UserGroupService userGroupService;
    @Autowired
    SecurityService securityService;

    @Override
    public void execute(ArtifactoryRestRequest<T> request, RestResponse response) {
        String passwordGenKey = request.getQueryParamByKey("key");
        String userName = request.getImodel().getUser();
        String newPassword = request.getImodel().getPassword();
        // save new generated password
        saveNewPassword(response, passwordGenKey, userName, newPassword);
    }

    /**
     * validate generated key and save user new password
     *
     * @param artifactoryResponse - encapsulate data require for response
     * @param passwordGenKey      - reset password generated key
     * @param userName            - user name  which run reset passsword
     * @param newPassword         - new updated password
     */
    private void saveNewPassword(RestResponse artifactoryResponse, String passwordGenKey, String userName,
            String newPassword) {
        MutableUserInfo user = InfoFactoryHolder.get().copyUser(
                userGroupService.findUser(userName));
        // perform key validation before saving new password
        boolean isKeyValid = validateKey(artifactoryResponse, passwordGenKey, user);
        if (!isKeyValid) {
            return;
        }
        // key is valid continue with reset password
        user.setPassword(securityService.generateSaltedPassword(newPassword));
        user.setGenPasswordKey(null);
        userGroupService.updateUser(user, false);
        log.info("The user: '{}' has successfully reset his password.", user.getUsername());
        artifactoryResponse.info("Password reset successfully.");
    }

    /**
     * validate generated key before saving password
     *
     * @param artifactoryResponse - encapsulate data require for response
     * @param passwordGenKey      - generated key
     * @param user                - user which run user password
     * @Return - if true - reset key is valid
     */
    private boolean validateKey(RestResponse artifactoryResponse, String passwordGenKey, MutableUserInfo user) {
        String passwordKey = user.getGenPasswordKey();
        if ((StringUtils.isEmpty(passwordKey)) || (!passwordKey.equals(passwordGenKey))) {
            artifactoryResponse.error("key is not valid");
            return false;
        }
        return true;
    }
}
