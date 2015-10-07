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
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.HaAddon;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.MutableStatusHolder;
import org.artifactory.config.InternalCentralConfigService;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.mbean.MBeanRegistrationService;
import org.artifactory.schedule.mbean.ManagedExecutor;
import org.artifactory.spring.ContextReadinessListener;
import org.artifactory.spring.Reloadable;
import org.artifactory.state.model.ArtifactoryStateManager;
import org.artifactory.storage.db.DbService;
import org.artifactory.version.CompoundVersionDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author yoavl
 */
@Service
@Reloadable(beanClass = TaskService.class,
        initAfter = {DbService.class, InternalCentralConfigService.class, ArtifactoryStateManager.class})
public class TaskServiceImpl implements TaskService, ContextReadinessListener {
    private static final Logger log = LoggerFactory.getLogger(TaskServiceImpl.class);

    @Autowired
    private CachedThreadPoolTaskExecutor executor;

    @Autowired
    private AddonsManager addonsManager;

    private ConcurrentMap<String, TaskBase> activeTasksByToken = new ConcurrentHashMap<>();
    private ConcurrentMap<String, TaskBase> inactiveTasksByToken = new ConcurrentHashMap<>();

    private AtomicBoolean openForScheduling = new AtomicBoolean();

    @Override
    public void init() {
        java.util.concurrent.Executor concurrentExecutor = executor.getConcurrentExecutor();
        if (concurrentExecutor instanceof ArtifactoryConcurrentExecutor) {
            ContextHelper.get().beanForType(MBeanRegistrationService.class).
                    register(new ManagedExecutor((ArtifactoryConcurrentExecutor) concurrentExecutor),
                            "Executor Pools", "Task Service");
        }
    }

    @Override
    public void destroy() {
        cancelAllTasks(true);
        //Shut down the executor service to terminate any async operations not managed by the task service
        executor.destroy();
    }

    @Override
    public void convert(CompoundVersionDetails source, CompoundVersionDetails target) {
    }

    @Override
    public void reload(CentralConfigDescriptor oldDescriptor) {
    }

    @Override
    public void onContextCreated() {
    }

    @Override
    public void onContextReady() {
        openForScheduling.set(true);
        for (String taskKey : inactiveTasksByToken.keySet()) {
            //Check the readiness and task status again since it can change
            if (openForScheduling.get()) {
                TaskBase task = inactiveTasksByToken.remove(taskKey);
                if (task != null) {
                    activeTasksByToken.put(taskKey, task);
                    task.schedule(false);
                }
            }
        }
    }

    @Override
    public void onContextUnready() {
        openForScheduling.set(false);
    }

    @Override
    public String startTask(TaskBase task, boolean waitForRunning) {
        if (ContextHelper.get().isOffline()) {
            log.error("Artifactory is in offline state, task are not allowed to run during offline state!");
            return task.getToken();
        }
        String token = task.getToken();
        ConcurrentMap<String, TaskBase> taskMap =
                openForScheduling.get() ? activeTasksByToken : inactiveTasksByToken;
        canBeStarted(task);
        if (task.isSingleton()) {
            //Reject duplicate singleton tasks - by type + check automatically during insert that we are not
            //rescheduling the same task instance
            if (hasTaskOfType(task.getType(), taskMap, false) || taskMap.putIfAbsent(token, task) != null) {
                throw new IllegalStateException("Cannot start a singleton task more than once (" + task + ").");
            }
        } else if (taskMap.put(token, task) != null) {
            log.warn("Overriding an active task with the same token {}.", task);
        }
        if (openForScheduling.get()) {
            task.schedule(waitForRunning);
        }
        return task.getToken();
    }

    @Override
    public String startTask(TaskBase task, boolean waitForRunning, boolean propagateToMaster) {
        if (propagateToMaster &&
                task.getAttribute(TaskBase.TASK_RUN_ONLY_ON_PRIMARY).equals(true) &&
                !addonsManager.addonByType(HaAddon.class).isPrimary()) {
            log.debug("Propagating task to master {}", task.getToken());
            addonsManager.addonByType(HaAddon.class).propagateTaskToPrimary(task);
            return task.getToken();
        } else {
            return startTask(task, waitForRunning);
        }
    }

