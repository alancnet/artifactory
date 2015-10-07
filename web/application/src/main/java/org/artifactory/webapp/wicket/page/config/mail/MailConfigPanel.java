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

package org.artifactory.webapp.wicket.page.config.mail;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.mail.MailServerConfiguration;
import org.artifactory.api.mail.MailService;
import org.artifactory.common.wicket.behavior.defaultbutton.DefaultButtonBehavior;
import org.artifactory.common.wicket.component.border.titled.TitledBorder;
import org.artifactory.common.wicket.component.checkbox.styled.StyledCheckbox;
import org.artifactory.common.wicket.component.form.SecureForm;
import org.artifactory.common.wicket.component.links.TitledAjaxSubmitLink;
import org.artifactory.common.wicket.component.panel.feedback.UnescapedFeedbackMessage;
import org.artifactory.common.wicket.component.panel.titled.TitledPanel;
import org.artifactory.common.wicket.util.AjaxUtils;
import org.artifactory.common.wicket.util.WicketUtils;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.mail.MailServerDescriptor;
import org.artifactory.security.crypto.CryptoHelper;
import org.artifactory.util.EmailException;
import org.artifactory.util.HttpUtils;
import org.artifactory.webapp.wicket.page.config.SchemaHelpBubble;
import org.artifactory.webapp.wicket.page.logs.SystemLogsPage;
import org.artifactory.webapp.wicket.util.validation.EmailAddressValidator;
import org.artifactory.webapp.wicket.util.validation.PortNumberValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * Displays the different fields required for the mail server configuration.
 *
 * @author Noam Tenne
 */
public class MailConfigPanel extends TitledPanel {

    private static final Logger log = LoggerFactory.getLogger(MailConfigPanel.class);

    @SpringBean
    private CentralConfigService centralConfigService;

    @SpringBean
    private MailService mailService;

    private TextField<String> testRecipientTextField;

    private Form<MailServerDescriptor> form;

