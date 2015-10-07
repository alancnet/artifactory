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

package org.artifactory.webapp.servlet;

import org.apache.http.HttpHeaders;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.ha.HaCommonAddon;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.request.ArtifactoryRequestBase;
import org.artifactory.repo.RepoPath;
import org.artifactory.request.ArtifactoryRequest;
import org.artifactory.util.HttpUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class HttpArtifactoryRequest extends ArtifactoryRequestBase {

    private final HttpServletRequest httpRequest;

    public HttpArtifactoryRequest(HttpServletRequest httpRequest) throws UnsupportedEncodingException {
        this.httpRequest = httpRequest;
        String servletPath = RequestUtils.getServletPathFromRequest(httpRequest);
        RepoPath repoPath = calculateRepoPath(servletPath);
        setRepoPath(repoPath);
    }

    @Override
    public long getLastModified() {
        return httpRequest.getDateHeader(HttpHeaders.LAST_MODIFIED);
    }

    @Override
    public boolean isHeadOnly() {
        return "HEAD".equalsIgnoreCase(httpRequest.getMethod());
    }

    @Override
    public String getClientAddress() {
        return HttpUtils.getRemoteClientAddress(httpRequest);
    }

    @Override
    public String getServletContextUrl() {
        return HttpUtils.getServletContextUrl(httpRequest);
    }

    @Override
    public long getIfModifiedSince() {
        return httpRequest.getDateHeader(HttpHeaders.IF_MODIFIED_SINCE);
    }

    @Override
    public boolean hasIfModifiedSince() {
        return getIfModifiedSince() != -1;
    }

    @Override
    public boolean isFromAnotherArtifactory() {
        Enumeration origins = getOrigins();
        return origins.hasMoreElements();
    }

    @Override
    public boolean isRecursive() {
        Enumeration<String> origins = getOrigins();
        if (origins != null && origins.hasMoreElements()) {
            ArrayList<String> originsList = Collections.list(origins);
            String currentHostId = ContextHelper.get().beanForType(AddonsManager.class).addonByType(
                    HaCommonAddon.class).getHostId();
            int numOfOrigins = originsList.size();
            for (String origin : originsList) {
                if (numOfOrigins > 1 && currentHostId.equals(origin)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return httpRequest.getInputStream();
    }

    /**
     * @see javax.servlet.ServletRequest#getContentLength()
     */
    @Override
    public long getContentLength() {
        return HttpUtils.getContentLength(httpRequest);
    }

    @Override
    public String getHeader(String headerName) {
        return httpRequest.getHeader(headerName);
    }

    @Override
    public Enumeration getHeaders(String headerName) {
        return httpRequest.getHeaders(headerName);
    }

    @Override
    public Map<String, String> getHeaders() {
        String headerName;
        Map<String, String> headers = new HashMap<>();
        Enumeration enumeration = httpRequest.getHeaderNames();
        while (enumeration != null && enumeration.hasMoreElements()) {
            headerName = (String)enumeration.nextElement();
            headers.put(headerName, httpRequest.getHeader(headerName));
        }
        return headers;
    }

    @Override
    public String getUri() {
        return httpRequest.getRequestURI();
    }

    @Override
    public String getParameter(String name) {
        return httpRequest.getParameter(name);
    }

    @Override
    public String[] getParameterValues(String name) {
        return httpRequest.getParameterValues(name);
    }

    @Override
    public String toString() {
        return getUri();
    }

    private Enumeration getOrigins() {
        Enumeration origins = getHeaders(ArtifactoryRequest.ARTIFACTORY_ORIGINATED);
        if (origins == null) {
            origins = getHeaders(ArtifactoryRequest.ORIGIN_ARTIFACTORY);
        }
        return origins;
    }
}