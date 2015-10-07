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

import org.apache.commons.io.input.NullInputStream;
import org.artifactory.factory.InfoFactory;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.md.Properties;
import org.artifactory.repo.RepoPath;
import org.artifactory.request.ArtifactoryRequest;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.artifactory.util.PathUtils;
import org.easymock.EasyMock;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.InputStream;

import static org.testng.Assert.*;

/**
 * Tests the specialized translated artifactory request. Makes sure that special actions are taken where needed and
 * otherwise delegates to the original request object
 *
 * @author Noam Y. Tenne
 */
public class TranslatedArtifactoryRequestTest extends ArtifactoryHomeBoundTest {

    private RepoPath translatedRepoPath;
    private RepoPath translatedMetadataRepoPath;
    private RepoPath translatedChecksumRepoPath;

    private TranslatedArtifactoryRequest translatedArtifactoryRequest;
    private TranslatedArtifactoryRequest translatedMetadataArtifactoryRequest;
    private TranslatedArtifactoryRequest translatedChecksumArtifactoryRequest;

    protected final InfoFactory factory = InfoFactoryHolder.get();
    private Properties properties = (Properties) factory.createProperties();
    private String[] parameterValues = new String[]{"vals"};
    private InputStream inputStream = new NullInputStream(0);

    @BeforeClass
    public void setUp() throws Exception {
        ArtifactoryRequest artifactoryRequestMock = EasyMock.createMock(ArtifactoryRequest.class);
        EasyMock.expect(artifactoryRequestMock.getClientAddress()).andReturn("sourceDescription").anyTimes();
        EasyMock.expect(artifactoryRequestMock.isRecursive()).andReturn(true).anyTimes();
        EasyMock.expect(artifactoryRequestMock.getModificationTime()).andReturn(2013944l).anyTimes();
        EasyMock.expect(artifactoryRequestMock.isFromAnotherArtifactory()).andReturn(false).anyTimes();
        EasyMock.expect(artifactoryRequestMock.isHeadOnly()).andReturn(true).anyTimes();
        EasyMock.expect(artifactoryRequestMock.getLastModified()).andReturn(563752346l).anyTimes();
        EasyMock.expect(artifactoryRequestMock.getIfModifiedSince()).andReturn(22076l).anyTimes();
        EasyMock.expect(artifactoryRequestMock.isNewerThan(-1l)).andReturn(false).anyTimes();
        EasyMock.expect(artifactoryRequestMock.getHeader("header")).andReturn("bob").anyTimes();
        EasyMock.expect(artifactoryRequestMock.getServletContextUrl()).andReturn("url").anyTimes();
        EasyMock.expect(artifactoryRequestMock.getUri()).andReturn("uri").anyTimes();
        EasyMock.expect(artifactoryRequestMock.getProperties()).andReturn(properties).anyTimes();
        EasyMock.expect(artifactoryRequestMock.hasProperties()).andReturn(true).anyTimes();
        EasyMock.expect(artifactoryRequestMock.getParameter("parameter")).andReturn("moo").anyTimes();
        EasyMock.expect(artifactoryRequestMock.getParameterValues("parameters")).andReturn(parameterValues).anyTimes();
        EasyMock.expect(artifactoryRequestMock.getInputStream()).andReturn(inputStream).anyTimes();
        EasyMock.expect(artifactoryRequestMock.getContentLength()).andReturn(1337L).anyTimes();
        EasyMock.expect(artifactoryRequestMock.getZipResourcePath()).andReturn(null).anyTimes();
        EasyMock.replay(artifactoryRequestMock);

        InfoFactory factory = this.factory;
        translatedRepoPath = factory.createRepoPath("translatedRepoKey", "translatedRepoPath");
        translatedMetadataRepoPath = factory.createRepoPath("translatedRepoKey",
                "translatedRepoPath:maven-metadata.xml");
        translatedChecksumRepoPath = factory.createRepoPath("translatedRepoKey", "translatedRepoPath.md5");

        translatedArtifactoryRequest = new TranslatedArtifactoryRequest(translatedRepoPath, artifactoryRequestMock);
        translatedMetadataArtifactoryRequest = new TranslatedArtifactoryRequest(translatedMetadataRepoPath,
                artifactoryRequestMock);
        translatedChecksumArtifactoryRequest = new TranslatedArtifactoryRequest(translatedChecksumRepoPath,
                artifactoryRequestMock);
    }

