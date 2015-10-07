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

package org.artifactory.webapp.wicket.page.security.profile;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.time.Duration;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.security.SecurityService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.wicket.ajax.NoAjaxIndicatorDecorator;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.component.help.HelpBubble;
import org.artifactory.common.wicket.component.panel.passwordstrength.PasswordStrengthComponentPanel;
import org.artifactory.common.wicket.component.panel.titled.TitledPanel;
import org.artifactory.security.MutableUserInfo;
import org.artifactory.security.crypto.CryptoHelper;
import org.artifactory.webapp.wicket.util.validation.EmailAddressValidator;
import org.artifactory.webapp.wicket.util.validation.PasswordStrengthValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.security.KeyPair;

/**
 * @author Yoav Landman
 */
public class ProfilePanel extends TitledPanel {
    private static final Logger log = LoggerFactory.getLogger(ProfilePanel.class);
    private static final String HIDDEN_PASSWORD = "************";

    @SpringBean
    private UserGroupService userGroupService;

    @SpringBean
    private AuthorizationService authService;

    @SpringBean
    private SecurityService securityService;

    private Label encryptedPasswordLabel;
    private Form form;

    public ProfilePanel(String id, Form form, ProfileModel profile) {
        super(id);
        this.form = form;
        setOutputMarkupId(true);

        setDefaultModel(new CompoundPropertyModel<>(profile));
        add(new CssClass("display:block"));

        WebMarkupContainer encryptedPasswordContainer = new WebMarkupContainer("encryptedPasswordContainer");
        add(encryptedPasswordContainer);
        encryptedPasswordLabel = new Label("encryptedPassword", HIDDEN_PASSWORD);
        encryptedPasswordLabel.setVisible(securityService.isPasswordEncryptionEnabled());
        encryptedPasswordContainer.add(encryptedPasswordLabel);
        encryptedPasswordContainer.add(
                new HelpBubble("encryptedPassword.help", new ResourceModel("encryptedPassword.help")));
        encryptedPasswordContainer.setVisible(!ConstantValues.uiHideEncryptedPassword.getBoolean());

        // Profile update fields are only displayed for users with permissions to do so
        final WebMarkupContainer updateFieldsContainer = new WebMarkupContainer("updateFieldsContainer");
        updateFieldsContainer.setVisible(authService.isUpdatableProfile());
        add(updateFieldsContainer);

        addPasswordFields(updateFieldsContainer);

        // Email
        TextField<String> emailTf = new TextField<>("email");
        emailTf.setEnabled(false);
        emailTf.add(EmailAddressValidator.getInstance());
        updateFieldsContainer.add(emailTf);
    }

    private void addPasswordFields(WebMarkupContainer updateFieldsContainer) {
        WebMarkupContainer passwordFieldsContainer = new WebMarkupContainer("passwordFieldsContainer");
        final TextField<String> newPassword;
        final WebMarkupContainer strength;
        final TextField<String> retypedPassword;

        if (authService.isDisableInternalPassword()) {
            newPassword = new TextField<>("newPassword");
            strength = new WebMarkupContainer("strengthPanel");
            retypedPassword = new TextField<>("retypedPassword");
            passwordFieldsContainer.setVisible(false);
        } else {
            // New password
            newPassword = new PasswordTextField("newPassword");
            newPassword.setRequired(false);
            newPassword.setEnabled(false);
            newPassword.add(PasswordStrengthValidator.getInstance());


            strength = new PasswordStrengthComponentPanel("strengthPanel",
                    new PropertyModel(newPassword, "modelObject"));
            strength.setOutputMarkupId(true);

            newPassword.add(new AjaxFormComponentUpdatingBehavior("onkeyup") {
                @Override
                protected IAjaxCallDecorator getAjaxCallDecorator() {
                    return new NoAjaxIndicatorDecorator();
                }

                @Override
                protected void onError(AjaxRequestTarget target, RuntimeException e) {
                    super.onError(target, e);
                    String password = getFormComponent().getRawInput();
                    newPassword.setDefaultModelObject(password);
                    target.add(strength);
                }

                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    target.add(strength);
                }
            }.setThrottleDelay(Duration.seconds(0.5)));

            retypedPassword = new PasswordTextField("retypedPassword");
            retypedPassword.setRequired(false);
            retypedPassword.setEnabled(false);
            form.add(new EqualPasswordInputValidator(newPassword, retypedPassword));
        }

        passwordFieldsContainer.add(newPassword);
        passwordFieldsContainer.add(strength);
        passwordFieldsContainer.add(retypedPassword);
        updateFieldsContainer.add(passwordFieldsContainer);
    }

    private ProfileModel getUserProfile() {
        return (ProfileModel) getDefaultModelObject();
    }

    @Override
    public String getTitle() {
        return "";
    }

    public void displayEncryptedPassword(MutableUserInfo mutableUser) {
        // generate a new KeyPair and update the user profile
        regenerateKeyPair(mutableUser);

        // display the encrypted password
        if (securityService.isPasswordEncryptionEnabled()) {
            String currentPassword = getUserProfile().getCurrentPassword();
            SecretKey secretKey = CryptoHelper.generatePbeKeyFromKeyPair(mutableUser.getPrivateKey(),
                    mutableUser.getPublicKey(), false);
            String encryptedPassword = CryptoHelper.encryptSymmetric(currentPassword, secretKey, false);
            encryptedPasswordLabel.setDefaultModelObject(encryptedPassword);
        }
    }

    private void regenerateKeyPair(MutableUserInfo mutableUser) {
        if (!StringUtils.hasText(mutableUser.getPrivateKey())) {
            log.debug("Generating new KeyPair for {}", mutableUser.getUsername());
            KeyPair keyPair = CryptoHelper.generateKeyPair();
            mutableUser.setPrivateKey(CryptoHelper.convertToString(keyPair.getPrivate()));
            mutableUser.setPublicKey(CryptoHelper.convertToString(keyPair.getPublic()));
            userGroupService.updateUser(mutableUser, false);
        }
    }
}
