/*
 * Copyright 2012 JFrog Ltd. All rights reserved.
 * JFROG PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package org.artifactory.schedule.aop.test;

import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This class is here only to simplify the async testing.
 *
 * @author Yossi Shaul
 */
@Component
public class AsyncComponentImpl implements AsyncComponent {

    private int invocationsCounter = 0;
    private boolean signal;

    public int asyncInvocationsCount() {
        return invocationsCounter;
    }

    public Future<Integer> invokeAsync() {
        invocationsCounter++;
        return new Future<Integer>() {
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            public boolean isCancelled() {
                return false;
            }

            public boolean isDone() {
                return true;
            }

            public Integer get() throws InterruptedException, ExecutionException {
                return invocationsCounter;
            }

            public Integer get(long timeout, TimeUnit unit)
                    throws InterruptedException, ExecutionException, TimeoutException {
                return get();
            }
        };
    }

    public void signal() {
        signal = true;
    }

    public void invokeAsyncAndWaitForSignal() {
        while (!signal) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void invokeAsyncDelayUntilAfterCommit() {
        // just for testing
    }

    public void invokeAsyncShared() {
        // just for testing
    }

}