    public MailConfigPanel(String id) {
        super(id);
        MailServerDescriptor descriptor = getMailServerDescriptor();
        form = new SecureForm("form", new CompoundPropertyModel<>(descriptor));

        form.add(new StyledCheckbox("enabled"));
        form.add(new SchemaHelpBubble("enabled.help"));

        form.add(new TextField<String>("host").setRequired(true));
        form.add(new SchemaHelpBubble("host.help"));

        form.add(new RequiredTextField<Integer>("port").add(new PortNumberValidator()).setOutputMarkupId(true));
        form.add(new SchemaHelpBubble("port.help"));

        form.add(new TextField<String>("username"));
        form.add(new SchemaHelpBubble("username.help"));

        form.add(new PasswordTextField("password").setResetPassword(false).setRequired(false));
        form.add(new SchemaHelpBubble("password.help"));

        form.add(new TextField<String>("from").add(EmailAddressValidator.getInstance()));
        form.add(new SchemaHelpBubble("from.help"));

        form.add(new TextField<String>("subjectPrefix"));
        form.add(new SchemaHelpBubble("subjectPrefix.help"));

        form.add(new TextField<String>("artifactoryUrl"));
        form.add(new SchemaHelpBubble("artifactoryUrl.help"));

        form.add(new StyledCheckbox("tls"));
        form.add(new SchemaHelpBubble("tls.help"));

        final StyledCheckbox sslCheckbox = new StyledCheckbox("ssl", new PropertyModel<Boolean>(descriptor, "ssl"));
        //Add behavior that auto-switches the port to default SSL or normal values
        sslCheckbox.add(new AjaxFormComponentUpdatingBehavior("onclick") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                form.getModelObject().setPort(sslCheckbox.isChecked() ? 465 : 25);
                Component portTextField = form.get("port");
                target.add(portTextField);
            }
        });

        form.add(sslCheckbox);
        form.add(new SchemaHelpBubble("ssl.help"));

        TitledBorder borderTest = new TitledBorder("testBorder");
        form.add(borderTest);

        testRecipientTextField = new TextField<>("testRecipient", new Model<String>());
        testRecipientTextField.add(EmailAddressValidator.getInstance());
        borderTest.add(testRecipientTextField);
        borderTest.add(createSendTestButton(form));

        add(form);
    }

    @Override
    public String getTitle() {
        return "Mail Server Settings";
    }

    /**
     * Creates the form save button
     *
     * @return TitledAjaxSubmitLink - The save button
     */
    public TitledAjaxSubmitLink createSaveButton() {
        TitledAjaxSubmitLink saveButton = new TitledAjaxSubmitLink("save", "Save", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form form) {
                MutableCentralConfigDescriptor cc = centralConfigService.getMutableDescriptor();
                cc.setMailServer(((MailServerDescriptor) form.getDefaultModelObject()));
                centralConfigService.saveEditedDescriptorAndReload(cc);
                info("Successfully updated Mail server settings");
                AjaxUtils.refreshFeedback(target);
            }
        };
        form.add(new DefaultButtonBehavior(saveButton));
        return saveButton;
    }

    /**
     * Creates the send test mail button
     *
     * @param form The panel's main form
     * @return TitledAjaxSubmitLink - The send button
     */
    private TitledAjaxSubmitLink createSendTestButton(Form form) {
        TitledAjaxSubmitLink searchButton = new TitledAjaxSubmitLink("sendTest", "Send Test Mail", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form form) {
                String testRecipient = testRecipientTextField.getValue();
                if (StringUtils.isEmpty(testRecipient)) {
                    displayError(target, "Please specify a recipient for the test message");
                    return;
                }
                MailServerDescriptor d = (MailServerDescriptor) form.getDefaultModelObject();
                MailServerConfiguration mailServerConfiguration = new MailServerConfiguration(
                        d.isEnabled(), d.getHost(), d.getPort(), d.getUsername(),
                        CryptoHelper.decryptIfNeeded(d.getPassword()), d.getFrom(), d.getSubjectPrefix(),
                        d.isTls(), d.isSsl(), d.getArtifactoryUrl());
                if (!validateConfig(mailServerConfiguration)) {
                    displayError(target, "Sending a test message requires the configuration to be enabled with " +
                            "defined host and port properties");
                    return;
                }

                sendMail(target, mailServerConfiguration);
            }

            public boolean validateConfig(MailServerConfiguration mailServerConfiguration) {
                boolean hasHost = false;
                boolean hasPort = false;
                if (mailServerConfiguration != null) {
                    hasHost = !StringUtils.isEmpty(mailServerConfiguration.getHost());
                    hasPort = (mailServerConfiguration.getPort() > 0);
                }

                return hasHost && hasPort && mailServerConfiguration.isEnabled();
            }

            private void sendMail(AjaxRequestTarget target, MailServerConfiguration configuration) {
                String testRecipient = testRecipientTextField.getValue();
                //Sanity check (has validator): If the recipient field is empty, alert
                if (!StringUtils.isEmpty(testRecipient)) {
                    try {
                        mailService.sendMail(new String[]{testRecipient}, "Test", createTestMessage(configuration),
                                configuration);
                        String confirmMessage = "Successfully sent test message to '" + testRecipient + "'";
                        info(confirmMessage);
                        log.info(confirmMessage);
                        AjaxUtils.refreshFeedback(target);
                    } catch (EmailException e) {
                        String message = e.getMessage();
                        if (message == null) {
                            message = "Failed to send e-mail";
                        }
                        log.error(message, e);
                        String systemLogsPage = WicketUtils.absoluteMountPathForPage(SystemLogsPage.class);
                        message += " Please review <a href=\"" + systemLogsPage + "\">log</a> for further details";
                        displayError(target, message);
                    }
                } else {
                    displayError(target, "Test recipient field cannot be empty");
                }
            }

            private void displayError(AjaxRequestTarget target, String error) {
                error(new UnescapedFeedbackMessage(error));
                AjaxUtils.refreshFeedback(target);
            }
        };
        return searchButton;
    }

    // Create a test message for the mailer, if an Artifactory URL is configured the mail will have a link
    // pointing to the Artifactory instance URL.

    private String createTestMessage(MailServerConfiguration configuration) {
        StringBuilder message = new StringBuilder();
        message.append("This is a test message from Artifactory").append("<br/>");
        String artifactoryUrl = configuration.getArtifactoryUrl();
        if (StringUtils.isNotBlank(artifactoryUrl)) {
            String artifactoryLink = createArtifactoryLinkFromUrl(artifactoryUrl);
            message.append("Your Artifactory base URL is: ").append(artifactoryLink);
        } else {
            message.append("No Artifactory base URL is configured");
        }
        return message.toString();
    }

    private String createArtifactoryLinkFromUrl(String artifactoryUrl) {
        StringBuilder builder = new StringBuilder();
        builder.append("<a href=").append(artifactoryUrl).append(" target=\"blank\"").append(">")
                .append(artifactoryUrl).append("<a/>");
        return builder.toString();
    }

    /**
     * Returns the mail server descriptor
     *
     * @return MailServerDescriptor - The mail server descriptor
     */
    private MailServerDescriptor getMailServerDescriptor() {
        CentralConfigDescriptor centralConfig = centralConfigService.getMutableDescriptor();
        MailServerDescriptor mailServerDescriptor = centralConfig.getMailServer();
        if (mailServerDescriptor == null) {
            mailServerDescriptor = new MailServerDescriptor();
            // if the descriptor does not exist, set the preliminary Artifactory URL from the UI request.
            setArtifactoryUrlInDescriptor(mailServerDescriptor);
        }
        return mailServerDescriptor;
    }

    private void setArtifactoryUrlInDescriptor(MailServerDescriptor mailServerDescriptor) {
        HttpServletRequest httpServletRequest = WicketUtils.getHttpServletRequest();
        mailServerDescriptor.setArtifactoryUrl(HttpUtils.getServletContextUrl(httpServletRequest));
    }
}