    @Test
    public void testGetRepoKey() throws Exception {
        assertEquals(translatedArtifactoryRequest.getRepoKey(), translatedRepoPath.getRepoKey());
        assertEquals(translatedMetadataArtifactoryRequest.getRepoKey(), translatedMetadataRepoPath.getRepoKey());
        assertEquals(translatedChecksumArtifactoryRequest.getRepoKey(), translatedChecksumRepoPath.getRepoKey());
    }

    @Test
    public void testGetPath() throws Exception {
        assertEquals(translatedArtifactoryRequest.getPath(), translatedRepoPath.getPath());
        assertEquals(translatedMetadataArtifactoryRequest.getPath(), translatedMetadataRepoPath.getPath());
        assertEquals(translatedChecksumArtifactoryRequest.getPath(), translatedChecksumRepoPath.getPath());
    }

    @Test
    public void testGetSourceDescription() throws Exception {
        assertEquals(translatedArtifactoryRequest.getClientAddress(), "sourceDescription");
        assertEquals(translatedMetadataArtifactoryRequest.getClientAddress(), "sourceDescription");
        assertEquals(translatedChecksumArtifactoryRequest.getClientAddress(), "sourceDescription");
    }

    @Test
    public void testIsMetadata() throws Exception {
        assertFalse(translatedArtifactoryRequest.isMetadata());
        assertTrue(translatedMetadataArtifactoryRequest.isMetadata());
        assertFalse(translatedChecksumArtifactoryRequest.isMetadata());
    }

    @Test
    public void testIsRecursive() throws Exception {
        assertTrue(translatedArtifactoryRequest.isRecursive());
        assertTrue(translatedMetadataArtifactoryRequest.isRecursive());
        assertTrue(translatedChecksumArtifactoryRequest.isRecursive());
    }

    @Test
    public void testGetModificationTime() throws Exception {
        assertEquals(translatedArtifactoryRequest.getModificationTime(), 2013944l);
        assertEquals(translatedMetadataArtifactoryRequest.getModificationTime(), 2013944l);
        assertEquals(translatedChecksumArtifactoryRequest.getModificationTime(), 2013944l);
    }

    @Test
    public void testGetName() throws Exception {
        assertEquals(translatedArtifactoryRequest.getName(), PathUtils.getFileName(translatedRepoPath.getPath()));
        assertEquals(translatedMetadataArtifactoryRequest.getName(),
                PathUtils.getFileName(translatedMetadataRepoPath.getPath()));
        assertEquals(translatedChecksumArtifactoryRequest.getName(),
                PathUtils.getFileName(translatedChecksumRepoPath.getPath()));
    }

    @Test
    public void testGetRepoPath() throws Exception {
        assertEquals(translatedArtifactoryRequest.getRepoPath(), translatedRepoPath);
        assertEquals(translatedMetadataArtifactoryRequest.getRepoPath(), translatedMetadataRepoPath);
        assertEquals(translatedChecksumArtifactoryRequest.getRepoPath(), translatedChecksumRepoPath);
    }

    @Test
    public void testIsChecksum() throws Exception {
        assertFalse(translatedArtifactoryRequest.isChecksum());
        assertFalse(translatedMetadataArtifactoryRequest.isChecksum());
        assertTrue(translatedChecksumArtifactoryRequest.isChecksum());
    }

    @Test
    public void testIsFromAnotherArtifactory() throws Exception {
        assertFalse(translatedArtifactoryRequest.isFromAnotherArtifactory());
        assertFalse(translatedMetadataArtifactoryRequest.isFromAnotherArtifactory());
        assertFalse(translatedChecksumArtifactoryRequest.isFromAnotherArtifactory());
    }

    @Test
    public void testIsHeadOnly() throws Exception {
        assertTrue(translatedArtifactoryRequest.isHeadOnly());
        assertTrue(translatedMetadataArtifactoryRequest.isHeadOnly());
        assertTrue(translatedChecksumArtifactoryRequest.isHeadOnly());
    }

    @Test
    public void testGetLastModified() throws Exception {
        assertEquals(translatedArtifactoryRequest.getLastModified(), 563752346l);
        assertEquals(translatedMetadataArtifactoryRequest.getLastModified(), 563752346l);
        assertEquals(translatedChecksumArtifactoryRequest.getLastModified(), 563752346l);
    }

