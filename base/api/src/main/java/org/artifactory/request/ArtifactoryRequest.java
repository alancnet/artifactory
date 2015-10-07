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

package org.artifactory.request;

public interface ArtifactoryRequest extends Request {
    @Deprecated
    String ORIGIN_ARTIFACTORY = "Origin-Artifactory";

    String ARTIFACTORY_ORIGINATED = "X-Artifactory-Originated";

    String ARTIFACTORY_OVERRIDE_BASE_URL = "X-Artifactory-Override-Base-Url";

    String CHECKSUM_SHA1 = "X-Checksum-Sha1";

    String CHECKSUM_MD5 = "X-Checksum-Md5";

    String ACCEPT_RANGES = "Accept-Ranges";

    String FILE_NAME = "X-Artifactory-Filename";

    /**
     * An header to trigger checksum deploy (when the value is true). Request must also include
     * {@link org.artifactory.request.ArtifactoryRequest#CHECKSUM_SHA1}.
     */
    String CHECKSUM_DEPLOY = "X-Checksum-Deploy";

    /**
     * Header to trigger bundle archive deployment (supports zip/tar/tar.gz)
     */
    String EXPLODE_ARCHIVE = "X-Explode-Archive";

    String RESULT_DETAIL = "X-Result-Detail";

    String PARAM_SKIP_JAR_INDEXING = "artifactory.skipJarIndexing";

    String PARAM_FORCE_DOWNLOAD_IF_NEWER = "artifactory.forceDownloadIfNewer";

    String PARAM_SEARCH_FOR_EXISTING_RESOURCE_ON_REMOTE_REQUEST =
            "artifactory.searchForExistingResourceOnRemoteRequest";

    String PARAM_ALTERNATIVE_REMOTE_DOWNLOAD_URL = "artifactory.alternativeRemoteDownloadUrl";

    String PARAM_REPLICATION_DOWNLOAD_REQUESET = "artifactory.replicationDownloadRequest";

    String PARAM_FOLDER_REDIRECT_ASSERTION = "artifactory.disableFolderRedirectAssertion";

    /**
     * Will replace the HEAD request in RetrieveInfo with a GET request
     */
    String PARAM_REPLACE_HEAD_IN_RETRIEVE_INFO_WITH_GET = "artifactory.replaceHeadInRetrieveInfoWithGet";

    /**
     * The path prefix name for list browsing.
     */
    String LIST_BROWSING_PATH = "list";

    /**
     * The path prefix name for simple browsing.
     */
    String SIMPLE_BROWSING_PATH = "simple";

    String LAST_MODIFIED = "X-Artifactory-Last-Modified";

    String CREATED = "X-Artifactory-Created";

    String MODIFIED_BY = "X-Artifactory-Modified-By";

    String CREATED_BY = "X-Artifactory-Created-By";

    String getRepoKey();

    String getPath();

    boolean isMetadata();

    /**
     * Indicates whether the request is coming back to the same proxy as a result of reverse mirroring
     */
    boolean isRecursive();

    long getModificationTime();

    String getName();

    /**
     * Indicates whether the request if for a directory instead of a file
     *
     * @return True if the request uri if for a directory
     */
    boolean isDirectoryRequest();
}
