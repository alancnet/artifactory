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
import org.apache.http.HttpStatus;
import org.artifactory.common.ConstantValues;
import org.artifactory.request.ArtifactoryRequest;
import org.artifactory.request.ArtifactoryResponseBase;
import org.artifactory.util.HttpUtils;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

public class HttpArtifactoryResponse extends ArtifactoryResponseBase {
    private static final Logger log = LoggerFactory.getLogger(HttpArtifactoryResponse.class);

    private final HttpServletResponse response;

    public HttpArtifactoryResponse(HttpServletResponse response) {
        this.response = response;
    }

    @Override
    public void setLastModified(long lastModified) {
        response.setDateHeader("Last-Modified", lastModified);
    }

    @Override
    public void setEtag(String etag) {
        if (etag != null) {
            response.setHeader("ETag", etag);
        } else {
            log.debug("Could not register a null etag with the response.");
        }
    }

    @Override
    public void setMd5(String md5) {
        if (md5 != null) {
            response.setHeader(ArtifactoryRequest.CHECKSUM_MD5, md5);
        } else {
            log.debug("Could not register a null md5 tag with the response.");
        }
    }

    public void setRangeSupport(String bytes) {
        if (bytes != null) {
            response.setHeader(ArtifactoryRequest.ACCEPT_RANGES, bytes);
        } else {
            log.debug("Could not register a null range support tag with the response.");
        }
    }

    @Override
    public void setSha1(String sha1) {
        if (sha1 != null) {
            response.setHeader(ArtifactoryRequest.CHECKSUM_SHA1, sha1);
        } else {
            log.debug("Could not register a null sha1 tag with the response.");
        }
    }

    /**
     * Set the content disposition type to attachment. This will instruct the browser downloading the file to save it
     * instead of displaying it inline (for example html files). <p/>
     * We don't set the name here and let the browser decide based on download URL.
     * For more info read <a href="https://www.ietf.org/rfc/rfc2183.txt">RFC2183</a>
     */
    @Override
    public void setContentDispositionAttachment(String filename) {
        if (ConstantValues.responseDisableContentDispositionFilename.getBoolean() || StringUtils.isBlank(filename)) {
            response.setHeader("Content-Disposition", "attachment");
        } else {
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"" +
                    "; filename*=UTF-8''" + HttpUtils.encodeQuery(filename));
        }
    }

    @Override
    public void setFilename(String filename) {
        if (StringUtils.isNotBlank(filename)) {
            response.setHeader(ArtifactoryRequest.FILE_NAME, HttpUtils.encodeQuery(filename));
        } else {
            log.debug("Could not register a null filename with the response.");
        }
    }

    @Override
    protected void sendErrorInternal(int statusCode, String reason) throws IOException {
        if (response.isCommitted()) {
            log.debug("Cannot send error " + statusCode +
                    (reason != null ? " (" + reason + ")" : "") +
                    ": response already committed.");
            return;
        }
        try {
            log.trace("Sending back error code {}. Reason: {}", statusCode, reason);
            HttpUtils.sendErrorResponse(response, statusCode, reason);
            flush();
        } catch (IOException e) {
            throw e;
        } catch (Throwable t) {
            log.warn("Failed to send http error (" + t.getMessage() + ").", t);
        }
    }

    @Override
    public void sendAuthorizationRequired(String message, String realm) throws IOException {
        try {
            response.addHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
            HttpUtils.sendErrorResponse(response, HttpStatus.SC_UNAUTHORIZED, "Unauthorized");
            flush();
        } catch (IOException e) {
            throw e;
        } catch (IllegalStateException e) {
            log.warn("Failed to send http error (" + e.getMessage() + ").", e);
        } catch (Throwable t) {
            log.warn("Failed to send http error (" + t.getMessage() + ").", t);
        }
    }

    @Override
    public void setContentLength(long length) {
        super.setContentLength(length);
        if (length <= Integer.MAX_VALUE) {
            response.setContentLength((int) length);
        } else {
            // servlet api doesn't support long values, set the header manually
            response.setHeader(HttpHeaders.CONTENT_LENGTH, length + "");
        }
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return response.getOutputStream();
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return response.getWriter();
    }

    @Override
    public void setStatus(int status) {
        super.setStatus(status);
        response.setStatus(status);
    }

    @Override
    public void setHeader(String header, String value) {
        response.setHeader(header, value);
    }

    @Override
    public void flush() {
        try {
            response.flushBuffer();
        } catch (IOException e) {
            String message = "Failed to commit http response (" + e.getMessage() + ").";
            log.warn(message);
            log.debug(message, e);
        }
    }

    @Override
    public void setContentType(String contentType) {
        response.setContentType(contentType);
    }

    @Override
    public boolean isCommitted() {
        return response.isCommitted();
    }
}