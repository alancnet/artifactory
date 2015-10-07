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
 * Constants used by the rest repositories resource
 *
 * @author Noam Tenne
 */
public interface RepositoriesRestConstants {
    String PATH_ROOT = "repositories";
    String PATH_CONFIGURATION = "configuration";
    String PARAM_REPO_TYPE = "type";

    String MT_REPOSITORY_DETAILS_LIST = RestConstants.MT_ARTIFACTORY_APP + PATH_ROOT + ".RepositoryDetailsList+json";
    String MT_LOCAL_REPOSITORY_CONFIGURATION = RestConstants.MT_ARTIFACTORY_APP + PATH_ROOT + ".LocalRepositoryConfiguration+json";
    String MT_REMOTE_REPOSITORY_CONFIG = RestConstants.MT_ARTIFACTORY_APP + PATH_ROOT + ".RemoteRepositoryConfiguration+json";
    String MT_VIRTUAL_REPOSITORY_CONFIGURATION = RestConstants.MT_ARTIFACTORY_APP + PATH_ROOT + ".VirtualRepositoryConfiguration+json";
    String MT_REMOTE_REPOSITORY_CONFIGURATION =
            RestConstants.MT_ARTIFACTORY_APP + PATH_ROOT + ".RepositoryConfiguration+json";

    String PATH = "path";
    String TARGET_REPO = "repo";
    String EXCLUDE_CONTENT = "content";
    String INCLUDE_METADATA = "metadata";
    String VERBOSE = "verbose";
    String POSITION = "pos";
}
