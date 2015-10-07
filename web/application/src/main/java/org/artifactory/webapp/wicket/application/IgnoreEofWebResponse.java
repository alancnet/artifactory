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

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.util.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import java.io.EOFException;

/**
 * WebResponse decorator. Ignores EOFException, see RTFACT-2997
 *
 * @author yoava
 */
public class IgnoreEofWebResponse extends WebResponse {
    private static final Logger log = LoggerFactory.getLogger(IgnoreEofWebResponse.class);
    private final WebResponse originalResponse;

    public IgnoreEofWebResponse(WebResponse originalResponse) {
        this.originalResponse = originalResponse;
    }

    @Override
    public void addCookie(Cookie cookie) {
        originalResponse.addCookie(cookie);
    }

    @Override
    public void clearCookie(Cookie cookie) {
        originalResponse.clearCookie(cookie);
    }

    @Override
    public void setHeader(String name, String value) {
        originalResponse.setHeader(name, value);
    }

    @Override
    public void addHeader(String name, String value) {
        originalResponse.addHeader(name, value);
    }

    @Override
    public void setDateHeader(String name, Time date) {
        originalResponse.setDateHeader(name, date);
    }

    @Override
    public void setContentLength(long length) {
        originalResponse.setContentLength(length);
    }

    @Override
    public void setContentType(String mimeType) {
        originalResponse.setContentType(mimeType);
    }

    @Override
    public void setStatus(int sc) {
        originalResponse.setStatus(sc);
    }

    @Override
    public void sendError(int sc, String msg) {
        originalResponse.sendError(sc, msg);
    }

    @Override
    public String encodeRedirectURL(CharSequence url) {
        return originalResponse.encodeRedirectURL(url);
    }

    @Override
    public void sendRedirect(String url) {
        originalResponse.sendRedirect(url);
    }

    @Override
    public boolean isRedirect() {
        return originalResponse.isRedirect();
    }

    @Override
    public void flush() {
        originalResponse.flush();
    }

    @Override
    public void write(CharSequence sequence) {
        try {
            originalResponse.write(sequence);
        } catch (Exception e) {
            ignoreEofException(e);
        }
    }

    @Override
    public void write(byte[] array) {
        try {
            originalResponse.write(array);
        } catch (Exception e) {
            ignoreEofException(e);
        }
    }

    @Override
    public String encodeURL(CharSequence url) {
        return originalResponse.encodeURL(url);
    }

    @Override
    public Object getContainerResponse() {
        return originalResponse.getContainerResponse();
    }

    @Override
    public void close() {
        originalResponse.close();
    }

    @Override
    public void reset() {
        originalResponse.reset();
    }

    private void ignoreEofException(Exception e) {
        Throwable throwable = e;
        while (throwable != null) {
            if (throwable instanceof EOFException) {
                log.debug("Ignoring EOFException when writing response.", e);
                return;
            }
            throwable = throwable.getCause();
        }
        throw new WicketRuntimeException("Unable to write the response", e);
    }
}