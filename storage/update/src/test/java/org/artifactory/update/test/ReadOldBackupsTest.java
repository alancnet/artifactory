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

package org.artifactory.update.test;

import org.artifactory.addon.CoreAddons;
import org.artifactory.api.security.SecurityService;
import org.artifactory.factory.InfoFactory;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.security.AclInfo;
import org.artifactory.security.GroupInfo;
import org.artifactory.security.PermissionTargetInfo;
import org.artifactory.security.SecurityInfo;
import org.artifactory.security.UserInfo;
import org.artifactory.update.security.SecurityInfoReader;
import org.artifactory.update.utils.BackupUtils;
import org.artifactory.version.ArtifactoryVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * @author freds
 * @date Nov 9, 2008
 */
public class ReadOldBackupsTest {
    private static final Logger log = LoggerFactory.getLogger(ReadOldBackupsTest.class);
    protected final InfoFactory factory = InfoFactoryHolder.get();

    @Test
    public void testReadingVersion() throws Exception {
        for (ArtifactoryVersion version : ArtifactoryVersion.values()) {
            if (version.isCurrent()) {
                // Nothing to do
                continue;
            }
            String backupVersionFolder = "/backups/" + version.name();
            URL resource = getClass().getResource(backupVersionFolder);
            if (resource != null) {
                File backupFolder = new File(resource.toURI());
                ArtifactoryVersion backupVersion = BackupUtils.findVersion(backupFolder);
                Assert.assertEquals(backupVersion, version);
            } else {
                log.debug("Version {} does not have a backup test folder in {}", version, backupVersionFolder);
            }
        }
    }

    @Test
    public void testSecurityConversion() throws Exception {
        ArtifactoryVersion version = ArtifactoryVersion.v130beta2;
        String backupVersionFolder = "/backups/" + version.name();
        URL resource = getClass().getResource(backupVersionFolder);
        Assert.assertNotNull(resource, "Resource folder " + backupVersionFolder + " should exists");
        File backupFolder = new File(resource.toURI());
        ArtifactoryVersion backupVersion = BackupUtils.findVersion(backupFolder);
        Assert.assertEquals(backupVersion, version);
        SecurityInfo securityConfig = new SecurityInfoReader().read(new File(backupFolder, SecurityService.FILE_NAME));
        List<UserInfo> users = securityConfig.getUsers();
        Assert.assertEquals(users.size(), 6, "Should have 6 users");
        UserInfo totoUser = users.get(users.indexOf(factory.createUser("toto")));
        Assert.assertFalse(totoUser.isAdmin(), "Toto user should not be admin");
        UserInfo superUser = users.get(users.indexOf(factory.createUser(CoreAddons.SUPER_USER_NAME)));
        Assert.assertTrue(superUser.isAdmin(), "Super user should be admin");
        List<GroupInfo> groups = securityConfig.getGroups();
        Assert.assertTrue(groups == null || groups.isEmpty(), "There should be no groups");
        List<AclInfo> acls = securityConfig.getAcls();
        Assert.assertEquals(acls.size(), 6, "There should be 6 ACLs");
        AclInfo anythingAcl = acls.get(acls.indexOf(factory.createAcl(
                factory.createPermissionTarget(PermissionTargetInfo.ANY_PERMISSION_TARGET_NAME,
                        Arrays.asList(PermissionTargetInfo.ANY_REPO)))));
        Assert.assertEquals(anythingAcl.getAces().size(), 4, "Should be 4 ACE in Anything");
    }
}
