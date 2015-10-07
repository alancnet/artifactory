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

package org.artifactory.schedule.aop;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.artifactory.api.repo.Async;
import org.artifactory.common.ConstantValues;
import org.artifactory.sapi.common.Lock;
import org.artifactory.schedule.CachedThreadPoolTaskExecutor;
import org.artifactory.spring.InternalArtifactoryContext;
import org.artifactory.spring.InternalContextHelper;
import org.artifactory.storage.fs.lock.aop.LockingAdvice;
import org.artifactory.storage.fs.session.StorageSession;
import org.artifactory.storage.fs.session.StorageSessionHolder;
import org.artifactory.storage.tx.SessionResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.framework.ReflectiveMethodInvocation;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * @author Yoav Landman
 */
public class AsyncAdvice implements MethodInterceptor {
    private static final Logger log = LoggerFactory.getLogger(AsyncAdvice.class);

    // holds all the pending and running invocations. used only during tests
    private ConcurrentHashMap<MethodInvocation, MethodInvocation> pendingInvocations = new ConcurrentHashMap<>();

    @Override
    public Future<?> invoke(final MethodInvocation invocation) throws Throwable {
        MethodAnnotation<Lock> lockMethodAnnotation = getMethodAnnotation(invocation, Lock.class);
        if (lockMethodAnnotation.annotation != null) {
            throw new RuntimeException("The @Async annotation cannot be used with the @Lock annotation. " +
                    "Use @Async#transactional=true instead: " + lockMethodAnnotation.method);
        }
        MethodAnnotation<Async> asyncMethodAnnotation = getMethodAnnotation(invocation, Async.class);
        boolean delayExecutionUntilCommit = asyncMethodAnnotation.annotation.delayUntilAfterCommit();
        boolean failIfNotScheduledFromTransaction =
                asyncMethodAnnotation.annotation.failIfNotScheduledFromTransaction();
        boolean inTransaction = LockingAdvice.isInTransaction();
        if (!inTransaction && delayExecutionUntilCommit) {
            if (failIfNotScheduledFromTransaction) {
                throw new IllegalStateException("Async invocation scheduled for after commit, " +
                        "cannot be scheduled outside a transaction: " + asyncMethodAnnotation.method);
            } else {
                log.debug("Async invocation scheduled for after commit, but not scheduled inside a transaction: {}",
                        asyncMethodAnnotation.method);
            }
        }

        TraceableMethodInvocation traceableInvocation =
                new TraceableMethodInvocation(invocation, Thread.currentThread().getName());
        log.trace("Adding: {}", traceableInvocation);
        if (ConstantValues.test.getBoolean()) {
            pendingInvocations.put(traceableInvocation, traceableInvocation);
        }
        try {
            if (delayExecutionUntilCommit && inTransaction) {
                //Schedule task submission for session save()
                StorageSession session = StorageSessionHolder.getSession();
                MethodCallbackSessionResource sessionCallbacks =
                        session.getOrCreateResource(MethodCallbackSessionResource.class);
                sessionCallbacks.setAdvice(this);
                sessionCallbacks.addInvocation(traceableInvocation, asyncMethodAnnotation.annotation.shared());
                //No future
                return null;
            } else {
                //Submit immediately
                Future<?> future = submit(traceableInvocation);
                return future;
            }
        } catch (Exception e) {
            // making sure to remove the invocation from the pending/executing
            removeInvocation(traceableInvocation);
            throw e;
        }
    }

    @SuppressWarnings({"unchecked"})
    private <T extends Annotation> MethodAnnotation<T> getMethodAnnotation(MethodInvocation invocation,
            Class<T> annotationClass) {
        Method method = invocation.getMethod();
        T annotation = method.getAnnotation(annotationClass);
        //Try to read the class level annotation if the interface is not found
        if (annotation != null) {
            return new MethodAnnotation(method, annotation);
        }
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(
                ((ReflectiveMethodInvocation) invocation).getProxy());
        Method specificMethod = AopUtils.getMostSpecificMethod(method, targetClass);
        annotation = specificMethod.getAnnotation(annotationClass);
        return new MethodAnnotation(specificMethod, annotation);
    }

