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

package org.artifactory.security;

import org.artifactory.api.security.SecurityListener;
import org.artifactory.api.security.UserInfoBuilder;
import org.artifactory.common.ConstantValues;
import org.artifactory.config.InternalCentralConfigService;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.security.SecurityDescriptor;
import org.artifactory.descriptor.security.ldap.LdapSetting;
import org.artifactory.factory.InfoFactory;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.LocalRepo;
import org.artifactory.repo.RemoteRepo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.repo.virtual.VirtualRepo;
import org.artifactory.storage.security.service.AclStoreService;
import org.artifactory.storage.security.service.UserGroupStoreService;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.easymock.EasyMock;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.easymock.EasyMock.*;
import static org.testng.Assert.*;

/**
 * SecurityServiceImpl unit tests. TODO: simplify the tests
 *
 * @author Yossi Shaul
 */
@Test
public class SecurityServiceImplTest extends ArtifactoryHomeBoundTest {
    private InfoFactory factory = InfoFactoryHolder.get();
    private SecurityContextImpl securityContext;
    private SecurityServiceImpl service;
    private List<AclInfo> testAcls;
    private AclStoreService aclStoreServiceMock;
    private String userAndGroupSharedName;
    private List<PermissionTargetInfo> permissionTargets;
    private InternalRepositoryService repositoryServiceMock;
    private LocalRepo localRepoMock;
    private LocalRepo cacheRepoMock;
    private VirtualRepo globalVirtualRepoMock;
    private InternalCentralConfigService centralConfigServiceMock;
    private UserGroupStoreService userGroupStoreService;
    private SecurityListener securityListenerMock;

    @BeforeClass
    public void initArtifactoryRoles() {
        testAcls = createTestAcls();
        aclStoreServiceMock = createMock(AclStoreService.class);
        globalVirtualRepoMock = createGlobalVirtualRepoMock();
        repositoryServiceMock = createRepoServiceMock();
        centralConfigServiceMock = createMock(InternalCentralConfigService.class);
        userGroupStoreService = createMock(UserGroupStoreService.class);
        localRepoMock = createLocalRepoMock();
        cacheRepoMock = createCacheRepoMock();
        securityListenerMock = createMock(SecurityListener.class);
    }

    @BeforeMethod
    public void setUp() {
        // create new security context
        securityContext = new SecurityContextImpl();
        SecurityContextHolder.setContext(securityContext);

        // new service instance
        service = new SecurityServiceImpl();
        // set the aclManager mock on the security service
        ReflectionTestUtils.setField(service, "userGroupStoreService", userGroupStoreService);
        ReflectionTestUtils.setField(service, "aclStoreService", aclStoreServiceMock);
        ReflectionTestUtils.setField(service, "repositoryService", repositoryServiceMock);
        ReflectionTestUtils.setField(service, "centralConfig", centralConfigServiceMock);

        // reset mocks
        reset(aclStoreServiceMock, repositoryServiceMock, centralConfigServiceMock);
    }

    public void isAdminOnAdminUser() {
        Authentication authentication = setAdminAuthentication();

        boolean admin = service.isAdmin();
        assertTrue(admin, "The user in test is admin");
        // un-authenticate
        authentication.setAuthenticated(false);
        admin = service.isAdmin();
        assertFalse(admin, "Unauthenticated token");
    }

    public void isAdminOnSimpleUser() {
        setSimpleUserAuthentication();

        boolean admin = service.isAdmin();
        assertFalse(admin, "The user in test is not an admin");
    }

    @Test(dependsOnMethods = "isAdminOnAdminUser")
    public void spidermanCanDoAnything() {
        setAdminAuthentication();
        assertFalse(service.isAnonymous());// sanity
        assertTrue(service.isAdmin());// sanity

        RepoPath path = InternalRepoPathFactory.create("someRepo", "blabla");
        boolean canRead = service.canRead(path);
        assertTrue(canRead);
        boolean canDeploy = service.canDeploy(path);
        assertTrue(canDeploy);
    }

    @Test
    public void userReadAndDeployPermissions() {
        Authentication authentication = setSimpleUserAuthentication();

        RepoPath securedPath = InternalRepoPathFactory.create("securedRepo", "blabla");
        expect(repositoryServiceMock.localOrCachedRepositoryByKey(securedPath.getRepoKey()))
                .andReturn(localRepoMock).anyTimes();
        expectGetGlocalVirtualRepositoryCall();
        replay(repositoryServiceMock);

        // cannot read the specified path
        expectGetAllAclsCall(authentication);
        replay(aclStoreServiceMock);
        boolean canRead = service.canRead(securedPath);
        assertFalse(canRead, "User should not have permissions for this path");
        verify(aclStoreServiceMock);
        reset(aclStoreServiceMock);

        // cannot deploy to the specified path
        expectGetAllAclsCall(authentication);
        replay(aclStoreServiceMock);

        boolean canDeploy = service.canDeploy(securedPath);
        assertFalse(canDeploy, "User should not have permissions for this path");
        verify(aclStoreServiceMock, repositoryServiceMock);
        reset(aclStoreServiceMock, repositoryServiceMock);

        RepoPath allowedReadPath = InternalRepoPathFactory.create("testRepo1", "blabla");
        expect(repositoryServiceMock.localOrCachedRepositoryByKey(allowedReadPath.getRepoKey()))
                .andReturn(localRepoMock).anyTimes();
        expectGetGlocalVirtualRepositoryCall();
        replay(repositoryServiceMock);

        // can read the specified path
        expectGetAllAclsCall(authentication);
        replay(aclStoreServiceMock);
        canRead = service.canRead(allowedReadPath);
        assertTrue(canRead, "User should have read permissions for this path");
        verify(aclStoreServiceMock);
        reset(aclStoreServiceMock);

        // cannot deploy to the specified path
        expectGetAllAclsCall(authentication);
        replay(aclStoreServiceMock);
        canDeploy = service.canDeploy(allowedReadPath);
        assertFalse(canDeploy, "User should not have permissions for this path");
        verify(aclStoreServiceMock, repositoryServiceMock);
        reset(aclStoreServiceMock);

        // cannot admin the specified path
        expectGetAllAclsCall(authentication);
        replay(aclStoreServiceMock);
        boolean canAdmin = service.canManage(allowedReadPath);
        assertFalse(canAdmin, "User should not have permissions for this path");
        verify(aclStoreServiceMock);
    }

