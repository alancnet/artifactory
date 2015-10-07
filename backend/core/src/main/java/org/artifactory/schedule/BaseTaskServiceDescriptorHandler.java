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

import com.google.common.collect.Sets;
import org.artifactory.descriptor.TaskDescriptor;
import org.artifactory.spring.InternalContextHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

/**
 * User: freds
 * Date: 7/10/11
 * Time: 5:54 PM
 */
public abstract class BaseTaskServiceDescriptorHandler<T extends TaskDescriptor>
        implements TaskServiceDescriptorHandler<T> {
    private static final Logger log = LoggerFactory.getLogger(BaseTaskServiceDescriptorHandler.class);

    public void unschedule() {
        TaskService taskService = InternalContextHelper.get().getBean(TaskService.class);
        log.info("Removing all job " + jobName() + " from task service handler.");
        taskService.cancelTasks(getAllPredicate(), true);
    }

    public void reschedule() {
        TaskService taskService = InternalContextHelper.get().getBean(TaskService.class);
        List<T> newDescriptors = getNewDescriptors();
        if (newDescriptors.isEmpty()) {
            log.debug("No " + jobName() + " configured. " + jobName() + " is disabled.");
            taskService.cancelTasks(getAllPredicate(), true);
            return;
        }
        Set<T> oldTasksNotToCancel = Sets.newHashSet();
        for (T newDesc : newDescriptors) {
            T oldDescriptor = findOldFromNew(newDesc);
            if (oldDescriptor != null && oldDescriptor.sameTaskDefinition(newDesc)) {
                oldTasksNotToCancel.add(oldDescriptor);
            } else {
                if (oldDescriptor != null) {
                    oldTasksNotToCancel.add(oldDescriptor);
                    taskService.cancelTasks(getPredicate(oldDescriptor), true);
                }
                activate(newDesc, false);
            }
        }
        List<T> oldDescriptors = getOldDescriptors();
        if (!oldDescriptors.isEmpty()) {
            for (T oldDesc : oldDescriptors) {
                if (!oldTasksNotToCancel.contains(oldDesc)) {
                    taskService.cancelTasks(getPredicate(oldDesc), true);
                }
            }
        }
    }

}
