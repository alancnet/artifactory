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
 */
package org.artifactory.rest.resource.mission.control;

import org.apache.http.HttpStatus;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.security.SecurityService;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.rest.resource.mission.control.model.MissionControlSetupRequest;
import org.artifactory.security.SaltedPassword;
import org.artifactory.security.UserInfo;
import org.artifactory.security.mission.control.MissionControlProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Gidi Shabat
 */

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Path(MissionControlResource.PATH_ROOT)
@RolesAllowed({AuthorizationService.ROLE_ADMIN})
public class MissionControlResource {
    public static final String PATH_ROOT = "setupmc";
    private static final Logger log = LoggerFactory.getLogger(MissionControlResource.class);

    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response setupMissionControl(MissionControlSetupRequest setupRequest) {
        try {
            log.debug("Starting to setup Mission Control properties");
            MissionControlProperties missionControlProperties = ContextHelper.get().beanForType(MissionControlProperties.class);
            SecurityService securityService = ContextHelper.get().beanForType(SecurityService.class);
            String token = setupRequest.getToken();
            String userName= UserInfo.MISSION_CONTROLL;
            int index = token.indexOf('@');
            if(index>0){
                userName=token.substring(0, index);
                token=token.substring(index+1,token.length());
            }
            SaltedPassword saltedPassword = securityService.generateSaltedPassword(token);
            missionControlProperties.setToken(saltedPassword.getPassword());
            missionControlProperties.setUrl(setupRequest.getUrl());
            missionControlProperties.setCreatedBy(userName);
            missionControlProperties.setCreatedDate(System.currentTimeMillis());
            ArtifactoryHome artifactoryHome = ArtifactoryHome.get();
            missionControlProperties.updateMissionControlPropertiesFile(artifactoryHome.getMissionControlPropertiesFile());
            log.debug("Successfully finished  to setup Mission Control properties");
        } catch (Exception e) {
            throw new RuntimeException("Failed to setup the Mission Control properties");
        }
        return Response.status(HttpStatus.SC_CREATED).build();
    }
}
