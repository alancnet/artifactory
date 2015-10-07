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

package org.artifactory.api.request;

import org.apache.commons.lang.StringUtils;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.md.Properties;
import org.artifactory.mime.NamingUtils;
import org.artifactory.repo.RepoPath;
import org.artifactory.request.ArtifactoryRequest;
import org.artifactory.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Enumeration;

public abstract class ArtifactoryRequestBase implements ArtifactoryRequest {
    private static final Logger log = LoggerFactory.getLogger(ArtifactoryRequestBase.class);

    private RepoPath repoPath;

    /**
     * A set of matrix parameters found on the request path in the form of:
     * <p/>
     * /pathseg1/pathseg2;param1=v1;param2=v2;param3=v3
     */
    private Properties properties = (Properties) InfoFactoryHolder.get().createProperties();

    /**
     * A path inside a zip file. The path never starts with leading slash. <p/> For example for path like
     * /path/to/zip!/path/to/resource/in/zip the resource path is path/to/resource/in/zip
     */
    private String zipResourcePath;

    private long modificationTime = -1;

    @Override
    public RepoPath getRepoPath() {
        return repoPath;
    }

    @Override
    public String getRepoKey() {
        return repoPath.getRepoKey();
    }

    @Override
    public String getPath() {
        return repoPath.getPath();
    }

    @Override
    public Properties getProperties() {
        return properties;
    }

    @Override
    public boolean hasProperties() {
        return !properties.isEmpty();
    }

    @Override
    public String getZipResourcePath() {
        return zipResourcePath;
    }

    @Override
    public boolean isZipResourceRequest() {
        return StringUtils.isNotBlank(zipResourcePath);
    }

