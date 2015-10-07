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

package org.artifactory.repo.interceptor;

import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.NuGetAddon;
import org.artifactory.addon.nuget.NuGetProperties;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.common.MutableStatusHolder;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.fs.FileInfo;
import org.artifactory.md.Properties;
import org.artifactory.model.xstream.fs.PropertiesImpl;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.sapi.fs.VfsFile;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.*;

/**
 * @author Noam Y. Tenne
 */
@Test
public class NuGetCalculationInterceptorTest {

    private NuGetCalculationInterceptor nuGetCalculationInterceptor = new NuGetCalculationInterceptor();
    private NuGetAddon nuGetAddon;

    @BeforeClass
    public void setUp() {
    }

    @BeforeMethod
    public void beforeMethod() {
        nuGetCalculationInterceptor.addonsManager = createMock(AddonsManager.class);
        nuGetAddon = createMock(NuGetAddon.class);
        expect(nuGetCalculationInterceptor.addonsManager.addonByType(NuGetAddon.class))
                .andReturn(nuGetAddon).anyTimes();
    }

    @Test
    public void testNotNuGetFile() {
        VfsFile vfsItemMock = createAndGetFsItemMock(RepoPathFactory.create("bob", "bob.jar"));
        replay(vfsItemMock);

        nuGetCalculationInterceptor.afterCopy(null, vfsItemMock, null, null);
        nuGetCalculationInterceptor.afterCreate(vfsItemMock, null);
        nuGetCalculationInterceptor.afterMove(null, vfsItemMock, null, null);
        nuGetCalculationInterceptor.beforeDelete(vfsItemMock, null);

        verify(vfsItemMock);
    }

    @Test
    public void testNotNuGetRepo() {
        RepositoryService repoServiceMock = createAndSetMockRepo(false, "bob");

        VfsFile vfsItemMock = createAndGetFsItemMock(RepoPathFactory.create("bob", "bob.nupkg"));
        replay(vfsItemMock, repoServiceMock);

        nuGetCalculationInterceptor.afterCopy(null, vfsItemMock, null, null);
        nuGetCalculationInterceptor.afterCreate(vfsItemMock, null);
        nuGetCalculationInterceptor.afterMove(null, vfsItemMock, null, null);
        nuGetCalculationInterceptor.beforeDelete(vfsItemMock, null);

        verify(vfsItemMock, repoServiceMock);
    }

    @Test
    public void testAfterCreate() {
        RepositoryService repoServiceMock = createAndSetMockRepo(true, "bob");
        RepoPath repoPath = RepoPathFactory.create("bob", "bob.nupkg");
        VfsFile vfsItemMock = createAndGetFsItemMock(repoPath);
        FileInfo fileInfoMock = createAndGetFileInfoMock(repoPath);
        expect(vfsItemMock.getInfo()).andReturn(fileInfoMock).anyTimes();
        nuGetAddon.extractNuPkgInfo(eq(fileInfoMock), isA(MutableStatusHolder.class), eq(true));
        expectLastCall();
        replay(vfsItemMock, repoServiceMock, nuGetCalculationInterceptor.addonsManager, nuGetAddon);
        nuGetCalculationInterceptor.afterCreate(vfsItemMock, new BasicStatusHolder());
        verify(vfsItemMock, repoServiceMock, nuGetCalculationInterceptor.addonsManager, nuGetAddon);
    }

    @Test
    public void testAfterMoveOrCopyNoIdProperty() {
        RepositoryService repoServiceMock = createAndSetMockRepo(true, "bob");
        RepoPath itemRepoPath = RepoPathFactory.create("bob", "bob.nupkg");
        FileInfo fileInfoMock = createAndGetFileInfoMock(itemRepoPath);
        VfsFile vfsItemMock = createAndGetFsItemMock(itemRepoPath);
        expect(vfsItemMock.getInfo()).andReturn(fileInfoMock).anyTimes();
        setVfsFileMockProperties(vfsItemMock, null);

        replay(vfsItemMock, repoServiceMock, nuGetCalculationInterceptor.addonsManager);
        nuGetCalculationInterceptor.afterCopy(null, vfsItemMock, null, null);
        nuGetCalculationInterceptor.afterMove(null, vfsItemMock, null, null);
        verify(vfsItemMock, repoServiceMock, nuGetCalculationInterceptor.addonsManager);
    }

