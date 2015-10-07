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

package org.artifactory.descriptor.backup;

import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RealRepoDescriptor;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;

/**
 * Tests the BackupDescriptor.
 *
 * @author Yossi Shaul
 */
@Test
public class BackupDescriptorTest {

    public void defaultConstructor() {
        BackupDescriptor backup = new BackupDescriptor();
        Assert.assertNull(backup.getKey(), "Key should be null by default");
        Assert.assertTrue(backup.isEnabled(), "Backup should be enabled by default");
        Assert.assertNull(backup.getCronExp(), "Cron expression should be null by default");
        Assert.assertNull(backup.getDir(), "Dir should be null by default");
        Assert.assertTrue(backup.getExcludedRepositories().isEmpty(),
                "Excluded repositories should be empty by default");
        Assert.assertEquals(backup.getRetentionPeriodHours(),
                BackupDescriptor.DEFAULT_RETENTION_PERIOD_HOURS,
                "Retention period should be {} by default");
        Assert.assertFalse(backup.isCreateArchive(), "Is create archive should be false by default");
        //Assert.assertFalse(backup.isIncrementalBackup(), "Is incremental backup should be false by default");
    }

    public void testSameTaskDefinition() {
        BackupDescriptor backup1 = new BackupDescriptor();
        String backupKey = "test";
        backup1.setKey(backupKey);
        backup1.setEnabled(true);
        backup1.setCronExp("* * * invalid");
        backup1.setCreateArchive(false);
        backup1.setRetentionPeriodHours(100);
        backup1.setSendMailOnError(false);
        backup1.setExcludedRepositories(new ArrayList<RealRepoDescriptor>(1) {{
            add(new LocalRepoDescriptor());
        }});
        BackupDescriptor backup2 = new BackupDescriptor();
        backup2.setKey(backupKey);
        backup2.setEnabled(true);
        backup2.setCronExp("* * * invalid");
        backup2.setCreateArchive(false);
        backup2.setRetentionPeriodHours(100);
        backup2.setSendMailOnError(false);
        backup2.setExcludedRepositories(new ArrayList<RealRepoDescriptor>(1) {{
            add(new LocalRepoDescriptor());
        }});

        Assert.assertTrue(backup1.sameTaskDefinition(backup2), "Backups should have same task definition");
        backup2.setCreateArchive(false);
        Assert.assertTrue(backup1.sameTaskDefinition(backup2), "Backups should have same task definition");
        backup2.setRetentionPeriodHours(200);
        Assert.assertTrue(backup1.sameTaskDefinition(backup2), "Backups should have same task definition");
        backup2.setSendMailOnError(true);
        Assert.assertTrue(backup1.sameTaskDefinition(backup2), "Backups should have same task definition");
        backup2.setExcludedRepositories(null);
        Assert.assertTrue(backup1.sameTaskDefinition(backup2), "Backups should have same task definition");
        backup2.setEnabled(false);
        Assert.assertFalse(backup1.sameTaskDefinition(backup2), "Backups should NOT have same task definition");
        backup2.setEnabled(true);
        backup2.setKey("wrong");
        Assert.assertFalse(backup1.sameTaskDefinition(backup2), "Backups should NOT have same task definition");
        backup2.setKey(backupKey);
        backup2.setCronExp("* * * another invalid");
        Assert.assertFalse(backup1.sameTaskDefinition(backup2), "Backups should NOT have same task definition");
    }
}
