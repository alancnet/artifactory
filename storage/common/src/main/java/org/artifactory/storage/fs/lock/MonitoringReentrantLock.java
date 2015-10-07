/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2013 JFrog Ltd.
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

package org.artifactory.storage.fs.lock;

import java.util.Collection;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A ReentrantLock that exposes information about the owner thread
 *
 * @author Yossi Shaul
 */
public class MonitoringReentrantLock extends ReentrantLock {

    @Override
    public final Collection<Thread> getQueuedThreads() {
        return super.getQueuedThreads();
    }

    @Override
    public String toString() {
        Thread owner = getOwner();
        return super.toString() + (owner == null ? "" : " Owner: " + owner.getName() + " Id=" + owner.getId());
    }
}