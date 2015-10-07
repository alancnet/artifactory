package org.artifactory.rest.filter;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import org.artifactory.addon.rest.AuthorizationRestException;
import org.artifactory.api.context.ContextHelper;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;

/**
 * author: gidis
 * Block Rest request during offline state
 */

public class OfflineRestFilter implements ContainerRequestFilter {

    @Context
    HttpServletResponse response;

    @Override
    public ContainerRequest filter(ContainerRequest containerRequest) {
        // Filter out all events in case of offline mode
        if (ContextHelper.get().isOffline()) {
            throw new AuthorizationRestException();
        }
        return containerRequest;
    }
}
