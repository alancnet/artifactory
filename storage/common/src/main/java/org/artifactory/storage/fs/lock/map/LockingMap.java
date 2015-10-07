package org.artifactory.storage.fs.lock.map;

import java.util.concurrent.TimeUnit;

/**
 * A data structure which allows only one concurrent writer and many readers.
 *
 * @author Shay Yaakov
 */
public interface LockingMap {

    /**
     * Adds a path to the map while locking other threads from writing the same path concurrently.
     *
     * @param path     The path to add
     * @param timeout  the maximum time to wait for the lock
     * @param timeUnit the time unit of the {@code time} argument
     * @return {@code true} if the lock was acquired and {@code false}
     * if the waiting time elapsed before the lock was acquired
     * @throws InterruptedException if the current thread is interrupted
     *                              while acquiring the lock (and interruption of lock
     *                              acquisition is supported)
     */
    boolean tryAddAndLock(String path, long timeout, TimeUnit timeUnit) throws InterruptedException;

    /**
     * Removes the path from the map and releases it's lock.
     *
     * @param path The path to remove
     */
    void removeAndUnlock(String path);


    /**
     * Removes the path from the map and releases it's lock regardless the thread owner.
     *
     * @param path The path to remove
     */
    void removeAndForceUnlock(String path);

    /**
     * checks if the path is locked.
     *
     * @param path The path to remove
     */
    boolean isLocked(String path);
}
