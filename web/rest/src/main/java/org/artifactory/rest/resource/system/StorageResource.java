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


import org.apache.commons.lang.StringUtils;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.backup.InternalBackupService;
import org.artifactory.common.StatusEntry;
import org.artifactory.descriptor.backup.BackupDescriptor;
import org.artifactory.storage.StorageService;
import org.artifactory.storage.binstore.service.InternalBinaryStore;
import org.artifactory.storage.binstore.service.ProviderConnectMode;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;

/**
 * @author yoavl
 */
public class StorageResource {
    private HttpServletResponse httpResponse;
    private StorageService storageService;
    private InternalBackupService backupService;
    private InternalBinaryStore binaryStore;

    public StorageResource(StorageService storageService, InternalBackupService backupService,
            InternalBinaryStore binaryStore, HttpServletResponse httpResponse) {
        this.storageService = storageService;
        this.httpResponse = httpResponse;
        this.binaryStore = binaryStore;
        this.backupService = backupService;
    }

    @POST
    @Path("compress")
    public Response compress() {
        BasicStatusHolder statusHolder = new ImportExportStreamStatusHolder(httpResponse);
        storageService.compress(statusHolder);
        return response(statusHolder);
    }

    @GET
    @Path("size")
    @Produces(MediaType.TEXT_PLAIN)
    public String size() {
        return binaryStore.getStorageSize() + "";
    }

    @POST
    @Path("addFilestore")
    @Deprecated
    public Response addExternalFilestore(@QueryParam("dir") String externalDir, @QueryParam("mode") String mode) {
        File extDir = new File(externalDir);
        ProviderConnectMode connectMode = ProviderConnectMode.PASS_THROUGH;
        if (StringUtils.isNotBlank(mode)) {
            connectMode = ProviderConnectMode.getConnectMode(mode);
        }
        binaryStore.addExternalFilestore(extDir, connectMode);
        return Response.ok("Directory " + extDir.getAbsolutePath() + " added as external filestore" +
                "using connection mode " + connectMode.propName).build();
    }

    @POST
    @Path("disconnectFilestore")
    @Deprecated
    public void disconnectExternalFilestore(@QueryParam("dir") String externalDir,
            @QueryParam("mode") String mode, @QueryParam("verbose") boolean verbose) {
        File extDir = new File(externalDir);
        ProviderConnectMode connectMode = ProviderConnectMode.PASS_THROUGH;
        if (StringUtils.isNotBlank(mode)) {
            connectMode = ProviderConnectMode.getConnectMode(mode);
        }
        ImportExportStreamStatusHolder holder = new ImportExportStreamStatusHolder(httpResponse);
        holder.setVerbose(verbose);
        binaryStore.disconnectExternalFilestore(extDir, connectMode, holder);
    }

    @POST
    @Path("exportds")
    @Deprecated
    public Response activateExport(@QueryParam("to") String destDir) {
        throw new IllegalStateException("Export data is no longer supported");
    }

    @POST
    @Path("gc")
    public Response activateGc() {
        BasicStatusHolder statusHolder = new ImportExportStreamStatusHolder(httpResponse);
        storageService.callManualGarbageCollect(statusHolder);
        return response(statusHolder);
    }

    @POST
    @Path("prune")
    public Response activatePruneEmptyDirs() {
        BasicStatusHolder statusHolder = new ImportExportStreamStatusHolder(httpResponse);
        binaryStore.prune(statusHolder);
        return response(statusHolder);
    }

    @POST
    @Path("backup")
    public Response activateBackup(@QueryParam("key") String backupKey) {
        final BasicStatusHolder statusHolder = new ImportExportStreamStatusHolder(httpResponse);
        BackupDescriptor backupDescriptor = backupService.getBackup(backupKey);
        if (backupDescriptor != null) {
            if (backupDescriptor.isEnabled()) {
                backupService.scheduleImmediateSystemBackup(backupDescriptor, statusHolder);
                return response(statusHolder);
            } else {
                return Response.serverError().entity(
                        "Backup identified with key '" + backupKey + "' is disabled").build();
            }
        } else {
            return Response.serverError().entity("No backup identified with key '" + backupKey + "'").build();
        }
    }

    private Response response(BasicStatusHolder statusHolder) {
        StatusEntry lastError = statusHolder.getLastError();
        return lastError == null ? Response.ok().build() :
                Response.serverError().entity(lastError.getMessage()).build();
    }
}