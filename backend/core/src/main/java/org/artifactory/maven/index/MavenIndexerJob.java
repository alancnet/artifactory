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

package org.artifactory.maven.index;

import org.artifactory.api.context.ContextHelper;
import org.artifactory.repo.service.ImportJob;
import org.artifactory.schedule.JobCommand;
import org.artifactory.schedule.StopCommand;
import org.artifactory.schedule.StopStrategy;
import org.artifactory.schedule.TaskUser;
import org.artifactory.schedule.quartz.QuartzCommand;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yoavl
 */
@JobCommand(
        singleton = true,
        schedulerUser = TaskUser.SYSTEM,
        manualUser = TaskUser.SYSTEM,
        commandsToStop = {
                @StopCommand(command = ImportJob.class, strategy = StopStrategy.IMPOSSIBLE)
        }
)
public class MavenIndexerJob extends QuartzCommand {
    private static final Logger log = LoggerFactory.getLogger(MavenIndexerJob.class);

    public static final String SETTINGS = "settings";

    @Override
    protected void onExecute(JobExecutionContext context) throws JobExecutionException {
        log.debug("Triggered MavenIndexerJob started");
        InternalMavenIndexerService indexer = ContextHelper.get().beanForType(InternalMavenIndexerService.class);
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        MavenIndexerRunSettings settings = (MavenIndexerRunSettings) jobDataMap.get(SETTINGS);
        settings.setFireTime(context.getFireTime());
        indexer.index(settings);
        log.debug("Triggered MavenIndexerJob finished");
    }
}