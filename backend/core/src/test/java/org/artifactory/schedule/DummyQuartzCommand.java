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

package org.artifactory.schedule;

import org.artifactory.schedule.quartz.QuartzCommand;
import org.artifactory.spring.InternalContextHelper;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yoavl
 */
@JobCommand(schedulerUser = TaskUser.CURRENT, manualUser = TaskUser.CURRENT)
public class DummyQuartzCommand extends QuartzCommand {

    public static final String FAIL = "FAIL";
    public static final String MSECS_TO_RUN = "MSECS_TO_RUN";

    private static final Logger log = LoggerFactory.getLogger(DummyQuartzCommand.class);

    private static long SLEEP = 50;

    public DummyQuartzCommand() {
    }

    @Override
    protected void onExecute(JobExecutionContext callbackContext) throws JobExecutionException {
        log.debug("Command for task " + currentTaskToken() + " is executing.");
        JobDataMap jobDataMap = callbackContext.getMergedJobDataMap();
        ArtifactoryHomeTaskTestStub artifactoryHome = getArtHomeStub();
        artifactoryHome.executed(currentTaskToken());
        if (jobDataMap.get(FAIL) != null &&
                jobDataMap.getBoolean(FAIL)) {
            log.info("Testing failing with exception!");
            artifactoryHome.failed(currentTaskToken());
            throw new RuntimeException("Voluntary failing execution to test Scheduler handling! No Worries!");
        }
        long msecsToRun = 500;
        if (jobDataMap.get(MSECS_TO_RUN) != null) {
            msecsToRun = jobDataMap.getLongValue(MSECS_TO_RUN);
        }
        long count = (long) (msecsToRun / (float) SLEEP);
        long i = 0L;
        for (; i < count; i++) {
            boolean shouldBreak = getTaskService().pauseOrBreak();
            if (shouldBreak) {
                log.debug("Command for task " + currentTaskToken() + " is breaking.");
                artifactoryHome.breaking(currentTaskToken());
                break;
            } else {
                try {
                    Thread.sleep(SLEEP);
                } catch (InterruptedException e) {
                    artifactoryHome.interrupted(currentTaskToken());
                    e.printStackTrace();
                    break;
                }
            }
        }
        if (i == count) {
            artifactoryHome.complete(currentTaskToken());
        }
        log.debug("Command for task " + currentTaskToken() + " has ended.");
    }

    protected ArtifactoryHomeTaskTestStub getArtHomeStub() {
        return (ArtifactoryHomeTaskTestStub) InternalContextHelper.get().getArtifactoryHome();
    }
}
