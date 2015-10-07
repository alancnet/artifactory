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

package org.artifactory.backup;

import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.repo.Async;
import org.artifactory.api.repo.BackupService;
import org.artifactory.descriptor.backup.BackupDescriptor;
import org.artifactory.spring.InternalArtifactoryContext;
import org.artifactory.spring.ReloadableBean;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Date;

/**
 * @author freds
 * @date Oct 27, 2008
 */
public interface InternalBackupService extends ReloadableBean, BackupService {
    /**
     * @param context The internal artifactory context
     * @param backup  Descriptor of backup to run
     * @return MultiStatusHolder containing messages
     */
    BasicStatusHolder backupSystem(InternalArtifactoryContext context, @Nonnull BackupDescriptor backup);

    /**
     * Iterate (non-recursively) on all folders/files in the backup dir and delete them if they are older than "now"
     * minus the retention period of the beckup.
     *
     * @param now       The base time to use for the cleanup
     * @param backupKey
     */
    void cleanupOldBackups(Date now, String backupKey);

    /**
     * Sends an email notification to the admins about errors that have occurred during backups
     *
     * @param backupName   Name of backup that failed
     * @param statusHolder Status holder containing errors
     * @throws Exception
     */
    @Async
    void sendBackupErrorNotification(String backupName, BasicStatusHolder statusHolder) throws Exception;

    BackupDescriptor getBackup(String backupKey);

    File getBackupDir(BackupDescriptor descriptor);
}
