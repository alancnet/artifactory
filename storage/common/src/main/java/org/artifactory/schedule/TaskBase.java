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

import org.artifactory.common.ConstantValues;
import org.artifactory.concurrent.LockingException;
import org.artifactory.concurrent.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Yoav Landman
 */
public abstract class TaskBase implements Task {
    private static final Logger log = LoggerFactory.getLogger(TaskBase.class);

    private TaskState state;
    //TODO: [by fsi] should use StateManager
    private final ReentrantLock stateSync;
    private final Condition stateChanged;
    private final Condition completed;
    private boolean executed;

    // Initialized from JobCommand annotation
    private boolean singleton;
    private boolean manuallyActivated;

    /**
     * Prevents resuming a task until all stoppers/pausers have resumed it
     */
    private int resumeBarriersCount;
    private final Class<? extends TaskCallback> callbackType;

    @SuppressWarnings({"unchecked"})
    protected TaskBase(Class<? extends TaskCallback> callbackType) {
        state = TaskState.VIRGIN;
        stateSync = new ReentrantLock();
        stateChanged = stateSync.newCondition();
        completed = stateSync.newCondition();
        executed = false;
        resumeBarriersCount = 0;
        this.callbackType = callbackType;
    }

    @Override
    public State getInitialState() {
        return TaskState.VIRGIN;
    }

    public boolean waitingForProcess() {
        return state == TaskState.PAUSING || state == TaskState.STOPPING;
    }

    @Override
    public boolean isRunning() {
        return state == TaskState.RUNNING || waitingForProcess();
    }

    public boolean processActive() {
        return isRunning() || state == TaskState.PAUSED;
    }

    void schedule(boolean waitForRunning) {
        lockState();
        try {
            scheduleTask();
            guardedTransitionToState(TaskState.SCHEDULED, waitForRunning);
        } finally {
            unlockState();
        }
    }

    void cancel(boolean wait) {
        lockState();
        try {
            log.trace("Entering cancel with state {} on {}", state, this);
            if (processActive() && !wait) {
                throw new IllegalStateException("Cannot cancel immediately an active task " + this);
            }

            if (processActive()) {
                log.trace("Waiting for active task: {} to finish.", this);
                if (waitingForProcess()) {
                    guardedWaitForNextStep();
                }
                if (processActive()) {
                    guardedTransitionToState(TaskState.STOPPING, wait);
                }
            }
            if (state == TaskState.CANCELED) {
                log.info("Task {} already canceled.", this);
            } else {
                log.debug("Canceling task: {}.", this);
                guardedSetState(TaskState.CANCELED);
            }
            cancelTask();
        } finally {
            unlockState();
        }
    }

    void pause(boolean wait) {
        lockState();
        try {
            log.trace("Entering pause with state {} on {}", state, this);
            if (state == TaskState.VIRGIN) {
                throw new IllegalStateException("Cannot stop a virgin task.");
            } else if (state == TaskState.RUNNING) {
                if (!wait) {
                    throw new IllegalStateException("Cannot pause immediately a running task " + this);
                }
                resumeBarriersCount++;
                log.trace("resumeBarriersCount++ to {} after pause while running on {}", resumeBarriersCount, this);
                guardedTransitionToState(TaskState.PAUSING, wait);
            } else if (state == TaskState.STOPPING) {
                // Already stopping, waiting for stop
                if (!wait) {
                    throw new IllegalStateException("Cannot pause immediately a stopping task " + this);
                }
                resumeBarriersCount++;
                log.trace("resumeBarriersCount++ to {} after pause while stopping on {}", resumeBarriersCount, this);
                guardedWaitForNextStep();
            } else if (state == TaskState.PAUSED || state == TaskState.PAUSING) {
                // Already paused, just count the barrier
                resumeBarriersCount++;
                log.trace("resumeBarriersCount++ to {} after pause already paused on {}", resumeBarriersCount, this);
            } else if (state == TaskState.CANCELED) {
                // Task canceled, forget it => do nothing
            } else {
                // Not running, just count the barrier, and set to stop
                resumeBarriersCount++;
                log.trace("resumeBarriersCount++ to {} after pause on {}", resumeBarriersCount, this);
                if (state != TaskState.STOPPED) {
                    guardedSetState(TaskState.STOPPED);
                }
            }
        } finally {
            unlockState();
        }
    }

