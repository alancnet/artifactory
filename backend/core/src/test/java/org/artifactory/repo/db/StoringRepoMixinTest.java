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

package org.artifactory.repo.db;

import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.api.repo.exception.ItemNotFoundRuntimeException;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.fs.FileInfo;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.LocalCacheRepo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.cache.expirable.CacheExpiry;
import org.artifactory.repo.local.LocalNonCacheOverridable;
import org.artifactory.repo.local.PathDeletionContext;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.lang.reflect.Field;

import static org.easymock.EasyMock.*;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Noam Y. Tenne
 */
public class StoringRepoMixinTest extends ArtifactoryHomeBoundTest {

    LocalCacheRepo storingRepo = createMock(LocalCacheRepo.class);
    ArtifactoryContext context = createMock(ArtifactoryContext.class);
    LocalRepoDescriptor localRepoDescriptor = new LocalRepoDescriptor();

    {
        localRepoDescriptor.setKey("somekey");
    }

    DbStoringRepoMixin storingRepoMixin = new DbStoringRepoMixin(localRepoDescriptor, null);

    @BeforeClass
    public void setUp() throws Exception {
        ArtifactoryContextThreadBinder.bind(context);
    }

    @Test
    public void testChecksumProtection() throws Exception {
        for (ChecksumType checksumType : ChecksumType.BASE_CHECKSUM_TYPES) {
            PathDeletionContext deletionContext = new PathDeletionContext.Builder(storingRepo,
                    checksumType.ext()).assertOverwrite(true).build();
            assertFalse(storingRepoMixin.shouldProtectPathDeletion(deletionContext),
                    "Checksum should never be protected.");
            deletionContext = new PathDeletionContext.Builder(storingRepo, checksumType.ext())
                    .assertOverwrite(false).build();
            assertFalse(storingRepoMixin.shouldProtectPathDeletion(deletionContext),
                    "Checksum should never be protected.");
        }
    }

    @Test
    public void testCacheAndExpirableProtection() throws Exception {
        PathDeletionContext deletionContext = new PathDeletionContext.Builder(storingRepo, "somepath")
                .assertOverwrite(false).build();
        assertTrue(storingRepoMixin.shouldProtectPathDeletion(deletionContext),
                "Non-checksum item should never be protected when not overriding.");
        expect(storingRepo.isCache()).andReturn(true);
        CacheExpiry expiry = createMock(CacheExpiry.class);
        expect(expiry.isExpirable(eq(storingRepo), eq("somepath"))).andReturn(true);
        expect(context.beanForType(CacheExpiry.class)).andReturn(expiry);
        InternalRepositoryService repositoryService = createMock(InternalRepositoryService.class);
        fieldSet(storingRepoMixin, "repositoryService", repositoryService);
        expect(repositoryService.storingRepositoryByKey("somekey")).andReturn(storingRepo);
        replay(expiry, context, storingRepo, repositoryService);
        deletionContext = new PathDeletionContext.Builder(storingRepo, "somepath")
                .assertOverwrite(true).build();
        assertFalse(storingRepoMixin.shouldProtectPathDeletion(deletionContext),
                "Expired path shouldn't be protected.");
        verify(expiry, context, storingRepo);
        reset(context, storingRepo);
    }

    @Test
    public void testLocalNotCacheAndOverridableProtection() throws Exception {
        expect(storingRepo.isCache()).andReturn(false);
        LocalNonCacheOverridable overridable = createMock(LocalNonCacheOverridable.class);
        expect(overridable.isOverridable(eq(storingRepo), eq("somepath"))).andReturn(true);
        expect(context.beanForType(LocalNonCacheOverridable.class)).andReturn(overridable);
        InternalRepositoryService repositoryService = createMock(InternalRepositoryService.class);
        fieldSet(storingRepoMixin, "repositoryService", repositoryService);
        expect(repositoryService.storingRepositoryByKey("somekey")).andReturn(storingRepo);

        replay(overridable, context, storingRepo, repositoryService);
        PathDeletionContext deletionContext = new PathDeletionContext.Builder(storingRepo, "somepath")
                .assertOverwrite(true).build();
        assertFalse(storingRepoMixin.shouldProtectPathDeletion(deletionContext),
                "Overridable path shouldn't be protected.");
        verify(overridable, context, storingRepo);
        reset(context, storingRepo);
    }

    @Test
    public void testLocalNotCacheAndMetadataProtection() throws Exception {
        //expect(storingRepo.isCache()).andReturn(false).times(2);
        LocalNonCacheOverridable overridable = createMock(LocalNonCacheOverridable.class);
        //expect(overridable.isOverridable(eq(storingRepo), eq("maven-metadata.xml")))
        //        .andReturn(false);
        //expect(overridable.isOverridable(eq(storingRepo), eq("some:metadata.xml")))
        //        .andReturn(false);
        //expect(context.beanForType(LocalNonCacheOverridable.class)).andReturn(overridable);
        InternalRepositoryService repositoryService = createMock(InternalRepositoryService.class);
        fieldSet(storingRepoMixin, "repositoryService", repositoryService);
        expect(repositoryService.storingRepositoryByKey("somekey")).andReturn(storingRepo);

        replay(overridable, context, storingRepo, repositoryService);
        PathDeletionContext deletionContext = new PathDeletionContext.Builder(storingRepo, "maven-metadata.xml")
                .assertOverwrite(true).build();
        assertFalse(storingRepoMixin.shouldProtectPathDeletion(deletionContext),
                "Metadata path shouldn't be protected.");
        //assertFalse(storingRepoMixin.shouldProtectPathDeletion("some:metadata.xml", true, null),
        //        "Metadata path shouldn't be protected.");
        verify(overridable, context, storingRepo);
        reset(context, storingRepo);
    }

