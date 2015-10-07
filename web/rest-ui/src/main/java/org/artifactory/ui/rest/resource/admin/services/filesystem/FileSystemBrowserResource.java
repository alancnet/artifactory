package org.artifactory.ui.rest.resource.admin.services.filesystem;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.ui.rest.resource.BaseResource;
import org.artifactory.ui.rest.service.admin.services.ServicesServiceFactory;
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
 * @author Chen Keinan
 */
@Path("browsefilesystem{path:(/[^/]+?)?}")
@RolesAllowed({AuthorizationService.ROLE_ADMIN})
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class FileSystemBrowserResource extends BaseResource {

    @Autowired
    protected ServicesServiceFactory serviceFactory;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response browseFileSystem()
            throws Exception {
        return runService(serviceFactory.browseFileSystemService());
    }
}