    @Test
    public void adminRolePermissions() {
        // user with admin role on permission target 'target1'
        Authentication authentication = setSimpleUserAuthentication("yossis");

        RepoPath allowedReadPath = InternalRepoPathFactory.create("testRepo1", "blabla");

        // can read the specified path
        expectGetAllAclsCall(authentication);
        expectGetGlocalVirtualRepositoryCall();
        replay(aclStoreServiceMock, repositoryServiceMock);
        boolean canRead = service.canRead(allowedReadPath);
        assertTrue(canRead, "User should have permissions for this path");
        verify(aclStoreServiceMock);
        reset(aclStoreServiceMock);

        // can deploy to the specified path
        expectGetAllAclsCall(authentication);
        replay(aclStoreServiceMock);
        boolean canDeploy = service.canDeploy(allowedReadPath);
        assertTrue(canDeploy, "User should have permissions for this path");
        verify(aclStoreServiceMock);
        reset(aclStoreServiceMock);

        // can admin the specified path
        expectGetAllAclsCall(authentication);
        replay(aclStoreServiceMock);
        boolean canAdmin = service.canManage(allowedReadPath);
        assertTrue(canAdmin, "User should have permissions for this path");
        verify(aclStoreServiceMock);
        reset(aclStoreServiceMock);

        // can admin
        AclInfo testRepo1Acl = testAcls.get(0);
        PermissionTargetInfo target = testRepo1Acl.getPermissionTarget();
        expect(aclStoreServiceMock.getAcl(target.getName())).andReturn(testRepo1Acl);
        replay(aclStoreServiceMock);
        boolean canAdminTarget = service.canManage(target);
        assertTrue(canAdminTarget, "User should have admin permissions for this target");
        verify(aclStoreServiceMock);
    }

    @Test
    public void groupPermissions() {
        Authentication authentication = setSimpleUserAuthentication("userwithnopermissions");

        RepoPath allowedReadPath = InternalRepoPathFactory.create("testRepo1", "**");
        expect(repositoryServiceMock.localOrCachedRepositoryByKey(allowedReadPath.getRepoKey()))
                .andReturn(localRepoMock).anyTimes();
        expectGetGlocalVirtualRepositoryCall();
        replay(repositoryServiceMock);

        // cannot deploy to the specified path
        expectGetAllAclsCall(authentication);
        replay(aclStoreServiceMock);
        boolean canDeploy = service.canDeploy(allowedReadPath);
        assertFalse(canDeploy, "User should have permissions for this path");
        verify(aclStoreServiceMock);
        reset(aclStoreServiceMock);

        // add the user to a group with permissions and expext permission garnted
        setSimpleUserAuthentication("userwithnopermissions", "deployGroup");
        expectGetAllAclsCallWithAnyArray();
        replay(aclStoreServiceMock);
        canDeploy = service.canDeploy(allowedReadPath);
        assertTrue(canDeploy, "User in a group with permissions for this path");
        verify(aclStoreServiceMock, repositoryServiceMock);
        reset(aclStoreServiceMock, repositoryServiceMock);
    }

    @Test
    public void userWithPermissionsToAGroupWithTheSameName() {
        setSimpleUserAuthentication(userAndGroupSharedName, userAndGroupSharedName);

        RepoPath testRepo1Path = InternalRepoPathFactory.create("testRepo1", "**");
        expectGetAllAclsCallWithAnyArray();
        expectGetGlocalVirtualRepositoryCall();
        replay(aclStoreServiceMock, repositoryServiceMock);
        boolean canRead = service.canRead(testRepo1Path);
        assertTrue(canRead, "User should have permissions for this path");
        verify(aclStoreServiceMock);
        reset(aclStoreServiceMock, repositoryServiceMock);

        RepoPath testRepo2Path = InternalRepoPathFactory.create("testRepo2", "**");
        expectGetAllAclsCallWithAnyArray();
        expectGetGlocalVirtualRepositoryCall();
        expect(repositoryServiceMock.localOrCachedRepositoryByKey("testRepo2")).andReturn(localRepoMock).anyTimes();
        replay(aclStoreServiceMock, repositoryServiceMock);
        canRead = service.canRead(testRepo2Path);
        assertTrue(canRead, "User belongs to a group with permissions to the path");
        verify(aclStoreServiceMock);
        reset(aclStoreServiceMock);
    }

