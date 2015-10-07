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

import com.google.common.collect.ImmutableMap;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.schedule.TaskBase;
import org.artifactory.schedule.TaskCallback;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yoavl
 */
public class QuartzTask extends TaskBase {
    private static final Logger log = LoggerFactory.getLogger(QuartzTask.class);
    public static final String ARTIFACTORY_GROUP = "artifactory";

    private final Trigger trigger;
    private final JobDetail jobDetail;

    public static TaskBase createQuartzTask(Class<? extends TaskCallback> command, Trigger trigger,
            JobDetail jobDetail) {
        return new QuartzTask(command, trigger, jobDetail);
    }

    private QuartzTask(Class<? extends TaskCallback> command, Trigger trigger, JobDetail jobDetail) {
        super(command);
        this.trigger = trigger;
        this.jobDetail = jobDetail;
    }

    @Override
    public void addAttribute(String key, Object value) {
        jobDetail.getJobDataMap().put(key, value);
    }

    public ImmutableMap getAttributeMap() {
        return ImmutableMap.copyOf(jobDetail.getJobDataMap());
    }

    @Override
    public Object getAttribute(String key) {
        return jobDetail.getJobDataMap().get(key);
    }

    @Override
    public boolean isSingleExecution() {
        if (trigger instanceof SimpleTrigger) {
            return ((SimpleTrigger) trigger).getRepeatCount() == 0;
        }
        return !trigger.mayFireAgain();
    }

    /**
     * Schedule the task
     */
    @Override
    protected void scheduleTask() {
        Scheduler scheduler = getScheduler();
        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            throw new RuntimeException("Error in scheduling job: " + trigger.getKey(), e);
        }
    }

    /**
     * Unschedule the task
     */
    @Override
    protected void cancelTask() {
        Scheduler scheduler = getScheduler();
        try {
            JobKey jobKey = jobDetail.getKey();
            if (!scheduler.deleteJob(jobKey)) {
                log.info("Task " + jobKey + " already deleted from scheduler");
            }
        } catch (SchedulerException e) {
            throw new RuntimeException("Failed to unschedule previous job: " + trigger.getKey(), e);
        }
    }

    @Override
    public String getToken() {
        return jobDetail.getKey().toString();
    }

    private static Scheduler getScheduler() {
        return ContextHelper.get().beanForType(Scheduler.class);
    }
}