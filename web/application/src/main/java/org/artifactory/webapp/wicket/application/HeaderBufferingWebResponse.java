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

package org.artifactory.webapp.wicket.application;

import org.apache.wicket.protocol.http.BufferedWebResponse;
import org.apache.wicket.protocol.http.IMetaDataBufferingWebResponse;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.time.Time;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * PATCHED BY YOAVA, see RTFACT-4619 and RTFACT-4635
 * <p/>
 * Response that keeps headers in buffers but writes the content directly to the response.
 * <p/>
 * This is necessary to get {@link #reset()} working without removing the JSESSIONID cookie. When
 * {@link HttpServletResponse#reset()} is called it removes all cookies, including the JSESSIONID
 * cookie.
 * <p/>
 * Calling {@link #reset()} on this response only clears the buffered headers. If there is any
 * content written to response it throws {@link IllegalStateException}.
 *
 * @author Matej Knopp
 */
class HeaderBufferingWebResponse extends WebResponse implements IMetaDataBufferingWebResponse {
    private final WebResponse originalResponse;
    private final BufferedWebResponse bufferedResponse;

    public HeaderBufferingWebResponse(WebResponse originalResponse) {
        this.originalResponse = originalResponse;
        bufferedResponse = new BufferedWebResponse(originalResponse);
    }

    private boolean bufferedWritten = false;

    private void writeBuffered() {
        if (!bufferedWritten) {
            bufferedResponse.writeTo(originalResponse);
            bufferedWritten = true;
        }
    }

    private void checkHeader() {
        // patched by yoava, see RTFACT-4619
        //if (bufferedWritten) {
        //    throw new IllegalStateException("Header was already written to response!");
        //}
    }

    @Override
    public void addCookie(Cookie cookie) {
        checkHeader();
        bufferedResponse.addCookie(cookie);
    }

    @Override
    public void clearCookie(Cookie cookie) {
        checkHeader();
        bufferedResponse.clearCookie(cookie);
    }

    private boolean flushed = false;

    @Override
    public void flush() {
        if (!bufferedWritten) {
            bufferedResponse.writeTo(originalResponse);
            bufferedResponse.reset();
        }
        originalResponse.flush();
        flushed = true;
    }

    @Override
    public boolean isRedirect() {
        return bufferedResponse.isRedirect();
    }

    @Override
    public void sendError(int sc, String msg) {
        checkHeader();
        bufferedResponse.sendError(sc, msg);
    }

    @Override
    public void sendRedirect(String url) {
        checkHeader();
        bufferedResponse.sendRedirect(url);
    }

    @Override
    public void setContentLength(long length) {
        checkHeader();
        bufferedResponse.setContentLength(length);
    }

    @Override
    public void setContentType(String mimeType) {
        checkHeader();
        bufferedResponse.setContentType(mimeType);
    }

    @Override
    public void setDateHeader(String name, Time date) {
        Args.notNull(date, "date");
        checkHeader();
        bufferedResponse.setDateHeader(name, date);
    }

    @Override
    public void setHeader(String name, String value) {
        checkHeader();
        bufferedResponse.setHeader(name, value);
    }

    @Override
    public void addHeader(String name, String value) {
        checkHeader();
        bufferedResponse.addHeader(name, value);
    }

    @Override
    public void setStatus(int sc) {
        bufferedResponse.setStatus(sc);
    }

    @Override
    public String encodeURL(CharSequence url) {
        // patched by yoava, see RTFACT-4635
        final String encodedURL = originalResponse.encodeURL(url);
        if (!encodedURL.contains("/")) {
            return "./" + encodedURL;
        }
        return encodedURL;
    }

    @Override
    public String encodeRedirectURL(CharSequence url) {
        return originalResponse.encodeRedirectURL(url);
    }

    @Override
    public void write(CharSequence sequence) {
        writeBuffered();
        originalResponse.write(sequence);
    }

    @Override
    public void write(byte[] array) {
        writeBuffered();
        originalResponse.write(array);
    }


    @Override
    public void write(byte[] array, int offset, int length) {
        writeBuffered();
        originalResponse.write(array, offset, length);
    }

    @Override
    public void reset() {
        if (flushed) {
            throw new IllegalStateException("Response has already been flushed!");
        }
        bufferedResponse.reset();
        bufferedWritten = false;
    }

    @Override
    public void writeMetaData(WebResponse webResponse) {
        bufferedResponse.writeMetaData(webResponse);
    }

    @Override
    public Object getContainerResponse() {
        return originalResponse.getContainerResponse();
    }
}

