package org.artifactory.ui.rest.resource.admin.configuration.licenses;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.ui.rest.resource.BaseResource;
import org.artifactory.ui.rest.service.admin.configuration.ConfigServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
@Path("licenseexport")
@RolesAllowed({AuthorizationService.ROLE_ADMIN})
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class LicenseExportResource extends BaseResource {

    @Autowired
    protected ConfigServiceFactory configServiceFactory;

    @Autowired
    @Qualifier("streamingRestResponse")
    public void setArtifactoryResponse(RestResponse artifactoryResponse) {
        this.artifactoryResponse = artifactoryResponse;
    }

    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response exportLicenseFile()
            throws Exception {
        return runService(configServiceFactory.exportLicenseFileService());
    }
}
