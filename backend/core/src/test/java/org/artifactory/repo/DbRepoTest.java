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

import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.common.StatusHolder;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.repo.db.DbLocalRepo;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.spring.InternalArtifactoryContext;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.artifactory.util.RepoLayoutUtils;
import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for the DbRepo.
 *
 * @author Noam Tenne
 */
public class DbRepoTest extends ArtifactoryHomeBoundTest {
    @Test
    public void testNoFilePermission() {
        AuthorizationService authService = EasyMock.createMock(AuthorizationService.class);

        InternalArtifactoryContext context = EasyMock.createMock(InternalArtifactoryContext.class);
        EasyMock.expect(context.getAuthorizationService()).andReturn(authService);
        EasyMock.replay(context);
        ArtifactoryContextThreadBinder.bind(context);

        InternalRepositoryService irs = EasyMock.createMock(InternalRepositoryService.class);
        EasyMock.replay(irs);

        LocalRepoDescriptor lrd = new LocalRepoDescriptor();
        lrd.setKey("libs");
        lrd.setRepoLayout(RepoLayoutUtils.MAVEN_2_DEFAULT);
        DbLocalRepo dbLocalRepo = new DbLocalRepo<>(lrd, irs, null);
        RepoPath path = InternalRepoPathFactory.create("libs", "jfrog/settings/jfrog-settings-sources.zip");
        EasyMock.expect(authService.canRead(path)).andReturn(false);
        EasyMock.expect(authService.currentUsername()).andReturn("testUser");
        EasyMock.replay(authService);

        StatusHolder holder = dbLocalRepo.checkDownloadIsAllowed(path);
        Assert.assertTrue(holder.isError(), "User should not have access to src files");
        EasyMock.verify(context, authService, irs);
    }
}
