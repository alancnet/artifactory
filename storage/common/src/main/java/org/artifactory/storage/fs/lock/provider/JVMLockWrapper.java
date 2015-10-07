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

package org.artifactory.storage.fs.lock.provider;

import org.artifactory.storage.fs.lock.MonitoringReentrantLock;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * @author Shay Yaakov
 */
public class JVMLockWrapper implements LockWrapper {

    private MonitoringReentrantLock delegate;

    public JVMLockWrapper(MonitoringReentrantLock delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
        return delegate.tryLock(timeout, unit);
    }

    @Override
    public void unlock() {
        delegate.unlock();
    }

    @Override
    public boolean isLocked() {
        return delegate.isLocked();
    }

    @Override
    public boolean isHeldByCurrentThread() {
        return delegate.isHeldByCurrentThread();
    }

    @Override
    public Collection<Thread> getQueuedThreads() {
        return delegate.getQueuedThreads();
    }

    @Override
    public void destroy() {
        //noop
    }
}