    /**
     * Stops but does not unschedule the task (can transition back to running state)
     *
     * @param wait
     */
    void stop(boolean wait) {
        lockState();
        try {
            log.trace("Entering stop with state {} on {}", state, this);
            if (state == TaskState.VIRGIN) {
                throw new IllegalStateException("Cannot stop a virgin task.");
            } else if (state == TaskState.RUNNING || state == TaskState.PAUSING) {
                if (!wait) {
                    throw new IllegalStateException("Cannot stop immediately a running task " + this);
                }
                //Stop
                resumeBarriersCount++;
                log.trace("resumeBarriersCount++ to {} after stop on {}", resumeBarriersCount, this);
                guardedTransitionToState(TaskState.STOPPING, wait);
            } else if (state == TaskState.CANCELED) {
                // Task canceled, forget it => do nothing
            } else if (state == TaskState.STOPPED || state == TaskState.STOPPING) {
                // Already stopped, just count the barrier
                resumeBarriersCount++;
                log.trace("resumeBarriersCount++ to {} after stop already stopped on {}", resumeBarriersCount, this);
                if (state == TaskState.STOPPING) {
                    guardedWaitForNextStep();
                }
            } else {
                //For both stop and pause
                resumeBarriersCount++;
                log.trace("resumeBarriersCount++ to {} after stop on {}", resumeBarriersCount, this);
                guardedSetState(TaskState.STOPPED);
            }
        } finally {
            unlockState();
        }
    }

    boolean resume() {
        lockState();
        try {
            if (state == TaskState.CANCELED) {
                throw new IllegalStateException("Cannot resume a canceled task.");
            }
            if (resumeBarriersCount > 0) {
                resumeBarriersCount--;
            } else {
                log.info("Skipping resume since there are no active resume barriers " +
                        "(probably invoked resume() more than needed).");
                return true;
            }
            log.trace("resumeBarriersCount-- to {} after resume on {}", resumeBarriersCount, this);
            if (resumeBarriersCount > 0) {
                log.debug("Cannot resume while there are still {} resume barriers.", resumeBarriersCount);
                return false;
            }
            if (state == TaskState.PAUSED || state == TaskState.PAUSING) {
                guardedSetState(TaskState.RUNNING);
            } else if (state == TaskState.STOPPED || state == TaskState.STOPPING) {
                //Nothing to do for single execution - either resume from pause or reached stopped
                //if resume by a different thread
                if (!isSingleExecution()) {
                    guardedSetState(TaskState.SCHEDULED);
                }
            }
            return true;
        } finally {
            unlockState();
        }
    }

    @Override
    public Class<? extends TaskCallback> getType() {
        return callbackType;
    }

    public abstract String getToken();

    /**
     * Starts or schedules the task
     */
    protected abstract void scheduleTask();

    /**
     * Stops or unschedules the task
     */
    protected abstract void cancelTask();

    /**
     * Needs to be called from the execution loop of the task that wants to check if to pause or to stop
     *
     * @return
     */
    public boolean blockIfPausedAndShouldBreak() {
        lockState();
        //if running continue, if pausing transition to pause else exit
        try {
            if (state == TaskState.PAUSING) {
                guardedSetState(TaskState.PAUSED);
            }
            try {
                log.trace("Entering wait for out of paused on: {}", this);
                int tries = ConstantValues.taskCompletionLockTimeoutRetries.getInt();
                long timeout = ConstantValues.locksTimeoutSecs.getLong();
                while (state == TaskState.PAUSED) {
                    stateChanged.await(timeout, TimeUnit.SECONDS);
                    tries--;
                    if (tries <= 0) {
                        throw new LockingException("Task " + this + " paused for more than " +
                                ConstantValues.taskCompletionLockTimeoutRetries.getInt() + " times.");
                    }
                    log.trace("One wait for out of paused from on {}", this);
                }
            } catch (InterruptedException e) {
                catchInterrupt(TaskState.PAUSED);
            }
            return state != TaskState.RUNNING;
        } finally {
            unlockState();
        }
    }

    /**
     * Whether this task is non-cyclic one and is canceled after a single execution
     *
     * @return
     */
    public boolean isSingleExecution() {
        return false;
    }

    /**
     * Weather the task with this callback type should be unique on the task service.
     * i.e., not other task with the same type should ever be running.
     *
     * @return True if this task should be unique
     */
    public boolean isSingleton() {
        return singleton;
    }

    public void setSingleton(boolean singleton) {
        this.singleton = singleton;
    }

    public boolean isManuallyActivated() {
        return manuallyActivated;
    }

    public void setManuallyActivated(boolean manuallyActivated) {
        this.manuallyActivated = manuallyActivated;
    }

    public boolean wasCompleted() {
        if (!isSingleExecution()) {
            throw new UnsupportedOperationException("Does not support waitForCompletion on cyclic tasks.");
        }
        return executed && (state == TaskState.STOPPED || state == TaskState.CANCELED);
    }

