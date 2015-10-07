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

import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.io.output.NullWriter;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.common.StatusHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/**
 * An internal response that is used as a dummy client to consume responses from Artifactory. It is used when
 * Artifactory is sending itself a request (e.g., to eager fetch sources jar). When using this response the download
 * traffic is not counted.
 *
 * @author Yossi Shaul
 */
public class InternalArtifactoryResponse extends ArtifactoryResponseBase {

    private static final Logger log = LoggerFactory.getLogger(InternalArtifactoryResponse.class);

    private String statusMessage;

    public InternalArtifactoryResponse() {
    }

    @Override
    public void setLastModified(long lastModified) {
        // ignore
    }

    @Override
    public void setEtag(String etag) {
        // ignore
    }

    @Override
    public void setMd5(String md5) {
        // ignore
    }

    @Override
    public void setRangeSupport(String bytes) {
        // ignore
    }

    @Override
    public void setSha1(String sha1) {
        // ignore
    }

    @Override
    public void sendErrorInternal(int statusCode, String reason) throws IOException {
        // nothing special
        statusMessage = reason;
    }

    @Override
    public void sendAuthorizationRequired(String message, String realm) throws IOException {
        super.sendError(HttpStatus.SC_FORBIDDEN, message, log);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return new NullOutputStream();
    }

    @Override
    public void setContentDispositionAttachment(String filename) {
        // ignore
    }

    @Override
    public void setFilename(String filename) {
        // ignore
    }

    @Override
    public Writer getWriter() throws IOException {
        return new NullWriter();
    }

    @Override
    public void setHeader(String header, String value) {
        // ignore
    }

    @Override
    public void flush() {
        // ignore
    }

    @Override
    public void setContentType(String contentType) {
        // ignore
    }

    @Override
    public boolean isCommitted() {
        return false;
    }

    @Override
    public void sendError(int statusCode, String reason, Logger log) throws IOException {
        super.sendError(statusCode, reason, log);
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public StatusHolder getStatusHolder() {
        BasicStatusHolder sh = new BasicStatusHolder();
        if (StringUtils.isNotBlank(statusMessage)) {
            if (isSuccessful()) {
                sh.status(statusMessage, getStatus(), log);
            } else {
                sh.error(statusMessage, getStatus(), log);
            }
        }
        return sh;
    }
}
