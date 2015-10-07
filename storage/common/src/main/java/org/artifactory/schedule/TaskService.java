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

import com.google.common.base.Predicate;
import org.artifactory.common.MutableStatusHolder;
import org.artifactory.spring.ReloadableBean;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * @author yoavl
 */
public interface TaskService extends ReloadableBean {

    /**
     * Starts a task and returns its token
     *
     * @param task
     * @param waitForRunning
     * @return the token of this task
     */
    String startTask(TaskBase task, boolean waitForRunning);

    /**
     * Starts a task and returns its token
     *
     * @param task
     * @param waitForRunning
     * @param propagateToMaster propagate to master on HA environment when job is
     *                          configured to {@link JobCommand#runOnlyOnPrimary()} and current member is not the master
     * @return the token of this task
     */
    String startTask(TaskBase task, boolean waitForRunning, boolean propagateToMaster);

    /**
     * Cancels and stops the task
     *
     * @param token The task token
     * @param wait  Whether to return immediately or wait for the tesk to be stopped
     * @return true if the task is actually stopped, false if already stopped
     */
    void stopTask(String token, boolean wait);

    /**
     * Pause a task
     *
     * @param token
     * @param wait
     */
    void pauseTask(String token, boolean wait);

    /**
     * If the task has been canceled (unscheduled) returns true to signal the caller that it needs to break from the
     * task's execution loop. If the task has been paused will simply pause the caller (and thus, the caller's execution
     * loop).
     *
     * @return true if the caller needs break from the task's callback execution loop
     */
    boolean pauseOrBreak();

    /**
     * Cancels and stops all active tasks of the specified type
     *
     * @param callbackType
     * @return true if tasks were stops, false if all already stopped
     */
    List<String> stopTasks(Class<? extends TaskCallback> callbackType);

    /**
     * Resume a paused task
     *
     * @param token
     * @return true if managed to resume, false if there are additional stop/pause holders
     */
    boolean resumeTask(String token);

    /**
     * Cancels (unschedules) a task
     *
     * @param wait
     */
    void cancelTask(String token, boolean wait);

    /**
     * Cancels all tasks of a certain type
     *
     * @param callbackType
     */
    void cancelTasks(@Nonnull Class<? extends TaskCallback> callbackType, boolean wait);

    /**
     * Cancels all tasks
     */
    void cancelAllTasks(boolean wait);

    /**
     * Awaits task execution to be finished
     *
     * @param token
     * @return true if execution completed, otherwise false if completed outside the execution loop
     */
    boolean waitForTaskCompletion(String token);

    /**
     * Returns the actual task object.
     * <p/>
     * THIS SHOULD BE CALLED BY PRIVATE API ONLY!
     * <p/>
     * Only exists since spring jdk-proxies with which the internal api works are interfaces.
     *
     * @param token
     * @param warnIfMissing
     */
    TaskBase getInternalActiveTask(String token, boolean warnIfMissing);

    /**
     * @param callbackType Type of callback to check if exists
     * @return True if a task with the callback type already exists
     */
    boolean hasTaskOfType(Class<? extends TaskCallback> callbackType);

    void cancelTasks(@Nullable Predicate<Task> predicate, boolean wait);

    List<TaskBase> getActiveTasks(@Nonnull Predicate<Task> predicate);

    void checkCanStartManualTask(Class<? extends TaskCallback> typeToRun, MutableStatusHolder statusHolder,
            Object... keyValues);

    void stopRelatedTasks(Class<? extends TaskCallback> typeToRun, List<String> tokenStopped, Object... keyValues);

    boolean waitForTaskCompletion(String token, long timeout);
}