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

/**
 * @author Shay Yaakov
 */
public interface TestRestConstants {
    String PATH_ROOT = "test";
    String PATH_FLUSH_STATS = "flushStats";
    String PATH_ARCHIVE_INDEXER_DONE = "archiveIndexerDone";
    String PATH_MAVEN_METADATA_DONE = "mavenMetadataDone";
    String PATH_MAVEN_INDEXER_DONE = "mavenIndexerDone";
    String PATH_NUGET_INDEX_DONE = "nuGetIndexDone";
    String PATH_All_ASYNC_DONE = "asyncDone";
}
