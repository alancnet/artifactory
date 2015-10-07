package org.artifactory.ui.rest.service.admin.security.user.userprofile;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.security.SecurityService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.security.AccessLogger;
import org.artifactory.security.MutableUserInfo;
import org.artifactory.security.UserInfo;
import org.artifactory.ui.rest.model.admin.configuration.bintray.BintrayUIModel;
import org.artifactory.ui.rest.model.admin.security.user.User;
import org.artifactory.ui.rest.model.admin.security.user.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UpdateUserProfileService implements RestService {

    @Autowired
    AuthorizationService authorizationService;

    @Autowired
    UserGroupService userGroupService;

    @Autowired
    SecurityService securityService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        UserProfile profile = (UserProfile) request.getImodel();
        // update user profile
        updateUserInfo(response, profile);
    }

    /**
     * update userInfo
     * @param artifactoryResponse
     * @param profile
     */
    private void updateUserInfo(RestResponse artifactoryResponse, UserProfile profile) {
        User user = profile.getUser();
        BintrayUIModel bintray = profile.getBintray();
        UserInfo userInfo = loadUserInfo();
        if (!StringUtils.hasText(profile.getUser().getEmail())) {
            artifactoryResponse.error("Field 'Email address' is required.");
        } else if (StringUtils.hasText(bintray.getUserName()) &&
                !StringUtils.hasText(bintray.getApiKey())) {
            artifactoryResponse.error("Cannot save Bintray username without an API key.");
        } else if (StringUtils.hasText(bintray.getApiKey()) &&
                !StringUtils.hasText(bintray.getUserName())) {
            artifactoryResponse.error("Cannot save Bintray API key without username.");
        } else {
            updateUserProfileToDB(artifactoryResponse, profile, user, bintray, userInfo);
        }
    }

    /**
     * update user profile to DB
     * @param artifactoryResponse - encapsulate data require for response
     * @param profile - update user profile
     * @param user - user part from user profile
     * @param bintray -- bintray part from user profile
     * @param userInfo - current logged user info
     */
    private void updateUserProfileToDB(RestResponse artifactoryResponse, UserProfile profile, User user, BintrayUIModel bintray,
            UserInfo userInfo) {
        MutableUserInfo mutableUser = InfoFactoryHolder.get().copyUser(userInfo);
        mutableUser.setEmail(profile.getUser().getEmail());
        if (org.apache.commons.lang.StringUtils.isNotBlank(bintray.getApiKey()) &&
                org.apache.commons.lang.StringUtils.isNotBlank(bintray.getUserName())){
            mutableUser.setBintrayAuth(bintray.getUserName() + ":" + bintray.getApiKey());
        }else{
            mutableUser.setBintrayAuth("");
        }
        if (!authorizationService.isDisableInternalPassword()) {
            String newPassword = user.getPassword();
            if (StringUtils.hasText(newPassword)) {
                mutableUser.setPassword(securityService.generateSaltedPassword(newPassword));
              }
        }
        userGroupService.updateUser(mutableUser, !mutableUser.hasSameAuthorizationContext(userInfo));
        AccessLogger.updated("User " + mutableUser.getUsername() + " has updated his profile successfully");
        artifactoryResponse.info("Successfully updated profile '" + mutableUser.getUsername() + "'");
    }

    /**
     * load current logged user info
     * @return - user info data
     */
    private UserInfo loadUserInfo() {
        // load the user directly from the database. the instance returned from currentUser() might not
        // be with the latest changes
        return userGroupService.findUser(userGroupService.currentUser().getUsername());
    }
}
