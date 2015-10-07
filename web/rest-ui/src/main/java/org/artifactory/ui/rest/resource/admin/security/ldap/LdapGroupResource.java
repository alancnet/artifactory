package org.artifactory.ui.rest.resource.admin.security.ldap;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.ui.rest.model.admin.security.ldap.LdapGroupModel;
import org.artifactory.ui.rest.model.admin.security.ldap.LdapImportModel;
import org.artifactory.ui.rest.resource.BaseResource;
import org.artifactory.ui.rest.service.admin.security.SecurityServiceFactory;
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
@RolesAllowed({AuthorizationService.ROLE_ADMIN})
@Component
@Path("ldapgroups{id:(/[^/]+?)?}")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class LdapGroupResource extends BaseResource {

    @Autowired
    private SecurityServiceFactory securityFactory;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createLdapGroup(LdapGroupModel ldapGroupModel)
            throws Exception {
        return runService(securityFactory.createLdapGroup(), ldapGroupModel);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateLdapGroup(LdapGroupModel ldapGroupModel)
            throws Exception {
        return runService(securityFactory.updateLdapGroup(), ldapGroupModel);
    }

    @DELETE
    public Response deleteLdapGroup()
            throws Exception {
        return runService(securityFactory.deleteLdapGroup());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLdapGroup()
            throws Exception {
        return runService(securityFactory.getLdapGroup());
    }

    @POST
    @Path("refresh{name:(/[^/]+?)?}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response refreshLdapGroup(LdapGroupModel ldapGroupModel)
            throws Exception {
        return runService(securityFactory.refreshLdapGroup(), ldapGroupModel);
    }

    @POST
    @Path("import")
    @Produces(MediaType.APPLICATION_JSON)
    public Response importLdapGroup(LdapImportModel ldapImportModel)
            throws Exception {
        return runService(securityFactory.importLdapGroup(), ldapImportModel);
    }

    @GET
    @Path("strategy")
    @Produces(MediaType.APPLICATION_JSON)
    public Response ldapGroupMappingStrategy()
            throws Exception {
        return runService(securityFactory.groupMappingStrategy());
    }
}
