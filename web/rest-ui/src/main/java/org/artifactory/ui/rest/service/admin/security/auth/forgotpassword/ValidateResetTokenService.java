package org.artifactory.ui.rest.service.admin.security.auth.forgotpassword;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.security.UserInfo;
import org.artifactory.ui.rest.model.admin.security.login.UserLogin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ValidateResetTokenService implements RestService {

    @Autowired
    UserGroupService userGroupService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String key = request.getQueryParamByKey("key");
        // validate key , return user info id found
        UserInfo userByKey = findUserByKey(key, response);
        updateResponseModel(response, userByKey);
    }

    private void updateResponseModel(RestResponse artifactoryResponse, UserInfo userByKey) {
        if (userByKey != null) {
            UserLogin userLogin = new UserLogin(userByKey.getUsername());
            artifactoryResponse.iModel(userLogin);
        }
    }

    /**
     * Returns a user info object that belongs to the user which is associated with the given key If the user is not
     * found, display a warning and redirect to the login page
     *
     * @param key GenPasswordKey
     * @return UserInfo - UserInfo object of the user that's associated with the given key. Null
     */
    public UserInfo findUserByKey(String key, RestResponse artifactoryResponse) {
        List<UserInfo> userInfoList = userGroupService.getAllUsers(true);
        for (UserInfo userInfo : userInfoList) {
            String userKey = userInfo.getGenPasswordKey();
            if ((!StringUtils.isEmpty(userKey)) && userKey.equals(key)) {
                return userInfo;
            }
        }
        artifactoryResponse.error("The reset link is invalid.");
        artifactoryResponse.responseCode(HttpServletResponse.SC_UNAUTHORIZED);
        return null;
    }
}
