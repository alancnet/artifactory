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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import static org.testng.Assert.assertTrue;

/**
 * @author Yoav Landman
 */
@Test
public class ArtifactoryResponseTest {

    private static final Logger log = LoggerFactory.getLogger(ArtifactoryResponseTest.class);

    public void sendErroChangesState() throws IOException {
        ArtifactoryResponseBase response = newResponse();
        response.sendError(500, "Bad bad thing", log);
        assertTrue(response.isError());
    }

    private ArtifactoryResponseBase newResponse() {
        return new ArtifactoryResponseBase() {
            @Override
            protected void sendErrorInternal(int code, String reason) throws IOException {
            }

            @Override
            public void setLastModified(long lastModified) {
            }

            @Override
            public void setEtag(String etag) {
            }

            @Override
            public void setSha1(String sha1) {
            }

            @Override
            public void setMd5(String md5) {
            }

            @Override
            public void setRangeSupport(String bytes) {
            }

            @Override
            public void setContentType(String contentType) {
            }

            @Override
            public OutputStream getOutputStream() throws IOException {
                return null;
            }

            @Override
            public void setContentDispositionAttachment(String filename) {

            }

            @Override
            public void setFilename(String filename) {

            }

            @Override
            public Writer getWriter() throws IOException {
                return null;
            }

            @Override
            public void setHeader(String header, String value) {
            }

            @Override
            public boolean isCommitted() {
                return false;
            }

            @Override
            public void flush() {
            }

            @Override
            public void sendAuthorizationRequired(String message, String realm) throws IOException {
            }
        };
    }
}
