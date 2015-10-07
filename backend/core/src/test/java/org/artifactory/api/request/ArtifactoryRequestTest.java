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

import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.md.Properties;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

/**
 * @author Yoav Landman
 */
@SuppressWarnings({"ConstantConditions"})
@Test
public class ArtifactoryRequestTest extends ArtifactoryHomeBoundTest {

    public void matrixParams() throws UnsupportedEncodingException {
        ArtifactoryRequestBase request = newRequest();
        request.setRepoPath(request.calculateRepoPath("libs-releases/path1/path2:metadata1;a=1;a=11;b=2;c;d=4;e"));
        Properties params = request.getProperties();
        assertMatrixParams(params);
        request = newRequest();
        request.setRepoPath(request.calculateRepoPath("libs-releases;a=1;a=11;b=2;c;d=4;e/path1/path2:metadata1"));
        params = request.getProperties();
        assertMatrixParams(params);
        request = newRequest();
        request.setRepoPath(request.calculateRepoPath("libs-releases;a=1;b=2/path1/path2:metadata1;a=11;c;d=4;e"));
        params = request.getProperties();
        assertMatrixParams(params);
        request = newRequest();
        request.setRepoPath(request.calculateRepoPath("libs-releases;a=1;a=11;b=2;c;d=4;e"));
        params = request.getProperties();
        assertMatrixParams(params);
    }

    public void matrixParamsWithEncodedValues() throws UnsupportedEncodingException {
        ArtifactoryRequestBase request = newRequest();
        request.setRepoPath(request.calculateRepoPath("libs-releases;a=1%2B2;b=1+2;c=1%232;d=c%20c;e="));
        Properties params = request.getProperties();
        assertEquals(params.size(), 5, "Expecting 5 parameters");
        assertEquals(params.get("a").iterator().next(), "1+2", "Character '%2B' should have been decoded to +");
        assertEquals(params.get("b").iterator().next(), "1 2", "Character '+' should have been decoded to space");
        assertEquals(params.get("c").iterator().next(), "1#2", "String '%23' should have been decoded to '#'");
        assertEquals(params.get("d").iterator().next(), "c c", "String '%20' should have been decoded to space");
        assertEquals(params.get("e").iterator().next(), "", "Expecting empty string");
    }

    public void pathsWithSpaces() throws UnsupportedEncodingException {
        ArtifactoryRequestBase request = newRequest();
        assertEquals(request.calculateRepoPath("libs-rele%C3%A3ses/%C3%A3%20path/with/some%20non%C3%A3scii.jar"),
                InfoFactoryHolder.get().createRepoPath("libs-releãses", "ã path/with/some nonãscii.jar"));
    }

    public void pathWithNonAscii() throws UnsupportedEncodingException {
        ArtifactoryRequestBase request = newRequest();
        assertEquals(request.calculateRepoPath("libs-releases%20momo/a%20path/with/some%20spaces.jar"),
                InfoFactoryHolder.get().createRepoPath("libs-releases momo", "a path/with/some spaces.jar"));
        assertEquals(request.calculateRepoPath("libs-releases%20momo/a%20path/with/some%20spaces.jar:andmetadata"),
                InfoFactoryHolder.get().createRepoPath("libs-releases momo",
                        "a path/with/some spaces.jar:andmetadata"));
    }

    public void zipResourceAbsolute() throws UnsupportedEncodingException {
        ArtifactoryRequestBase request = newRequest();
        request.setRepoPath(request.calculateRepoPath("libs-releases/path/to/zip!/zip/resource"));
        assertEquals(request.getRepoPath(), InfoFactoryHolder.get().createRepoPath("libs-releases", "path/to/zip"));
        assertEquals(request.getZipResourcePath(), "zip/resource");
    }

    public void zipResourceRelative() throws UnsupportedEncodingException {
        ArtifactoryRequestBase request = newRequest();
        request.setRepoPath(request.calculateRepoPath("libs-releases/path/to/zip!zip/resource"));
        assertEquals(request.getRepoPath(), InfoFactoryHolder.get().createRepoPath("libs-releases", "path/to/zip"));
        assertEquals(request.getZipResourcePath(), "zip/resource");
    }

    public void zipResourceEmptyPath() throws UnsupportedEncodingException {
        ArtifactoryRequestBase request = newRequest();
        request.setRepoPath(request.calculateRepoPath("libs-releases/path/to/zip!"));
        assertEquals(request.getRepoPath(), InfoFactoryHolder.get().createRepoPath("libs-releases", "path/to/zip"));
        assertEquals(request.getZipResourcePath(), "",
                "Expected empty path to zip resource: " + request.getZipResourcePath());
    }

    private void assertMatrixParams(Properties params) {
        assertEquals(params.size(), 6);
        assertEquals(params.get("a").toArray(), new String[]{"1", "11"});
        assertEquals(params.get("b").iterator().next(), "2");
        assertEquals(params.get("c").iterator().next(), "");
        assertEquals(params.get("d").iterator().next(), "4");
        assertEquals(params.get("e").iterator().next(), "");
        assertFalse(params.get("f").iterator().hasNext());
    }

    private ArtifactoryRequestBase newRequest() {
        return new ArtifactoryRequestBase() {
            @Override
            public long getLastModified() {
                return 0;
            }

            @Override
            public long getIfModifiedSince() {
                return 0;
            }

            @Override
            public boolean hasIfModifiedSince() {
                return false;
            }

            @Override
            public String getClientAddress() {
                return null;
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return null;
            }

            @Override
            public boolean isHeadOnly() {
                return false;
            }

            @Override
            public boolean isRecursive() {
                return false;
            }

            @Override
            public boolean isFromAnotherArtifactory() {
                return false;
            }

            @Override
            public long getContentLength() {
                return 0;
            }

            @Override
            public String getHeader(String headerName) {
                return null;
            }

            @Override
            public Enumeration getHeaders(String headerName) {
                return null;
            }

            @Override
            public Map<String, String> getHeaders() {
                return null;
            }

            @Override
            public String getUri() {
                return null;
            }

            @Override
            public String getServletContextUrl() {
                return "";
            }
        };
    }
}
