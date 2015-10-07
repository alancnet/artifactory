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

import org.artifactory.schedule.Task;
import org.artifactory.schedule.TaskCallback;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;

/**
 * @author yoavl
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public abstract class QuartzCommand extends TaskCallback<JobExecutionContext> implements Job {
    private static final Logger log = LoggerFactory.getLogger(QuartzCommand.class);

    @Override
    public final void execute(final JobExecutionContext jobContext) throws JobExecutionException {
        try {
            boolean shouldExecute = beforeExecute(jobContext);
            String token = currentTaskToken();
            String taskName = getClass().getName();
            if (!shouldExecute) {
                log.debug("Skipping execution of task {} for token {}.", taskName, token);
            } else {
                log.debug("Executing task {} for token {}.", taskName, token);
                onExecute(jobContext);
                log.debug("Finished execution of task {} for token {}.", taskName, token);
            }
        } finally {
            afterExecute();
        }
    }

    @Override
    protected String triggeringTaskTokenFromWorkContext(JobExecutionContext jobContext) {
        String token = jobContext.getMergedJobDataMap().getString(Task.TASK_TOKEN);
        return token;
    }

    @Override
    protected Authentication getAuthenticationFromWorkContext(JobExecutionContext jobContext) {
        Authentication authentication =
                (Authentication) jobContext.getMergedJobDataMap().get(Task.TASK_AUTHENTICATION);
        return authentication;
    }

    @Override
    protected boolean isRunOnlyOnMaster(JobExecutionContext jobContext) {
        return jobContext.getMergedJobDataMap().getBoolean(Task.TASK_RUN_ONLY_ON_PRIMARY);
    }
}