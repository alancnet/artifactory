package org.artifactory.addon.ha.semaphore;

import java.util.concurrent.TimeUnit;

public interface SemaphoreWrapper {

    boolean tryAcquire();

    boolean tryAcquire(long timeout, TimeUnit unit) throws InterruptedException;

    void release();
}