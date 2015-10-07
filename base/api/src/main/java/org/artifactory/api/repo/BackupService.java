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

package org.artifactory.api.repo;

import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.config.ExportSettingsImpl;
import org.artifactory.descriptor.backup.BackupDescriptor;
import org.artifactory.descriptor.repo.RealRepoDescriptor;

import java.io.File;
import java.util.List;

/**
 * User: freds Date: Aug 5, 2008 Time: 9:25:33 PM
 */
public interface BackupService {
    void backupRepos(File backupDir, ExportSettingsImpl exportSettings);

    void backupRepos(File backupDir, List<RealRepoDescriptor> excludeRepositories,
            ExportSettingsImpl exportSettings);

    void scheduleImmediateSystemBackup(BackupDescriptor backupDescriptor, BasicStatusHolder statusHolder);
}
