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


import org.apache.http.HttpStatus;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.CoreAddons;
import org.artifactory.api.config.ExportSettingsImpl;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.rest.constant.ImportRestConstants;
import org.artifactory.api.rest.constant.SystemRestConstants;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.rest.common.exception.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author freds
 * @date Sep 4, 2008
 */
@Path(SystemRestConstants.PATH_EXPORT)
@RolesAllowed(AuthorizationService.ROLE_ADMIN)
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ExportResource {
    private static final Logger log = LoggerFactory.getLogger(ExportResource.class);

    @Context
    HttpServletResponse httpResponse;

    @Autowired
    RepositoryService repoService;

    @GET
    @Path(ImportRestConstants.SYSTEM_PATH)
    @Produces({SystemRestConstants.MT_EXPORT_SETTINGS, MediaType.APPLICATION_JSON})
    public ExportSettingsConfigurationImpl settingsExample() {
        assertNotAol();
        ExportSettingsConfigurationImpl settings = new ExportSettingsConfigurationImpl();
        settings.setExportPath("/export/path");
        return settings;
    }

    @POST
    @Path(ImportRestConstants.SYSTEM_PATH)
    @Consumes({SystemRestConstants.MT_EXPORT_SETTINGS, MediaType.APPLICATION_JSON})
    public Response activateExport(ExportSettingsConfigurationImpl settings) {
        assertNotAol();
        ImportExportStreamStatusHolder holder = new ImportExportStreamStatusHolder(httpResponse);
        ExportSettingsImpl exportSettings = new ExportSettingsImpl(new File(settings.getExportPath()), holder);
        exportSettings.setIncludeMetadata(settings.isIncludeMetadata());
        exportSettings.setCreateArchive(settings.isCreateArchive());
        exportSettings.setIgnoreRepositoryFilteringRulesOn(settings.isBypassFiltering());
        exportSettings.setVerbose(settings.isVerbose());
        exportSettings.setFailFast(settings.isFailOnError());
        exportSettings.setFailIfEmpty(settings.isFailIfEmpty());
        exportSettings.setM2Compatible(settings.isM2());
        exportSettings.setIncremental(settings.isIncremental());
        exportSettings.setExcludeContent(settings.isExcludeContent());

        if (settings.isIncludeMetadata() || !settings.isExcludeContent()) {
            exportSettings.setRepositories(getAllLocalRepoKeys());
        }
        log.debug("Activating export {}", settings);
        try {
            ContextHelper.get().exportTo(exportSettings);
            if (!httpResponse.isCommitted() && holder.isError()) {
                return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(
                        "Export finished with errors. Check " +
                                "Artifactory logs for more details."
                ).build();
            }
        } catch (Exception e) {
            if (!httpResponse.isCommitted()) {
                return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
            }
        }
        return Response.ok().build();
    }

    private List<String> getAllLocalRepoKeys() {
        List<String> repoKeys = new ArrayList<>();
        for (LocalRepoDescriptor localRepoDescriptor : repoService.getLocalAndCachedRepoDescriptors()) {
            repoKeys.add(localRepoDescriptor.getKey());
        }
        return repoKeys;
    }

    private void assertNotAol() {
        if (ContextHelper.get().beanForType(AddonsManager.class).addonByType(CoreAddons.class).isAol()) {
            log.debug("Canceling system export request - not allowed on AOL");
            throw new BadRequestException("Export System function is not supported when running on the cloud");
        }
    }
}