    /**
     * Wait for the task to stop running
     *
     * @return
     */
    boolean waitForCompletion(long timeout) {
        if (!isSingleExecution()) {
            throw new UnsupportedOperationException("Does not support waitForCompletion on cyclic tasks.");
        }
        if (!isRunning()) {
            // The task may have already complete
            return true;
        }
        boolean completed = false;
        lockState();
        try {
            try {
                //Wait forever (tries * lock timeout) until it finished the current execution
                if (timeout == 0) {
                    timeout = ConstantValues.locksTimeoutSecs.getLong() * ConstantValues.taskCompletionLockTimeoutRetries.getInt();
                }
                long start = System.currentTimeMillis();
                while (true) {
                    // If already executed (passed to running state) and now stooped or canceled
                    //   => It means already completed
                    if (executed) {
                        if (state == TaskState.STOPPED || state == TaskState.CANCELED) {
                            completed = true;
                            break;
                        }
                    }
                    // Waiting on the completed condition
                    boolean success = this.completed.await(ConstantValues.locksTimeoutSecs.getLong(), TimeUnit.SECONDS);
                    if (success) {
                        completed = true;
                        break;
                    }
                    if (start + timeout >= System.currentTimeMillis()) {
                        throw new LockingException("Waited for task " + this + " more than " + timeout + "ms.");
                    }
                }
            } catch (InterruptedException e) {
                catchInterrupt(state);
            }
        } finally {
            unlockState();
        }
        return completed;
    }

    boolean started() {
        boolean shouldExecute = false;
        lockState();
        //Check if should run
        try {
            if (state == TaskState.SCHEDULED) {
                guardedSetState(TaskState.RUNNING);
                shouldExecute = true;
            }
        } finally {
            unlockState();
        }
        return shouldExecute;
    }

    void completed() {
        lockState();
        try {
            if (state == TaskState.STOPPED) {
                //Do nothing
                return;
            }
            if (state == TaskState.PAUSED || state == TaskState.STOPPING || state == TaskState.PAUSING) {
                guardedSetState(TaskState.STOPPED);
            } else if (state != TaskState.CANCELED) {
                if (isSingleExecution()) {
                    guardedSetState(TaskState.STOPPED);
                } else if (state != TaskState.SCHEDULED) {
                    //Could be on SCHEDULED if resumed after stopped
                    guardedSetState(TaskState.SCHEDULED);
                }
            }
            completed.signal();
        } finally {
            unlockState();
        }
    }

    private <V> V guardedTransitionToState(TaskState newState, boolean waitForNextStep) {
        V result = null;
        if (state == newState) {
            return result;
        }
        guardedSetState(newState);
        if (waitForNextStep) {
            guardedWaitForNextStep();
        }
        return result;
    }

    private TaskState guardedWaitForNextStep() {
        long timeout = ConstantValues.locksTimeoutSecs.getLong();
        return guardedWaitForNextStep(timeout);
    }

    private TaskState guardedWaitForNextStep(long timeout) {
        TaskState oldState = state;
        TaskState newState = oldState;
        try {
            log.trace("Entering wait for next step from {} on: {}", oldState, this);
            while (state == oldState) {
                boolean success = stateChanged.await(timeout, TimeUnit.SECONDS);
                if (!success) {
                    throw new LockingException(
                            "Timeout after " + timeout + " seconds when trying to wait for next state in '" + oldState +
                                    "'."
                    );
                }
                newState = state;
                log.trace("Exiting wait for next step from {} to {} on {}", oldState, newState, this);
            }
        } catch (InterruptedException e) {
            catchInterrupt(oldState);
        }
        return newState;
    }

    private void guardedSetState(TaskState newState) {
        boolean validNewState = state.canTransitionTo(newState);
        if (!validNewState) {
            throw new IllegalArgumentException("Cannot transition from " + this.state + " to " + newState + ".");
        }
        log.trace("Changing state: {}: {}-->{}", this.toString(), this.state, newState);
        state = newState;
        if (state == TaskState.RUNNING) {
            executed = true;
        } else if (state == TaskState.SCHEDULED) {
            executed = false;
        }
        stateChanged.signal();
    }

    private void lockState() {
        try {
            int holdCount = stateSync.getHoldCount();
            log.trace("Thread {} trying lock (activeLocks={}) on {}",
                    Thread.currentThread(), holdCount, this);
            if (holdCount > 0) {
                //Clean all and throw
                while (holdCount > 0) {
                    stateSync.unlock();
                    holdCount--;
                }
                throw new LockingException("Locking an already locked task state: " +
                        this + " active lock(s) already active!");
            }
            boolean success = stateSync.tryLock() ||
                    stateSync.tryLock(getStateLockTimeOut(), TimeUnit.SECONDS);
            if (!success) {
                throw new LockingException(
                        "Could not acquire state lock in " + getStateLockTimeOut() + " secs");
            }
        } catch (InterruptedException e) {
            log.warn("Interrupted while trying to lock {}.", this);
        }
    }

    private long getStateLockTimeOut() {
        return ConstantValues.locksTimeoutSecs.getLong();
    }

