package org.artifactory.ui.rest.resource.admin.security.ldap;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.ui.rest.model.admin.security.ldap.LdapSettingModel;
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
import java.util.List;

/**
 * @author Chen Keinan
 */
@RolesAllowed({AuthorizationService.ROLE_ADMIN})
@Component
@Path("ldap")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class LdapSettingResource extends BaseResource {

    @Autowired
    private SecurityServiceFactory securityFactory;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createLdapSetting(LdapSettingModel ldapSettingModel) throws Exception {
        return runService(securityFactory.createLdapSettings(), ldapSettingModel);
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateLdapSetting(LdapSettingModel ldapSettingModel) {
        return runService(securityFactory.updateLdapSettings(), ldapSettingModel);
    }

    @DELETE
    @Path("{id}")
    public Response deleteLdapSetting() {
        return runService(securityFactory.deleteLdapSettings());
    }


    //Due to a stupid Jersey bug with their regexes we have to split
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLdapSettings() {
        return runService(securityFactory.getLdapSettings());
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLdapSetting() {
        return runService(securityFactory.getLdapSettings());
    }

    @POST
    @Path("test/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response testLdapSetting(LdapSettingModel ldapSettingModel) {
        return runService(securityFactory.testLdapSettingsService(), ldapSettingModel);
    }

    @POST
    @Path("reorder")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response reorderLdapSettings(List<String> newOrderList) {
        return runService(securityFactory.reorderLdapSettings(), newOrderList);
    }
}
