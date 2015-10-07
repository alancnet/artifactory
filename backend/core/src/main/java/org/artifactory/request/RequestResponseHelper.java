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

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.request.ArtifactoryResponse;
import org.artifactory.api.rest.constant.ArtifactRestConstants;
import org.artifactory.descriptor.repo.RealRepoDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.fs.RepoResource;
import org.artifactory.mime.NamingUtils;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.request.range.RangeAwareContext;
import org.artifactory.resource.RepoResourceInfo;
import org.artifactory.resource.ResourceStreamHandle;
import org.artifactory.resource.ZipEntryResource;
import org.artifactory.security.AccessLogger;
import org.artifactory.traffic.TrafficService;
import org.artifactory.traffic.entry.DownloadEntry;
import org.artifactory.util.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.artifactory.api.rest.constant.ArtifactRestConstants.MT_ITEM_PROPERTIES;
import static org.artifactory.request.range.ResponseWithRangeSupportHelper.createRangeAwareContext;

/**
 * @author yoavl
 */
public final class RequestResponseHelper {
    private static final Logger log = LoggerFactory.getLogger(RequestResponseHelper.class);

    private TrafficService trafficService;

    public RequestResponseHelper(TrafficService service) {
        trafficService = service;
    }

    private static void addDebugLog(long actualLength, long resLenght, RepoPath repoPath) {
        if (log.isDebugEnabled()) {
            log.debug("Sending back body response for '{}'. Original resource size: {}, actual size: {}.",
                    repoPath, resLenght, actualLength);
        }
    }

    public void sendBodyResponse(ArtifactoryResponse response, RepoResource res, ResourceStreamHandle handle,
            InternalRequestContext requestContext) throws IOException {
        // Try to get range headers
        String rangesString = tryToGetRangeHeaderInsensitive(HttpHeaders.RANGE, requestContext.getRequest());
        String ifRangesString = tryToGetRangeHeaderInsensitive(HttpHeaders.IF_RANGE, requestContext.getRequest());
        // Get mimetype
        String mimeType = res.getMimeType();
        InputStream inputStream = handle.getInputStream();
        // Get Actual file actualLength (Note that it might not be equal to the res.getSize)
        long actualLength = handle.getSize();
        AccessLogger.downloaded(res.getRepoPath());
        addDebugLog(actualLength, res.getSize(), res.getRepoPath());
        // Ensure valid content length
        actualLength = actualLength > 0 ? actualLength : res.getSize();
        // Get artifact last modified date
        long lastModified = res.getLastModified();
        // Get artifact sha1
        String sha1 = res.getInfo().getSha1();
        // Create range aware data for the response
        RangeAwareContext context = createRangeAwareContext(inputStream, actualLength, rangesString, ifRangesString,
                mimeType, lastModified, sha1);
        // If request range not satisfiable update response status end return
        if (context.getStatus() == HttpStatus.SC_REQUESTED_RANGE_NOT_SATISFIABLE) {
            response.setHeader(HttpHeaders.CONTENT_RANGE, context.getContentRange());
            response.setStatus(context.getStatus());
            return;
        }
        // Update response with repo resource info
        updateResponseFromRepoResource(response, res, context.getEtagExtension());
        // update content length with range aware content length
        response.setContentLength(context.getContentLength());
        // update content type with range aware content type
        response.setContentType(context.getContentType());
        // update headers with range aware headers
        if (context.getContentRange() != null) {
            response.setHeader(HttpHeaders.CONTENT_RANGE, context.getContentRange());
        }
        // Set response status
        if (context.getStatus() > 0) {
            response.setStatus(context.getStatus());
        }
        // Get range aware input stream
        inputStream = context.getInputStream();
        // Get current time for logs
        long start = System.currentTimeMillis();
        // Send range aware input stream
        response.sendStream(inputStream);
        fireDownloadTrafficEvent(response, res.getRepoPath(), actualLength, start);
    }

    private String tryToGetRangeHeaderInsensitive(String name, Request request) {
        String header = request.getHeader(name);
        if (StringUtils.isBlank(header)) {
            header = request.getHeader(name.toLowerCase());
        }
        return header;
    }

