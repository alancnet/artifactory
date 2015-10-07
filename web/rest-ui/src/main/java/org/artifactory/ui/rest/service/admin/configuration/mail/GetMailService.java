package org.artifactory.ui.rest.service.admin.configuration.mail;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.configuration.mail.MailServer;
import org.artifactory.ui.rest.service.utils.AolUtils;
import org.artifactory.util.HttpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetMailService implements RestService {

    @Autowired
    private CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        AolUtils.assertNotAol("GetMail");
        String contextUrl = HttpUtils.getServletContextUrl(request.getServletRequest());
        MailServer mailServer = getMailServerFromConfigDescriptor(contextUrl);
        // update response with mail server model
        response.iModel(mailServer);
    }

    /**
     * get mail server from config descriptor and populate data to mail server model
     *
     * @return mail server model
     * @param contextUrl
     */
    private MailServer getMailServerFromConfigDescriptor(String contextUrl) {
        MutableCentralConfigDescriptor configDescriptor = centralConfigService.getMutableDescriptor();
        if (configDescriptor.getMailServer() != null) {
            return new MailServer(configDescriptor.getMailServer());
        } else {
            MailServer mailServer = new MailServer();
            mailServer.setArtifactoryUrl(contextUrl);
            return mailServer;
        }
    }
}
