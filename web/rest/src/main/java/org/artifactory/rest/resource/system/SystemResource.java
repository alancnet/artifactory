/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2012 JFrog Ltd.
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


import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.rest.constant.SystemRestConstants;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.security.MasterEncryptionService;
import org.artifactory.api.security.SecurityService;
import org.artifactory.backup.InternalBackupService;
import org.artifactory.info.InfoWriter;
import org.artifactory.security.crypto.CryptoHelper;
import org.artifactory.storage.StorageService;
import org.artifactory.storage.binstore.service.InternalBinaryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * User: freds Date: Aug 12, 2008 Time: 6:11:53 PM
 */
@Path(SystemRestConstants.PATH_ROOT)
@RolesAllowed(AuthorizationService.ROLE_ADMIN)
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class SystemResource {
    private static final Logger log = LoggerFactory.getLogger(SystemResource.class);

    @Context
    HttpServletResponse httpResponse;

    @Context
    private HttpServletRequest httpServletRequest;

    @Autowired
    CentralConfigService centralConfigService;

    @Autowired
    MasterEncryptionService encryptionService;

    @Autowired
    SecurityService securityService;

    @Autowired
    StorageService storageService;

    @Autowired
    InternalBinaryStore binaryStore;

    @Autowired
    InternalBackupService backupService;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getSystemInfo() throws Exception {
        return InfoWriter.getInfoString();
    }

    @Path(SystemRestConstants.PATH_CONFIGURATION)
    public ConfigResource getConfigResource() {
        return new ConfigResource(centralConfigService, httpServletRequest);
    }

    @Path(SystemRestConstants.PATH_SECURITY)
    public SecurityResource getSecurityResource() {
        return new SecurityResource(securityService, centralConfigService, httpServletRequest);
    }

    @Path(SystemRestConstants.PATH_STORAGE)
    public StorageResource getStorageResource() {
        return new StorageResource(storageService, backupService, binaryStore, httpResponse);
    }

    @Path(SystemRestConstants.PATH_LICENSE)
    public ArtifactoryLicenseResource getLicenseResource() {
        return new ArtifactoryLicenseResource();
    }

    @POST
    @Path(SystemRestConstants.PATH_ENCRYPT)
    @Produces(MediaType.TEXT_PLAIN)
    public Response encryptWithMasterKey() {
        try {
            encryptionService.encrypt();
            return Response.ok().entity("DONE").build();
        } catch (Exception e) {
            String msg = "Could not encrypt with master key, due to: " + e.getMessage();
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @POST
    @Path(SystemRestConstants.PATH_DECRYPT)
    @Produces(MediaType.TEXT_PLAIN)
    public Response decryptFromMasterKey() {
        try {
            if (!CryptoHelper.hasMasterKey()) {
                return Response.status(Response.Status.CONFLICT).entity(
                        "Cannot decrypt without master key file").build();
            }
            encryptionService.decrypt();
        } catch (Exception e) {
            String msg = "Could not decrypt with master key, due to: " + e.getMessage();
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
        return Response.ok().entity("DONE").build();
    }

}
