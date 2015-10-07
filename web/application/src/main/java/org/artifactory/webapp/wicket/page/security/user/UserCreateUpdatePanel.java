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

package org.artifactory.webapp.wicket.page.security.user;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.lang.Classes;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.validation.validator.StringValidator;
import org.artifactory.addon.AddonsManager;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.security.SecurityService;
import org.artifactory.api.security.UserAwareAuthenticationProvider;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.api.security.UserInfoBuilder;
import org.artifactory.common.wicket.ajax.NoAjaxIndicatorDecorator;
import org.artifactory.common.wicket.behavior.defaultbutton.DefaultButtonBehavior;
import org.artifactory.common.wicket.component.CreateUpdateAction;
import org.artifactory.common.wicket.component.CreateUpdatePanel;
import org.artifactory.common.wicket.component.border.titled.TitledBorder;
import org.artifactory.common.wicket.component.checkbox.styled.StyledCheckbox;
import org.artifactory.common.wicket.component.deletable.listview.DeletableLabelGroup;
import org.artifactory.common.wicket.component.help.HelpBubble;
import org.artifactory.common.wicket.component.links.TitledAjaxSubmitLink;
import org.artifactory.common.wicket.component.modal.ModalHandler;
import org.artifactory.common.wicket.component.modal.links.ModalCloseLink;
import org.artifactory.common.wicket.component.panel.passwordstrength.PasswordStrengthComponentPanel;
import org.artifactory.common.wicket.util.AjaxUtils;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.security.AccessLogger;
import org.artifactory.security.MutableUserInfo;
import org.artifactory.security.SaltedPassword;
import org.artifactory.security.UserGroupInfo;
import org.artifactory.security.UserInfo;
import org.artifactory.util.SerializablePair;
import org.artifactory.webapp.wicket.util.validation.EmailAddressValidator;
import org.artifactory.webapp.wicket.util.validation.NameValidator;
import org.artifactory.webapp.wicket.util.validation.PasswordStrengthValidator;
import org.ocpsoft.prettytime.PrettyTime;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Yoav Landman
 */
public class UserCreateUpdatePanel extends CreateUpdatePanel<UserModel> {
    public static final String SUPPRESS_ENTER_JS = "if(event.keyCode==13 ||\nwindow.event.keyCode==13){return false;}else{return true;}";
    @SpringBean
    private UserGroupService userGroupService;

    @SpringBean
    private AddonsManager addons;

    @SpringBean
    private AuthorizationService authorizationService;

    @SpringBean
    private SecurityService securityService;

    @SpringBean
    private UserAwareAuthenticationProvider provider;

    PasswordTextField passwordField;
    PasswordTextField retypedPasswordField;

    StyledCheckbox adminCheckbox;
    StyledCheckbox updatableProfileCheckbox;
    private final UsersTable usersListTable;

    public UserCreateUpdatePanel(CreateUpdateAction action, UserModel user, UsersTable usersListTable) {
        super(action, user);
        this.usersListTable = usersListTable;
        setWidth(412);
        form.setOutputMarkupId(true);
        add(form);

        TitledBorder border = new TitledBorder("border");
        form.add(border);

        final boolean create = isCreate();

        //Username
        RequiredTextField<String> usernameField = new RequiredTextField<>("username");
        setDefaultFocusField(usernameField);
        usernameField.setEnabled(create);
        usernameField.add(StringValidator.maximumLength(100));
        usernameField.add(new NameValidator("Invalid username '%s'"));
        border.add(usernameField);

        //Password
        passwordField = new PasswordTextField("password") {
            @Override
            public boolean isEnabled() {
                return !entity.isDisableInternalPassword();
            }

            @Override
            protected void onComponentTag(ComponentTag tag) {
                // Prevent submitting when pressing enter
                super.onComponentTag(tag);
                tag.put("onkeydown", SUPPRESS_ENTER_JS);
                tag.put("onkeypress", SUPPRESS_ENTER_JS);
            }
        };
        passwordField.setRequired(create).setOutputMarkupId(true);
        passwordField.add(PasswordStrengthValidator.getInstance());
        passwordField.setResetPassword(false);
        border.add(passwordField);

        final PasswordStrengthComponentPanel strength =
                new PasswordStrengthComponentPanel("strengthPanel", new PropertyModel(passwordField, "modelObject"));
        border.add(strength.setOutputMarkupId(true));

        passwordField.add(new AjaxFormComponentUpdatingBehavior("onkeyup") {
            @Override
            protected IAjaxCallDecorator getAjaxCallDecorator() {
                return new NoAjaxIndicatorDecorator();
            }

            @Override
            protected void onError(AjaxRequestTarget target, RuntimeException e) {
                super.onError(target, e);
                String password = getFormComponent().getRawInput();
                passwordField.setDefaultModelObject(password);
                target.add(strength);
            }

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                target.add(strength);
            }
        }.setThrottleDelay(Duration.seconds(0.5)));