    @Test
    public void userWithPermissionsToANonUniqueGroupName() {
        // here we test that a user that belongs to a group which has
        // the same name of a nother user will only get the group permissions
        // and not the user with the same name permissions
        setSimpleUserAuthentication("auser", userAndGroupSharedName);

        RepoPath testRepo1Path = InternalRepoPathFactory.create("testRepo1", "**");
        expectGetAllAclsCallWithAnyArray();
        expect(repositoryServiceMock.localOrCachedRepositoryByKey("testRepo1"))
                .andReturn(localRepoMock).anyTimes();
        expectGetGlocalVirtualRepositoryCall();
        replay(aclStoreServiceMock, repositoryServiceMock);
        boolean canRead = service.canRead(testRepo1Path);
        assertFalse(canRead, "User should not have permissions for this path");
        verify(aclStoreServiceMock, repositoryServiceMock);
        reset(aclStoreServiceMock, repositoryServiceMock);

        RepoPath testRepo2Path = InternalRepoPathFactory.create("testRepo2", "**");
        expectGetAllAclsCallWithAnyArray();
        expect(repositoryServiceMock.localOrCachedRepositoryByKey("testRepo2"))
                .andReturn(localRepoMock).anyTimes();
        expectGetGlocalVirtualRepositoryCall();
        replay(aclStoreServiceMock, repositoryServiceMock);
        canRead = service.canRead(testRepo2Path);
        assertTrue(canRead, "User belongs to a group with permissions to the path");
        verify(aclStoreServiceMock, repositoryServiceMock);
    }

    @Test
    public void hasPermissionPassingUserInfo() {
        SimpleUser user = createNonAdminUser("yossis");
        UserInfo userInfo = user.getDescriptor();

        RepoPath testRepo1Path = InternalRepoPathFactory.create("testRepo1", "any/path");

        expectGetAllAclsCallWithAnyArray();
        expectGetGlocalVirtualRepositoryCall();
        replay(aclStoreServiceMock, repositoryServiceMock);
        boolean canRead = service.canRead(userInfo, testRepo1Path);
        assertTrue(canRead, "User should have permissions for this path");
        verify(aclStoreServiceMock);
        reset(aclStoreServiceMock, repositoryServiceMock);

        expectGetAllAclsCallWithAnyArray();
        expectGetGlocalVirtualRepositoryCall();
        replay(aclStoreServiceMock, repositoryServiceMock);
        boolean canDeploy = service.canDeploy(userInfo, testRepo1Path);
        assertTrue(canDeploy, "User should have permissions for this path");
        verify(aclStoreServiceMock);
        reset(aclStoreServiceMock, repositoryServiceMock);

        expect(repositoryServiceMock.localOrCachedRepositoryByKey("testRepo1")).andReturn(localRepoMock).anyTimes();
        expectGetAllAclsCallWithAnyArray();
        expectGetGlocalVirtualRepositoryCall();
        replay(aclStoreServiceMock, repositoryServiceMock);
        boolean canDelete = service.canDelete(userInfo, testRepo1Path);
        assertFalse(canDelete, "User should not have permissions for this path");
        verify(aclStoreServiceMock, repositoryServiceMock);
        reset(aclStoreServiceMock);

        expectGetAllAclsCallWithAnyArray();
        replay(aclStoreServiceMock);
        boolean canAdmin = service.canManage(userInfo, testRepo1Path);
        assertTrue(canAdmin, "User should have permissions for this path");
        verify(aclStoreServiceMock);
        reset(aclStoreServiceMock, repositoryServiceMock);

        RepoPath testRepo2Path = InternalRepoPathFactory.create("testRepo2", "**");

        expect(repositoryServiceMock.localOrCachedRepositoryByKey("testRepo2")).andReturn(localRepoMock).anyTimes();
        expectGetGlocalVirtualRepositoryCall();
        expectGetAllAclsCallWithAnyArray();
        replay(aclStoreServiceMock, repositoryServiceMock);
        canRead = service.canRead(userInfo, testRepo2Path);
        assertFalse(canRead, "User should not have permissions for this path");
        verify(aclStoreServiceMock);
        reset(aclStoreServiceMock, repositoryServiceMock);

        SecurityDescriptor securityDescriptor = new SecurityDescriptor();
        securityDescriptor.setAnonAccessEnabled(false);

        CentralConfigDescriptor configDescriptor = createMock(CentralConfigDescriptor.class);
        expect(configDescriptor.getSecurity()).andReturn(securityDescriptor).anyTimes();
        replay(configDescriptor);
        expect(centralConfigServiceMock.getDescriptor()).andReturn(configDescriptor).anyTimes();
        replay(centralConfigServiceMock);

        SimpleUser anon = createNonAdminUser(UserInfo.ANONYMOUS);
        UserInfo anonUserInfo = anon.getDescriptor();

        RepoPath testMultiRepo = InternalRepoPathFactory.create("multi1", "**");
        expect(repositoryServiceMock.localOrCachedRepositoryByKey("multi1")).andReturn(cacheRepoMock).anyTimes();
        expectGetAllAclsCallWithAnyArray();
        replay(repositoryServiceMock);

        canRead = service.canRead(anonUserInfo, testMultiRepo);
        assertFalse(canRead, "Anonymous user should have permissions for this path");
        verify(configDescriptor, centralConfigServiceMock);
    }

