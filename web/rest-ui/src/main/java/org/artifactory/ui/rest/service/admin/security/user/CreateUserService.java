package org.artifactory.ui.rest.service.admin.security.user;

import org.artifactory.api.security.SecurityService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.api.security.UserInfoBuilder;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.security.MutableUserInfo;
import org.artifactory.security.SaltedPassword;
import org.artifactory.ui.rest.model.admin.security.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CreateUserService<T extends User> implements RestService<T> {
    @Autowired
    protected SecurityService securityService;
    @Autowired
    protected UserGroupService userGroupService;

    @Override
    public void execute(ArtifactoryRestRequest<T> request, RestResponse response) {
        User user = request.getImodel();
        MutableUserInfo newUser = getMutableUserInfo(user);
        //create user in DB
        boolean created = userGroupService.createUser(newUser);
        //Update Artifactory Response Data
        updateArtifactoryResponse(response,user,created,true);
    }

    /**
     * create mutable user info from user model data
     * @param user - user model
     * @return mutable user info build with user model data
     */
    private MutableUserInfo getMutableUserInfo(User user) {
        UserInfoBuilder builder = new UserInfoBuilder(user.getName());
        SaltedPassword saltedPassword;
        if (user.isInternalPasswordDisabled()) {
            saltedPassword = SaltedPassword.INVALID_PASSWORD;
        } else {
            saltedPassword = securityService.generateSaltedPassword(user.getPassword());
        }
        builder.password(saltedPassword)
                .email(user.getEmail())
                .admin(user.isAdmin())
                .updatableProfile(user.isProfileUpdatable())
                .groups(user.getUserGroups());
        return builder.build();
    }
    /**
     * update artifactory response with model ans status code to response
     * @param artifactoryRestResponse - encapsulate data require for response
     * @param user - new user model
     * @param succeeded - if true user has been successfully created
     * @param create - if true create user else update
     */
    private void updateArtifactoryResponse(RestResponse artifactoryRestResponse,
            User user,boolean succeeded ,boolean create){
        if (!succeeded) {
            artifactoryRestResponse.error("User '" + user.getName() + "' already exists");
            return;
        }
        // update successful user creation data
        if(create) {
            artifactoryRestResponse.info("Successfully created user '" + user.getName() + "'");
            artifactoryRestResponse.responseCode(HttpServletResponse.SC_CREATED);
        }
    }
}
