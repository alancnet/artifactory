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
public interface SearchRestConstants {
    String PATH_ROOT = "search";

    //Query path
    String PATH_ARTIFACT = "artifact";
    String PATH_ARCHIVE = "archive";
    String PATH_GAVC = "gavc";
    String PATH_PROPERTY = "prop";
    String PATH_USAGE_SINCE = "usage";
    String PATH_CREATED_IN_RANGE = "creation";
    String PATH_DATES_IN_RANGE = "dates";
    String PATH_PATTERN = "pattern";
    String PATH_LICENSE = "license";
    String PATH_CHECKSUM = "checksum";
    String PATH_BAD_CHECKSUM = "badChecksum";
    String PATH_DEPENDENCY = "dependency";
    String PATH_VERSIONS = "versions";
    String PATH_LATEST_VERSION = "latestVersion";
    String PATH_BUILD_ARTIFACTS = "buildArtifacts";

    //Common query params
    String PARAM_REPO_TO_SEARCH = "repos";
    String PARAM_SEARCH_NAME = "name";
    String PARAM_PROPERTIES = "properties";

    //Gavc query params
    String PARAM_GAVC_GROUP_ID = "g";
    String PARAM_GAVC_ARTIFACT_ID = "a";
    String PARAM_GAVC_VERSION = "v";
    String PARAM_GAVC_CLASSIFIER = "c";

    //Xpath query params
    String PARAM_METADATA_NAME_SEARCH = "name";
    String PARAM_METADATA_SEARCH_TYPE = "metadata";
    String PARAM_METADATA_PATH = "xpath";
    String PARAM_METADATA_VALUE = "val";

    //Downloaded Since query params
    String PARAM_SEARCH_NOT_USED_SINCE = "notUsedSince";
    String PARAM_CREATED_BEFORE = "createdBefore";

    //Modified in range query params
    String PARAM_IN_RANGE_FROM = "from";
    String PARAM_IN_RANGE_TO = "to";

    //Pattern of artifacts to search for
    String PARAM_PATTERN = "pattern";

    //Licenses query params
    String UNAPPROVED_PARAM = "unapproved";
    String UNKNOWN_PARAM = "unknown";
    String NOT_FOUND_PARAM = "notfound";
    String NEUTRAL_PARAM = "neutral";
    String APPROVED_PARAM = "approved";
    String AUTOFIND_PARAM = "autofind";
    String REPOS_PARAM = "repos";

    String PARAM_MD5_CHECKSUM = "md5";
    String PARAM_SHA1_CHECKSUM = "sha1";

    //Artifact versions params
    String PARAM_FETCH_FROM_REMOTE = "remote";

    // Dynamic search params
    String PARAM_DATE_FIELDS = "dateFields";

    //Build artifacts params
    String BUILD_NAME_PARAM = "buildName";
    String BUILD_NUMBER_PARAM = "buildNumber";
    String BUILD_ARTIFACTS_INCLUDES = "includes";
    String BUILD_ARTIFACTS_EXCLUDES = "excludes";

    //Media types
    String MT_ARTIFACT_SEARCH_RESULT = RestConstants.MT_ARTIFACTORY_APP + PATH_ROOT + ".ArtifactSearchResult+json";
    String MT_ARCHIVE_ENTRY_SEARCH_RESULT =
            RestConstants.MT_ARTIFACTORY_APP + PATH_ROOT + ".ArchiveEntrySearchResult+json";
    String MT_GAVC_SEARCH_RESULT = RestConstants.MT_ARTIFACTORY_APP + PATH_ROOT + ".GavcSearchResult+json";
    String MT_PROPERTY_SEARCH_RESULT = RestConstants.MT_ARTIFACTORY_APP + PATH_ROOT + ".MetadataSearchResult+json";
    String MT_XPATH_SEARCH_RESULT = RestConstants.MT_ARTIFACTORY_APP + PATH_ROOT + ".XpathSearchResult+json";
    String MT_USAGE_SINCE_SEARCH_RESULT = RestConstants.MT_ARTIFACTORY_APP + PATH_ROOT + ".ArtifactUsageResult+json";
    String MT_CREATED_IN_RANGE_SEARCH_RESULT =
            RestConstants.MT_ARTIFACTORY_APP + PATH_ROOT + ".ArtifactCreationResult+json";
    String MT_PATTERN_SEARCH_RESULT = RestConstants.MT_ARTIFACTORY_APP + PATH_ROOT + ".PatternResultFileSet+json";
    String MT_LICENSE_SEARCH_RESULT = RestConstants.MT_ARTIFACTORY_APP + PATH_ROOT + ".LicenseResult+json";
    String MT_CHECKSUM_SEARCH_RESULT = RestConstants.MT_ARTIFACTORY_APP + PATH_ROOT + ".ChecksumSearchResult+json";
    String MT_BAD_CHECKSUM_SEARCH_RESULT =
            RestConstants.MT_ARTIFACTORY_APP + PATH_ROOT + ".BadChecksumSearchResult+json";
    String MT_DEPENDENCY_BUILDS = RestConstants.MT_ARTIFACTORY_APP + PATH_ROOT + ".DependencyBuilds+json";
    String MT_ARTIFACT_VERSIONS_SEARCH_RESULT =
            RestConstants.MT_ARTIFACTORY_APP + PATH_ROOT + ".ArtifactVersionsResult+json";
    String MT_BUILD_ARTIFACTS_SEARCH_RESULT = RestConstants.MT_ARTIFACTORY_APP + PATH_ROOT + ".BuildArtifactsSearchResult+json";
    String MT_ARTIFACT_RESULT = RestConstants.MT_ARTIFACTORY_APP + PATH_ROOT + ".ArtifactResult+json";

    String NOT_FOUND = "No results found.";
}