    @Override
    public void cancelTask(String token, boolean wait) {
        TaskBase task = activeTasksByToken.get(token);
        if (task != null) {
            task.cancel(wait);
        } else {
            log.warn("Could not find task {} to cancel.", token);
        }
        activeTasksByToken.remove(token);
    }

    @Override
    public List<TaskBase> getActiveTasks(@Nonnull Predicate<Task> predicate) {
        List<TaskBase> results = Lists.newArrayList();
        for (TaskBase task : activeTasksByToken.values()) {
            if (predicate.apply(task)) {
                results.add(task);
            }
        }
        return results;
    }

    @Override
    public void cancelTasks(@Nonnull Predicate<Task> predicate, boolean wait) {
        List<TaskBase> toCancel = getActiveTasks(predicate);
        for (Task task : toCancel) {
            //TODO: Don't wait on each job in a serial fashion
            cancelTask(task.getToken(), wait);
        }
    }

    @Override
    public void cancelTasks(@Nonnull final Class<? extends TaskCallback> callbackType, boolean wait) {
        cancelTasks(TaskUtils.createPredicateForType(callbackType), wait);
    }

    @Override
    public void cancelAllTasks(boolean wait) {
        cancelTasks(new Predicate<Task>() {
            @Override
            public boolean apply(@Nullable Task input) {
                return true;
            }
        }, wait);
    }

    @Override
    public void stopTask(String token, boolean wait) {
        TaskBase task = activeTasksByToken.get(token);
        if (task != null) {
            task.stop(wait);
        } else {
            log.warn("Could not find task {} to stop.", token);
        }
    }

    public void canBeStarted(TaskBase task) {
        Class<? extends TaskCallback> callbackType = task.getType();
        JobCommand jobCommand = callbackType.getAnnotation(JobCommand.class);
        if (jobCommand == null) {
            throw new IllegalArgumentException(
                    "Callback type " + callbackType.getName() + " does not have the " +
                            JobCommand.class.getName() + " annotation!");
        }
        if (task.isManuallyActivated() && jobCommand.manualUser() == TaskUser.INVALID) {
            throw new IllegalStateException("Cannot start task (" + task + ") manually!" +
                    " Manual user not defined!");
        }
        boolean hasKey = jobCommand.keyAttributes().length != 0;
        if (!jobCommand.singleton() && !hasKey) {
            return;
        }
        EnumMap<StopStrategy, Predicate<Task>> predicatePerStrategy = getPredicatePerStrategy(callbackType,
                task.getKeyValues());
        // Should not count myself :)
        Predicate<Task> notMyself = getNotMySelfPredicate();

        // Only impossible are blocker here
        Predicate<Task> impossibleFilter = predicatePerStrategy.get(StopStrategy.IMPOSSIBLE);
        if (impossibleFilter != null) {
            for (TaskBase activeTask : getActiveTasks(Predicates.and(notMyself, impossibleFilter))) {
                if (task.isManuallyActivated()) {
                    if (activeTask.processActive()) {
                        throw new IllegalStateException("Cannot start task (" + task + ") manually!" +
                                " Task " + activeTask + " is active!");
                    }
                    if (activeTask.isManuallyActivated() && !activeTask.wasCompleted()) {
                        throw new IllegalStateException("Cannot start task (" + task + ") manually!" +
                                " Another manual task " + activeTask + " exists and is not completed!");
                    }
                } else {
                    // Starting impossible filter of same type means singleton or key based singleton match
                    if (activeTask.getType().equals(callbackType)) {
                        if (activeTask.isManuallyActivated()) {
                            if (!activeTask.wasCompleted()) {
                                throw new IllegalStateException("Cannot schedule task (" + task + ")!" +
                                        " Another manual task of same type and key " + activeTask + " exists and is not completed!");
                            }
                        } else {
                            throw new IllegalStateException("Cannot schedule task (" + task + ")!" +
                                    " Another task of same type and key " + activeTask + " is already scheduled!");
                        }
                    }
                }
            }
        }
    }