    @Test
    public void testGetIfModifiedSince() throws Exception {
        assertEquals(translatedArtifactoryRequest.getIfModifiedSince(), 22076l);
        assertEquals(translatedMetadataArtifactoryRequest.getIfModifiedSince(), 22076l);
        assertEquals(translatedChecksumArtifactoryRequest.getIfModifiedSince(), 22076l);
    }

    @Test
    public void testIsNewerThan() throws Exception {
        assertFalse(translatedArtifactoryRequest.isNewerThan(-1l));
        assertFalse(translatedMetadataArtifactoryRequest.isNewerThan(-1l));
        assertFalse(translatedChecksumArtifactoryRequest.isNewerThan(-1l));
    }

    @Test
    public void testGetHeader() throws Exception {
        assertEquals(translatedArtifactoryRequest.getHeader("header"), "bob");
        assertEquals(translatedMetadataArtifactoryRequest.getHeader("header"), "bob");
        assertEquals(translatedChecksumArtifactoryRequest.getHeader("header"), "bob");
    }

    @Test
    public void testGetServletContextUrl() throws Exception {
        assertEquals(translatedArtifactoryRequest.getServletContextUrl(), "url");
        assertEquals(translatedMetadataArtifactoryRequest.getServletContextUrl(), "url");
        assertEquals(translatedChecksumArtifactoryRequest.getServletContextUrl(), "url");
    }

    @Test
    public void testGetUri() throws Exception {
        assertEquals(translatedArtifactoryRequest.getUri(), "uri");
        assertEquals(translatedMetadataArtifactoryRequest.getUri(), "uri");
        assertEquals(translatedChecksumArtifactoryRequest.getUri(), "uri");
    }

    @Test
    public void testGetProperties() throws Exception {
        assertEquals(translatedArtifactoryRequest.getProperties(), properties);
        assertEquals(translatedMetadataArtifactoryRequest.getProperties(), properties);
        assertEquals(translatedChecksumArtifactoryRequest.getProperties(), properties);
    }

    @Test
    public void testHasProperties() throws Exception {
        assertTrue(translatedArtifactoryRequest.hasProperties());
        assertTrue(translatedMetadataArtifactoryRequest.hasProperties());
        assertTrue(translatedChecksumArtifactoryRequest.hasProperties());
    }

    @Test
    public void testGetParameter() throws Exception {
        assertEquals(translatedArtifactoryRequest.getParameter("parameter"), "moo");
        assertEquals(translatedMetadataArtifactoryRequest.getParameter("parameter"), "moo");
        assertEquals(translatedChecksumArtifactoryRequest.getParameter("parameter"), "moo");
    }

    @Test
    public void testGetParameterValues() throws Exception {
        assertEquals(translatedArtifactoryRequest.getParameterValues("parameters"), parameterValues);
        assertEquals(translatedMetadataArtifactoryRequest.getParameterValues("parameters"), parameterValues);
        assertEquals(translatedChecksumArtifactoryRequest.getParameterValues("parameters"), parameterValues);
    }

    @Test
    public void testGetInputStream() throws Exception {
        assertEquals(translatedArtifactoryRequest.getInputStream(), inputStream);
        assertEquals(translatedMetadataArtifactoryRequest.getInputStream(), inputStream);
        assertEquals(translatedChecksumArtifactoryRequest.getInputStream(), inputStream);
    }

    @Test
    public void testGetContentLength() throws Exception {
        assertEquals(translatedArtifactoryRequest.getContentLength(), 1337);
        assertEquals(translatedMetadataArtifactoryRequest.getContentLength(), 1337);
        assertEquals(translatedChecksumArtifactoryRequest.getContentLength(), 1337);
    }

    @Test
    public void testToString() throws Exception {
        assertEquals(translatedArtifactoryRequest.toString(),
                "source=sourceDescription, path=translatedRepoPath, lastModified=563752346, ifModifiedSince=22076");
        assertEquals(translatedMetadataArtifactoryRequest.toString(), "source=sourceDescription, " +
                "path=translatedRepoPath:maven-metadata.xml, lastModified=563752346, ifModifiedSince=22076");
        assertEquals(translatedChecksumArtifactoryRequest.toString(), "source=sourceDescription, " +
                "path=translatedRepoPath.md5, lastModified=563752346, ifModifiedSince=22076");
    }
}
