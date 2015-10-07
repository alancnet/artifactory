package org.artifactory.ui.rest.resource.admin.configuration.propertysets;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.ui.rest.model.admin.configuration.propertysets.AdminPropertySetModel;
import org.artifactory.ui.rest.model.admin.configuration.propertysets.DeletePropertySetModel;
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
 * @author Dan Feldman
 */
@RolesAllowed({AuthorizationService.ROLE_ADMIN})
@Component
@Path("propertysets")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class PropertySetsResource extends BaseResource {

    public static final String PROP_SET_NAME = "name";
    public static final String PATH_PROP_SET_NAME = "{" + PROP_SET_NAME + "}";

    @Autowired
    protected ConfigServiceFactory configServiceFactory;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createPropertySet(AdminPropertySetModel propertySet) throws Exception {
        return runService(configServiceFactory.createPropertySet(), propertySet);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPropertySetNames() throws Exception {
        return runService(configServiceFactory.getPropertySetNames());
    }

    @GET
    @Path(PATH_PROP_SET_NAME)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPropertySet() throws Exception {
        return runService(configServiceFactory.getPropertySet());
    }

    @PUT
    @Path(PATH_PROP_SET_NAME)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updatePropertySet(AdminPropertySetModel propertySet) throws Exception {
        return runService(configServiceFactory.updatePropertySet(), propertySet);
    }

    @POST
    @Path("deletePropertySet")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deletePropertySet(DeletePropertySetModel propertySetModel) throws Exception {
        return runService(configServiceFactory.deletePropertySet(),propertySetModel);
    }
}