    @Test
    public void hasPermissionWithSpecificTarget() {
        SimpleUser user = createNonAdminUser("shay");
        UserInfo userInfo = user.getDescriptor();

        RepoPath testRepo1Path = InternalRepoPathFactory.create("specific-repo", "com", true);

        expect(repositoryServiceMock.localOrCachedRepositoryByKey("specific-repo")).andReturn(localRepoMock).anyTimes();
        expectGetAllAclsCallWithAnyArray();
        expectGetGlocalVirtualRepositoryCall();
        replay(aclStoreServiceMock, repositoryServiceMock);
        boolean canRead = service.canRead(userInfo, testRepo1Path);
        assertTrue(canRead, "User should have read permissions for this path");
        verify(aclStoreServiceMock, repositoryServiceMock);
        reset(aclStoreServiceMock);

        expectGetAllAclsCallWithAnyArray();
        replay(aclStoreServiceMock);
        boolean canDeploy = service.canDeploy(userInfo, testRepo1Path);
        assertTrue(canDeploy, "User should have deploy permissions for this path");
        verify(aclStoreServiceMock, repositoryServiceMock);
        reset(aclStoreServiceMock);

        expectGetAllAclsCallWithAnyArray();
        replay(aclStoreServiceMock);
        boolean canDelete = service.canDelete(userInfo, testRepo1Path);
        assertFalse(canDelete, "User should not have delete permissions for this path");
        verify(aclStoreServiceMock, repositoryServiceMock);
        reset(aclStoreServiceMock);

        expectGetAllAclsCallWithAnyArray();
        replay(aclStoreServiceMock);
        boolean canAdmin = service.canManage(userInfo, testRepo1Path);
        assertFalse(canAdmin, "User should not have admin permissions for this path");
        verify(aclStoreServiceMock, repositoryServiceMock);
        reset(aclStoreServiceMock, repositoryServiceMock);
    }

    @Test
    public void hasPermissionForGroupInfo() {
        GroupInfo groupInfo = InfoFactoryHolder.get().createGroup("deployGroup");

        RepoPath testRepo1Path = InternalRepoPathFactory.create("testRepo1", "any/path");
        expect(repositoryServiceMock.localOrCachedRepositoryByKey("testRepo1"))
                .andReturn(localRepoMock).anyTimes();
        expectGetGlocalVirtualRepositoryCall();
        replay(repositoryServiceMock);

        expectGetAllAclsCallWithAnyArray();
        replay(aclStoreServiceMock);
        boolean canRead = service.canRead(groupInfo, testRepo1Path);
        assertFalse(canRead, "Group should not have permissions for this path");
        verify(aclStoreServiceMock);
        reset(aclStoreServiceMock);

        expectGetAllAclsCallWithAnyArray();
        replay(aclStoreServiceMock);
        boolean canDeploy = service.canDeploy(groupInfo, testRepo1Path);
        assertTrue(canDeploy, "Group should have permissions for this path");
        verify(aclStoreServiceMock);
        reset(aclStoreServiceMock);

        expectGetAllAclsCallWithAnyArray();
        replay(aclStoreServiceMock);
        boolean canDelete = service.canDelete(groupInfo, testRepo1Path);
        assertFalse(canDelete, "Group should not have permissions for this path");
        verify(aclStoreServiceMock);
        reset(aclStoreServiceMock);

        expectGetAllAclsCallWithAnyArray();
        replay(aclStoreServiceMock);
        boolean canAdmin = service.canManage(groupInfo, testRepo1Path);
        assertFalse(canAdmin, "Group should not have permissions for this path");
        verify(aclStoreServiceMock);
        reset(aclStoreServiceMock, repositoryServiceMock);

        RepoPath testRepo2Path = InternalRepoPathFactory.create("testRepo2", "some/path");

        expect(repositoryServiceMock.localOrCachedRepositoryByKey("testRepo2"))
                .andReturn(localRepoMock).anyTimes();
        expectGetGlocalVirtualRepositoryCall();
        replay(repositoryServiceMock);
        expectGetAllAclsCallWithAnyArray();
        replay(aclStoreServiceMock);
        canRead = service.canRead(groupInfo, testRepo2Path);
        assertFalse(canRead, "Group should not have permissions for this path");
        verify(aclStoreServiceMock, repositoryServiceMock);
        reset(aclStoreServiceMock, repositoryServiceMock);

        GroupInfo anyRepoGroupRead = InfoFactoryHolder.get().createGroup("anyRepoReadersGroup");

        RepoPath somePath = InternalRepoPathFactory.create("blabla", "some/path");

        expect(repositoryServiceMock.localOrCachedRepositoryByKey("blabla")).andReturn(localRepoMock).anyTimes();
        expectGetGlocalVirtualRepositoryCall();
        expectGetAllAclsCallWithAnyArray();
        replay(aclStoreServiceMock, repositoryServiceMock);
        canRead = service.canRead(anyRepoGroupRead, somePath);
        assertTrue(canRead, "Group should have permissions for this path");
        verify(aclStoreServiceMock, repositoryServiceMock);
        reset(aclStoreServiceMock);

        expectGetAllAclsCallWithAnyArray();
        replay(aclStoreServiceMock);
        canDeploy = service.canDeploy(anyRepoGroupRead, somePath);
        assertFalse(canDeploy, "Group should not have permissions for this path");
        verify(aclStoreServiceMock);
        reset(aclStoreServiceMock, repositoryServiceMock);

        GroupInfo multiRepoGroupRead = InfoFactoryHolder.get().createGroup("multiRepoReadersGroup");

        RepoPath multiPath = InternalRepoPathFactory.create("multi1", "some/path");
        expect(repositoryServiceMock.localOrCachedRepositoryByKey("multi1")).andReturn(localRepoMock).anyTimes();
        expect(repositoryServiceMock.localOrCachedRepositoryByKey("multi2")).andReturn(localRepoMock).anyTimes();
        expectGetAllAclsCallWithAnyArray();
        expectGetGlocalVirtualRepositoryCall();
        replay(aclStoreServiceMock, repositoryServiceMock);
        canRead = service.canRead(multiRepoGroupRead, multiPath);
        assertTrue(canRead, "Group should have permissions for this path");
        verify(aclStoreServiceMock, repositoryServiceMock);
        reset(aclStoreServiceMock);

        RepoPath multiPath2 = InternalRepoPathFactory.create("multi2", "some/path");
        expectGetAllAclsCallWithAnyArray();
        replay(aclStoreServiceMock);
        canRead = service.canRead(multiRepoGroupRead, multiPath2);
        assertTrue(canRead, "Group should have permissions for this path");
        verify(aclStoreServiceMock, repositoryServiceMock);
        reset(aclStoreServiceMock);

        expectGetAllAclsCallWithAnyArray();
        replay(aclStoreServiceMock);
        canDeploy = service.canDeploy(multiRepoGroupRead, multiPath);
        assertFalse(canDeploy, "Group should not have permissions for this path");
        verify(aclStoreServiceMock, repositoryServiceMock);
    }