    public void sendBodyResponse(ArtifactoryResponse response, RepoPath repoPath, String content, Request request)
            throws IOException {
        // Make sure that the response is not empty
        if (content == null) {
            RuntimeException exception = new RuntimeException("Cannot send null response");
            response.sendInternalError(exception, log);
            throw exception;
        }
        byte[] bytes = content.getBytes("utf-8");
        try (InputStream is = new ByteArrayInputStream(bytes)) {
            String path = repoPath.getPath();
            String mimeType = NamingUtils.getMimeTypeByPathAsString(path);
            response.setContentType(mimeType);
            int bodySize = bytes.length;
            response.setContentLength(bodySize);
            response.setLastModified(System.currentTimeMillis());
            AccessLogger.downloaded(repoPath);
            // Try to get range header
            String rangesString = tryToGetRangeHeaderInsensitive(HttpHeaders.RANGE, request);
            // Create range aware data for the response
            // If-Range is not supported
            RangeAwareContext context = createRangeAwareContext(is, bodySize, rangesString, null, mimeType, -1, null);
            // If request range not satisfiable update response status end return
            if (context.getStatus() == HttpStatus.SC_REQUESTED_RANGE_NOT_SATISFIABLE) {
                response.setHeader(HttpHeaders.CONTENT_RANGE, context.getContentRange());
                response.setStatus(context.getStatus());
                return;
            }
            // update content length with range aware content length
            response.setContentLength(context.getContentLength());
            // update content type with range aware content type
            response.setContentType(context.getContentType());
            // update headers with range aware headers
            if (context.getContentRange() != null) {
                response.setHeader(HttpHeaders.CONTENT_RANGE, context.getContentRange());
            }
            // Get range aware input stream
            InputStream inputStream = context.getInputStream();
            // Set response status
            if (context.getStatus() > 0) {
                response.setStatus(context.getStatus());
            }
            // Get current time for logs
            long start = System.currentTimeMillis();
            response.sendStream(inputStream);
            fireDownloadTrafficEvent(response, repoPath, bodySize, start);
        }
    }

    private void fireDownloadTrafficEvent(ArtifactoryResponse response, RepoPath repoPath, long size,
            long start) {
        if (!(response instanceof InternalArtifactoryResponse)) {
            String remoteAddress = HttpUtils.getRemoteClientAddress();
            DownloadEntry downloadEntry = new DownloadEntry(
                    repoPath.getId(), size, System.currentTimeMillis() - start, remoteAddress);
            trafficService.handleTrafficEntry(downloadEntry);
        }
    }

    public void sendHeadResponse(ArtifactoryResponse response, RepoResource res) {
        log.debug("{}: Sending HEAD meta-information", res.getRepoPath());
        if (!isContentLengthSet(response)) {
            response.setContentLength(res.getSize());
        }
        updateResponseFromRepoResource(response, res, "");
        response.setContentType(res.getMimeType());
        response.sendSuccess();
    }

    public void sendNotModifiedResponse(ArtifactoryResponse response, RepoResource res) throws IOException {
        log.debug("{}: Sending NOT-MODIFIED response", res.toString());
        response.setContentLength(0);
        updateResponseFromRepoResource(response, res, "");
        response.setContentType(res.getMimeType());
        response.setStatus(HttpStatus.SC_NOT_MODIFIED);
    }

