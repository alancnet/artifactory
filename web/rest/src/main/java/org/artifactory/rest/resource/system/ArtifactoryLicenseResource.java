/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2014 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.artifactory.rest.resource.system;

import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.CoreAddons;
import org.artifactory.addon.license.LicenseInstaller;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.web.WebappService;
import org.artifactory.state.ArtifactoryServerState;
import org.artifactory.state.model.ArtifactoryStateManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.*;

/**
 * @author Yoav Luft
 */
public class ArtifactoryLicenseResource {
    private static final Logger log = LoggerFactory.getLogger(ArtifactoryLicenseResource.class);

    private final ArtifactoryStateManager stateManager;
    private final WebappService webService;
    private AddonsManager addonsManager;

    public ArtifactoryLicenseResource() {
        stateManager = ContextHelper.get().beanForType(ArtifactoryStateManager.class);
        addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        webService = ContextHelper.get().beanForType(WebappService.class);
    }

    private class ResponseMessage {
        public final int status;
        public final String message;

        public ResponseMessage(String message, int status) {
            this.message = message;
            this.status = status;
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLicenseInfo() {
        try {
            String[] details = addonsManager.getLicenseDetails();
            LicenseDetails licenseDetails = new LicenseDetails(details[2], details[1], details[0]);
            return Response.ok().entity(licenseDetails).build();
        } catch (UnsupportedOperationException e) {
            return Response.ok().entity(new LicenseDetails("Open Source", "", "")).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response installLicense(LicenseConfiguration licenseMessage) {
        String licenseKey = licenseMessage.licenseKey;
        if (addonsManager.addonByType(CoreAddons.class).isAol()) {
            String message = "Cannot manage license on Artifactory Online. Please contact JFrog " +
                    "support at support@jfrog.com for managing your AOL installation.";
            return Response.status(BAD_REQUEST).entity(
                    new ResponseMessage(message, BAD_REQUEST.getStatusCode())).build();
        }
        if (StringUtils.isBlank(licenseKey)) {
            return Response.status(BAD_REQUEST).entity(
                    new ResponseMessage("No license key supplied.", BAD_REQUEST.getStatusCode())).build();
        }
        final Response.ResponseBuilder responseBuilder = Response.ok();

        LicenseInstaller.LicenseInstallCallback callback = new LicenseInstaller.LicenseInstallCallback() {
            @Override
            public void handleSuccess() {
                log.info(LicenseInstaller.SUCCESSFULLY_INSTALL);
                webService.rebuildSiteMap();
                boolean success = stateManager.forceState(ArtifactoryServerState.RUNNING);
                if (success) {
                    responseBuilder.entity(
                            new ResponseMessage(LicenseInstaller.SUCCESSFULLY_INSTALL, OK.getStatusCode()));
                } else {
                    responseBuilder.entity(
                            new ResponseMessage(LicenseInstaller.SUCCESSFULLY_INSTALL + " In order for " +
                                    "the change will take place, please restart the server", OK.getStatusCode()));
                }
            }

            @Override
            public void switchOffline(String message) {
                log.warn(message);
                stateManager.forceState(ArtifactoryServerState.OFFLINE);
                responseBuilder.status(Response.Status.ACCEPTED).entity(
                        new ResponseMessage(message, ACCEPTED.getStatusCode()));
            }

            @Override
            public void handleError(String message) {
                log.error(message);
                responseBuilder.status(BAD_REQUEST).entity(new ResponseMessage(message, BAD_REQUEST.getStatusCode()));
            }
        };
        new LicenseInstaller().install(licenseKey, callback);
        return responseBuilder.build();
    }
}
