package org.artifactory.addon.ha.semaphore;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * @author mamo
 */
public class JVMSemaphoreWrapper implements SemaphoreWrapper {
    private final Semaphore semaphore;

    public JVMSemaphoreWrapper(Semaphore semaphore) {
        this.semaphore = semaphore;
    }

    @Override
    public boolean tryAcquire() {
        return semaphore.tryAcquire();
    }

    @Override
    public boolean tryAcquire(long timeout, TimeUnit unit) throws InterruptedException {
        return semaphore.tryAcquire(timeout, unit);
    }

    @Override
    public void release() {
        semaphore.release();
    }
}
