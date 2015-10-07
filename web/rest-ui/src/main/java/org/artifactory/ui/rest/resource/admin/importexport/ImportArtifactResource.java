package org.artifactory.ui.rest.resource.admin.importexport;

import com.sun.jersey.multipart.FormDataMultiPart;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.ui.rest.model.admin.importexport.ImportExportSettings;
import org.artifactory.ui.rest.model.utils.FileUpload;
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
 * @author chen keinan
 */
@RolesAllowed({AuthorizationService.ROLE_ADMIN})
@Component
@Path("artifactimport")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ImportArtifactResource extends BaseResource {

    @Autowired
    protected ImportExportServiceFactory importExportFactory;

    @POST
    @Path("repository")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response importRepository(ImportExportSettings importExportSettings)
            throws Exception {
        return runService(importExportFactory.importRepositoryService(), importExportSettings);
    }

    @POST
    @Path("upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadExtractedZip(FormDataMultiPart formParams)
            throws Exception {
        FileUpload fileUpload = new FileUpload(formParams);
        return runService(importExportFactory.uploadExtractedZip(), fileUpload);
    }

    @POST
    @Path("system")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response importSystem(ImportExportSettings systemImport)
            throws Exception {
        return runService(importExportFactory.importSystem(), systemImport);
    }

    @POST
    @Path("systemUpload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response systemExtractedZip(FormDataMultiPart formParams)
            throws Exception {
        FileUpload fileUpload = new FileUpload(formParams);
        return runService(importExportFactory.uploadSystemExtractedZip(), fileUpload);
    }

}
