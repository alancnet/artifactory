package org.artifactory.ui.rest.service.admin.configuration.mail;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.mail.MailServerConfiguration;
import org.artifactory.api.mail.MailService;
import org.artifactory.descriptor.mail.MailServerDescriptor;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.security.crypto.CryptoHelper;
import org.artifactory.ui.rest.model.admin.configuration.mail.MailServer;
import org.artifactory.ui.rest.service.utils.AolUtils;
import org.artifactory.util.EmailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class TestMailService<T extends MailServer> implements RestService<T> {
    private static final Logger log = LoggerFactory.getLogger(TestMailService.class);

    @Autowired
    private MailService mailService;

    @Override
    public void execute(ArtifactoryRestRequest<T> request, RestResponse response) {
        AolUtils.assertNotAol("TestMail");
        // get mail configuration instance
        MailServerConfiguration mailServerConfiguration = getMailServerConfiguration(request);
        // validate configuration data
        if (!validateConfig(mailServerConfiguration)) {
            response.error("Sending a test message requires the configuration to be enabled with " +
                    "defined host and port properties, at least.");
            return;
        }
        // send Test mail
        sendMail(mailServerConfiguration, request.getImodel(), response);
    }

    /**
     * get mail server configuration
     *
     * @param artifactoryRequest - encapsulate all date related to request
     * @return instance of mail server configuration data
     */
    private MailServerConfiguration getMailServerConfiguration(ArtifactoryRestRequest artifactoryRequest) {
        MailServerDescriptor serverData = (MailServerDescriptor) artifactoryRequest.getImodel();
        return new MailServerConfiguration(
                serverData.isEnabled(), serverData.getHost(), serverData.getPort(), serverData.getUsername(),
                CryptoHelper.decryptIfNeeded(serverData.getPassword()), serverData.getFrom(),
                serverData.getSubjectPrefix(),
                serverData.isTls(), serverData.isSsl(), serverData.getArtifactoryUrl());
    }

    /**
     * validate mail server setting
     *
     * @param mailServerConfiguration - mail server configuration
     * @return if true - configuration is valid
     */
    public boolean validateConfig(MailServerConfiguration mailServerConfiguration) {
        boolean hasHost = false;
        boolean hasPort = false;
        if (mailServerConfiguration != null) {
            hasHost = !StringUtils.isEmpty(mailServerConfiguration.getHost());
            hasPort = (mailServerConfiguration.getPort() > 0);
        }
        return hasHost && hasPort && mailServerConfiguration.isEnabled();
    }

    /**
     * send test mail to receipt
     *
     * @param configuration - mail server configuration
     * @param model         - mail server model
     * @param response      - encapsulate all data require for response
     */
    private void sendMail(MailServerConfiguration configuration, MailServer model, RestResponse response) {
        MailServer mailServer = model;
        String testRecipient = mailServer.getTestReceipt();
        if (!StringUtils.isEmpty(testRecipient)) {
            try {
                mailService.sendMail(new String[]{testRecipient}, "Test", createTestMessage(configuration),
                        configuration);
                String confirmMessage = "A test message has been sent successfully to '" + testRecipient + "'";
                response.info(confirmMessage);
                log.info(confirmMessage);
            } catch (EmailException e) {
                String message = e.getMessage();
                if (message == null) {
                    message = "An error has occurred while sending an e-mail.";
                }
                log.error(message, e);
                response.error(message);
            }
        } else {
            response.error("Test recipient field cannot be empty");
        }
    }

    /**
     * create test mail message
     *
     * @param configuration - mail server configuration
     * @return test message
     */
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

    /**
     * create artifactory link from url
     *
     * @param artifactoryUrl - artifactory url
     * @return url
     */
    private String createArtifactoryLinkFromUrl(String artifactoryUrl) {
        StringBuilder builder = new StringBuilder();
        builder.append("<a href=").append(artifactoryUrl).append(" target=\"blank\"").append(">")
                .append(artifactoryUrl).append("<a/>");
        return builder.toString();
    }
}
