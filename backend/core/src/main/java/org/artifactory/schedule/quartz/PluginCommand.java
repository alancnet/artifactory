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

package org.artifactory.schedule.quartz;

import org.artifactory.repo.service.ImportJob;
import org.artifactory.schedule.JobCommand;
import org.artifactory.schedule.StopCommand;
import org.artifactory.schedule.StopStrategy;
import org.artifactory.schedule.TaskUser;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A quartz command job that runs a plugin closure
 *
 * @author Yoav Landman
 */
@JobCommand(schedulerUser = TaskUser.SYSTEM, manualUser = TaskUser.SYSTEM,
        commandsToStop = {@StopCommand(command = ImportJob.class, strategy = StopStrategy.IMPOSSIBLE)}
)
public class
        PluginCommand extends QuartzCommand {

    private static final Logger log = LoggerFactory.getLogger(PluginCommand.class);

    public static final String ACTION_CLOSURE = "action-closure";

    @Override
    protected void onExecute(JobExecutionContext callbackContext) throws JobExecutionException {
        JobDetail jobDetail = callbackContext.getJobDetail();
        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        ExecuteAction action = (ExecuteAction) jobDataMap.get(ACTION_CLOSURE);
        String jobName = jobDetail.getKey().toString();
        log.debug("Executing plugin command '{}'...", jobName);
        action.execute();
        log.debug("Plugin command '{}'executed.", jobName);
    }
}
