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
 * @author Eli Givoni
 */
public interface ArtifactRestConstants {
    String PATH_PARAM = "path";
    String PATH_ROOT = "storage";
    String PATH_STATISTICS = "statistics";
    String MT_FOLDER_INFO = RestConstants.MT_ARTIFACTORY_APP + PATH_ROOT + ".FolderInfo+json";
    String MT_FILE_INFO = RestConstants.MT_ARTIFACTORY_APP + PATH_ROOT + ".FileInfo+json";
    String MT_ITEM_PROPERTIES = RestConstants.MT_ARTIFACTORY_APP + PATH_ROOT + ".ItemProperties+json";
    String MT_COPY_MOVE_RESULT = RestConstants.MT_ARTIFACTORY_APP + PATH_ROOT + ".CopyOrMoveResult+json";
    String MT_FILE_LIST = RestConstants.MT_ARTIFACTORY_APP + PATH_ROOT + ".FileList+json";
    String MT_ITEM_LAST_MODIFIED = RestConstants.MT_ARTIFACTORY_APP + PATH_ROOT + ".ItemLastModified+json";
    String MT_ITEM_CREATED = RestConstants.MT_ARTIFACTORY_APP + PATH_ROOT + ".ItemCreated+json";
    String MT_ITEM_PERMISSIONS = RestConstants.MT_ARTIFACTORY_APP + PATH_ROOT + ".ItemPermissions+json";
    String MT_STATS_INFO = RestConstants.MT_ARTIFACTORY_APP + PATH_ROOT + ".StatsInfo+json";

    String PATH_COPY = "copy";
    String PATH_MOVE = "move";
    String PARAM_TARGET = "to";
    String PARAM_DRY_RUN = "dry";
    String PARAM_SUPPRESS_LAYOUTS = "suppressLayouts";
    String PARAM_FAIL_FAST = "failFast";

    String PATH_DOWNLOAD = "download";
    String PARAM_CONTENT = "content";
    String PARAM_MARK = "mark";

    String PATH_SYNC = "sync";
    String PARAM_PROGRESS = "progress";
    String PARAM_DELETE = "delete";
    String PARAM_OVERWRITE = "overwrite";
    String PARAM_TIMEOUT = "timeout";

    String TRACE_PARAM = "trace";
    String PROPERTIES_PARAM = "properties";
    String PROPERTIES_XML_PARAM = "propertiesXml";
}