    private Future<?> submit(final MethodInvocation invocation) {
        InternalArtifactoryContext context = InternalContextHelper.get();
        CachedThreadPoolTaskExecutor executor = context.beanForType(CachedThreadPoolTaskExecutor.class);
        Future<?> future = null;
        try {
            future = executor.submit(new Callable<Object>() {
                @Override
                public Object call() {
                    try {
                        if (TransactionSynchronizationManager.isSynchronizationActive()) {
                            //Sanity check we should never have a tx sync on an existing pooled thread
                            throw new IllegalStateException(
                                    "An async invocation (" + invocation.getMethod() + ") " +
                                            "should not be associated with an existing transaction.");
                        }
                        Object result = doInvoke(invocation);
                        // if the result is not of type Future don't bother returning it (unless you are fond of ClassCastExceptions)
                        if (result instanceof Future) {
                            return ((Future) result).get();
                        } else {
                            return null;
                        }
                    } catch (Throwable throwable) {
                        Throwable loggedThrowable;
                        if (invocation instanceof TraceableMethodInvocation) {
                            Throwable original = ((TraceableMethodInvocation) invocation).getThrowable();
                            original.initCause(throwable);
                            loggedThrowable = original;
                        } else {
                            loggedThrowable = throwable;
                        }
                        Method method;
                        if (invocation instanceof CompoundInvocation) {
                            method = ((CompoundInvocation) invocation).getLatestMethod();
                        } else {
                            method = invocation.getMethod();
                        }
                        log.error("Could not execute async method: '" + method + "'.", loggedThrowable);
                        return null;
                    }
                }
            });
        } catch (TaskRejectedException e) {
            log.error("Task {} rejected by scheduler: {}", invocation, e.getMessage());
            log.debug("Task {} rejected by scheduler: {}", invocation, e.getMessage(), e);
        }

        // only return the future result if the method returns a Future object
        if (!(invocation instanceof CompoundInvocation) &&
                Future.class.isAssignableFrom(invocation.getMethod().getReturnType())) {
            return future;
        } else {
            return null;
        }
    }

    Object doInvoke(MethodInvocation invocation) throws Throwable {
        if (invocation instanceof CompoundInvocation) {
            invocation.proceed();
            return null;    // multiple invocations -> no single return type
        }

        Authentication originalAuthentication = null;
        try {
            MethodAnnotation<Async> methodAnnotation =
                    getMethodAnnotation(((TraceableMethodInvocation) invocation).wrapped, Async.class);
            if (methodAnnotation.annotation == null) {
                throw new IllegalArgumentException(
                        "An async invocation (" + invocation.getMethod() +
                                ") should be used with an @Async annotated invocation.");
            }
            if (methodAnnotation.annotation.authenticateAsSystem()) {
                SecurityContext securityContext = SecurityContextHolder.getContext();
                originalAuthentication = securityContext.getAuthentication();
                InternalContextHelper.get().getSecurityService().authenticateAsSystem();
            }
            if (methodAnnotation.annotation.transactional()) {
                //Wrap in a transaction
                log.trace("Invoking {} in transaction", invocation);
                return new LockingAdvice().invoke(invocation);
            } else {
                log.trace("Invoking {} ", invocation);
                return invocation.proceed();
            }
        } catch (TaskRejectedException e) {
            log.warn("Task was rejected by scheduler: {}", e.getMessage());
            log.debug("Task was reject by scheduler", e);
            return null;
        } finally {
            if (originalAuthentication != null) {
                SecurityContext securityContext = SecurityContextHolder.getContext();
                securityContext.setAuthentication(originalAuthentication);
            }

            // remove the invocations here (called from the Compound also)
            removeInvocation(invocation);
        }
    }

