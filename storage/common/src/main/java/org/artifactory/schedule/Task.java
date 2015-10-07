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

import org.artifactory.concurrent.StateAware;

/**
 * Represents the control unit over execution (as opposed to the actual work callback). Has a state that can be
 * controlled through the {@link TaskService}.
 * <p/>
 *
 * @author Yoav Landman
 */
public interface Task extends StateAware {
    String TASK_TOKEN = "TASK_TOKEN";
    String TASK_AUTHENTICATION = "TASK_AUTHENTICATION";
    String TASK_RUN_ONLY_ON_PRIMARY = "TASK_RUN_ONLY_ON_PRIMARY";
    String REPO_KEY = "repoKey";
    String PUSH_REPLICATION_URL = "repoUrl";

    boolean DEFAULT_TASK_RUN_ONLY_ON_PRIMARY = true; //default is run only on master

    Class getType();

    String getToken();

    boolean isRunning();

    Object getAttribute(String key);

    boolean keyEquals(Object... keyValues);

    boolean keyEquals(Task task);

    Object[] getKeyValues();
}