        retypedPasswordField = new PasswordTextField("retypedPassword") {
            @Override
            protected void onBeforeRender() {
                super.onBeforeRender();
                entity.setRetypedPassword(entity.getPassword());
            }

            @Override
            public boolean isEnabled() {
                return !entity.isDisableInternalPassword();
            }
        };
        retypedPasswordField.setRequired(create).setOutputMarkupId(true);
        retypedPasswordField.setResetPassword(false);
        border.add(retypedPasswordField);

        // validate password and retyped password
        form.add(new EqualPasswordInputValidator(passwordField, retypedPasswordField) {
            @Override
            public void validate(Form form) {
                if (entity.isDisableInternalPassword()) {
                    // no need to validate passwords if internal passwords are disabled
                    return;
                }
                if (!create && !StringUtils.hasText(passwordField.getDefaultModelObjectAsString())) {
                    return;
                }
                super.validate(form);
            }

            @Override
            protected String resourceKey() {
                return Classes.simpleName(EqualPasswordInputValidator.class);
            }
        });

        //Email
        RequiredTextField<String> emailTf = new RequiredTextField<>("email");
        emailTf.add(EmailAddressValidator.getInstance());
        border.add(emailTf);

        //Can update profile
        updatableProfileCheckbox = new StyledCheckbox("updatableProfile") {
            @Override
            public boolean isEnabled() {
                return !entity.isAdmin();
            }
        };
        updatableProfileCheckbox.setOutputMarkupId(true);
        border.add(updatableProfileCheckbox);

        // Internal password
        final StyledCheckbox disableInternalPassword = new StyledCheckbox("disableInternalPassword") {
            @Override
            public boolean isEnabled() {
                // disable if it's an admin user
                return !entity.isAdmin();
            }
        };

