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

package org.artifactory.api.rest.constant;

/**
 * @author yoavl
 */
public interface PluginRestConstants {
    String PATH_ROOT = "plugins";
    String PATH_EXECUTE = "execute";
    String PATH_BUILD = "build/";
    String PATH_STAGING = PATH_BUILD + "staging";
    String PATH_PROMOTE = PATH_BUILD + "promote";

    String PARAM_PARAMS = "params";
    String PARAM_ASYNC = "async";

    String MT_BUILD_STAGING_STRATEGY = RestConstants.MT_JFROG_APP + PATH_ROOT + ".BuildStagingStrategy+json";
}