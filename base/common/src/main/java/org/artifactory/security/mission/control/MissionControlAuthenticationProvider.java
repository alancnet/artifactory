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
package org.artifactory.security.mission.control;

import org.springframework.security.core.Authentication;

/**
 * @author Gidi Shabat
 */
public interface MissionControlAuthenticationProvider {
    public static final String REALM = "mission-control";
    public static final String HEADER_NAME = "Authorization-token";

    /**
     * Generate Mission Control authentication for success Mission Control token authentication
     */
    Authentication getFullAuthentication(String UserName);

    /**
     * Generate Anonymous authentication for invalid Mission Control token authentication
     */
    Authentication getAnonymousAuthentication();
}
