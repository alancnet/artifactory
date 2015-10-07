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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.testng.Assert.assertTrue;

/**
 * @author Yoav Landman
 * @author Fred Simon
 */
@Test(sequential = true, enabled = false)
public class TaskServiceTest extends TaskServiceTestBase {
    private static final Logger log = LoggerFactory.getLogger(TaskServiceTest.class);

    @Test(enabled = false, invocationCount = 6, threadPoolSize = 3)
    public void testServiceSynchronization() throws Exception {
        TaskBase task1 = TaskUtils.createRepeatingTask(DummyQuartzCommand.class, 100, 200);
        ArtifactoryHomeTaskTestStub homeStub = getOrCreateArtifactoryHomeStub();
        taskService.startTask(task1, false);
        taskService.pauseTask(task1.getToken(), false);
        homeStub.getTaskData(task1.getToken()).assertNbs(0, 0);
        TaskBase task2 = TaskUtils.createRepeatingTask(DummyQuartzCommand.class, 100, 0);
        task2.addAttribute(DummyQuartzCommand.MSECS_TO_RUN, "400");
        taskService.startTask(task2, false);
        Thread.sleep(600);
        taskService.stopTask(task1.getToken(), true);
        taskService.stopTask(task2.getToken(), true);
        homeStub.getTaskData(task2.getToken()).assertNbs(2, 1);
        homeStub.getTaskData(task1.getToken()).assertNbs(0, 0);
        taskService.cancelTask(task1.getToken(), true);
        taskService.cancelTask(task2.getToken(), true);
        Assert.assertNull(taskService.getInternalActiveTask(task1.getToken(), false));
        Assert.assertNull(taskService.getInternalActiveTask(task2.getToken(), false));
    }

    @Test(enabled = false)
    public void testStopWhileRunning() throws Exception {
        TaskBase task1 = TaskUtils.createRepeatingTask(DummyQuartzCommand.class, 0, 0);
        ArtifactoryHomeTaskTestStub homeStub = getOrCreateArtifactoryHomeStub();
        taskService.startTask(task1, true);
        assertTrue(task1.isRunning());
        try {
            taskService.pauseTask(task1.getToken(), false);
            Assert.fail("Should not be able to pause with no wait on running task");
        } catch (Exception e) {
            log.debug("Fail to pause running task OK!");
        }
        taskService.stopTask(task1.getToken(), true);
        // Need to wait for cancel afterExecute to be activated
        Thread.sleep(50);
        Assert.assertNull(taskService.getInternalActiveTask(task1.getToken(), false));
        homeStub.getTaskData(task1.getToken()).assertNbs(1, 1);
    }

    @Test(enabled = false)
    public void testCancelWhileRunning() throws Exception {
        TaskBase task1 = TaskUtils.createRepeatingTask(DummyQuartzCommand.class, 0, 0);
        ArtifactoryHomeTaskTestStub homeStub = getOrCreateArtifactoryHomeStub();
        taskService.startTask(task1, true);
        assertTrue(task1.isRunning());
        taskService.cancelTask(task1.getToken(), true);
        Assert.assertFalse(task1.isRunning());
        Assert.assertNull(taskService.getInternalActiveTask(task1.getToken(), false));
        assertTrue(task1.waitForCompletion(0));
        homeStub.getTaskData(task1.getToken()).assertNbs(1, 1);
    }

