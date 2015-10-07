package org.artifactory.ui.rest.resource.admin.configuration.proxies;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.ui.rest.model.admin.configuration.proxy.DeleteProxiesModel;
import org.artifactory.ui.rest.model.admin.configuration.proxy.Proxy;
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
 * @author Chen Keinan
 */
@Path("proxies")
@RolesAllowed({AuthorizationService.ROLE_ADMIN})
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ProxyResource extends BaseResource {

    @Autowired
    protected ConfigServiceFactory configServiceFactory;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createProxies(Proxy proxy)
            throws Exception {
        return runService(configServiceFactory.createProxiesService(), proxy);
    }


    @PUT
    @Path("crud{id:(/[^/]+?)?}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateProxies(Proxy proxy)
            throws Exception {
        return runService(configServiceFactory.updateProxiesService(), proxy);
    }

    @GET
    @Path("crud{id:(/[^/]+?)?}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProxies()
            throws Exception {
        return runService(configServiceFactory.getProxiesService());
    }

    @Path("deleteProxies")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteProxies(DeleteProxiesModel deleteProxiesModel)
            throws Exception {
        return runService(configServiceFactory.deleteProxiesService(),deleteProxiesModel);
    }
}
