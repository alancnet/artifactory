package org.artifactory.webapp.wicket.page.security.profile;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.component.help.HelpBubble;
import org.artifactory.common.wicket.component.links.TitledAjaxSubmitLink;
import org.artifactory.common.wicket.component.panel.titled.TitledPanel;
import org.artifactory.common.wicket.util.AjaxUtils;
import org.artifactory.common.wicket.util.SetEnableVisitor;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.security.InternalUsernamePasswordAuthenticationToken;
import org.artifactory.security.MutableUserInfo;
import org.artifactory.security.UserInfo;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * User: gidis
 */
public class ProfileLockPanel extends TitledPanel {

    @SpringBean
    private AuthenticationManager authenticationManager;

    @SpringBean
    private AuthorizationService authService;

    @SpringBean
    private UserGroupService userGroupService;

    public ProfileLockPanel(final ProfilePanel profilePanel, final BintrayProfilePanel bintrayProfilePanel,
            final ProfilePage profilePage, ProfileModel profile, final WebMarkupContainer adminAuthOverlay) {
        super("lockPanel");

        setOutputMarkupId(true);

        setDefaultModel(new CompoundPropertyModel(profile));
        // unlock notification message
        final Label unlockNotificationMessage = new Label("unlockNotificationMessage",
                "Insert the password and press the Unlock button to edit the profile.");
        add(unlockNotificationMessage);
        // current password
        final PasswordTextField currentPassword = new PasswordTextField("currentPassword");

        add(currentPassword);
        add(new HelpBubble("currentPassword.help", getString("currentPassword.help")));


        // submit password
        add(new TitledAjaxSubmitLink("unlock", "Unlock") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                UserInfo userInfo = loadUserInfo();
                String enteredCurrentPassword = getUserProfile().getCurrentPassword();
                if (!authenticate(userInfo, enteredCurrentPassword)) {
                    error("The specified current password is incorrect.");
                } else {
                    unlockProfile(userInfo, target);
                }
                target.add(profilePanel);
                AjaxUtils.refreshFeedback(target);

            }

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

            private void unlockProfile(UserInfo userInfo, AjaxRequestTarget target) {
                currentPassword.setEnabled(false);
                this.setEnabled(false);
                if (authService.isUpdatableProfile()) {
                    unlockNotificationMessage.setVisible(false);
                    profilePanel.visitChildren(new SetEnableVisitor(true));
                    bintrayProfilePanel.visitChildren(new SetEnableVisitor(true));
                }
                profilePage.add(new CssClass("profile-panel"));
                adminAuthOverlay.add(new CssClass("adminAuthOverlayHide"));

                MutableUserInfo mutableUser = InfoFactoryHolder.get().copyUser(userInfo);
                profilePanel.displayEncryptedPassword(mutableUser);

                send(getPage(), Broadcast.BREADTH, new ProfileEvent(target, mutableUser));
            }
        });


    }

    private UserInfo loadUserInfo() {
        // load the user directly from the database. the instance returned from currentUser() might not
        // be with the latest changes
        return userGroupService.findUser(userGroupService.currentUser().getUsername());
    }

    private ProfileModel getUserProfile() {
        return (ProfileModel) getDefaultModelObject();
    }
}
