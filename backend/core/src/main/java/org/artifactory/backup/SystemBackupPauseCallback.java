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

import com.google.common.collect.Sets;
import org.artifactory.common.ConstantValues;
import org.artifactory.sapi.common.ExportSettings;
import org.artifactory.sapi.common.FileExportCallback;
import org.artifactory.sapi.common.FileExportEvent;
import org.artifactory.sapi.common.FileExportInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Set;

/**
 * Should be used for system backups only.<br>
 * Sleeps every certain interval of time if the backup is working for a long time.
 *
 * @author Noam Y. Tenne
 */
public class SystemBackupPauseCallback implements FileExportCallback {
    private static final Logger log = LoggerFactory.getLogger(SystemBackupPauseCallback.class);

    private static final long jobTimeThreshold = ConstantValues.gcScanStartSleepingThresholdMillis.getLong();
    private static final long sleepIterationMillis = ConstantValues.backupFileExportSleepIterationMillis.getLong();
    private static final long sleepMillis = ConstantValues.backupFileExportSleepMillis.getLong();
    private boolean jobTimeThresholdReached = false;
    private long lastPauseTimeMillis = 0;

    public void callback(ExportSettings currentSettings, FileExportInfo info) {
        if (!jobTimeThresholdReached &&
                (System.currentTimeMillis() - currentSettings.getTime().getTime() > jobTimeThreshold)) {
            jobTimeThresholdReached = true;
            log.debug("Slowing down system backup.");
        }
        if (jobTimeThresholdReached && ((lastPauseTimeMillis == 0) ||
                (System.currentTimeMillis() - lastPauseTimeMillis > sleepIterationMillis))) {
            lastPauseTimeMillis = System.currentTimeMillis();
            try {
                log.trace("Slowing down system backup at: '{}' for '{}' millis.",
                        new Date(lastPauseTimeMillis).toString(), sleepMillis);
                Thread.sleep(sleepMillis);
            } catch (InterruptedException e) {
                log.warn("Scheduled system backup pause callback was interrupted: " + e.getMessage());
            }
        }
    }

    @Override
    public void cleanup() {
    }

    @Override
    public Set<FileExportEvent> triggeringEvents() {
        return Sets.newHashSet(FileExportEvent.BEFORE_FILE_EXPORT);
    }
}