    @Test
    public void testAfterCopy() {
        RepositoryService repoServiceMock = createAndSetMockRepo(true, "bob");
        RepoPath itemRepoPath = RepoPathFactory.create("bob", "bob.nupkg");
        VfsFile vfsItemMock = createAndGetFsItemMock(itemRepoPath);
        setVfsFileMockProperties(vfsItemMock, "bobsId");

        replay(vfsItemMock, repoServiceMock, nuGetCalculationInterceptor.addonsManager);
        nuGetCalculationInterceptor.afterCopy(null, vfsItemMock, null, null);
        verify(vfsItemMock, repoServiceMock, nuGetCalculationInterceptor.addonsManager);
    }

    @Test
    public void testAfterMoveNoNuGetProperties() {
        RepositoryService repoServiceMock = createAndSetMockRepo(true, "bob");
        RepoPath targetRepoPath = RepoPathFactory.create("bob", "bob.nupkg");
        FileInfo fileInfoMock = createAndGetFileInfoMock(targetRepoPath);
        VfsFile targetVfsFile = createAndGetFsItemMock(targetRepoPath);
        expect(targetVfsFile.getInfo()).andReturn(fileInfoMock).anyTimes();
        setVfsFileMockProperties(targetVfsFile, null);

        BasicStatusHolder statusHolder = new BasicStatusHolder();
        nuGetAddon.extractNuPkgInfo(eq(fileInfoMock), eq(statusHolder), eq(true));
        expectLastCall();

        replay(targetVfsFile, repoServiceMock, nuGetCalculationInterceptor.addonsManager);
        nuGetCalculationInterceptor.afterMove(targetVfsFile, targetVfsFile, statusHolder, null);
        verify(targetVfsFile, repoServiceMock, nuGetCalculationInterceptor.addonsManager);
    }

    @Test
    public void testAfterImport() throws Exception {
        RepositoryService repoServiceMock = createAndSetMockRepo(true, "bob");
        RepoPath itemRepoPath = RepoPathFactory.create("bob", "bob.nupkg");
        FileInfo fileInfoMock = createAndGetFileInfoMock(itemRepoPath);
        VfsFile vfsItemMock = createAndGetFsItemMock(itemRepoPath);
        expect(vfsItemMock.getInfo()).andReturn(fileInfoMock).anyTimes();
        BasicStatusHolder statusHolder = new BasicStatusHolder();

        nuGetAddon.extractNuPkgInfo(eq(fileInfoMock), eq(statusHolder), eq(true));
        expectLastCall();

        replay(vfsItemMock, repoServiceMock, nuGetCalculationInterceptor.addonsManager, nuGetAddon);
        nuGetCalculationInterceptor.afterImport(vfsItemMock, statusHolder);
        verify(vfsItemMock, repoServiceMock, nuGetCalculationInterceptor.addonsManager, nuGetAddon);
    }

    private VfsFile createAndGetFsItemMock(RepoPath repoPath) {
        VfsFile vfsItemMock = createMock(VfsFile.class);
        expect(vfsItemMock.isFile()).andReturn(true).anyTimes();
        expect(vfsItemMock.getRepoPath()).andReturn(repoPath).anyTimes();
        return vfsItemMock;
    }

    private FileInfo createAndGetFileInfoMock(RepoPath repoPath) {
        FileInfo fileInfoMock = createMock(FileInfo.class);
        expect(fileInfoMock.getRepoPath()).andReturn(repoPath).anyTimes();
        return fileInfoMock;
    }

    private RepositoryService createAndSetMockRepo(boolean nuGetSupported, String repoKey) {
        RepositoryService repoServiceMock = createMock(RepositoryService.class);
        LocalRepoDescriptor localRepoDescriptor = new LocalRepoDescriptor();
        if(nuGetSupported) {
            localRepoDescriptor.setType(RepoType.NuGet);
        }
        expect(repoServiceMock.repoDescriptorByKey(repoKey)).andReturn(localRepoDescriptor).anyTimes();
        nuGetCalculationInterceptor.repositoryService = repoServiceMock;
        return repoServiceMock;
    }

    private void setVfsFileMockProperties(VfsFile vfsFile, String nugetId) {
        Properties toReturn = new PropertiesImpl();
        if (!StringUtils.isBlank(nugetId)) {
            toReturn.put(NuGetProperties.Id.nodePropertyName(), nugetId);
        }

        expect(vfsFile.getProperties()).andReturn(toReturn).anyTimes();
    }
}
