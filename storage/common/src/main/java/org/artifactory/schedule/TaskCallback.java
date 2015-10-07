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

import com.google.common.collect.Lists;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.ha.HaCommonAddon;
import org.artifactory.storage.spring.ArtifactoryStorageContext;
import org.artifactory.storage.spring.StorageContextHelper;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

/**
 * A callback for doing the actual work of a scheduled {@link Task}, with some typed work context
 *
 * @author yoavl
 */
public abstract class TaskCallback<C extends JobExecutionContext> {
    private static final Logger log = LoggerFactory.getLogger(TaskCallback.class);

    private static final InheritableThreadLocal<String> currentTaskToken = new InheritableThreadLocal<>();

    public static String currentTaskToken() {
        return currentTaskToken.get();
    }

    private TaskBase activeTask;
    private List<String> tasksStopped = Lists.newArrayList();

    protected abstract String triggeringTaskTokenFromWorkContext(C workContext);

    protected abstract Authentication getAuthenticationFromWorkContext(C callbackContext);

    protected abstract boolean isRunOnlyOnMaster(C jobContext);

    protected boolean beforeExecute(C callbackContext) {
        ArtifactoryStorageContext context = StorageContextHelper.get();
        if (context == null || !context.isReady()) {
            log.debug("Task {} was requested to execute before context {} completed initialization and will be skipped",
                    this, context);
            return false;
        }
        String taskToken = triggeringTaskTokenFromWorkContext(callbackContext);
        currentTaskToken.set(taskToken);
        Authentication authentication = getAuthenticationFromWorkContext(callbackContext);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        TaskService taskService = getTaskService();
        activeTask = taskService.getInternalActiveTask(taskToken, true);
        if (activeTask == null) {
            log.warn("Before execute: Could not locate active task with token {}. Task {} may have been canceled.",
                    taskToken, this);
            return false;
        }
        this.tasksStopped.clear();
        try {
            taskService.stopRelatedTasks(activeTask.getType(), this.tasksStopped, activeTask.getKeyValues());
        } catch (TaskImpossibleToStartException e) {
            log.debug("Task " + taskToken + " prohibited from running: " + e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.warn("Couldn't start task " + taskToken + ": " + e.getMessage());
            log.debug("Couldn't start task " + taskToken + ": " + e.getMessage(), e);
            return false;
        }

        //check for HA
        if (isRunOnlyOnMaster(callbackContext)) {
            HaCommonAddon haCommonAddon = context.beanForType(AddonsManager.class).addonByType(HaCommonAddon.class);
            if (!haCommonAddon.isPrimary()) {
                log.debug("Could not start task {}: the task can only run on master", taskToken);
                return false;
            }
        }

        return activeTask.started();
    }

    protected abstract void onExecute(C callbackContext) throws JobExecutionException;

    protected void afterExecute() {
        try {
            String token = currentTaskToken();
            if (token == null) {
                //We were not started (probably deferred due to context not being ready)
                return;
            }
            //Notify listeners that we are done
            if (activeTask != null) {
                activeTask.completed();
                if (activeTask.isSingleExecution()) {
                    TaskService taskService = getTaskService();
                    //Cancel the active task
                    taskService.cancelTask(token, true);
                }
            } else {
                log.warn("After execute: Could not locate active task with token {}. Task may have been canceled.",
                        token);
            }
            log.debug("Finished task {}.", token);
        } finally {
            if (!tasksStopped.isEmpty()) {
                try {
                    TaskService taskService = getTaskService();
                    for (String taskToken : tasksStopped) {
                        try {
                            taskService.resumeTask(taskToken);
                        } catch (Exception e) {
                            log.warn("After execute: Could not locate reactive task with token {}", taskToken);
                        }
                    }
                } finally {
                    tasksStopped.clear();
                }
            }
            SecurityContextHolder.getContext().setAuthentication(null);
            activeTask = null;
            currentTaskToken.remove();
        }
    }

    protected static TaskService getTaskService() {
        return StorageContextHelper.get().getTaskService();
    }
}