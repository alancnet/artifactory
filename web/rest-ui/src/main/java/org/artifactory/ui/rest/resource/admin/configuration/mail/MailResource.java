package org.artifactory.ui.rest.resource.admin.configuration.mail;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.ui.rest.model.admin.configuration.mail.MailServer;
import org.artifactory.ui.rest.resource.BaseResource;
import org.artifactory.ui.rest.service.admin.configuration.ConfigServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Chen Keinan
 */
@Path("mail")
@RolesAllowed({AuthorizationService.ROLE_ADMIN})
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class MailResource extends BaseResource {

    @Autowired
    protected ConfigServiceFactory configServiceFactory;

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateMail(MailServer mailServer)
            throws Exception {
        return runService(configServiceFactory.updateMailService(), mailServer);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMail()
            throws Exception {
        return runService(configServiceFactory.getMailService());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response testMail(MailServer mailServer)
            throws Exception {
        return runService(configServiceFactory.testMailService(), mailServer);
    }
}
