package org.artifactory.ui.rest.model.admin.security.login;

import org.artifactory.rest.common.model.BaseModel;

/**
 * @author chen keinan
 */
public class UserLogin extends BaseModel{

    private String user;
    private  String password;
    Boolean forgotPassword;
    private String ssoProviderLink;
    private String oauthProviderLink;

    public UserLogin(){}

    public UserLogin(String user) {
        this.user = user;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getForgotPassword() {
        return forgotPassword;
    }

    public void setForgotPassword(Boolean forgotPassword) {
        this.forgotPassword = forgotPassword;
    }

    public String getSsoProviderLink() {
        return ssoProviderLink;
    }

    public String getOauthProviderLink() {
        return oauthProviderLink;
    }

    public void setSsoProviderLink(String ssoProviderLink) {
        this.ssoProviderLink = ssoProviderLink;
    }

    public void setOauthProviderLink(String oauthProviderLink) {
        this.oauthProviderLink = oauthProviderLink;
    }
}
