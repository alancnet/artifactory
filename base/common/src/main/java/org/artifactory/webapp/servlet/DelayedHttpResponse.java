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

import org.apache.commons.io.IOUtils;
import org.artifactory.request.ArtifactoryResponseBase;
import org.artifactory.util.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * This class delays successful responses until {@link org.artifactory.webapp.servlet.DelayedHttpResponse#commitResponseCode()}.
 * is manually called. Use it whenever using http response inside a transaction that might fail after interacting with
 * the response.<p/> It assumes the clients will not call sendStream directly. When used, any writes to the output will
 * be cached. So be aware not to use it when responding with a lot of data.
 *
 * @author Yossi Shaul
 * @deprecated Usage of this class is not required anymore. The upload service now uses a more fine grained transactions
 * so a successful response is sent by the upload service only after the DB transaction was successfully committed
 */
@Deprecated
public class DelayedHttpResponse extends ArtifactoryResponseBase {
    private static final Logger log = LoggerFactory.getLogger(DelayedHttpResponse.class);

    private final HttpArtifactoryResponse response;
    private int status;
    private ByteArrayOutputStream out;
    private PrintWriter writer;
    private StringWriter stringWriter;

    public DelayedHttpResponse(HttpArtifactoryResponse response) {
        this.response = response;
        status = HttpServletResponse.SC_OK; // default status
    }

    /**
     * Call at the end of the <b>successful</b> request (after transaction commit or external processes are done) to
     * commit the response back to the client.<p/> Unsuccessful requests are assumes to call sendError which will send
     * the information without delay.
     *
     * @throws IOException If failed to write to the response stream.
     */
    public void commitResponseCode() throws IOException {
        if (isCommitted()) {
            log.trace("Response has already been committed.");
            return;
        }
        // set internal status only if successful (otherwise it was already done)
        if (isSuccessful()) {
            response.setStatus(status);
        }
        // write the response output
        if (out != null) {
            IOUtils.write(out.toByteArray(), response.getOutputStream());
        } else if (writer != null) {
            IOUtils.write(stringWriter.getBuffer(), response.getWriter());
        }
        response.flush();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        // return an internal output stream that will cache the response data
        if (out == null) {
            out = new ByteArrayOutputStream();
        }
        return out;
    }

    @Override
    public void setContentDispositionAttachment(String filename) {
        response.setContentDispositionAttachment(filename);
    }

    @Override
    public void setFilename(String filename) {
        response.setFilename(filename);
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        // return an internal writer that will cache the response data
        if (writer == null) {
            stringWriter = new StringWriter();
            writer = new PrintWriter(stringWriter);
        }
        return writer;
    }

    @Override
    public void setStatus(int status) {
        this.status = status;
        super.setStatus(status);    // call the super (NOT the http response)
        // only update the http response status is unsuccessful response code
        if (!HttpUtils.isSuccessfulResponseCode(status)) {
            response.setStatus(status);
        }
    }

    @Override
    public void sendSuccess() {
        setStatus(getStatus());
    }

    @Override
    public void flush() {
        // delay if status is ok
        if (!HttpUtils.isSuccessfulResponseCode(status)) {
            response.flush();
        }
    }

    // delegate unmodified methods //

    @Override
    public void setLastModified(long lastModified) {
        response.setLastModified(lastModified);
    }

    @Override
    public void setEtag(String etag) {
        response.setEtag(etag);
    }

    @Override
    public void setMd5(String md5) {
        response.setMd5(md5);
    }

    @Override
    public void setRangeSupport(String bytes) {
        response.setRangeSupport(bytes);
    }

    @Override
    public void setSha1(String sha1) {
        response.setSha1(sha1);
    }

    @Override
    public void sendErrorInternal(int statusCode, String reason) throws IOException {
        response.sendErrorInternal(statusCode, reason);
    }

    @Override
    public void sendAuthorizationRequired(String message, String realm) throws IOException {
        response.sendAuthorizationRequired(message, realm);
    }

    @Override
    public void setContentLength(long length) {
        response.setContentLength(length);
    }

    @Override
    public void setHeader(String header, String value) {
        response.setHeader(header, value);
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
