package org.artifactory.ui.rest.resource.admin.importexport;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.ui.rest.model.admin.importexport.ImportExportSettings;
import org.artifactory.ui.rest.resource.BaseResource;
import org.artifactory.ui.rest.service.admin.importexport.ImportExportServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Chen Keinan
 */
@RolesAllowed({AuthorizationService.ROLE_ADMIN})
@Component
@Path("artifactexport")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ExportArtifactResource extends BaseResource {

    @Autowired
    protected ImportExportServiceFactory importExportFactory;

    @POST
    @Path("repository")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response exportRepository(ImportExportSettings importExportSettings)
            throws Exception {
        return runService(importExportFactory.exportRepository(), importExportSettings);
    }

    @POST
    @Path("system")
    public Response exportSystem(ImportExportSettings systemImport)
            throws Exception {
        return runService(importExportFactory.exportSystem(), systemImport);
    }
}
