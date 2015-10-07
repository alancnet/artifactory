/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2012 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.artifactory.addon.wicket;

import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.api.security.SecurityService;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.wicket.component.links.TitledSubmitLink;
import org.artifactory.common.wicket.util.WicketUtils;
import org.artifactory.security.AuthenticationHelper;
import org.artifactory.webapp.wicket.application.ArtifactoryApplication;
import org.artifactory.webapp.wicket.page.security.login.LoginInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.RememberMeServices;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Eli Givoni
 */
public class DefaultLoginLink extends TitledSubmitLink {
    private static final Logger log = LoggerFactory.getLogger(DefaultLoginLink.class);

    @SpringBean
    private SecurityService securityService;

    @SpringBean
    private RememberMeServices rememberMeServices;

    public DefaultLoginLink(String id, String title, Form form) {
        super(id, title, form);
    }

    @Override
    public void onSubmit() {
        LoginInfo loginInfo = (LoginInfo) form.getDefaultModelObject();
        String username = loginInfo.getUsername();
        String password = loginInfo.getPassword();
        boolean signedIn = AuthenticatedWebSession.get().signIn(username, password);

        HttpServletRequest httpServletRequest = WicketUtils.getHttpServletRequest();
        if (signedIn) {
            /**
             * If login has been called because the user was not yet logged in, than continue to the original
             * destination otherwise to the Home page
             */
            try {
                if (!continueToOriginalDestination()) {
                    setResponsePage(ArtifactoryApplication.get().getHomePage());
                }
            } catch (RuntimeException ignored) {
                setResponsePage(ArtifactoryApplication.get().getHomePage());
            }
            //set a remember me cookie for the first success login
            if (!ConstantValues.securityDisableRememberMe.getBoolean()) {
                try {
                    rememberMeServices.loginSuccess(httpServletRequest, WicketUtils.getHttpServletResponse(),
                            AuthenticationHelper.getAuthentication());
                } catch (UsernameNotFoundException e) {
                    log.warn("Remember Me service is not supported for transient external users.");
                }
            }
        } else {
            if (!ConstantValues.securityDisableRememberMe.getBoolean()) {
                //Try the component based localizer first. If not found try the application localizer. Else use the default
                error("Username or password are incorrect. Login failed.");
                rememberMeServices.loginFail(httpServletRequest, getHttpServletResponse());
            }
        }
    }

    private HttpServletResponse getHttpServletResponse() {
        return WicketUtils.getHttpServletResponse();
    }
}