    public void updateResponseForProperties(ArtifactoryResponse response, RepoResource res,
            String content, MediaType mediaType, InternalRequestContext requestContext) throws IOException {
        RepoPath propsDownloadRepoPath;
        String contentType;
        if (mediaType.equals(MediaType.APPLICATION_XML)) {
            propsDownloadRepoPath = RepoPathFactory.create(res.getRepoPath().getRepoKey(),
                    res.getRepoPath().getPath() + "?" + ArtifactRestConstants.PROPERTIES_XML_PARAM);
            contentType = mediaType.getType();
        } else if (mediaType.equals(MediaType.APPLICATION_JSON)) {
            propsDownloadRepoPath = RepoPathFactory.create(res.getRepoPath().getRepoKey(),
                    res.getRepoPath().getPath() + "?" + ArtifactRestConstants.PROPERTIES_PARAM);
            contentType = MT_ITEM_PROPERTIES;
        } else {
            response.sendError(HttpStatus.SC_BAD_REQUEST, "Media Type " + mediaType + " not supported!", log);
            return;
        }
        // props generated xml and json always browsable
        setBasicHeaders(response, res, false, "");
        noCache(response);
        byte[] bytes = content.getBytes("utf-8");
        try (InputStream is = new ByteArrayInputStream(bytes)) {
            int bodySize = bytes.length;
            response.setContentLength(bodySize);
            AccessLogger.downloaded(propsDownloadRepoPath);
            // Try to get range header
            String rangesString = tryToGetRangeHeaderInsensitive(HttpHeaders.RANGE, requestContext.getRequest());
            String ifRangesString = tryToGetRangeHeaderInsensitive(HttpHeaders.IF_RANGE, requestContext.getRequest());
            // Get artifact last modified date
            long lastModified = res.getLastModified();
            // Get artifact sha1
            String sha1 = res.getInfo().getSha1();
            // Create range aware data for the response
            RangeAwareContext context = createRangeAwareContext(is, bodySize, rangesString, ifRangesString, contentType,
                    lastModified, sha1);
            // If request range not satisfiable update response status end return
            if (context.getStatus() == HttpStatus.SC_REQUESTED_RANGE_NOT_SATISFIABLE) {
                response.setHeader(HttpHeaders.CONTENT_RANGE, context.getContentRange());
                response.setStatus(context.getStatus());
                return;
            }
            // update content length with range aware content length
            response.setContentLength(context.getContentLength());
            // update content type with range aware content type
            response.setContentType(context.getContentType());
            // update headers with range aware headers
            if (context.getContentRange() != null) {
                response.setHeader(HttpHeaders.CONTENT_RANGE, context.getContentRange());
            }
            // Set response status
            if (context.getStatus() > 0) {
                response.setStatus(context.getStatus());
            }
            // Get range aware input stream
            InputStream inputStream = context.getInputStream();
            // Get current time for logs
            long start = System.currentTimeMillis();
            // Send stream
            response.sendStream(inputStream);
            // Fire Download traffic event
            fireDownloadTrafficEvent(response, propsDownloadRepoPath, bodySize, start);
        }
    }

    private void updateResponseFromRepoResource(ArtifactoryResponse response, RepoResource res, String etagExtension) {
        setBasicHeaders(response, res, contentBrowsingDisabled(res), etagExtension);
        if (res.isExpirable()) {
            noCache(response);
        }
    }

    private boolean isContentLengthSet(ArtifactoryResponse response) {
        return response.getContentLength() != -1;
    }

    private void setBasicHeaders(ArtifactoryResponse response, RepoResource res, boolean contentBrowsingDisabled,
            String etagExtension) {
        response.setLastModified(res.getLastModified());
        RepoResourceInfo info = res.getInfo();
        // set the sha1 as the eTag and the sha1 header
        String sha1 = info.getSha1();
        String etag = sha1 != null ? sha1 + etagExtension : null;
        response.setEtag(etag);
        response.setSha1(sha1);
        response.setRangeSupport("bytes");
        // set the md5 header
        String md5 = info.getMd5();
        response.setMd5(md5);
        if (response instanceof ArtifactoryResponseBase) {
            String fileName = info.getName();
            if (!isNotZipResource(res)) {
                // The filename is the zip entry inside the zip
                ZipEntryResource zipEntryResource = (ZipEntryResource) res;
                fileName = zipEntryResource.getEntryPath();
            }
            ((ArtifactoryResponseBase) response).setFilename(fileName);

            // content disposition is not set only for archive resources when archived browsing is enabled
            if (contentBrowsingDisabled) {
                ((ArtifactoryResponseBase) response).setContentDispositionAttachment(fileName);
            }
        }
    }

    private void noCache(ArtifactoryResponse response) {
        response.setHeader("Cache-Control", "no-store");
    }

    private boolean isNotZipResource(RepoResource res) {
        return !(res instanceof ZipEntryResource);
    }

    private boolean contentBrowsingDisabled(RepoResource res) {
        boolean result = true;
        RepositoryService repositoryService = ContextHelper.get().getRepositoryService();
        String repoKey = res.getResponseRepoPath().getRepoKey();
        RepoDescriptor repoDescriptor = repositoryService.repoDescriptorByKey(repoKey);
        if (repoDescriptor != null) {
            if (repoDescriptor instanceof RealRepoDescriptor) {
                result = !((RealRepoDescriptor) repoDescriptor).isArchiveBrowsingEnabled();
            }
        }

        // We return true by default if we couldn't get the flag from the descriptor
        return result;
    }
}