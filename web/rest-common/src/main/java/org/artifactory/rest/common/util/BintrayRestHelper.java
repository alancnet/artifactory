package org.artifactory.rest.common.util;

import org.apache.http.HttpStatus;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.CoreAddons;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.jackson.JacksonFactory;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.StatusEntry;
import org.codehaus.jackson.JsonGenerator;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Helper class containing shared functions for bintray rest endpoints in Artifactory
 *
 * @author Dan Feldman
 */
public class BintrayRestHelper {

    public static boolean isPushToBintrayAllowed() {
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        CoreAddons addons = addonsManager.addonByType(CoreAddons.class);
        UserGroupService userGroupService = ContextHelper.get().beanForType(UserGroupService.class);
        AuthorizationService authorizationService = ContextHelper.get().beanForType(AuthorizationService.class);
        CentralConfigService centralConfigService = ContextHelper.get().beanForType(CentralConfigService.class);

        boolean userExists = !addons.isAolAdmin() && !userGroupService.currentUser().isTransientUser();
        boolean anonymousUser = authorizationService.isAnonymous();
        boolean hideUploads = ConstantValues.bintrayUIHideUploads.getBoolean();
        boolean offlineMode = centralConfigService.getDescriptor().isOfflineMode();
        boolean canDeploy = authorizationService.canDeployToLocalRepository();
        return !anonymousUser && !hideUploads && !offlineMode && userExists && canDeploy;
    }

    public static Response createAggregatedResponse(final BasicStatusHolder status, final String performedOn) {
        StreamingOutput streamingOutput = new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                JsonGenerator jsonGenerator = JacksonFactory.createJsonGenerator(outputStream);
                jsonGenerator.writeStartObject();
                if (status.hasErrors()) {
                    jsonGenerator.writeArrayFieldStart("errors");
                    for (StatusEntry error : status.getErrors()) {
                        jsonGenerator.writeStartObject();
                        jsonGenerator.writeNumberField("status", error.getStatusCode());
                        jsonGenerator.writeStringField("message", error.getMessage());
                        jsonGenerator.writeEndObject();
                    }
                    jsonGenerator.writeEndArray();
                } else {
                    String msg = "Pushing " + performedOn + " to Bintray finished" + (status.hasWarnings() ?
                            " with warnings, view the log for details" : " to Bintray finished successfully.");
                    jsonGenerator.writeStringField("message", msg);
                }
                jsonGenerator.writeEndObject();
                jsonGenerator.close();
            }
        };
        int statusCode = HttpStatus.SC_OK;
        if (status.hasErrors()) {
            statusCode = status.getErrors().size() > 1 ? HttpStatus.SC_CONFLICT : status.getStatusCode();
        }
        return Response.status(statusCode).entity(streamingOutput).type(MediaType.APPLICATION_JSON_TYPE).build();
    }
}
