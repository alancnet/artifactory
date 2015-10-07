package org.artifactory.rest.resource.aql;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.rest.AuthorizationRestException;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.aql.AqlException;
import org.artifactory.aql.AqlService;
import org.artifactory.aql.result.AqlJsonStreamer;
import org.artifactory.aql.result.AqlLazyResult;
import org.artifactory.aql.result.AqlRestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

/**
 * @author Gidi Shabat
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Path(AqlResource.PATH_ROOT)
@RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
public class AqlResource {
    public static final String PATH_ROOT = "search/aql";
    private static final Logger log = LoggerFactory.getLogger(AqlResource.class);
    @Autowired
    AddonsManager addonsManager;
    @Autowired
    AuthorizationService authorizationService;
    @Autowired
    AqlService aqlService;
    @Context
    private HttpServletRequest request;

    @POST
    @Produces({MediaType.APPLICATION_JSON})
    public Response getLatestVersionByPath(String contentQuery) {
        // Only none anonymous users can access AQL
        if (authorizationService.isAnonymous()) {
            throw new AuthorizationRestException("Only non-anonymous users are allowed to access AQL queries\n");
        }
        //Try to load the query from URL params or attached file
        String query = getQuery(contentQuery);
        if (StringUtils.isBlank(query)) {
            log.error("Couldn't find the query neither in the request URL and the attached file");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        // Execute the query
        try {
            final AqlRestResult restResult = executeAqlQuery(query);
            // After success query execution prepare the result in stream.
            StreamingOutput stream = os -> {
                byte[] array = restResult.read();
                try {
                    while (array != null) {
                        os.write(array);
                        array = restResult.read();
                    }
                    os.flush();
                } finally {
                    IOUtils.closeQuietly(restResult);
                }
            };
            return Response.ok(stream).build();
        } catch (AqlException e) {
            log.error("Fail to parse query: {}: ", query, e.getMessage());
            log.debug("Fail to parse query: {}: ", query, e);
            if (e.getCause() == null) {
                return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST).entity(
                        e.getMessage() + "\ncause: " + e.getCause().getMessage()).build();
            }
        } catch (Exception e) {
            log.error("Fail to execute the following AqlApi, reason: ", e);
            return Response.serverError().build();
        }
    }

    private AqlRestResult executeAqlQuery(String query) {
        AqlLazyResult result = aqlService.executeQueryLazy(query);
        final AqlRestResult restResult;
        restResult = new AqlJsonStreamer(result);
        return restResult;
    }

    private String getQuery(String contentQuery) {
        try {
            String query = contentQuery;
            if (StringUtils.isBlank(query)) {
                // Try to find query in the attached params ()
                query = ((String[]) request.getParameterMap().get("query"))[0];
            }
            return query;
        } catch (Exception e) {
            return null;
        }
    }
}
