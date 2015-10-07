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

package org.artifactory.addon.rest;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Yoav Landman
 */
public class MissingRestAddonException extends WebApplicationException {

    private static final String MESSAGE = "This REST API is available only in Artifactory Pro (see: " +
            "http://www.jfrog.com/addons.php). If you are already running Artifactory Pro please make " +
            "sure your server is activated with a valid license key.\n";

    public MissingRestAddonException() {
        super(Response.status(Response.Status.BAD_REQUEST).entity(MESSAGE).type(MediaType.TEXT_PLAIN).build());
    }

    @Override
    public String getMessage() {
        return MESSAGE;
    }

    @Override
    public String getLocalizedMessage() {
        return MESSAGE;
    }
}