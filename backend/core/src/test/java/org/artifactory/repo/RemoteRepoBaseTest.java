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

package org.artifactory.repo;

import org.apache.commons.io.IOUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.HaAddon;
import org.artifactory.addon.LayoutsCoreAddon;
import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.common.StatusHolder;
import org.artifactory.descriptor.repo.HttpRepoDescriptor;
import org.artifactory.io.checksum.Checksum;
import org.artifactory.repo.db.DbCacheRepo;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.spring.InternalArtifactoryContext;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.artifactory.util.RepoLayoutUtils;
import org.easymock.EasyMock;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.InputStream;

/**
 * This class tests the behaviour of the allowsDownloadMethod and checks that it returns the proper results on different
 * scenarios
 *
 * @author Noam Tenne
 */
public class RemoteRepoBaseTest extends ArtifactoryHomeBoundTest {
    AuthorizationService authService = EasyMock.createNiceMock(AuthorizationService.class);
    InternalRepositoryService internalRepoService = EasyMock.createMock(InternalRepositoryService.class);
    InternalArtifactoryContext context = EasyMock.createMock(InternalArtifactoryContext.class);
    AddonsManager addonsManager = EasyMock.createMock(AddonsManager.class);
    LayoutsCoreAddon layoutsCoreAddon = EasyMock.createMock(LayoutsCoreAddon.class);
    HaAddon haAddon = EasyMock.createMock(HaAddon.class);

    HttpRepoDescriptor httpRepoDescriptor = new HttpRepoDescriptor();
    HttpRepo httpRepo;
    RepoPath repoPath = InternalRepoPathFactory.create("remote-repo-cache", "test/test/1.0/test-1.0.jar");

    @BeforeClass
    public void setup() {
        EasyMock.expect(context.getAuthorizationService()).andReturn(authService).anyTimes();
        EasyMock.expect(context.beanForType(AddonsManager.class)).andReturn(addonsManager).anyTimes();
        EasyMock.expect(addonsManager.addonByType(LayoutsCoreAddon.class)).andReturn(layoutsCoreAddon).anyTimes();
        EasyMock.expect(addonsManager.addonByType(HaAddon.class)).andReturn(haAddon).anyTimes();
        EasyMock.replay(authService, context, addonsManager);
        ArtifactoryContextThreadBinder.bind(context);
        httpRepoDescriptor.setRepoLayout(RepoLayoutUtils.MAVEN_2_DEFAULT);
        httpRepo = new HttpRepo(httpRepoDescriptor, internalRepoService, false, null);
    }

    /**
     * Try to download with ordinary path
     */
    @Test
    public void testDownloadWithCachePrefix() {
        expectCanRead(true);
        createCacheRepo();
        StatusHolder statusHolder = tryAllowsDownload(repoPath);
        Assert.assertFalse(statusHolder.isError());
    }

    /**
     * Try to download while supplying a path with no "-cache" prefix
     */
    @Test
    public void testDownloadWithNoCachePrefix() {
        RepoPath repoPathNoPrefix = InternalRepoPathFactory.create("remote-repo", "test/test/1.0/test-1.0.jar");
        expectCanRead(true);
        createCacheRepo();
        StatusHolder statusHolder = tryAllowsDownload(repoPathNoPrefix);
        Assert.assertFalse(statusHolder.isError());
    }

    /**
     * Try to download when Anonymous mode is enabled
     */
    @Test
    public void testDownloadWithAnonymousEnabledAndNoReadPermissions() {
        expectCanRead(false);       // don't give read access
        createCacheRepo();   // and enable anon
        StatusHolder statusHolder = tryAllowsDownload(repoPath);
        Assert.assertTrue(statusHolder.isError());
    }

    /**
     * Try to download when the repo is blacked out
     */
    @Test
    public void testDownloadWithBlackedOut() {
        httpRepoDescriptor.setBlackedOut(true);
        createCacheRepo();
        StatusHolder statusHolder = tryAllowsDownload(repoPath);
        Assert.assertTrue(statusHolder.isError());
        httpRepoDescriptor.setBlackedOut(false);
    }

    /**
     * Try to download when the repo does not serve releases
     */
    @Test
    public void testDownloadWithNoReleases() {
        httpRepoDescriptor.setHandleReleases(false);
        createCacheRepo();
        StatusHolder statusHolder = tryAllowsDownload(repoPath);
        Assert.assertTrue(statusHolder.isError());
        httpRepoDescriptor.setHandleReleases(true);
    }

    /**
     * Try to download when requested resource is excluded
     */
    @Test
    public void testDownloadWithExcludes() {
        httpRepoDescriptor.setExcludesPattern("test/test/1.0/**");
        createCacheRepo();
        StatusHolder statusHolder = tryAllowsDownload(repoPath);
        Assert.assertTrue(statusHolder.isError());
        httpRepoDescriptor.setExcludesPattern("");
    }

    /**
     * Verify reading of valid checksum file to test the {@link Checksum#checksumStringFromStream(java.io.InputStream)}
     * method
     */
    @Test
    public void testReadValidChecksum() throws Exception {
        invokeReadChecksum("valid", "dasfasdf4r234234q32asdfadfasasdfasdf");
    }

    /**
     * Verify reading of a checksum file containing comments to test the {@link Checksum#checksumStringFromStream(java.io.InputStream)}
     * method
     */
    @Test
    public void testReadCommentChecksum() throws Exception {
        invokeReadChecksum("comment", "asdfaeaef435345435asdf");
    }

    /**
     * Verify reading of a checksum file containing file description to test the {@link
     * Checksum#checksumStringFromStream(java.io.InputStream)} method
     */
    @Test
    public void testReadDescChecksum() throws Exception {
        invokeReadChecksum("desc", "dasfasdf4r234234q32asdfadfasdf");
    }

    /**
     * Verify reading of an empty checksum file to test the {@link Checksum#checksumStringFromStream(java.io.InputStream)}
     * method
     */
    @Test
    public void testReadEmptyChecksum() throws Exception {
        invokeReadChecksum("empty", "");
    }

    /**
     * Check the different values that are read from the given checksum are as expected
     *
     * @param checksumType  Type of checksum test file to read
     * @param expectedValue Expected checksum value
     */
    private void invokeReadChecksum(String checksumType, String expectedValue) throws Exception {
        InputStream stream = getClass().getResourceAsStream("/org/artifactory/repo/test-" + checksumType + ".md5");
        try {
            String checksum = Checksum.checksumStringFromStream(stream);
            Assert.assertEquals(expectedValue, checksum, "Incorrect checksum value returned.");
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    private void createCacheRepo() {
        httpRepoDescriptor.setKey("remote-repo");
        DbCacheRepo localCacheRepo = new DbCacheRepo(httpRepo, null);
        ReflectionTestUtils.setField(httpRepo, "localCacheRepo", localCacheRepo);
    }

    private void expectCanRead(boolean canRead) {
        EasyMock.expect(authService.canRead(repoPath)).andReturn(canRead);
        EasyMock.replay(authService);
    }

    /**
     * Try to download the the given path
     *
     * @param path Repopath to test the download with
     * @return StatusHolder - Contains any errors which might be monitored
     */
    private StatusHolder tryAllowsDownload(RepoPath path) {
        StatusHolder statusHolder = httpRepo.checkDownloadIsAllowed(path);
        return statusHolder;
    }

    /**
     * Resets the mock objects after every test method to make sure the numbers of the expected method calls are exact
     */
    @BeforeMethod
    private void resetMocks() {
        EasyMock.reset(authService);
    }
}
