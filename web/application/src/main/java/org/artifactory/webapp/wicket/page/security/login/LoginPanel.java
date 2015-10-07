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

package org.artifactory.webapp.wicket.page.security.login;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IFormSubmittingComponent;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.wicket.SamlAddon;
import org.artifactory.addon.wicket.WebApplicationAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.security.SecurityService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.wicket.component.checkbox.styled.StyledCheckbox;
import org.artifactory.common.wicket.component.links.TitledPageLink;
import org.artifactory.common.wicket.component.panel.titled.TitledActionPanel;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.security.UserInfo;
import org.artifactory.webapp.wicket.application.ArtifactoryApplication;
import org.artifactory.webapp.wicket.page.security.login.forgot.ForgotPasswordLink;
import org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices;

/**
 * @author Yoav Aharoni
 */
public class LoginPanel extends TitledActionPanel {

    @SpringBean
    private CentralConfigService centralConfig;

    @SpringBean
    private AddonsManager addons;

    @SpringBean
    private SecurityService securityService;

    @SpringBean
    private UserGroupService userGroupService;

    public LoginPanel(String string, Form form) {
        super(string);

        Label defaultCredentialsLabel = new Label("defaultCredentials", " (default: admin/password)");
        try {
            UserInfo userInfo = userGroupService.findUser("admin");
            defaultCredentialsLabel.setVisible(neverLoggedIn(userInfo) && defaultAdminPassword(userInfo));
        } catch (Exception ignored) {
            defaultCredentialsLabel.setVisible(false);
        }
        add(defaultCredentialsLabel);

        // add username
        TextField username = new TextField("username");
        username.setRequired(true);
        username.setMarkupId("username");
        username.setOutputMarkupId(true);
        username.add(new AttributeModifier("autocomplete", new AbstractReadOnlyModel<String>() {
            @Override
            public String getObject() {
                return ConstantValues.useUserNameAutoCompleteOnLogin.getString();
            }
        }));

        add(username);

        // add password
        PasswordTextField password = new PasswordTextField("password");
        password.setRequired(false);
        password.setMarkupId("password");
        password.setOutputMarkupId(true);
        add(password);

        // add login link
        TitledPageLink ssoLoginLink = new TitledPageLink("ssoLogin", "SSO Login", null) {
            @Override
            protected CharSequence getURL() {
                return addons.addonByType(SamlAddon.class).getSamlLoginIdentityProviderUrl();
            }

            @Override
            public boolean isEnabled() {
                return addons.addonByType(SamlAddon.class).isSamlEnabled();
            }

            @Override
            public boolean isVisible() {
                return addons.addonByType(SamlAddon.class).isSamlEnabled();
            }
        };
        add(ssoLoginLink);
        // add login link
        IFormSubmittingComponent loginLink =
                addons.addonByType(WebApplicationAddon.class).getLoginLink("loginLink", form);
        addDefaultButton(loginLink);


        // add remember me checkbox
        StyledCheckbox checkbox = new StyledCheckbox("rememberMe") {
            @Override
            protected String getCheckboxInputName(String defaultName) {
                // set the parameter name to springs' remember me filter default name
                return AbstractRememberMeServices.DEFAULT_PARAMETER;
            }
        };
        checkbox.setSubmitButton((Component) loginLink);
        checkbox.setOutputMarkupPlaceholderTag(true);
        checkbox.setVisible(!ConstantValues.securityDisableRememberMe.getBoolean());
        add(checkbox);

        // add cancel link
        addButton(new TitledPageLink("cancel", "Cancel", ArtifactoryApplication.get().getHomePage()));

        // add forgot password link
        if (isMailServerConfigured()) {
            addButton(new ForgotPasswordLink("forgotPassword"));
        }
    }

    private boolean neverLoggedIn(UserInfo userInfo) {
        return (userInfo.getLastLoginTimeMillis() == 0) && (StringUtils.isEmpty(userInfo.getLastLoginClientIp()));
    }

    private boolean defaultAdminPassword(UserInfo userInfo) {
        String defaultPassword = securityService.generateSaltedPassword(SecurityService.DEFAULT_ADMIN_PASSWORD,
                userInfo.getSalt()).getPassword();
        return defaultPassword.equals(userInfo.getPassword());
    }

    /**
     * Checks if the mail server is configured (central config descriptor and mail server descriptor are not null)
     *
     * @return boolean - True if mail server is configured.
     */
    private boolean isMailServerConfigured() {
        CentralConfigDescriptor descriptor = centralConfig.getDescriptor();
        return ((descriptor != null) && (descriptor.getMailServer() != null) && descriptor.getMailServer().isEnabled());
    }
}
