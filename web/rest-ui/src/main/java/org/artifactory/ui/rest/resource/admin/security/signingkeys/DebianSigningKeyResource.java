package org.artifactory.ui.rest.resource.admin.security.signingkeys;

import com.sun.jersey.multipart.FormDataMultiPart;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.ui.rest.model.admin.security.signingkey.SignKey;
import org.artifactory.ui.rest.model.utils.FileUpload;
import org.artifactory.ui.rest.resource.BaseResource;
import org.artifactory.ui.rest.service.admin.security.SecurityServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Chen Keinan
 */
@RolesAllowed({AuthorizationService.ROLE_ADMIN})
@Component
@Path("signingkeys")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DebianSigningKeyResource extends BaseResource {

    @Autowired
    private SecurityServiceFactory securityFactory;

    @POST
    @Path("install")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadDebianKey(FormDataMultiPart formParams)
            throws Exception {
        FileUpload fileUpload = new FileUpload(formParams);
        return runService(securityFactory.uploadDebianKey(), fileUpload);
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeSigningKey()
            throws Exception {
        return runService(securityFactory.removeDebianKeyService());
    }


    @POST
    @Path("verify")
    @Produces(MediaType.APPLICATION_JSON)
    public Response verifySigningKey(SignKey signKey)
            throws Exception {
        return runService(securityFactory.verifyDebianKey(), signKey);
    }

    @PUT
    @Path("update")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updatePassPhrase(SignKey signKey)
            throws Exception {
        return runService(securityFactory.updateDebianKey(), signKey);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSigningKey()
            throws Exception {
        return runService(securityFactory.getDebianSigningKey());
    }
}
