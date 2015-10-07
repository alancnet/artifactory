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

import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.security.SecurityService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.wicket.behavior.defaultbutton.DefaultButtonBehavior;
import org.artifactory.common.wicket.component.form.SecureForm;
import org.artifactory.common.wicket.component.links.TitledAjaxLink;
import org.artifactory.common.wicket.component.links.TitledAjaxSubmitLink;
import org.artifactory.common.wicket.util.AjaxUtils;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.security.AccessLogger;
import org.artifactory.security.MutableUserInfo;
import org.artifactory.security.UserInfo;
import org.artifactory.webapp.wicket.application.ArtifactoryApplication;
import org.artifactory.webapp.wicket.application.ArtifactoryWebSession;
import org.artifactory.webapp.wicket.page.base.AuthenticatedPage;
import org.artifactory.webapp.wicket.page.base.BasePage;
import org.springframework.util.StringUtils;

public class ProfilePage extends AuthenticatedPage {

    @SpringBean
    private AuthorizationService authorizationService;

    @SpringBean
    private SecurityService securityService;

    @SpringBean
    private UserGroupService userGroupService;
    private final ProfilePanel profilePanel;
    private final TitledAjaxLink cancelLink;
    private final TitledAjaxSubmitLink updateLink;
    private final Form form;
    private final BintrayProfilePanel bintrayProfilePanel;
    private MavenSettingsPanel mavenSettingsPanel;

    public ProfilePage() {
        if (!isEnabled()) {
            Class<? extends Page> accessDeniedPage = getApplication().getApplicationSettings().getAccessDeniedPage();
            setResponsePage(accessDeniedPage);
        }

        ProfileModel profile = new ProfileModel();
        UserInfo userInfo = loadUserInfo();
        profile.setEmail(userInfo.getEmail());
        profile.setBintrayAuth(userInfo.getBintrayAuth());
        setOutputMarkupId(true);
        setDefaultModel(new CompoundPropertyModel<>(profile));

        form = new SecureForm("form");

        //Submit
        updateLink = createUpdateProfileButton(form);
        updateLink.setEnabled(false);
        updateLink.setVisible(authorizationService.isUpdatableProfile());
        add(updateLink);

        //Cancel
        cancelLink = new TitledAjaxLink("cancel", "Cancel") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                setResponsePage(Application.get().getHomePage());
            }
        };

        add(cancelLink);

        form.add(new DefaultButtonBehavior(updateLink));
        add(form);
        WebMarkupContainer adminAuthOverlay = new WebMarkupContainer("adminAuthOverlay");

        form.add(adminAuthOverlay);
        profilePanel = new ProfilePanel("updatePanel", form, profile);
        form.add(profilePanel);

        bintrayProfilePanel = new BintrayProfilePanel<>("bintrayProfilePanel", profile, false);
        form.add(bintrayProfilePanel);

        ProfileLockPanel profileLockPanel = new ProfileLockPanel(profilePanel, bintrayProfilePanel, this, profile,
                adminAuthOverlay);
        form.add(profileLockPanel);

        WebMarkupContainer mavenSettingsPanel = new WebMarkupContainer("mavenSettingsPanel");
        mavenSettingsPanel.setVisible(false);
        form.add(mavenSettingsPanel);
    }

    private TitledAjaxSubmitLink createUpdateProfileButton(final Form form) {
        return new TitledAjaxSubmitLink("update", "Update", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form form) {
                ProfileModel profile = getUserProfile();
                UserInfo userInfo = loadUserInfo();
                if (!StringUtils.hasText(profile.getEmail())) {
                    error("Field 'Email address' is required.");
                } else if (StringUtils.hasText(profile.getBintrayUsername()) &&
                        !StringUtils.hasText(profile.getBintrayApiKey())) {
                    error("Cannot save Bintray username without an API key.");
                } else if (StringUtils.hasText(profile.getBintrayApiKey()) &&
                        !StringUtils.hasText(profile.getBintrayUsername())) {
                    error("Cannot save Bintray API key without username.");
                } else {
                    MutableUserInfo mutableUser = InfoFactoryHolder.get().copyUser(userInfo);
                    mutableUser.setEmail(profile.getEmail());
                    mutableUser.setBintrayAuth(profile.getBintrayAuth());
                    if (!authorizationService.isDisableInternalPassword()) {
                        String newPassword = profile.getNewPassword();
                        if (StringUtils.hasText(newPassword)) {
                            mutableUser.setPassword(securityService.generateSaltedPassword(newPassword));
                            profile.setCurrentPassword(newPassword);
                            profilePanel.displayEncryptedPassword(mutableUser);
                            if (mavenSettingsPanel != null && !ConstantValues.uiHideEncryptedPassword.getBoolean()) {
                                mavenSettingsPanel.displayEncryptedPassword(mutableUser);
                                target.add(mavenSettingsPanel);
                            }
                        }
                    }
                    userGroupService.updateUser(mutableUser, !mutableUser.hasSameAuthorizationContext(userInfo));
                    AccessLogger.updated("User " + mutableUser.getUsername() + " has updated his profile successfully");
                    info("Successfully updated profile '" + mutableUser.getUsername() + "'");
                }
                form.clearInput();
                target.add(profilePanel);
                AjaxUtils.refreshFeedback(target);

            }
        };
    }

    private UserInfo loadUserInfo() {
        // load the user directly from the database. the instance returned from currentUser() might not
        // be with the latest changes
        return userGroupService.findUser(userGroupService.currentUser().getUsername());
    }

    private ProfileModel getUserProfile() {
        return (ProfileModel) getDefaultModelObject();
    }

    @Override
    public String getPageName() {
        return "User Profile: " + authorizationService.currentUsername();
    }

    @Override
    protected Class<? extends BasePage> getMenuPageClass() {
        return ArtifactoryApplication.get().getHomePage();
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled()
                && ArtifactoryWebSession.get().isSignedIn()
                && !authorizationService.isAnonymous()
                && (authorizationService.isUpdatableProfile()
                || securityService.isPasswordEncryptionEnabled());
    }

    @Override
    public void onEvent(IEvent<?> event) {
        Object payload = event.getPayload();
        if (payload instanceof ProfileEvent) {
            ProfileEvent profileEvent = (ProfileEvent) payload;
            boolean updatableProfile = authorizationService.isUpdatableProfile();
            updateLink.setEnabled(updatableProfile);
            cancelLink.setEnabled(updatableProfile);

            ProfileModel userProfile = getUserProfile();
            if (!ConstantValues.uiHideEncryptedPassword.getBoolean()) {
                mavenSettingsPanel = new MavenSettingsPanel("mavenSettingsPanel", userProfile);
                form.replace(mavenSettingsPanel);
            }
            bintrayProfilePanel.updateDefaultModel(userProfile, updatableProfile);
            profileEvent.getTarget().add(this, bintrayProfilePanel);
        }
    }
}