    @Override
    public void checkCanStartManualTask(Class<? extends TaskCallback> typeToRun, MutableStatusHolder statusHolder,
            Object... keyValues) {
        JobCommand jobCommand = typeToRun.getAnnotation(JobCommand.class);
        if (ContextHelper.get().isOffline()) {
            statusHolder.error("Artifactory is in offline state, task are not allowed to run during offline state!",
                    log);
            return;
        }
        if (jobCommand == null) {
            statusHolder.error(
                    "Task type " + typeToRun.getName() + " does not have the " +
                            JobCommand.class.getName() + " annotation!", log);
            return;
        }
        if (jobCommand.manualUser() == TaskUser.INVALID) {
            statusHolder.error("Task type " + typeToRun.getName() + " is not defined to run manually!", log);
            return;
        }
        String currentToken = TaskCallback.currentTaskToken();
        if (currentToken != null) {
            statusHolder.error(
                    "Cannot check for manual run with status from inside a running task: " + currentToken, log);
            return;
        }

        // Extract task filters for each stop strategy
        EnumMap<StopStrategy, Predicate<Task>> predicatePerStrategy = getPredicatePerStrategy(typeToRun, keyValues);

        // Check first for impossible to run
        Predicate<Task> impossibleFilter = predicatePerStrategy.get(StopStrategy.IMPOSSIBLE);
        if (impossibleFilter != null) {
            for (TaskBase activeTask : getActiveTasks(impossibleFilter)) {
                if (activeTask.processActive()) {
                    statusHolder.error(
                            "Task " + typeToRun.getName() + " cannot stop a mandatory related job " +
                                    activeTask.getType().getName() + " while it's running!",
                            log);
                }
                if (activeTask.getType().equals(
                        typeToRun) && activeTask.isManuallyActivated() && !activeTask.wasCompleted()) {
                    statusHolder.error(
                            "Another manual task " + typeToRun.getName() + " is still active!",
                            log);
                }
            }
        }

        // Then check for stopped
        Predicate<Task> stopFilter = predicatePerStrategy.get(StopStrategy.STOP);
        if (stopFilter != null) {
            for (TaskBase activeTask : getActiveTasks(stopFilter)) {
                if (activeTask.processActive()) {
                    statusHolder.warn(
                            "Task " + activeTask.getType().getName() + " will be stop by running " + typeToRun.getName() + " !",
                            log);
                }
            }
        }

        // Then pause what's needed
        Predicate<Task> pauseFilter = predicatePerStrategy.get(StopStrategy.PAUSE);
        if (pauseFilter != null) {
            for (TaskBase activeTask : getActiveTasks(pauseFilter)) {
                if (activeTask.processActive()) {
                    statusHolder.warn(
                            "Task " + activeTask.getType().getName() + " will be paused by running " + typeToRun.getName() + " !",
                            log);
                }
            }
        }

        if (!statusHolder.isError()) {
            statusHolder.debug("Task " + typeToRun.getName() + " can run.", log);
        }
    }

    @Override
    public void stopRelatedTasks(Class<? extends TaskCallback> typeToRun, List<String> tokenStopped,
            Object... keyValues) {
        // Extract task filters for each stop strategy
        EnumMap<StopStrategy, Predicate<Task>> predicatePerStrategy = getPredicatePerStrategy(typeToRun, keyValues);

        // Each predicates should not count myself :)
        Predicate<Task> notMyself = getNotMySelfPredicate();

        // Stop early if impossible to run
        Predicate<Task> impossibleFilter = predicatePerStrategy.get(StopStrategy.IMPOSSIBLE);
        if (impossibleFilter != null) {
            for (TaskBase activeTask : getActiveTasks(Predicates.and(notMyself, impossibleFilter))) {
                if (activeTask.processActive()) {
                    throw new TaskImpossibleToStartException(
                            "Job " + typeToRun.getName() + " cannot stop related job " + activeTask + " while it's running!");
                }
                // Immediate stop fails if task in running mode
                activeTask.stop(false);
                // Single execution task that are stopped don't need to be resumed because they'll die.
                // So, don't add the token to the list for stopped single exec tasks
                if (!activeTask.isSingleExecution()) {
                    tokenStopped.add(activeTask.getToken());
                }
            }
        }

        // Then stop what's needed
        Predicate<Task> stopFilter = predicatePerStrategy.get(StopStrategy.STOP);
        if (stopFilter != null) {
            for (TaskBase activeTask : getActiveTasks(Predicates.and(notMyself, stopFilter))) {
                // Just stop and keep for resume
                activeTask.stop(true);
                // Single execution task that are stopped don't need to be resumed because they'll die.
                // So, don't add the token to the list for stopped single exec tasks
                if (!activeTask.isSingleExecution()) {
                    tokenStopped.add(activeTask.getToken());
                }
            }
        }

        // Then pause what's needed
        Predicate<Task> pauseFilter = predicatePerStrategy.get(StopStrategy.PAUSE);
        if (pauseFilter != null) {
            for (TaskBase activeTask : getActiveTasks(Predicates.and(notMyself, pauseFilter))) {
                // Just stop and always keep for resume, since even single execution paused should be resumed
                activeTask.pause(true);
                tokenStopped.add(activeTask.getToken());
            }
        }
    }

