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

import org.artifactory.common.StatusHolder;
import org.slf4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;

public interface ArtifactoryResponse {

    String ARTIFACTORY_ID = "X-Artifactory-Id";

    boolean isError();

    void setLastModified(long lastModified);

    void setEtag(String etag);

    void setSha1(String sha1);

    void setMd5(String md5);

    void setRangeSupport(String bytes);

    long getContentLength();

    void setContentLength(long length);

    void setContentType(String contentType);

    Writer getWriter() throws IOException;

    void sendInternalError(Exception exception, Logger logger) throws IOException;

    void sendError(int statusCode, String reason, Logger logger) throws IOException;

    void sendError(StatusHolder statusHolder) throws IOException;

    void sendStream(InputStream is) throws IOException;

    void sendSuccess();

    int getStatus();

    void setStatus(int statusCode);

    void setHeader(String header, String value);

    boolean isCommitted();

    boolean isSuccessful();

    void flush();

    void sendAuthorizationRequired(String message, String realm) throws IOException;

    boolean isPropertiesQuery();

    String getPropertiesMediaType();

    void setPropertiesMediaType(String propsQueryFormat);

    void close(Closeable closeable);
}