/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2013 JFrog Ltd.
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

import static org.artifactory.api.rest.constant.RestConstants.PATH_API;

/**
 * @author yoavl
 */
public interface HaRestConstants {
    String PATH_ROOT = "ha";
    String HA_ADMIN_ROOT = "ha-admin";
    String PATH_PREFIX = "/" + PATH_API + "/" + PATH_ROOT + "/";
    String PROPAGATE_TASK = "propagateTask";
    String CLUSTER_DUMP = "clusterDump";
    String ACTION = "action";

    String ROLE_HA = "ha";

    String ARTIFACTORY_HA_ORIGINATED_SERVER_ID = "X-Artifactory-HA-Originated-ServerId";
    String ARTIFACTORY_HA_SECURITY_TOKEN = "X-Artifactory-HA-Security-Token";
    String ARTIFACTORY_HA_ORIGINATED_USERNAME = "X-Artifactory-HA-Originated-Username";
    String ARTIFACTORY_HA_EVENT = "X-Artifactory-HA-Event";
}