    private void unlockState() {
        log.trace("Unlocking {}", this);
        stateSync.unlock();
    }

    private static void catchInterrupt(TaskState state) {
        log.warn("Interrupted during state wait from '{}'.", state);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TaskBase)) {
            return false;
        }
        TaskBase base = (TaskBase) o;
        return getToken().equals(base.getToken());
    }

    @Override
    public int hashCode() {
        return getToken().hashCode();
    }

    @Override
    public String toString() {
        return getToken();
    }

    public abstract void addAttribute(String key, Object value);

    @Override
    public boolean keyEquals(Object... keyValues) {
        JobCommand jobCommand = this.getType().getAnnotation(JobCommand.class);
        String[] keys = jobCommand.keyAttributes();
        if (keyValues.length != keys.length) {
            throw new IllegalArgumentException("Cannot compare key values for task " + getType() + "\n" +
                    "Received " + Arrays.toString(keyValues) + " and expected values for " + Arrays.toString(keys));
        }
        for (int i = 0; i < keys.length; i++) {
            Object attribute = getAttribute(keys[i]);
            if (attribute == null) {
                log.warn("Task attribute is NULL: {}, given keyValues: {}", keys[i], keyValues);
                return false;
            }
            if (!attribute.equals(keyValues[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean keyEquals(Task task) {
        JobCommand otherJobCommand = (JobCommand) task.getType().getAnnotation(JobCommand.class);
        JobCommand myJobCommand = this.getType().getAnnotation(JobCommand.class);
        String[] myKeys = myJobCommand.keyAttributes();
        String[] otherKeys = otherJobCommand.keyAttributes();
        if (!Arrays.equals(myKeys, otherKeys)) {
            throw new IllegalArgumentException(
                    "Cannot compare key values between task " + this + " and task " + task + "\n" +
                            "Keys " + Arrays.toString(myKeys) + " not equals to " + Arrays.toString(otherKeys)
            );
        }
        for (String key : myKeys) {
            if (!getAttribute(key).equals(task.getAttribute(key))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Object[] getKeyValues() {
        JobCommand myJobCommand = this.getType().getAnnotation(JobCommand.class);
        String[] myKeys = myJobCommand.keyAttributes();
        Object[] result = new Object[myKeys.length];
        for (int i = 0; i < myKeys.length; i++) {
            result[i] = getAttribute(myKeys[i]);
        }
        return result;
    }

    public enum TaskState implements State {
        VIRGIN,
        SCHEDULED,
        RUNNING,
        PAUSING,
        STOPPING,
        STOPPED, //Will not start if refired by the scheduler
        PAUSED, //Blocked by executions thread (and will not start if refired by scheduler)
        CANCELED;

        @Override
        @SuppressWarnings({"SuspiciousMethodCalls"})
        public boolean canTransitionTo(State newState) {
            Set<TaskState> states = getPossibleTransitionStates(this);
            return states.contains(newState);
        }

        @SuppressWarnings({"OverlyComplexMethod"})
        private static Set<TaskState> getPossibleTransitionStates(TaskState oldState) {
            HashSet<TaskState> states = new HashSet<>();
            switch (oldState) {
                case VIRGIN:
                    states.add(TaskState.SCHEDULED);
                    states.add(TaskState.CANCELED);
                    return states;
                case SCHEDULED:
                    states.add(TaskState.RUNNING);
                    states.add(TaskState.PAUSING);
                    states.add(TaskState.STOPPING);
                    states.add(TaskState.STOPPED);
                    states.add(TaskState.CANCELED);
                    return states;
                case RUNNING:
                    states.add(TaskState.PAUSING);
                    states.add(TaskState.STOPPING);
                    states.add(TaskState.STOPPED);
                    states.add(TaskState.CANCELED);
                    states.add(TaskState.SCHEDULED);
                    return states;
                case PAUSING:
                    states.add(TaskState.PAUSED);
                    states.add(TaskState.RUNNING);
                    states.add(TaskState.STOPPING);
                    states.add(TaskState.STOPPED);
                    states.add(TaskState.CANCELED);
                    return states;
                case PAUSED:
                    states.add(TaskState.RUNNING);
                    states.add(TaskState.STOPPING);
                    states.add(TaskState.STOPPED);
                    states.add(TaskState.CANCELED);
                    return states;
                case STOPPING:
                    states.add(TaskState.CANCELED);
                case STOPPED:
                    states.add(TaskState.STOPPED);
                    states.add(TaskState.RUNNING);
                    states.add(TaskState.SCHEDULED);
                    states.add(TaskState.CANCELED);
                    return states;
                case CANCELED:
                    //Unscheduled
                    return states;
                default:
                    throw new IllegalArgumentException(
                            "No transitions defined for state: " + oldState);
            }
        }
    }

}