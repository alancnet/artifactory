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

import org.artifactory.md.Properties;
import org.artifactory.mime.NamingUtils;
import org.artifactory.repo.RepoPath;
import org.artifactory.request.ArtifactoryRequest;
import org.artifactory.util.PathUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Map;

/**
 * @author Noam Y. Tenne
 */
public class TranslatedArtifactoryRequest implements ArtifactoryRequest {

    private RepoPath translatedRepoPath;
    private ArtifactoryRequest originalRequest;

    public TranslatedArtifactoryRequest(RepoPath translatedRepoPath, ArtifactoryRequest originalRequest) {
        this.translatedRepoPath = translatedRepoPath;
        this.originalRequest = originalRequest;
    }

    @Override
    public String getRepoKey() {
        return translatedRepoPath.getRepoKey();
    }

    @Override
    public String getPath() {
        return translatedRepoPath.getPath();
    }

    @Override
    public String getClientAddress() {
        return originalRequest.getClientAddress();
    }

    @Override
    public boolean isMetadata() {
        return NamingUtils.isMetadata(getPath());
    }

    @Override
    public boolean isRecursive() {
        return originalRequest.isRecursive();
    }

    @Override
    public long getModificationTime() {
        return originalRequest.getModificationTime();
    }

    @Override
    public String getName() {
        return PathUtils.getFileName(getPath());
    }

    @Override
    public boolean isDirectoryRequest() {
        return originalRequest.isDirectoryRequest();
    }

    @Override
    public RepoPath getRepoPath() {
        return translatedRepoPath;
    }

    @Override
    public boolean isChecksum() {
        return NamingUtils.isChecksum(getPath()) || NamingUtils.isChecksum(originalRequest.getZipResourcePath());
    }

    @Override
    public boolean isFromAnotherArtifactory() {
        return originalRequest.isFromAnotherArtifactory();
    }

    @Override
    public boolean isHeadOnly() {
        return originalRequest.isHeadOnly();
    }

    @Override
    public long getLastModified() {
        return originalRequest.getLastModified();
    }

    @Override
    public long getIfModifiedSince() {
        return originalRequest.getIfModifiedSince();
    }

    @Override
    public boolean hasIfModifiedSince() {
        return originalRequest.hasIfModifiedSince();
    }

    @Override
    public boolean isNewerThan(long time) {
        return originalRequest.isNewerThan(time);
    }

    @Override
    public String getHeader(String headerName) {
        return originalRequest.getHeader(headerName);
    }

    @Override
    public Enumeration getHeaders(String headerName) {
        return originalRequest.getHeaders(headerName);
    }

    @Override
    public Map<String, String> getHeaders() {
        return originalRequest.getHeaders();
    }

    @Override
    public String getServletContextUrl() {
        return originalRequest.getServletContextUrl();
    }

    @Override
    public String getUri() {
        return originalRequest.getUri();
    }

    @Override
    public Properties getProperties() {
        return originalRequest.getProperties();
    }

    @Override
    public boolean hasProperties() {
        return originalRequest.hasProperties();
    }

    @Override
    public String getZipResourcePath() {
        return originalRequest.getZipResourcePath();
    }

    @Override
    public boolean isZipResourceRequest() {
        return originalRequest.isZipResourceRequest();
    }

    @Override
    public String getParameter(String name) {
        return originalRequest.getParameter(name);
    }

    @Override
    public String[] getParameterValues(String name) {
        return originalRequest.getParameterValues(name);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return originalRequest.getInputStream();
    }

    @Override
    public long getContentLength() {
        return originalRequest.getContentLength();
    }

    @Override
    public boolean isNoneMatch(String etag) {
        return originalRequest.isNoneMatch(etag);
    }

    @Override
    public boolean hasIfNoneMatch() {
        return originalRequest.hasIfNoneMatch();
    }

    @Override
    public String toString() {
        return "source=" + getClientAddress()
                + ", path=" + getPath() + ", lastModified=" + getLastModified()
                + ", ifModifiedSince=" + getIfModifiedSince();
    }
}
