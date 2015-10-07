/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2013 JFrog Ltd.
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

package org.artifactory.storage.jobs;

import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.schedule.JobCommand;
import org.artifactory.schedule.TaskUser;
import org.artifactory.schedule.quartz.QuartzCommand;
import org.artifactory.storage.service.RemoteStatsService;
import org.artifactory.storage.service.RemoteStatsServiceImpl;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple job to trigger statistics flushing.
 *
 * @author Michael Pasternak
 * @see org.artifactory.storage.fs.service.StatsService#flushStats()
 */
@JobCommand(singleton = true, runOnlyOnPrimary = false, schedulerUser = TaskUser.SYSTEM, manualUser = TaskUser.SYSTEM)
public class RemoteStatsFlushJob extends QuartzCommand {
    private static final Logger log = LoggerFactory.getLogger(RemoteStatsFlushJob.class);

    @Override
    protected void onExecute(JobExecutionContext callbackContext) throws JobExecutionException {
        log.debug("Remote Stats FlushJob started");
        ArtifactoryContext artifactoryContext = ContextHelper.get();
        RemoteStatsService statsService = artifactoryContext.beanForType(RemoteStatsServiceImpl.class);
        statsService.flushStats();
        log.debug("Remote  Stats FlushJob finished");
    }
}
