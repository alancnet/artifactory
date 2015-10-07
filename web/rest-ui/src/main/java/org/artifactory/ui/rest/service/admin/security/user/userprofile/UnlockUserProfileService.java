package org.artifactory.ui.rest.service.admin.security.user.userprofile;

import org.artifactory.api.security.SecurityService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.security.InternalUsernamePasswordAuthenticationToken;
import org.artifactory.security.MutableUserInfo;
import org.artifactory.security.UserInfo;
import org.artifactory.security.crypto.CryptoHelper;
import org.artifactory.ui.rest.model.admin.configuration.bintray.BintrayUIModel;
import org.artifactory.ui.rest.model.admin.security.login.UserLogin;
import org.artifactory.ui.rest.model.admin.security.user.User;
import org.artifactory.ui.rest.model.admin.security.user.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.security.KeyPair;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UnlockUserProfileService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(UnlockUserProfileService.class);

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    private SecurityService securityService;

    @Autowired
    UserGroupService userGroupService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        UserLogin userLogin = (UserLogin) request.getImodel();
        // fetch user profile
        fetchUserProfile(response, userLogin);
     }

    /**
     * fetch user profile
     * @param artifactoryResponse - encapsulate data related to response
     * @param userLogin - hold entered password details
     */
    private void fetchUserProfile(RestResponse artifactoryResponse, UserLogin userLogin) {
        UserInfo userInfo = loadUserInfo();
        String enteredCurrentPassword = userLogin.getPassword();
        if (!authenticate(userInfo, enteredCurrentPassword)) {
           artifactoryResponse.error("The specified current password is incorrect.");
        } else {
            // get user  profile
            UserProfile userProfile = getUserProfile(userInfo, userLogin);
            artifactoryResponse.iModel(userProfile);
        }
    }

    /**
     * get user profile66
     * @param userInfo - current logged user info
     * @return current user profile
     */
    private UserProfile getUserProfile(UserInfo userInfo, UserLogin userLogin) {
        UserProfile userProfile = new UserProfile();
        updateUserInfo(userInfo, userProfile, userLogin);
        updateBintrayData(userInfo, userProfile);
        return userProfile;
    }

    /**
     * update user info in profile
     *
     * @param userInfo    - user info
     * @param userProfile - user profile
     */
    private void updateUserInfo(UserInfo userInfo, UserProfile userProfile, UserLogin userLogin) {
        User user = new User();
        user.setEmail(userInfo.getEmail());
        user.setProfileUpdatable(userInfo.isUpdatableProfile());
        userProfile.setUser(user);
        MutableUserInfo mutableUser = InfoFactoryHolder.get().copyUser(userInfo);
        String encryptedPassword = getEncryptedPassword(mutableUser, userLogin);
        user.setPassword(encryptedPassword);
    }

    /**
     */
    public String getEncryptedPassword(MutableUserInfo mutableUserInfo, UserLogin userLogin) {
        // generate a new KeyPair and update the user profile
        regenerateKeyPair(mutableUserInfo);
        // display the encrypted password
        if (securityService.isPasswordEncryptionEnabled()) {
            SecretKey secretKey = CryptoHelper.generatePbeKeyFromKeyPair(mutableUserInfo.getPrivateKey(),
                    mutableUserInfo.getPublicKey(), false);
            return CryptoHelper.encryptSymmetric(userLogin.getPassword(), secretKey, false);
        }
        return null;
    }

    /**
     * @param mutableUser
     */
    private void regenerateKeyPair(MutableUserInfo mutableUser) {
        if (!StringUtils.hasText(mutableUser.getPrivateKey())) {
            log.debug("Generating new KeyPair for {}", mutableUser.getUsername());
            KeyPair keyPair = CryptoHelper.generateKeyPair();
            mutableUser.setPrivateKey(CryptoHelper.convertToString(keyPair.getPrivate()));
            mutableUser.setPublicKey(CryptoHelper.convertToString(keyPair.getPublic()));
            userGroupService.updateUser(mutableUser, false);
        }
    }

    /**
     * update user profile bintray data
     *
     * @param userInfo    - user info from db
     * @param userProfile - user profile
     */
    private void updateBintrayData(UserInfo userInfo, UserProfile userProfile) {
        String[] splitBintrayAuth = null;
        if (userInfo.getBintrayAuth() != null) {
            splitBintrayAuth = userInfo.getBintrayAuth().split(":");
        }
        BintrayUIModel bintray = new BintrayUIModel();
        if (splitBintrayAuth != null && splitBintrayAuth.length > 0) {
            bintray.setUserName(splitBintrayAuth[0]);
            bintray.setApiKey(splitBintrayAuth[1]);
        }
        userProfile.setBintray(bintray);
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

    /**
     * authenticate current logged user against entered password
     * @param userInfo - logged user info
     * @param enteredCurrentPassword - entered password
     * @return - if true user is authenticated
     */
    private boolean authenticate(UserInfo userInfo, String enteredCurrentPassword) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new InternalUsernamePasswordAuthenticationToken(userInfo.getUsername(),
                            enteredCurrentPassword));
            return (authentication != null) && authentication.isAuthenticated();
        } catch (AuthenticationException e) {
            return false;
        }
    }
}
