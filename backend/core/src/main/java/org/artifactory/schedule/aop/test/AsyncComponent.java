/*
 * Copyright 2012 JFrog Ltd. All rights reserved.
 * JFROG PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package org.artifactory.schedule.aop.test;

import org.artifactory.api.repo.Async;

import java.util.concurrent.Future;

/**
 * This class is here only to simplify the async testing.
 *
 * @author Yossi Shaul
 */
public interface AsyncComponent {

    /**
     * @return Number of async invocations done on this component.
     */
    int asyncInvocationsCount();

    @Async
    Future<Integer> invokeAsync();

    /**
     * Sends a signal to the async method {@link AsyncComponent#invokeAsyncAndWaitForSignal()} to finish.
     */
    void signal();

    @Async
    void invokeAsyncAndWaitForSignal();

    @Async(delayUntilAfterCommit = true)
    void invokeAsyncDelayUntilAfterCommit();

    @Async(delayUntilAfterCommit = true, shared = true)
    void invokeAsyncShared();

}