        disableInternalPassword.add(new AjaxFormComponentUpdatingBehavior("onclick") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                if (entity.isDisableInternalPassword()) {
                    entity.setPassword("");
                }
                target.add(passwordField);
                target.add(retypedPasswordField);
            }
        });
        border.add(disableInternalPassword.setOutputMarkupId(true));
        StringResourceModel helpMessage = new StringResourceModel("disableInternalPasswordHelp", this, null);
        border.add(new HelpBubble("disableInternalPasswordHelp", helpMessage));

        //Admin
        adminCheckbox = new StyledCheckbox("admin");
        adminCheckbox.add(new AjaxFormComponentUpdatingBehavior("onclick") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                if (entity.isAdmin()) {
                    entity.setUpdatableProfile(true);
                    entity.setDisableInternalPassword(false);
                }
                target.add(updatableProfileCheckbox);
                target.add(disableInternalPassword);
                target.add(passwordField);
                target.add(retypedPasswordField);
            }
        });
        adminCheckbox.setLabel(Model.of("Admin"));
        border.add(adminCheckbox);

        // groups
        Set<UserGroupInfo> userGroups = user.getGroups();
        if (!create) {
            provider.addExternalGroups(user.getUsername(), user.getRealm(), userGroups);
        }

        final DeletableLabelGroup<UserGroupInfo> groupsListView =
                new DeletableLabelGroup<>("groups", userGroups);
        groupsListView.setLabelClickable(false);
        groupsListView.setVisible(!create);
        border.add(groupsListView);
        String groupsLabelText = "Groups";
        if ((userGroups == null) || (userGroups.isEmpty())) {
            groupsLabelText = "User has no group memberships";
        }
        Label groupsLabel = new Label("groupsLabel", groupsLabelText);
        groupsLabel.setVisible(!create);
        border.add(groupsLabel);

        addLastLoginLabel(border);

        //Cancel
        form.add(new ModalCloseLink("cancel"));

        //Submit
        String submitCaption = create ? "Create" : "Save";
        TitledAjaxSubmitLink submit = new TitledAjaxSubmitLink("submit", submitCaption, form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form form) {
                String username = entity.getUsername();
                username = username.toLowerCase();
                boolean successful = true;
                if (create) {
                    successful = createNewUser(username);
                    AccessLogger.created("Successfully created user '" + username + "'");
                } else {
                    updateUser(username);
                    AccessLogger.updated("Successfully updated user '" + username + "'");
                }
                AjaxUtils.refreshFeedback(target);
                if (successful) {
                    ModalHandler.closeCurrent(target);
                }
            }

            private boolean createNewUser(String username) {
                UserInfoBuilder builder = new UserInfoBuilder(username);
                builder.password(securityService.generateSaltedPassword(entity.getPassword()))
                        .email(entity.getEmail())
                        .admin(entity.isAdmin())
                        .updatableProfile(entity.isUpdatableProfile())
                        .groups(new HashSet<>(groupsListView.getData()));
                MutableUserInfo newUser = builder.build();

                boolean created = userGroupService.createUser(newUser);
                if (!created) {
                    error("User '" + username + "' already exists.");
                } else {
                    String successMessage = "Successfully created user '" + username + "'";
                    boolean userHasPermissions = authorizationService.userHasPermissions(username);
                    if (!userHasPermissions) {
                        successMessage += "\nUser has no assigned permissions yet. You can directly assign " +
                                "permissions to the user or add him to an existing group that has assigned permissions.";
                    }
                    getPage().info(successMessage);
                }
                return created;
            }

            private void updateUser(String username) {
                // get the user info from the database and update it from the model
                UserInfo origUser = userGroupService.findUser(username);
                MutableUserInfo userInfo = InfoFactoryHolder.get().copyUser(origUser);
                userInfo.setEmail(entity.getEmail());
                userInfo.setAdmin(entity.isAdmin());
                userInfo.setUpdatableProfile(entity.isUpdatableProfile());
                userInfo.setGroups(new HashSet<>(groupsListView.getData()));
                if (entity.isDisableInternalPassword()) {
                    // user should authenticate externally - set password to invalid
                    userInfo.setPassword(SaltedPassword.INVALID_PASSWORD);
                } else if (StringUtils.hasText(entity.getPassword())) {
                    userInfo.setPassword(securityService.generateSaltedPassword(entity.getPassword()));
                }
                userGroupService.updateUser(userInfo, !userInfo.hasSameAuthorizationContext(origUser));
                getPage().info("Successfully updated user '" + username + "'");
            }
        };
        form.add(submit);
        form.add(new DefaultButtonBehavior(submit));
    }

    @Override
    public void onClose(AjaxRequestTarget target) {
        // we need to reload the model on both save and cancel
        usersListTable.refreshUsersList(target);
        super.close(target);
    }

    private void addLastLoginLabel(TitledBorder border) {
        SerializablePair<String, Long> lastLoginInfo = null;

        //If user exists
        if (!isCreate()) {
            lastLoginInfo = securityService.getUserLastLoginInfo(entity.getUsername());
        }
        final boolean loginInfoValid = (lastLoginInfo != null);
        Label lastLogin = new Label("lastLogin", new Model());
        border.add(lastLogin);
        if (loginInfoValid) {
            Date date = new Date(lastLoginInfo.getSecond());
            String clientIp = lastLoginInfo.getFirst();
            PrettyTime prettyTime = new PrettyTime();
            lastLogin.setDefaultModelObject(
                    "Last logged in: " + prettyTime.format(date) + " (" + date.toString() + "), from "
                            + clientIp + ".");
        } else {
            lastLogin.setDefaultModelObject("Last logged in: " + "Never.");
        }
    }
}