    private Predicate<Task> getNotMySelfPredicate() {
        final String currentToken = TaskCallback.currentTaskToken();
        return new Predicate<Task>() {
            @Override
            public boolean apply(@Nullable Task input) {
                // null task always false
                if (input == null) {
                    return false;
                }
                return currentToken == null || !currentToken.equals(input.getToken());
            }
        };
    }

    private EnumMap<StopStrategy, Predicate<Task>> getPredicatePerStrategy(
            Class<? extends TaskCallback> typeToRun,
            final Object... keyValues) {
        EnumMap<StopStrategy, Predicate<Task>> result = new EnumMap<>(
                StopStrategy.class);
        JobCommand toRunJobCommand = typeToRun.getAnnotation(JobCommand.class);
        final String[] keys = toRunJobCommand.keyAttributes();
        if (keys.length != keyValues.length) {
            throw new IllegalArgumentException(
                    "Cannot check if task " + typeToRun + " can start without key values!\n" +
                            "Received " + Arrays.toString(keyValues) + " and expected values for " + Arrays.toString(
                            keys));
        }
        for (StopCommand stopCommand : toRunJobCommand.commandsToStop()) {
             Class<? extends TaskCallback> callbackType;
            // get callback type from command or command name
            callbackType = getCommand(typeToRun,stopCommand);
            if ((callbackType.getModifiers() & Modifier.ABSTRACT) != 0) {
                throw new IllegalArgumentException(
                        "Job command definition for " + typeToRun.getName() + " contain an abstract class to stop!");
            }
            StopStrategy stopStrategy = stopCommand.strategy();
            TaskTypePredicate taskPredicate = (TaskTypePredicate) result.get(stopStrategy);
            if (stopCommand.useKey()) {
                taskPredicate = getTaskPredicateForKey(callbackType, taskPredicate, keyValues);
            } else {
                taskPredicate = getTaskPredicateForType(callbackType, taskPredicate);
            }
            result.put(stopStrategy, taskPredicate);
        }

        if (toRunJobCommand.singleton()) {
            // If singleton and currently running => Impossible to stop
            TaskTypePredicate taskPredicate = (TaskTypePredicate) result.get(StopStrategy.IMPOSSIBLE);
            taskPredicate = getTaskPredicateForType(typeToRun, taskPredicate);
            result.put(StopStrategy.IMPOSSIBLE, taskPredicate);
        }

        if (keys.length != 0) {
            // If key based and currently running => Impossible to stop
            TaskTypePredicate taskPredicate = (TaskTypePredicate) result.get(StopStrategy.IMPOSSIBLE);
            taskPredicate = getTaskPredicateForKey(typeToRun, taskPredicate, keyValues);
            result.put(StopStrategy.IMPOSSIBLE, taskPredicate);
        }

        return result;
    }

    /**
     * get task to do stop command on it
     *
     * @param typeToRun
     * @param stopCommand - stop command to execute on job
     * @return callback task class
     */
    private Class<? extends TaskCallback> getCommand(Class<? extends TaskCallback> typeToRun, StopCommand stopCommand){

        Class<? extends TaskCallback> callbackType = null;
        ScheduleJobEnum commandName =  stopCommand.commandName();
        // is command name define bu no command type
        if (!commandName.equals(ScheduleJobEnum.DUMMY_JOB) && stopCommand.command() == DummyJob.class){
             try {
                callbackType = (Class<? extends TaskCallback>) Class.forName(commandName.jobName);
            } catch (ClassNotFoundException e) {
                log.error("class commandName not found ",e);
             }
        }
        // no command name or command type has been define
        else if (commandName.equals(ScheduleJobEnum.DUMMY_JOB) && stopCommand.command() == DummyJob.class){
            log.error("no job command type or name was define for job class: "+typeToRun.getSimpleName());
         }
        else {//command type has been define
           callbackType = stopCommand.command();
        }
        return callbackType;
    }

