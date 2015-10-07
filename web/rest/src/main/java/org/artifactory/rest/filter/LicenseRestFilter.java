package org.artifactory.rest.filter;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.rest.AuthorizationRestException;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.request.ArtifactoryResponse;
import org.artifactory.webapp.servlet.HttpArtifactoryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import java.io.IOException;

/**
 * @author Gidi Shabat
 */
public class LicenseRestFilter implements ContainerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(LicenseRestFilter.class);

    @Context
    HttpServletResponse response;

    @Override
    public ContainerRequest filter(ContainerRequest containerRequest) {
        // Filter out all events in case that the trial license expired
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        ArtifactoryResponse artifactoryResponse = new HttpArtifactoryResponse(response);
        try {
            String path = containerRequest.getPath();
            addonsManager.interceptRestResponse(artifactoryResponse, path);
            if (artifactoryResponse.isError()) {
                // throw the exception to make sure to return
                throw new AuthorizationRestException("License expired or not installed");
            }
        } catch (IOException e) {
            log.error("Fail to intercept license REST validation", e);
            throw new RuntimeException("Fail to intercept license validation during rest request", e);
        }
        return containerRequest;
    }
}
