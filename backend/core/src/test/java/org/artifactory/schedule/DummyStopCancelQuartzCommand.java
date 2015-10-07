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

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * User: freds
 * Date: 7/5/11
 * Time: 11:45 AM
 */
public abstract class DummyStopCancelQuartzCommand extends DummyQuartzCommand {

    private ArtifactoryHomeTaskTestStub.TaskTestData taskData;

    @Override
    protected void onExecute(JobExecutionContext callbackContext) throws JobExecutionException {
        ArtifactoryHomeTaskTestStub artHome = getArtHomeStub();
        taskData = artHome.getTaskData(currentTaskToken());
        taskData.userNameOnExecute = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        taskData.taskStoppedOnExecute = new ArrayList<String>((List<String>) ReflectionTestUtils.getField(this,
                "tasksStopped"));
        super.onExecute(callbackContext);
    }

    @Override
    protected void afterExecute() {
        super.afterExecute();
        // May be null on exception in running
        if (taskData != null) {
            taskData.taskStoppedAfterExecute = new ArrayList<String>(
                    (List<String>) ReflectionTestUtils.getField(this, "tasksStopped"));
        }
    }
}
