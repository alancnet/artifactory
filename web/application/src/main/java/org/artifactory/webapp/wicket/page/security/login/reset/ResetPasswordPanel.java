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

package org.artifactory.webapp.wicket.page.security.login.reset;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.api.security.SecurityService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.common.wicket.component.LabeledValue;
import org.artifactory.common.wicket.component.form.SecureForm;
import org.artifactory.common.wicket.component.links.TitledAjaxSubmitLink;
import org.artifactory.common.wicket.component.panel.titled.TitledActionPanel;
import org.artifactory.common.wicket.util.WicketUtils;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.security.MutableUserInfo;
import org.artifactory.security.UserInfo;
import org.artifactory.webapp.wicket.page.security.login.LoginPage;
import org.artifactory.webapp.wicket.util.validation.PasswordStrengthValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Displays the "Reset SaltedPassword" interface
 *
 * @author Noam Tenne
 */
public class ResetPasswordPanel extends TitledActionPanel {

    private static final Logger log = LoggerFactory.getLogger(ResetPasswordPanel.class);

    @SpringBean
    private SecurityService securityService;
    @SpringBean
    private UserGroupService userGroupService;

    protected ResetPasswordPanel(String id) {
        super(id);
        setVersioned(false);

        final String passwordGenKey = WicketUtils.getParameter("key");

        //If there is no key supplied, redirect to the login page
        if (StringUtils.isEmpty(passwordGenKey)) {
            setResponse();
            return;
        }

        //If user is found
        final UserInfo userInfo = findUserByKey(passwordGenKey);
        if (userInfo != null) {
            Form resetForm = new SecureForm("resetForm");

            resetForm.add(new LabeledValue("description",
                    "Please choose a new password."));

            final PasswordTextField passwordTextField = new PasswordTextField("password", new Model<String>());
            passwordTextField.setRequired(true);
            passwordTextField.add(PasswordStrengthValidator.getInstance());
            resetForm.add(passwordTextField);
            PasswordTextField retypedPasswordTextField = new PasswordTextField("retypedPassword", new Model<String>());
            retypedPasswordTextField.setRequired(true);
            resetForm.add(retypedPasswordTextField);

            // validate password and retyped password
            resetForm.add(new EqualPasswordInputValidator(passwordTextField, retypedPasswordTextField));

            addButton(new TitledAjaxSubmitLink("reset", "Reset My Password", resetForm) {

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form form) {
                    /**
                     * We double check the key validity, since after the first reset, the user can back up into the page
                     * And won't have to pass the validity checks on init
                     */
                    MutableUserInfo user = InfoFactoryHolder.get().copyUser(
                            userGroupService.findUser(userInfo.getUsername()));
                    String passwordKey = user.getGenPasswordKey();
                    if ((StringUtils.isEmpty(passwordKey)) || (!passwordKey.equals(passwordGenKey))) {
                        invalidKeyResponse();
                        return;
                    }
                    String chosenPassword = passwordTextField.getValue();
                    user.setPassword(securityService.generateSaltedPassword(chosenPassword));
                    user.setGenPasswordKey(null);
                    userGroupService.updateUser(user, false);
                    log.info("The user: '{}' has successfully reset his password.", user.getUsername());
                    Session.get().info("Password reset successfully.");
                    setResponse();
                }
            });

            add(resetForm);
        }
    }

    /**
     * Returns a user info object that belongs to the user which is associated with the given key If the user is not
     * found, display a warning and redirect to the login page
     *
     * @param key GenPasswordKey
     * @return UserInfo - UserInfo object of the user that's associated with the given key. Null
     */
    public UserInfo findUserByKey(String key) {
        List<UserInfo> userInfoList = userGroupService.getAllUsers(true);
        for (UserInfo userInfo : userInfoList) {
            String userKey = userInfo.getGenPasswordKey();
            if ((!StringUtils.isEmpty(userKey)) && userKey.equals(key)) {
                return userInfo;
            }
        }

        invalidKeyResponse();
        return null;
    }

    private void invalidKeyResponse() {
        Session.get().warn("The key you have supplied is invalid.");
        setResponse();
    }

    /**
     * Set the login page as a response
     */
    private void setResponse() {
        throw new RestartResponseException(LoginPage.class);
    }
}