    @Test
    public void getAllPermissionTargetsForAdminUser() {
        setAdminAuthentication();

        expect(aclStoreServiceMock.getAllAcls()).andReturn(testAcls);
        //expect(aclManagerMock.getAllAcls()).andReturn(testAcls);
        //expectGetAllAclsCall(authentication);
        replay(aclStoreServiceMock);
        List<PermissionTargetInfo> permissionTargets = service.getPermissionTargets(ArtifactoryPermission.MANAGE);
        assertEquals(permissionTargets.size(), permissionTargets.size());
        verify(aclStoreServiceMock);
    }

    @Test
    public void getAllPermissionTargetsForUserWithNoPermission() {
        setSimpleUserAuthentication("noadminpermissionsuser");

        expectAclScan();

        List<PermissionTargetInfo> permissionTargets = service.getPermissionTargets(ArtifactoryPermission.MANAGE);
        assertEquals(permissionTargets.size(), 0);

        verify(aclStoreServiceMock);
    }

    @Test(enabled = false)
    public void getDeployPermissionTargetsForUserWithNoPermission() {
        setSimpleUserAuthentication("user");

        expectAclScan();

        List<PermissionTargetInfo> targets = service.getPermissionTargets(ArtifactoryPermission.DEPLOY);
        assertEquals(targets.size(), 0);

        verify(aclStoreServiceMock);
    }

    @Test
    public void getDeployPermissionTargetsForUserWithDeployPermission() {
        setSimpleUserAuthentication("yossis");

        expectAclScan();

        List<PermissionTargetInfo> targets = service.getPermissionTargets(ArtifactoryPermission.DEPLOY);
        assertEquals(targets.size(), 1, "Expecting one deploy permission");

        verify(aclStoreServiceMock);
    }

    @Test
    public void userPasswordMatches() {
        setSimpleUserAuthentication("user");

        assertTrue(service.userPasswordMatches("password"));
        assertFalse(service.userPasswordMatches(""));
        assertFalse(service.userPasswordMatches("Password"));
        assertFalse(service.userPasswordMatches("blabla"));
    }

    @Test
    public void permissionOnRemoteRoot() {
        Authentication authentication = setSimpleUserAuthentication();

        expect(repositoryServiceMock.repositoryByKey("testRemote")).andReturn(createRemoteRepoMock()).anyTimes();
        expect(repositoryServiceMock.localOrCachedRepositoryByKey("testRemote-cache")).andReturn(null).anyTimes();
        expectGetGlocalVirtualRepositoryCall();
        replay(repositoryServiceMock);

        // cannot read the specified path
        expectGetAllAclsCall(authentication);
        replay(aclStoreServiceMock);
        boolean hasPermissionOnRemoteRoot = service.userHasPermissionsOnRepositoryRoot("testRemote");
        assertTrue(hasPermissionOnRemoteRoot, "User should have permissions for this path");
        verify(aclStoreServiceMock, repositoryServiceMock);
        reset(aclStoreServiceMock, repositoryServiceMock);
    }

    public void testUserHasPermissions() {
        SimpleUser user = createNonAdminUser("noperm");
        reset(userGroupStoreService);
        expect(userGroupStoreService.findUser("noperm")).andReturn(user.getDescriptor()).once();

        expectGetAllAclsCallWithAnyArray();
        replay(aclStoreServiceMock, userGroupStoreService);
        boolean hasPermissions = service.userHasPermissions("noperm");
        assertFalse(hasPermissions, "User should not have permissions");
        verify(aclStoreServiceMock, userGroupStoreService);
        reset(aclStoreServiceMock, userGroupStoreService);
    }

    public void testUserHasPermissionsFromGroup() {
        SimpleUser user = createNonAdminUser("noperm", userAndGroupSharedName);
        reset(userGroupStoreService);
        expect(userGroupStoreService.findUser("noperm")).andReturn(user.getDescriptor()).once();

        expectGetAllAclsCallWithAnyArray();
        replay(aclStoreServiceMock, userGroupStoreService);
        boolean hasPermissions = service.userHasPermissions("noperm");
        assertTrue(hasPermissions, "User should have permissions for this path");
        verify(aclStoreServiceMock, userGroupStoreService);
        reset(aclStoreServiceMock, userGroupStoreService);
    }

    public void userReadAndDeployPermissionsOnAnyRemote() {
        final Authentication authentication = setSimpleUserAuthentication();

        // can read the specified path
        RepoPath securedPath = InternalRepoPathFactory.create("repo1-cache", "blabla");
        expect(repositoryServiceMock.localOrCachedRepositoryByKey(securedPath.getRepoKey()))
                .andReturn(cacheRepoMock).anyTimes();
        expect(aclStoreServiceMock.getAllAcls()).andReturn(createAnyRemotelAcl());

        verifyAnyRemoteOrAnyLocal(authentication, securedPath);
    }

