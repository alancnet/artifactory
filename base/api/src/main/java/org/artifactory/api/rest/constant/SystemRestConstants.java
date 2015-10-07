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
public interface SystemRestConstants {
    String PATH_ROOT = "system";
    String PATH_IMPORT = "import";
    String PATH_EXPORT = "export";
    String PATH_CONFIGURATION = "configuration";
    String PATH_SECURITY = "security";
    String PATH_STORAGE = "storage";
    String PATH_STORAGE_COMPRESS = "compress";
    String PATH_REPOSITORIES = "repositories";
    String PATH_VERSION = "version";
    String PATH_PING = "ping";
    String PATH_ENCRYPT = "encrypt";
    String PATH_DECRYPT = "decrypt";
    String PATH_LICENSE = "license";
    String MT_VERSION_RESULT = RestConstants.MT_ARTIFACTORY_APP + PATH_ROOT + ".Version+json";
    String MT_IMPORT_SETTINGS = RestConstants.MT_ARTIFACTORY_APP + PATH_ROOT + ".ImportSettings+json";
    String MT_EXPORT_SETTINGS = RestConstants.MT_ARTIFACTORY_APP + PATH_ROOT + ".ExportSettings+json";
}