    @Test
    public void testLocalNotCacheAndNotFileProtection() throws Exception {
        expect(storingRepo.isCache()).andReturn(false);
        LocalNonCacheOverridable overridable = createMock(LocalNonCacheOverridable.class);
        expect(overridable.isOverridable(eq(storingRepo), eq("somefile"))).andReturn(false);
        expect(context.beanForType(LocalNonCacheOverridable.class)).andReturn(overridable);
        InternalRepositoryService repositoryService = createMock(InternalRepositoryService.class);
        expect(repositoryService.exists(eq(InternalRepoPathFactory.create("somekey", "somefile"))))
                .andReturn(false);
        expect(repositoryService.storingRepositoryByKey(eq("somekey"))).andReturn(storingRepo);
        fieldSet(storingRepoMixin, "repositoryService", repositoryService);
        replay(overridable, repositoryService, context, storingRepo);
        PathDeletionContext deletionContext = new PathDeletionContext.Builder(storingRepo, "somefile")
                .assertOverwrite(true).build();
        assertFalse(storingRepoMixin.shouldProtectPathDeletion(deletionContext),
                "Items which aren't files shouldn't be protected.");
        verify(overridable, repositoryService, context, storingRepo);
        reset(context, storingRepo);
    }

    @Test
    public void allowOverrideOfAnArtifactWithProvidedChecksumIfExistingFileHasTheSameChecksum() throws Exception {

        InternalRepositoryService repositoryService = createMock(InternalRepositoryService.class);
        fieldSet(storingRepoMixin, "repositoryService", repositoryService);
        FileInfo fileInfo = createMock(FileInfo.class);

        final RepoPath repoPath = InternalRepoPathFactory.create("somekey", "path");

        //
        expect(repositoryService.exists(eq(repoPath))).andReturn(true);
        replay(storingRepo, repositoryService);
        assertTrue(storingRepoMixin.shouldProtectPathDeletion("path", null),
                "delete protection when not deploy with checksum for file");
        verify(storingRepo, repositoryService);
        reset(storingRepo, repositoryService);

        //
        expect(repositoryService.exists(eq(repoPath))).andReturn(false);
        replay(storingRepo, repositoryService);
        assertFalse(storingRepoMixin.shouldProtectPathDeletion("path", null),
                "no delete protection when not deploy with checksum for non file");
        verify(storingRepo, repositoryService);
        reset(storingRepo, repositoryService);

        //
        expect(repositoryService.exists(eq(repoPath))).andReturn(false);
        replay(storingRepo, repositoryService);
        assertFalse(storingRepoMixin.shouldProtectPathDeletion("path", null),
                "no delete protection when not deploy with checksum for non file");
        verify(storingRepo, repositoryService);
        reset(storingRepo, repositoryService);

        //
        expect(repositoryService.getFileInfo(eq(repoPath))).andThrow(new ItemNotFoundRuntimeException("aha"));
        replay(storingRepo, repositoryService);
        assertFalse(storingRepoMixin.shouldProtectPathDeletion("path", "non-null"),
                "no delete protection when item not found in repo path");
        verify(storingRepo, repositoryService);
        reset(storingRepo, repositoryService);

        //
        expect(repositoryService.getFileInfo(repoPath)).andReturn(fileInfo);
        expect(fileInfo.getSha1()).andReturn("sha1");
        replay(storingRepo, repositoryService, fileInfo);
        assertFalse(storingRepoMixin.shouldProtectPathDeletion("path", "sha1"),
                "no delete protection when deploy with checksum that matches existing path checksum");
        verify(storingRepo, repositoryService, fileInfo);
        reset(storingRepo, repositoryService, fileInfo);

        //
        expect(repositoryService.getFileInfo(repoPath)).andReturn(fileInfo);
        expect(fileInfo.getSha1()).andReturn("sha1");
        replay(storingRepo, repositoryService, fileInfo);
        assertTrue(storingRepoMixin.shouldProtectPathDeletion("path", "sha2"),
                "delete protection when deploy with checksum that does not match existing path checksum");
        verify(storingRepo, repositoryService, fileInfo);
        reset(storingRepo, repositoryService, fileInfo);
    }

    private static void fieldSet(Object object, String field, Object value) throws NoSuchFieldException,
            IllegalAccessException {
        Field jcrServiceField = object.getClass().getDeclaredField(field);
        jcrServiceField.setAccessible(true);
        jcrServiceField.set(object, value);
    }
}