    private void removeInvocation(MethodInvocation invocation) {
        if (ConstantValues.test.getBoolean()) {
            log.trace("Removing: {}", invocation);
            pendingInvocations.remove(invocation);
        }
    }

    public ImmutableSet<MethodInvocation> getCurrentInvocations() {
        return ImmutableSet.copyOf(pendingInvocations.keySet());
    }

    public void clearPendingInvocations() {
        if (ConstantValues.test.getBoolean()) {
            log.trace("Clearing all asyn invocations: {}", pendingInvocations);
            pendingInvocations.clear();
        }
    }

    /**
     * @param method The method to check if pending execution (usually the interface method, not the implementation!)
     * @return True if there is an pending (or running) async call to the given method
     */
    public boolean isPending(Method method) {
        // iterate on a copy to avoid ConcurrentModificationException
        for (MethodInvocation invocation : getCurrentInvocations()) {
            if (invocation instanceof CompoundInvocation) {
                ImmutableList<MethodInvocation> invocations = ((CompoundInvocation) invocation).getInvocations();
                for (MethodInvocation methodInvocation : invocations) {
                    if (method.equals(methodInvocation.getMethod())) {
                        return true;
                    }
                }
            } else {
                if (method.equals(invocation.getMethod())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static class MethodCallbackSessionResource implements SessionResource {
        AsyncAdvice advice;
        final List<MethodInvocation> invocations = new ArrayList<>();
        final CompoundInvocation sharedInvocations = new CompoundInvocation();

        public void setAdvice(AsyncAdvice advice) {
            this.advice = advice;
            sharedInvocations.setAdvice(advice);
        }

        public void addInvocation(TraceableMethodInvocation invocation, boolean shared) {
            if (shared) {
                sharedInvocations.add(invocation);
            } else {
                invocations.add(invocation);
            }
        }

        @Override
        public void afterCompletion(boolean commit) {
            if (commit) {
                //Submit the shared ones first
                if (!sharedInvocations.isEmpty()) {
                    advice.submit(sharedInvocations);
                }
                if (!invocations.isEmpty()) {
                    //Clear the invocations for this session and submit them for async execution
                    ArrayList<MethodInvocation> tmpInvocations = Lists.newArrayList(invocations);
                    //Reset internal state
                    invocations.clear();
                    for (MethodInvocation invocation : tmpInvocations) {
                        advice.submit(invocation);
                    }
                }
            } else {
                sharedInvocations.clear();
                invocations.clear();
            }
        }

        @Override
        public boolean hasPendingResources() {
            return !invocations.isEmpty();
        }

        @Override
        public void onSessionSave() {
        }
    }

    private static class TraceableMethodInvocation implements MethodInvocation {

        private final MethodInvocation wrapped;
        private final Throwable throwable;

        public TraceableMethodInvocation(MethodInvocation wrapped, String threadName) {
            this.wrapped = wrapped;
            String msg = "[" + threadName + "] async call to '" + wrapped.getMethod() + "' completed with error.";
            this.throwable = new Throwable(msg);
        }

        public Throwable getThrowable() {
            return throwable;
        }

        @Override
        public Method getMethod() {
            return wrapped.getMethod();
        }

        @Override
        public Object[] getArguments() {
            return wrapped.getArguments();
        }

        @Override
        public Object proceed() throws Throwable {
            return wrapped.proceed();
        }

        @Override
        public Object getThis() {
            return wrapped.getThis();
        }

        @Override
        public AccessibleObject getStaticPart() {
            return wrapped.getStaticPart();
        }

        @Override
        public String toString() {
            return wrapped.toString();
        }
    }

    private static class MethodAnnotation<T extends Annotation> {
        private MethodAnnotation(Method method, T annotation) {
            this.method = method;
            this.annotation = annotation;
        }

        private final Method method;
        private final T annotation;
    }
}