    public void userReadAndDeployPermissionsOnAnyLocal() {
        Authentication authentication = setSimpleUserAuthentication();

        // can read the specified path
        RepoPath securedPath = InternalRepoPathFactory.create("local-repo", "mama");
        expect(repositoryServiceMock.localOrCachedRepositoryByKey(securedPath.getRepoKey()))
                .andReturn(localRepoMock).anyTimes();
        expect(aclStoreServiceMock.getAllAcls()).andReturn(createAnyLocalAcl());

        verifyAnyRemoteOrAnyLocal(authentication, securedPath);
    }

    public void updateLastLoginWithNotExistingUserTest() throws InterruptedException {
        // Make sure that if the user doesn't exists we stop the "updateLastLogin" process without  exception
        reset(userGroupStoreService);
        // Enable the update last login process "userLastAccessUpdatesResolutionSecs" must be greater or equals to "1"
        getBound().setProperty(ConstantValues.userLastAccessUpdatesResolutionSecs, "1");
        expect(userGroupStoreService.findUser("user")).andReturn(null).once();
        replay(userGroupStoreService);
        service.updateUserLastLogin("user", "momo", System.currentTimeMillis() + 1000);
        verify(userGroupStoreService);
        reset(userGroupStoreService);
    }

    public void testUserLastLoginTimeUpdateBuffer() throws InterruptedException {
        getBound().setProperty(ConstantValues.userLastAccessUpdatesResolutionSecs, "0");
        service.updateUserLastLogin("user", "momo", System.currentTimeMillis());
        getBound().setProperty(ConstantValues.userLastAccessUpdatesResolutionSecs,
                ConstantValues.userLastAccessUpdatesResolutionSecs.getDefValue());

        MutableUserInfo user = new UserInfoBuilder("user").build();
        user.setLastLoginTimeMillis(0);

        //Simulate No existing last login, expect an update
        expect(userGroupStoreService.findUser("user")).andReturn(user).once();
        userGroupStoreService.updateUser(user);
        EasyMock.expectLastCall();
        replay(userGroupStoreService);
        service.updateUserLastLogin("user", "momo", System.currentTimeMillis());

        //Give a last login from the near past, expect no update
        long nearPastLogin = System.currentTimeMillis();

        verify(userGroupStoreService);
        reset(userGroupStoreService);
        user = new UserInfoBuilder("user").build();
        user.setLastLoginTimeMillis(nearPastLogin);
        expect(userGroupStoreService.findUser("user")).andReturn(user).once();
        replay(userGroupStoreService);
        service.updateUserLastLogin("user", "momo", nearPastLogin + 100l);

        //Give a last login from the future, expect an update
        verify(userGroupStoreService);
        reset(userGroupStoreService);
        expect(userGroupStoreService.findUser("user")).andReturn(user).once();
        userGroupStoreService.updateUser(user);
        EasyMock.expectLastCall();
        replay(userGroupStoreService);
        service.updateUserLastLogin("user", "momo", System.currentTimeMillis() + 6000l);
    }

    public void testSelectiveReload() {
        TreeSet<SecurityListener> securityListeners = new TreeSet<>();
        securityListeners.add(securityListenerMock);
        ReflectionTestUtils.setField(service, "securityListeners", securityListeners);
        reset(securityListenerMock);
        securityListenerMock.onClearSecurity();
        expect(securityListenerMock.compareTo(securityListenerMock)).andReturn(0).anyTimes();
        replay(securityListenerMock);

        SecurityDescriptor newSecurityDescriptor = new SecurityDescriptor();
        SecurityDescriptor oldSecurityDescriptor = new SecurityDescriptor();
        oldSecurityDescriptor.addLdap(new LdapSetting());

        CentralConfigDescriptor newConfigDescriptor = createMock(CentralConfigDescriptor.class);
        expect(newConfigDescriptor.getSecurity()).andReturn(newSecurityDescriptor).anyTimes();
        replay(newConfigDescriptor);

        CentralConfigDescriptor oldConfigDescriptor = createMock(CentralConfigDescriptor.class);
        expect(oldConfigDescriptor.getSecurity()).andReturn(oldSecurityDescriptor).anyTimes();
        replay(oldConfigDescriptor);

        expect(centralConfigServiceMock.getDescriptor()).andReturn(newConfigDescriptor).anyTimes();
        replay(centralConfigServiceMock);

        service.reload(oldConfigDescriptor);
        verify(securityListenerMock);

        // The security conf is the same, so onClearSecurity should NOT be called
        service.reload(newConfigDescriptor);
        verify(securityListenerMock);
        ReflectionTestUtils.setField(service, "securityListeners", null);
    }

