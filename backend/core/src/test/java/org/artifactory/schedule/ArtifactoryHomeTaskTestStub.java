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

import org.artifactory.test.ArtifactoryHomeStub;
import org.testng.Assert;
import org.testng.collections.Maps;

import java.util.List;
import java.util.Map;

/**
 * User: freds
 * Date: 7/5/11
 * Time: 1:52 PM
 */
public class ArtifactoryHomeTaskTestStub extends ArtifactoryHomeStub {
    public static class TaskTestData {
        int nbExecutions;
        int nbFails;
        int nbBreaks;
        int nbInterrupted;
        int nbCompleted;
        String userNameOnExecute;
        List<String> taskStoppedOnExecute;
        List<String> taskStoppedAfterExecute;

        public void assertNbs(int nbExec, int nbBreak) {
            Assert.assertEquals(nbExecutions, nbExec);
            Assert.assertEquals(nbFails, 0);
            Assert.assertEquals(nbBreaks, nbBreak);
            Assert.assertEquals(nbInterrupted, 0);
            Assert.assertEquals(nbCompleted, nbExec - nbBreak);
        }

        public void assertPause(int nbExec, int nbPause) {
            Assert.assertEquals(nbExecutions, nbExec);
            Assert.assertEquals(nbFails, 0);
            Assert.assertEquals(nbBreaks, nbExec - nbPause);
            Assert.assertEquals(nbInterrupted, 0);
            Assert.assertEquals(nbCompleted, nbExec - nbPause);
        }

        public void assertFails(int nbExec, int fails) {
            Assert.assertEquals(nbExecutions, nbExec);
            Assert.assertEquals(nbFails, fails);
            Assert.assertEquals(nbBreaks, 0);
            Assert.assertEquals(nbInterrupted, 0);
            Assert.assertEquals(nbCompleted, nbExec - fails);
        }
    }

    private final Map<String, TaskTestData> testDataMap = Maps.newHashMap();

    public void executed(String taskTestId) {
        getTaskData(taskTestId).nbExecutions++;
    }

    public void failed(String taskTestId) {
        getTaskData(taskTestId).nbFails++;
    }

    public void breaking(String taskTestId) {
        getTaskData(taskTestId).nbBreaks++;
    }

    public void interrupted(String taskTestId) {
        getTaskData(taskTestId).nbInterrupted++;
    }

    public void complete(String taskTestId) {
        getTaskData(taskTestId).nbCompleted++;
    }

    public synchronized TaskTestData getTaskData(String taskTestId) {
        TaskTestData taskTestData = testDataMap.get(taskTestId);
        if (taskTestData == null) {
            taskTestData = new TaskTestData();
            testDataMap.put(taskTestId, taskTestData);
        }
        return taskTestData;
    }
}
