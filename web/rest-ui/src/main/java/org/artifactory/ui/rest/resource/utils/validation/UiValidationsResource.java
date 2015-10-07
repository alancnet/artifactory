package org.artifactory.ui.rest.resource.utils.validation;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.ui.rest.resource.BaseResource;
import org.artifactory.ui.rest.service.utils.validation.ValidationsServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Resource for server side validations sent from the UI forms.
 *
 * @author Yossi Shaul
 */
@RolesAllowed({AuthorizationService.ROLE_ADMIN})
@Component
@Path("validations")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UiValidationsResource extends BaseResource {

    @Autowired
    private ValidationsServiceFactory validatorsFactory;

    @GET
    @Path("dateformat")
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateDateFormat() throws Exception {
        return runService(validatorsFactory.getTimeFormatValidatorService());
    }

    @GET
    @Path("name")
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateName() throws Exception {
        return runService(validatorsFactory.getNameValidatorService());
    }

    @GET
    @Path("uniqueid")
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateUniqueXmlKey() throws Exception {
        return runService(validatorsFactory.getUniqueXmlIdValidatorService());
    }

    @GET
    @Path("xmlname")
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateXsdCName() throws Exception {
        return runService(validatorsFactory.getXmlNameValidatorService());
    }
}