    private void verifyAnyRemoteOrAnyLocal(Authentication authentication, RepoPath securedPath) {
        expectGetGlocalVirtualRepositoryCall();
        replay(aclStoreServiceMock, repositoryServiceMock);
        boolean canRead = service.canRead(securedPath);
        assertTrue(canRead, "User should have permissions for this path");
        verify(aclStoreServiceMock);
        reset(aclStoreServiceMock);

        // can deploy to the specified path
        expectGetAllAclsCall(authentication);
        replay(aclStoreServiceMock);

        boolean canDeploy = service.canDeploy(securedPath);
        assertFalse(canDeploy, "User should have permissions for this path");
        verify(aclStoreServiceMock, repositoryServiceMock);
        reset(aclStoreServiceMock, repositoryServiceMock);

        RepoPath allowedReadPath = InternalRepoPathFactory.create("testRepo1", "blabla");
        expect(repositoryServiceMock.localOrCachedRepositoryByKey(allowedReadPath.getRepoKey()))
                .andReturn(localRepoMock).anyTimes();
        expectGetGlocalVirtualRepositoryCall();
        replay(repositoryServiceMock);

        // can read the specified path
        expectGetAllAclsCall(authentication);
        replay(aclStoreServiceMock);
        canRead = service.canRead(allowedReadPath);
        assertTrue(canRead, "User should have read permissions for this path");
        verify(aclStoreServiceMock);
        reset(aclStoreServiceMock);

        // cannot deploy to the specified path
        expectGetAllAclsCall(authentication);
        replay(aclStoreServiceMock);
        canDeploy = service.canDeploy(allowedReadPath);
        assertFalse(canDeploy, "User should not have permissions for this path");
        verify(aclStoreServiceMock, repositoryServiceMock);
        reset(aclStoreServiceMock);

        // cannot admin the specified path
        expectGetAllAclsCall(authentication);
        replay(aclStoreServiceMock);
        boolean canAdmin = service.canManage(allowedReadPath);
        assertFalse(canAdmin, "User should not have permissions for this path");
        verify(aclStoreServiceMock);
    }

    private void expectAclScan() {
        expect(aclStoreServiceMock.getAllAcls()).andReturn(testAcls).anyTimes();
        /*
        expect(aclStoreServiceMock.getAcl(permissionTargets.get(0).getName())).andReturn(testAcls.get(0));
        expect(aclStoreServiceMock.getAcl(permissionTargets.get(1).getName())).andReturn(testAcls.get(1));
        expect(aclStoreServiceMock.getAcl(permissionTargets.get(2).getName())).andReturn(testAcls.get(2));
        expect(aclStoreServiceMock.getAcl(permissionTargets.get(3).getName())).andReturn(testAcls.get(3));
        expect(aclStoreServiceMock.getAcl(permissionTargets.get(4).getName())).andReturn(testAcls.get(4));
        */
        replay(aclStoreServiceMock);
    }

    private void expectGetAllAclsCall(Authentication authentication) {
        expect(aclStoreServiceMock.getAllAcls()).andReturn(testAcls).anyTimes();
    }

    private void expectGetAllAclsCallWithAnyArray() {
        expect(aclStoreServiceMock.getAllAcls()).andReturn(testAcls);
    }


    private void expectGetGlocalVirtualRepositoryCall() {
        expect(repositoryServiceMock.getGlobalVirtualRepo()).andReturn(globalVirtualRepoMock).anyTimes();
    }

    private List<AclInfo> createTestAcls() {
        userAndGroupSharedName = "usergroup";
        PermissionTargetInfo pmi = InfoFactoryHolder.get().createPermissionTarget("target1",
                Arrays.asList("testRepo1", "testRemote-cache"));
        // yossis has all the permissions (when all permissions are checked)
        MutableAceInfo adminAce = factory.createAce("yossis", false, ArtifactoryPermission.MANAGE.getMask());
        adminAce.setDeploy(true);
        adminAce.setRead(true);
        MutableAceInfo readerAce = factory.createAce("user", false, ArtifactoryPermission.READ.getMask());
        MutableAceInfo deleteAce = factory.createAce("shay", false, ArtifactoryPermission.DELETE.getMask());
        deleteAce.setDeploy(true);
        deleteAce.setAnnotate(true);
        deleteAce.setRead(true);
        MutableAceInfo userGroupAce =
                factory.createAce(userAndGroupSharedName, false, ArtifactoryPermission.READ.getMask());
        MutableAceInfo deployerGroupAce =
                factory.createAce("deployGroup", true, ArtifactoryPermission.DEPLOY.getMask());
        Set<AceInfo> aces = new HashSet<AceInfo>(
                Arrays.asList(adminAce, readerAce, userGroupAce, deployerGroupAce));
        AclInfo aclInfo = factory.createAcl(pmi, aces, "me");

        PermissionTargetInfo pmi2 = InfoFactoryHolder.get().createPermissionTarget("target2",
                Arrays.asList("testRepo2"));
        MutableAceInfo target2GroupAce = factory.createAce(userAndGroupSharedName, true,
                ArtifactoryPermission.READ.getMask());
        Set<AceInfo> target2Aces = new HashSet<AceInfo>(Arrays.asList(target2GroupAce));
        AclInfo aclInfo2 = factory.createAcl(pmi2, target2Aces, "me");

        // acl for any repository with read permissions to group
        PermissionTargetInfo anyTarget = InfoFactoryHolder.get().createPermissionTarget("anyRepoTarget",
                Arrays.asList(PermissionTargetInfo.ANY_REPO));
        MutableAceInfo readerGroupAce =
                factory.createAce("anyRepoReadersGroup", true, ArtifactoryPermission.READ.getMask());
        Set<AceInfo> anyTargetAces = new HashSet<AceInfo>(Arrays.asList(readerGroupAce));
        AclInfo anyTargetAcl = factory.createAcl(anyTarget, anyTargetAces, "me");

        // acl with multiple repo keys with read permissions to group and anonymous
        PermissionTargetInfo multiReposTarget = InfoFactoryHolder.get().createPermissionTarget("multiRepoTarget",
                Arrays.asList("multi1", "multi2"));
        MutableAceInfo multiReaderGroupAce =
                factory.createAce("multiRepoReadersGroup", true, ArtifactoryPermission.READ.getMask());
        MutableAceInfo multiReaderAnonAce =
                factory.createAce(UserInfo.ANONYMOUS, false, ArtifactoryPermission.READ.getMask());
        Set<AceInfo> multiTargetAces = new HashSet<AceInfo>(Arrays.asList(multiReaderGroupAce, multiReaderAnonAce));
        AclInfo multiReposAcl = factory.createAcl(multiReposTarget, multiTargetAces, "me");

        // acl for any repository with specific path delete permissions to user
        MutablePermissionTargetInfo anyRepoSpecificPathTarget = InfoFactoryHolder.get().createPermissionTarget(
                "anyRepoSpecificPathTarget",
                Arrays.asList("specific-repo"));
        anyRepoSpecificPathTarget.setIncludes(Arrays.asList("com/acme/**"));
        Set<AceInfo> specificDeleteAces = new HashSet<AceInfo>(Arrays.asList(deleteAce));
        AclInfo allReposSpecificPathAcl = factory.createAcl(anyRepoSpecificPathTarget, specificDeleteAces, "me");

        List<AclInfo> acls = Arrays.asList(aclInfo, aclInfo2, anyTargetAcl, multiReposAcl, allReposSpecificPathAcl);
        permissionTargets = Arrays.asList(
                aclInfo.getPermissionTarget(), aclInfo2.getPermissionTarget(),
                anyTargetAcl.getPermissionTarget(), multiReposAcl.getPermissionTarget(),
                allReposSpecificPathAcl.getPermissionTarget());
        return acls;
    }

