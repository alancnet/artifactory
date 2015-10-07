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

package org.artifactory.repo.service;

import org.artifactory.api.config.ExportSettingsImpl;
import org.artifactory.common.MutableStatusHolder;
import org.artifactory.repo.cleanup.ArtifactCleanupJob;
import org.artifactory.repo.cleanup.IntegrationCleanupJob;
import org.artifactory.sapi.common.BaseSettings;
import org.artifactory.schedule.JobCommand;
import org.artifactory.schedule.StopCommand;
import org.artifactory.schedule.StopStrategy;
import org.artifactory.schedule.Task;
import org.artifactory.schedule.TaskUser;
import org.artifactory.schedule.quartz.QuartzCommand;
import org.artifactory.spring.InternalContextHelper;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This job can run on HA-slave machine, so it should only be started manually
 *
 * @author freds
 * @date Nov 6, 2008
 */
@JobCommand(manualUser = TaskUser.CURRENT,
        keyAttributes = {Task.REPO_KEY},
        commandsToStop = {
                @StopCommand(command = ArtifactCleanupJob.class, strategy = StopStrategy.STOP),
                @StopCommand(command = IntegrationCleanupJob.class, strategy = StopStrategy.STOP),
                @StopCommand(command = ImportJob.class, strategy = StopStrategy.IMPOSSIBLE)
        },
        runOnlyOnPrimary = false
)
public class ExportJob extends QuartzCommand {
    private static final Logger log = LoggerFactory.getLogger(ExportJob.class);

    @Override
    protected void onExecute(JobExecutionContext callbackContext) throws JobExecutionException {
        MutableStatusHolder status = null;
        try {
            JobDataMap jobDataMap = callbackContext.getJobDetail().getJobDataMap();
            String repoKey = (String) jobDataMap.get(Task.REPO_KEY);
            if (repoKey == null) {
                throw new IllegalStateException("Cannot export unknown source for " + this);
            }
            ExportSettingsImpl settings = (ExportSettingsImpl) jobDataMap.get(ExportSettingsImpl.class.getName());
            status = settings.getStatusHolder();
            InternalRepositoryService service =
                    InternalContextHelper.get().beanForType(InternalRepositoryService.class);
            if (BaseSettings.FULL_SYSTEM.equals(repoKey)) {
                service.exportTo(settings);
            } else {
                service.exportRepo(repoKey, settings);
            }
        } catch (Exception e) {
            if (status != null) {
                status.error("Error occurred during export: " + e.getMessage(), e, log);
            } else {
                log.error("Error occurred during export", e);
            }
        }
    }
}
