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

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.artifactory.api.request.ArtifactoryResponse;
import org.artifactory.common.StatusHolder;
import org.artifactory.util.ExceptionUtils;
import org.artifactory.util.HttpClientUtils;
import org.artifactory.util.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class ArtifactoryResponseBase implements ArtifactoryResponse {
    private static final Logger log = LoggerFactory.getLogger(ArtifactoryResponseBase.class);

    private int status = HttpStatus.SC_OK;
    private long contentLength = -1;
    private String propertiesMediaType = null;

    private static String makeDebugMessage(int statusCode, String reason) {
        StringBuilder builder = new StringBuilder("Sending HTTP error code ").append(statusCode);
        if (reason != null) {
            builder.append(": ").append(reason);
        }
        return builder.toString();
    }

    @Override
    public void sendStream(InputStream is) throws IOException {
        OutputStream os = getOutputStream();
        setStatus(status);
        try {
            long bytesCopied = IOUtils.copyLarge(is, os);
            if (bytesCopied == 0 && getContentLength() > 0) {
                log.warn("Zero bytes sent to client but expected {} bytes.", getContentLength());
            } else {
                long expectedLength = getContentLength();
                if (expectedLength > 0 && bytesCopied != expectedLength) {
                    log.warn("Actual bytes sent to client ({}) are different than expected ({}).", bytesCopied,
                            expectedLength);
                } else {
                    log.debug("{} bytes sent to client.", bytesCopied);
                }
            }
            sendSuccess();
        } catch (Exception e) {
            sendInternalError(e, log);
        } finally {
            IOUtils.closeQuietly(os);
            IOUtils.closeQuietly(is);
        }
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public void sendSuccess() {
        if (isSuccessful() || HttpUtils.isRedirectionResponseCode(status)) {
            flush();
        } else {
            log.error("Could not send success. Exiting status: {}.", status);
            if (log.isDebugEnabled()) {
                log.debug("Could not send success.", new Throwable());
            }
        }
    }

    @Override
    public void sendError(int statusCode, String reason, Logger logger) throws IOException {
        String msg = makeDebugMessage(statusCode, reason);
        if (statusCode == HttpStatus.SC_NOT_FOUND || statusCode == HttpStatus.SC_NOT_MODIFIED) {
            logger.debug(msg);
        } else {
            log.warn(msg);
        }
        this.status = statusCode;
        sendErrorInternal(statusCode, reason);
    }

    @Override
    public void sendError(StatusHolder statusHolder) throws IOException {
        sendError(statusHolder.getStatusCode(), statusHolder.getStatusMsg(), log);
    }

    @Override
    public void sendInternalError(Exception exception, Logger logger) throws IOException {
        Throwable ioException = ExceptionUtils.getCauseOfTypes(exception, IOException.class);
        String reason;
        if (ioException != null) {
            reason = HttpClientUtils.getErrorMessage(ioException);
            String message;
            if (isCommitted()) {
                // The client already received a status answer, so changing the status code
                // is for internal use only. Meaning that traffic, request and access logger
                // will not confuse this as a successful download.
                // Using the 499 HTTP code used by Nginx for Client Closed Request
                status = 499;
                message = "Client Closed Request " + status + ": " + reason;
            } else {
                status = HttpStatus.SC_NOT_FOUND;
                message = makeDebugMessage(status, reason);
            }
            logger.debug(message, exception);
            logger.warn(message);
        } else {
            status = HttpStatus.SC_INTERNAL_SERVER_ERROR;
            reason = exception.getMessage();
            String message = makeDebugMessage(status, reason);
            logger.debug(message, exception);
            logger.error(message);
        }
        sendErrorInternal(status, reason);
    }

    @Override
    public boolean isSuccessful() {
        return HttpUtils.isSuccessfulResponseCode(status);
    }

    @Override
    public boolean isError() {
        return !isSuccessful();
    }

    @Override
    public long getContentLength() {
        return contentLength;
    }

    @Override
    public void setContentLength(long length) {
        //Cache the content length locally
        this.contentLength = length;
    }

    @Override
    public boolean isPropertiesQuery() {
        return propertiesMediaType != null;
    }

    @Override
    public String getPropertiesMediaType() {
        return propertiesMediaType;
    }

    @Override
    public void setPropertiesMediaType(String propsQueryFormat) {
        this.propertiesMediaType = propsQueryFormat;
    }

    @Override
    public void close(Closeable closeable) {
        IOUtils.closeQuietly(closeable);
    }

    protected abstract void sendErrorInternal(int code, String reason) throws IOException;

    protected abstract OutputStream getOutputStream() throws IOException;

    public abstract void setContentDispositionAttachment(String filename);

    public abstract void setFilename(String filename);
}