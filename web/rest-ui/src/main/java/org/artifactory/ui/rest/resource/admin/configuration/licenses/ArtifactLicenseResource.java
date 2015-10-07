package org.artifactory.ui.rest.resource.admin.configuration.licenses;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.ui.rest.model.admin.configuration.licenses.DeleteLicensesModel;
import org.artifactory.ui.rest.model.admin.configuration.licenses.License;
import org.artifactory.ui.rest.resource.BaseResource;
import org.artifactory.ui.rest.service.admin.configuration.ConfigServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Chen Kainan
 */
@Path("licenses")
@RolesAllowed({AuthorizationService.ROLE_ADMIN})
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ArtifactLicenseResource extends BaseResource {
    @Autowired
    protected ConfigServiceFactory configServiceFactory;

    @POST
    @Path("crud{id:(/[^/]+?)?}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createLicense(License license)
            throws Exception {
        return runService(configServiceFactory.createArtifactLicenseService(), license);
    }

    @PUT
    @Path("crud{id:(/[^/]+?)?}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateLicense(License license)
            throws Exception {
        return runService(configServiceFactory.updateArtifactLicenseService(), license);
    }

    @GET
    @Path("crud{id:(/[^/]+?)?}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLicense()
            throws Exception {
        return runService(configServiceFactory.getArtifactLicenseService());
    }

    @POST
    @Path("deleteLicense")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteLicense(DeleteLicensesModel deleteLicensesModel)
            throws Exception {
        return runService(configServiceFactory.deleteArtifactLicenseService(),deleteLicensesModel);
    }
}
