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

package org.artifactory.request.aop;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.repo.AggregatingRequestListener;
import org.artifactory.api.repo.DirectRequestListener;
import org.artifactory.api.repo.Request;
import org.artifactory.api.repo.RequestListener;
import org.artifactory.security.AuthenticationHelper;
import org.artifactory.spring.InternalContextHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;

import java.util.Collection;
import java.util.Map;

/**
 * Request advice method interceptor
 *
 * @author Noam Tenne
 */
public class RequestAdvice implements MethodInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RequestAdvice.class);

    public RequestAdvice() {
        log.debug("Creating request advice interceptor");
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Request annotation = invocation.getMethod().getAnnotation(Request.class);
        boolean aggregate = annotation.aggregateEventsByTimeWindow();

        Map<String, ? extends RequestListener> listenerMap = getListenerMap(aggregate);

        String remoteAddress = getRemoteAddress();
        boolean performRequestAdvice = shouldPerformRequestAdvice(aggregate, remoteAddress);
        Collection<? extends RequestListener> listeners = listenerMap.values();

        onBeginRequest(remoteAddress, performRequestAdvice, listeners);

        Object result;
        try {
            result = invocation.proceed();
        } catch (Throwable t) {
            onException(remoteAddress, performRequestAdvice, listeners);
            throw t;
        }

        onEnd(remoteAddress, performRequestAdvice, listeners);
        return result;
    }

    /**
     * Returns the listener map relevant to the request annotation params
     *
     * @param aggregate Should current request annotation aggregate events
     * @return Listener map
     */
    private Map<String, ? extends RequestListener> getListenerMap(boolean aggregate) {
        //If aggregation of events is required, then search for the aggregating implementation
        if (aggregate) {
            return InternalContextHelper.get().beansForType(AggregatingRequestListener.class);
        }
        return InternalContextHelper.get().beansForType(DirectRequestListener.class);
    }

    /**
     * Returns the remote address of the request
     *
     * @return Remote address
     */
    private String getRemoteAddress() {
        Authentication authentication = AuthenticationHelper.getAuthentication();
        return (authentication != null) ? AuthenticationHelper.getRemoteAddress(authentication) : null;
    }

    /**
     * Indicates if the request advice (before, on-exception and after) should be performed
     *
     * @param aggregate     Indicates if aggregation of events is required
     * @param remoteAddress The remote address of the request
     * @return True if request advice should be performed. False if not
     */
    private boolean shouldPerformRequestAdvice(boolean aggregate, String remoteAddress) {
        /**
         * If aggregation is required, but the remote address is invalid, do not perform advice. The address is required
         * For the aggregation
         */
        if (aggregate && StringUtils.isBlank(remoteAddress)) {
            log.debug("Skipping request advice. Events should be aggregated, but no remote address could be resolved.");
            return false;
        }

        return true;
    }

    /**
     * Performs on begin request advice
     *
     * @param remoteAddress        The remote address of the request
     * @param performRequestAdvice Indicates if request advice should be performed
     * @param listeners            Listeners to invoke
     */
    private void onBeginRequest(String remoteAddress, boolean performRequestAdvice,
            Collection<? extends RequestListener> listeners) {
        //Invoke listeners only if advice should be performed
        if (performRequestAdvice) {
            for (RequestListener listener : listeners) {
                listener.onBeginRequest(remoteAddress);
            }
        }
    }

    /**
     * Performs on exception request advice
     *
     * @param remoteAddress        The remote address of the request
     * @param performRequestAdvice Indicates if request advice should be performed
     * @param listeners            Listeners to invoke
     */
    private void onException(String remoteAddress, boolean performRequestAdvice,
            Collection<? extends RequestListener> listeners) {
        //Invoke listeners only if advice should be performed
        if (performRequestAdvice) {
            for (RequestListener listener : listeners) {
                listener.onException(remoteAddress);
            }
        }
    }

    /**
     * Performs on end request advice
     *
     * @param remoteAddress        The remote address of the request
     * @param performRequestAdvice Indicates if request advice should be performed
     * @param listeners            Listeners to invoke
     */
    private void onEnd(String remoteAddress, boolean performRequestAdvice,
            Collection<? extends RequestListener> listeners) {
        //Invoke listeners only if advice should be performed
        if (performRequestAdvice) {
            for (RequestListener listener : listeners) {
                listener.onEndRequest(remoteAddress);
            }
        }
    }
}