    @Override
    public boolean isNoneMatch(String etag) {
        if (StringUtils.isBlank(etag)) {
            return true;
        }
        Enumeration ifNoneMatch = getHeaders("If-None-Match");
        while (ifNoneMatch != null && ifNoneMatch.hasMoreElements()) {
            Object requestIfNoneMatch = ifNoneMatch.nextElement();
            if (etag.equals(requestIfNoneMatch)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean hasIfNoneMatch() {
        return getHeader("If-None-Match") != null;
    }

    @Override
    public boolean isMetadata() {
        return NamingUtils.isMetadata(getPath());
    }

    @Override
    public boolean isChecksum() {
        return NamingUtils.isChecksum(getPath()) || NamingUtils.isChecksum(zipResourcePath);
    }

    @Override
    public String getName() {
        return PathUtils.getFileName(getPath());
    }

    @Override
    public boolean isNewerThan(long resourceLastModified) {
        long modificationTime = getModificationTime();
        //Check that the resource has a modification time and that it is older than the request's one.
        //Since HTTP dates do not carry millisecond-level data compare with the value rounded-down to the nearest sec.
        log.debug("Check isNewerThan. resourceLastModified={}, roundedResourceLastModified={}, modificationTime={}",
                resourceLastModified, roundMillis(resourceLastModified), modificationTime);
        return resourceLastModified >= 0 && roundMillis(resourceLastModified) <= modificationTime;
    }

    @Override
    public long getModificationTime() {
        //If not calculated yet
        if (modificationTime < 0) {
            //These headers are not filled by mvn lw-http wagon (doesn't call "getIfNewer")
            long lastModified = getLastModified();
            long ifModifiedSince = getIfModifiedSince();
            if (lastModified < 0 && ifModifiedSince < 0) {
                if (log.isDebugEnabled()) {
                    log.debug("Neither If-Modified-Since nor Last-Modified are set");
                }
                return -1;
            }
            if (lastModified >= 0 && ifModifiedSince >= 0 && lastModified != ifModifiedSince) {
                if (log.isDebugEnabled()) {
                    log.warn(
                            "If-Modified-Since (" + ifModifiedSince + ") AND Last-Modified (" + lastModified +
                                    ") both set and unequal");
                }

            }
            modificationTime = Math.max(lastModified, ifModifiedSince);
        }
        return modificationTime;
    }

    protected void setRepoPath(RepoPath repoPath) {
        this.repoPath = repoPath;
    }

    protected void setZipResourcePath(String zipResourcePath) {
        this.zipResourcePath = zipResourcePath;
    }

    public static long roundMillis(long time) {
        if (time != -1) {
            return (time / 1000) * 1000;
        }
        return time;
    }

    @Override
    public String getParameter(String name) {
        return null;
    }

    @Override
    public String[] getParameterValues(String name) {
        return new String[0];
    }

    /**
     * Decodes and calculates a repoPath based on the given servlet path (path after the context root, including the
     * repo prefix).
     */
    @SuppressWarnings({"deprecation"})
    protected RepoPath calculateRepoPath(String requestPath) throws UnsupportedEncodingException {
        String repoKey = PathUtils.getFirstPathElement(requestPath);
        // index where the path to the file or directory starts (i.e., the request path after the repository key)
        int pathStartIndex;
        if (NamingUtils.isMetadata(repoKey)) {
            //Support repository-level metadata requests
            repoKey = NamingUtils.stripMetadataFromPath(repoKey);
            pathStartIndex = repoKey.length() + NamingUtils.METADATA_PREFIX.length();
        } else if (LIST_BROWSING_PATH.equals(repoKey)) {
            int repoKeyStartIndex = requestPath.indexOf(LIST_BROWSING_PATH) + LIST_BROWSING_PATH.length() + 1;
            if (repoKeyStartIndex > requestPath.length()) {
                repoKeyStartIndex--;    // request doesn't end with '/', no repo key
            }
            repoKey = PathUtils.getFirstPathElement(requestPath.substring(repoKeyStartIndex));
            pathStartIndex = repoKeyStartIndex + repoKey.length() + 1;
        } else if (ArtifactoryRequest.SIMPLE_BROWSING_PATH.equals(repoKey)) {
            int repoKeyStartIndex = requestPath.indexOf(SIMPLE_BROWSING_PATH) + SIMPLE_BROWSING_PATH.length() + 1;
            if (repoKeyStartIndex > requestPath.length()) {
                repoKeyStartIndex--;    // request doesn't end with '/', no repo key
            }
            repoKey = PathUtils.getFirstPathElement(requestPath.substring(repoKeyStartIndex));
            pathStartIndex = repoKeyStartIndex + repoKey.length() + 1;
        } else {
            pathStartIndex = requestPath.startsWith("/") ? repoKey.length() + 2 : repoKey.length() + 1;
        }

        //REPO HANDLING

        //Calculate matrix params on the repo
        repoKey = processMatrixParamsIfExist(repoKey);

        /**
         * Decode the repo key before performing sys-prop based substitution, otherwise the substitution would be based
         * on the potentially encoded repo key
         */
        repoKey = URLDecoder.decode(repoKey, "UTF-8");

        //Test if we need to substitute the targetRepo due to system prop existence
        String substTargetRepo = ArtifactoryHome.get().getArtifactoryProperties().getSubstituteRepoKeys().get(repoKey);
        if (substTargetRepo != null) {
            repoKey = substTargetRepo;
        }

        //PATH HANDLING

        //Strip any trailing '/'
        boolean endsWithSlash = requestPath.endsWith("/");
        int pathEndIndex = endsWithSlash ? requestPath.length() - 1 : requestPath.length();
        String path = pathStartIndex < pathEndIndex ? requestPath.substring(pathStartIndex, pathEndIndex) : "";

        //Calculate matrix params on the path and return path without matrix params
        path = processMatrixParamsIfExist(path);

        path = URLDecoder.decode(path.replace("+", "%2B"), "UTF-8").replace("%2B", "+");

        // calculate zip resource path and return path without the zip resource path
        path = processZipResourcePathIfExist(path);

        return InfoFactoryHolder.get().createRepoPath(repoKey, path, endsWithSlash);
    }

    protected String processMatrixParamsIfExist(String fragment) {
        int matrixParamStart = fragment.indexOf(Properties.MATRIX_PARAMS_SEP);
        if (matrixParamStart > 0) {
            processMatrixParams(this.properties, fragment.substring(matrixParamStart));
            //Return the clean fragment
            return fragment.substring(0, matrixParamStart);
        } else {
            return fragment;
        }
    }

    /**
     * Extracts the zip resource sub path from the path and returns the main resource path.<p/> For example if the
     * request path is /path/to/zip!/path/to/resource/in/zip the method will detect '/path/to/resource/in/zip' as the
     * zip resource path and will return '/path/to/zip' as the root path.
     *
     * @param path Valid path to resource in the repository
     * @return The path without the zip resource sub path. Same path if it's not a zip resource request (i.e., doesn't
     * contain '!' as part of the path).
     */
    private String processZipResourcePathIfExist(String path) {
        String[] splitPath = PathUtils.splitZipResourcePathIfExist(path, false);
        if (splitPath.length > 1) {
            zipResourcePath = splitPath[1];
        }
        return splitPath[0];
    }

    @Override
    public String toString() {
        return "source=" + getClientAddress()
                + ", path=" + getPath() + ", lastModified=" + getLastModified()
                + ", ifModifiedSince=" + getIfModifiedSince();
    }

    /**
     * Extracts the matrix params from the given strings and adds them to the properties object.<br> Note that the
     * matrix params string must begin with matrix params, and any params found are omitted, so only the rest of the
     * path (if exists) will remain in the string.
     *
     * @param propertyCollection Property collection to append to. Cannot be null
     * @param matrixParams       Matrix params to process. Cannot be null
     */
    public static void processMatrixParams(Properties propertyCollection, String matrixParams) {
        int matrixParamStart = 0;
        do {
            int matrixParamEnd = matrixParams.indexOf(Properties.MATRIX_PARAMS_SEP, matrixParamStart + 1);
            if (matrixParamEnd < 0) {
                matrixParamEnd = matrixParams.length();
            }
            String param = matrixParams.substring(matrixParamStart + 1, matrixParamEnd);
            int equals = param.indexOf('=');
            if (equals > 0) {
                String key = param.substring(0, equals);
                String value = param.substring(equals + 1);
                // url-decode the value
                try {
                    value = URLDecoder.decode(value, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    log.warn("Encoding not supported: {}. Using original value", e.getMessage());
                }
                propertyCollection.put(key, value);
            } else if (param.length() > 0) {
                propertyCollection.put(param, "");
            } // else no key declared, ignore
            matrixParamStart = matrixParamEnd;
        } while (matrixParamStart > 0 && matrixParamStart < matrixParams.length());
    }

    @Override
    public boolean isDirectoryRequest() {
        String uri = this.getUri();
        boolean endsWithSlash = uri.endsWith("/");
        boolean containsSlashWithSemicolon = uri.contains("/;");
        boolean lastSlashIndex = uri.lastIndexOf("/;") >= uri.lastIndexOf("/");
        return endsWithSlash || (containsSlashWithSemicolon && lastSlashIndex);
    }
}