    private List<AclInfo> createAnyRemotelAcl() {
        PermissionTargetInfo pmi = InfoFactoryHolder.get().createPermissionTarget("target1",
                Arrays.asList(PermissionTargetInfo.ANY_REMOTE_REPO));
        MutableAceInfo readerAndDeployer = factory.createAce("user", false,
                ArtifactoryPermission.READ.getMask() | ArtifactoryPermission.DEPLOY.getMask());
        Set<AceInfo> aces = new HashSet<AceInfo>(Arrays.asList(readerAndDeployer));
        AclInfo aclInfo = factory.createAcl(pmi, aces, "me");

        return Arrays.asList(aclInfo);
    }

    private List<AclInfo> createAnyLocalAcl() {
        PermissionTargetInfo pmi = InfoFactoryHolder.get().createPermissionTarget("target1",
                Arrays.asList(PermissionTargetInfo.ANY_LOCAL_REPO));
        MutableAceInfo readerAndDeployer = factory.createAce("user", false,
                ArtifactoryPermission.READ.getMask() | ArtifactoryPermission.DEPLOY.getMask());
        Set<AceInfo> aces = new HashSet<AceInfo>(Arrays.asList(readerAndDeployer));
        AclInfo aclInfo = factory.createAcl(pmi, aces, "me");

        return Arrays.asList(aclInfo);
    }

    private Authentication setAdminAuthentication() {
        SimpleUser adminUser = createAdminUser();
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                adminUser, null, SimpleUser.ADMIN_GAS);
        securityContext.setAuthentication(authenticationToken);
        return authenticationToken;
    }

    private Authentication setSimpleUserAuthentication() {
        return setSimpleUserAuthentication("user");
    }

    private Authentication setSimpleUserAuthentication(String username, String... groups) {
        SimpleUser simpleUser = createNonAdminUser(username, groups);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                simpleUser, "password", SimpleUser.USER_GAS);
        securityContext.setAuthentication(authenticationToken);
        return authenticationToken;
    }

    private static SimpleUser createNonAdminUser(String username, String... groups) {
        UserInfo userInfo = new UserInfoBuilder(username).updatableProfile(true)
                .internalGroups(new HashSet<String>(Arrays.asList(groups))).build();
        return new SimpleUser(userInfo);
    }

    private static SimpleUser createAdminUser() {
        UserInfo userInfo = new UserInfoBuilder("spiderman").admin(true).updatableProfile(true).build();
        return new SimpleUser(userInfo);
    }

    private static LocalRepo createLocalRepoMock() {
        LocalRepo localRepo = createMock(LocalRepo.class);
        expect(localRepo.isLocal()).andReturn(true).anyTimes();
        expect(localRepo.isCache()).andReturn(false).anyTimes();
        replay(localRepo);
        return localRepo;
    }

    private static LocalRepo createCacheRepoMock() {
        LocalRepo localRepo = createMock(LocalRepo.class);
        expect(localRepo.isLocal()).andReturn(true).anyTimes();
        expect(localRepo.isCache()).andReturn(true).anyTimes();
        replay(localRepo);
        return localRepo;
    }

    private RemoteRepo createRemoteRepoMock() {
        RemoteRepo remoteRepo = createMock(RemoteRepo.class);
        expect(remoteRepo.isReal()).andReturn(true).anyTimes();
        replay(remoteRepo);
        return remoteRepo;
    }

    private InternalRepositoryService createRepoServiceMock() {
        InternalRepositoryService repositoryService = createMock(InternalRepositoryService.class);
        expect(repositoryService.getGlobalVirtualRepo()).andReturn(globalVirtualRepoMock).anyTimes();
        replay(repositoryService);
        return repositoryService;
    }

    private VirtualRepo createGlobalVirtualRepoMock() {
        VirtualRepo globalVirtualRepo= createMock(VirtualRepo.class);
        expect(globalVirtualRepo.getRemoteRepositoriesMap()).andReturn(new HashMap<String, RemoteRepo>()).anyTimes();
        replay(globalVirtualRepo);
        return globalVirtualRepo;
    }
}