    private TaskTypePredicate getTaskPredicateForType(Class<? extends TaskCallback> callbackType,
            TaskTypePredicate taskPredicate) {
        if (taskPredicate == null) {
            taskPredicate = new TaskTypePredicate(callbackType);
        } else {
            taskPredicate = new TaskTypePredicate(taskPredicate, callbackType);
        }
        return taskPredicate;
    }

    private TaskTypePredicate getTaskPredicateForKey(final Class<? extends TaskCallback> callbackType,
            TaskTypePredicate taskPredicate, final Object[] keyValues) {
        if (taskPredicate == null) {
            taskPredicate = new TaskTypePredicate();
        }
        taskPredicate.addSubPredicate(new Predicate<Task>() {
            @Override
            public boolean apply(@Nullable Task input) {
                return (input != null) && ClassUtils.isAssignable(callbackType, input.getType()) &&
                        input.keyEquals(keyValues);
            }
        });
        return taskPredicate;
    }

    @Override
    public List<String> stopTasks(Class<? extends TaskCallback> callbackType) {
        String currentToken = TaskCallback.currentTaskToken();
        List<String> results = Lists.newArrayList();
        for (TaskBase task : activeTasksByToken.values()) {
            // If callback type is null means all, cannot stop myself
            if ((callbackType == null || ClassUtils.isAssignable(callbackType, task.getType()))
                    && (currentToken == null || !task.getToken().equals(currentToken))) {
                //TODO: Don't wait on each job in a serial fashion
                stopTask(task.getToken(), true);
                results.add(task.getToken());
            }
        }
        return results;
    }

    @Override
    public void pauseTask(String token, boolean wait) {
        TaskBase task = getInternalActiveTask(token, true);
        if (task != null) {
            task.pause(wait);
        } else {
            log.warn("Could not find task {} to pause.", token);
        }
    }

    @Override
    public boolean resumeTask(String token) {
        TaskBase task = getInternalActiveTask(token, false);
        if (task != null) {
            return task.resume();
        } else {
            log.debug("Could not find task {} to resume.", token);
            return false;
        }
    }

    @Override
    public boolean waitForTaskCompletion(String token) {
        TaskBase task = getInternalActiveTask(token, false);
        //Check for null since task may have already been canceled
        return task == null || task.waitForCompletion(0);
    }

    @Override
    public boolean waitForTaskCompletion(String token, long timeout) {
        TaskBase task = getInternalActiveTask(token, false);
        //Check for null since task may have already been canceled
        return task == null || task.waitForCompletion(timeout);
    }

    @Override
    public boolean pauseOrBreak() {
        String token = TaskCallback.currentTaskToken();
        // If not in a task the token is null
        if (token == null) {
            //Since this is called by external clients, it may be called directly or in tests with no surrounding task
            log.debug("No current task is found on thread - nothing to block or pause.");
            return false;
        }
        TaskBase task = getInternalActiveTask(token, true);
        if (task == null) {
            log.warn("Could not find task {} to check block on.", token);
            return false;
        }
        return task.blockIfPausedAndShouldBreak();
    }

    @Override
    public TaskBase getInternalActiveTask(String token, boolean warnIfMissing) {
        if (token == null) {
            throw new IllegalArgumentException("Could not find task with null token");
        }
        TaskBase task = activeTasksByToken.get(token);
        if (warnIfMissing && task == null) {
            log.warn("Could not locate active task with token {}. Task may have been canceled.", token);
        }
        return task;
    }

    @Override
    public boolean hasTaskOfType(Class<? extends TaskCallback> callbackType) {
        return hasTaskOfType(callbackType, activeTasksByToken, true);
    }

    private boolean hasTaskOfType(Class<? extends TaskCallback> callbackType, ConcurrentMap<String, TaskBase> taskMap,
            boolean withManual) {
        if (callbackType == null) {
            return false;
        }
        for (TaskBase task : taskMap.values()) {
            if (ClassUtils.isAssignable(callbackType, task.getType()) &&
                    !(withManual && task.isManuallyActivated())) {
                return true;
            }
        }
        return false;
    }
}