    @Test(enabled = false, invocationCount = 6, threadPoolSize = 3)
    public void testMutliResume() throws Exception {
        final CyclicBarrier pauseBarrier1 = new CyclicBarrier(2);
        final CyclicBarrier pauseBarrier2 = new CyclicBarrier(2);
        final TaskBase tsk = TaskUtils.createRepeatingTask(DummyQuartzCommand.class, 0, 0);
        Callable<String> c1 = new Callable<String>() {
            @Override
            public String call() throws Exception {
                try {
                    log.debug("........... PAUSING-1");
                    taskService.pauseTask(tsk.getToken(), true);
                    pauseBarrier1.await();
                    pauseBarrier2.await();
                } catch (Exception e) {
                    e.printStackTrace();
                    return e.getMessage();
                }
                return null;
            }
        };
        Callable<String> c2 = new Callable<String>() {
            @Override
            public String call() throws Exception {
                boolean resumed;
                try {
                    pauseBarrier1.await();
                    log.debug("........... PAUSING-2");
                    taskService.pauseTask(tsk.getToken(), true);
                    pauseBarrier2.await();
                    log.debug("........... RESUMING-1");
                    resumed = taskService.resumeTask(tsk.getToken());
                    if (resumed) {
                        return "Resume barriers are broken - resumed too early.";
                    }
                    log.debug("........... RESUMING-2");
                    resumed = taskService.resumeTask(tsk.getToken());
                    if (!resumed) {
                        return "Resume barriers are broken - expected resume was not be possible.";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return e.getMessage();
                }
                return null;
            }
        };
        taskService.startTask(tsk, true);
        ExecutorService executorService = Executors.newCachedThreadPool();
        Future<String> c1Result = executorService.submit(c1);
        Future<String> c2Result = executorService.submit(c2);
        String error = c2Result.get();
        Assert.assertNull(error, error);
        error = c1Result.get();
        Assert.assertNull(error, error);
        assertTrue(taskService.waitForTaskCompletion(tsk.getToken()));
        getOrCreateArtifactoryHomeStub().getTaskData(tsk.getToken()).assertNbs(1, 0);
        Thread.sleep(50);
        Assert.assertNull(taskService.getInternalActiveTask(tsk.getToken(), false));
    }

    @Test(enabled = false)
    public void testCancelWhenWaitingOnStateTransition() throws Exception {
        final TaskBase tsk = TaskUtils.createRepeatingTask(DummyQuartzCommand.class, 1000, 0);
        final String tskToken = taskService.startTask(tsk, true);
        ArtifactoryHomeTaskTestStub.TaskTestData taskData = getOrCreateArtifactoryHomeStub().getTaskData(tskToken);
        assertTrue(tsk.isRunning());
        taskService.pauseTask(tskToken, true);
        assertTrue(tsk.processActive());
        taskService.cancelTask(tsk.getToken(), true);
        Assert.assertFalse(tsk.processActive());
        Assert.assertNull(taskService.getInternalActiveTask(tsk.getToken(), false));
        taskData.assertNbs(1, 1);
    }

    @Test(enabled = false)
    public void testMultiSingleExecution() throws Exception {
        taskService.cancelAllTasks(true);
        QuartzCommand cmd = new DummyQuartzCommand();
        final TaskBase tsk1 = TaskUtils.createRepeatingTask(cmd.getClass(), 0, 0);
        //Make the task run at least 500ms, so that it wont finish before the second conflicting one is scheduled
        tsk1.addAttribute(DummyQuartzCommand.MSECS_TO_RUN, "500");
        tsk1.setSingleton(true);
        taskService.startTask(tsk1, false);
        log.debug("........... STARTED TSK1");
        final TaskBase tsk2 = TaskUtils.createRepeatingTask(cmd.getClass(), 0, 0);
        tsk2.setSingleton(true);
        try {
            taskService.startTask(tsk2, false);
            Assert.fail("Should not be able to run 2 singleton tasks concurrently.");
        } catch (IllegalStateException e) {
            //Good - we expected it
        }
        Assert.assertTrue(taskService.waitForTaskCompletion(tsk1.getToken()));
        Assert.assertTrue(taskService.waitForTaskCompletion(tsk2.getToken()));
    }

    @Test(enabled = false)
    public void testSingleExecutionWithError() throws Exception {
        taskService.cancelAllTasks(true);
        QuartzCommand cmd = new DummyQuartzCommand();
        final TaskBase tsk1 = TaskUtils.createRepeatingTask(cmd.getClass(), 0, 0);
        tsk1.setSingleton(true);
        tsk1.addAttribute(DummyQuartzCommand.FAIL, Boolean.TRUE);
        taskService.startTask(tsk1, true);
        log.debug("........... WAITING FOR TSK1");
        assertTrue(taskService.waitForTaskCompletion(tsk1.getToken()));
        getOrCreateArtifactoryHomeStub().getTaskData(tsk1.getToken()).assertFails(1, 1);
        // Need to wait for cancel afterExecute to be activated
        Thread.sleep(50);
        Assert.assertNull(taskService.getInternalActiveTask(tsk1.getToken(), false));
        final TaskBase tsk2 = TaskUtils.createRepeatingTask(cmd.getClass(), 0, 0);
        tsk2.setSingleton(true);
        taskService.startTask(tsk2, true);
        taskService.waitForTaskCompletion(tsk2.getToken());
        getOrCreateArtifactoryHomeStub().getTaskData(tsk2.getToken()).assertNbs(1, 0);
        // Need to wait for cancel afterExecute to be activated
        Thread.sleep(50);
        Assert.assertNull(taskService.getInternalActiveTask(tsk2.getToken(), false));
    }

    @Test(enabled = false)
    public void testConcurrentStopResumes() throws Exception {
        final CyclicBarrier barrier1 = new CyclicBarrier(2);
        final CyclicBarrier barrier2 = new CyclicBarrier(2);
        final TaskBase tsk = TaskUtils.createRepeatingTask(DummyQuartzCommand.class, 0, 0);
        Callable<String> c1 = new Callable<String>() {
            @Override
            public String call() throws Exception {
                barrier1.await();
                log.debug("........... PAUSING-1");
                taskService.pauseTask(tsk.getToken(), true);
                barrier2.await();
                return null;
            }
        };
        Callable<String> c2 = new Callable<String>() {
            @Override
            public String call() throws Exception {
                barrier1.await();
                log.debug("........... STOPING-2");
                taskService.stopTask(tsk.getToken(), true);
                barrier2.await();
                return null;
            }
        };
        taskService.startTask(tsk, true);
        Thread.sleep(200);
        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.submit(c1);
        executorService.submit(c2);
        Assert.assertTrue(taskService.waitForTaskCompletion(tsk.getToken()));
        taskService.cancelTask(tsk.getToken(), true);
    }

    @Test(enabled = false)
    public void testDoubleResume() throws Exception {
        TaskBase task1 = TaskUtils.createRepeatingTask(DummyQuartzCommand.class, 1000, 0);
        taskService.startTask(task1, true);
        taskService.pauseTask(task1.getToken(), true);
        taskService.stopTask(task1.getToken(), true);
        log.debug("........... RESUMING-1");
        taskService.resumeTask(task1.getToken());
        log.debug("........... RESUMING-2");
        taskService.resumeTask(task1.getToken());
        log.debug("........... RESUMING-3");
        taskService.resumeTask(task1.getToken());
        log.debug("........... RESUMING-4");
        taskService.resumeTask(task1.getToken());
        log.debug("........... DONE");
        taskService.cancelTask(task1.getToken(), true);
    }

    @Test(enabled = false)
    public void testMisfireIgnored() throws Exception {
        // Create task running every second and lasting for 1.2s
        TaskBase task1 = TaskUtils.createCronTask(DummyQuartzCommand.class, "* * * * * ?");
        task1.addAttribute(DummyQuartzCommand.MSECS_TO_RUN, "1200");
        taskService.startTask(task1, true);
        Thread.sleep(3000);
        taskService.cancelTask(task1.getToken(), true);
        // Should have executed 3 times and one break
        getOrCreateArtifactoryHomeStub().getTaskData(task1.getToken()).assertNbs(3, 1);
    }

    @Test(enabled = false)
    public void testMisfireIgnoredOnPause() throws Exception {
        // Create task running every second and pause for 1.2s
        TaskBase task1 = TaskUtils.createCronTask(DummyQuartzCommand.class, "* * * * * ?");
        taskService.startTask(task1, true);
        Thread.sleep(100);
        taskService.pauseTask(task1.getToken(), true);
        Thread.sleep(1200);
        Assert.assertTrue(taskService.resumeTask(task1.getToken()));
        Thread.sleep(100);
        taskService.cancelTask(task1.getToken(), true);
        // Should have executed 3 times and one break
        getOrCreateArtifactoryHomeStub().getTaskData(task1.getToken()).assertNbs(1, 1);
    }
}