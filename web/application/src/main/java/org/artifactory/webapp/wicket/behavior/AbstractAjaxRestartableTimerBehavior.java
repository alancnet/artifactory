/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
* Based on: org.apache.wicket.ajax.AbstractAjaxRestartableTimerBehavior
*
*/

package org.artifactory.webapp.wicket.behavior;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.util.time.Duration;

/**
 * A behavior that generates an AJAX restartable update callback at a regular interval.
 * A similar class will be available with the release of Wicket 6.0.
 *
 * @author Natan Schochet
 */
public abstract class AbstractAjaxRestartableTimerBehavior extends AbstractDefaultAjaxBehavior {

    private static final long serialVersionUID = 1L;

    /**
     * The update interval
     */
    private Duration updateInterval;

    private boolean stopped = false;

    private boolean headRendered = false;

    private String id;

    public AbstractAjaxRestartableTimerBehavior(final Duration updateInterval, String id) {
        if (updateInterval == null || updateInterval.getMilliseconds() <= 0) {
            throw new IllegalArgumentException("Invalid update interval");
        }
        this.updateInterval = updateInterval;
        this.id = id;
    }

    /**
     * Stops the timer
     */
    public final void stop() {
        stopped = true;
    }

    /**
     * Stops the timer
     */
    public final void stop(String id) {
        this.id = id;
        stopped = true;
    }

    /**
     * Sets the update interval duration. This method should only be called within the
     * {@link #onTimer(AjaxRequestTarget)} method.
     *
     * @param updateInterval
     */
    public final void setUpdateInterval(Duration updateInterval) {
        if (updateInterval == null || updateInterval.getMilliseconds() <= 0) {
            throw new IllegalArgumentException("Invalid update interval");
        }
        this.updateInterval = updateInterval;
    }

    /**
     * Returns the update interval
     *
     * @return The update interval
     */
    public final Duration getUpdateInterval() {
        return updateInterval;
    }

    @Override
    public void renderHead(Component component, IHeaderResponse response) {
        super.renderHead(component, response);

        WebRequest request = (WebRequest) RequestCycle.get().getRequest();

        if (!stopped && (!headRendered || !request.isAjax())) {
            headRendered = true;
            response.renderOnDomReadyJavaScript(getJsTimeoutCall(updateInterval));
        }
    }

    /**
     * @param updateInterval Duration between AJAX callbacks
     * @return JS script
     */
    protected final String getJsTimeoutCall(final Duration updateInterval) {
        // this might look strange, but it is necessary for IE not to leak :(
        if (!stopped) {
            return id + "=setTimeout(\"" + getCallbackScript() + "\", " + updateInterval.getMilliseconds() +
                    ");";
        }
        return "setTimeout(\"" + getCallbackScript() + "\", " + updateInterval.getMilliseconds() +
                ");";


    }

    @Override
    protected CharSequence getCallbackScript() {
        return generateCallbackScript("wicketAjaxGet('" + getCallbackUrl() + "'");
    }

    /**
     * @see org.apache.wicket.ajax.AbstractDefaultAjaxBehavior#getPreconditionScript()
     */
    @Override
    protected CharSequence getPreconditionScript() {
        String precondition = null;
        if (!(getComponent() instanceof Page)) {
            String componentId = getComponent().getMarkupId();
            precondition = "var c = Wicket.$('" + componentId +
                    "'); return typeof(c) != 'undefined' && c != null";
        }
        return precondition;
    }

    /**
     * @see org.apache.wicket.ajax.AbstractDefaultAjaxBehavior#respond(org.apache.wicket.ajax.AjaxRequestTarget)
     */
    @Override
    protected final void respond(final AjaxRequestTarget target) {
        onTimer(target);

        if (!stopped && isEnabled(getComponent())) {
            target.getHeaderResponse().renderOnLoadJavaScript(getJsTimeoutCall(updateInterval));
        }
    }

    /**
     * Listener method for the AJAX timer event.
     *
     * @param target The request target
     */
    protected abstract void onTimer(final AjaxRequestTarget target);

    /**
     * @return {@code true} if has been stopped via {@link #stop()}
     */
    public final boolean isStopped() {
        return stopped;
    }

    public final void restart(final AjaxRequestTarget target) {
        if (stopped) {
            stopped = false;
            headRendered = false;
            if (target != null) {
                target.addComponent(getComponent());
            }
        }
    }

    public final void start() {

        stopped = false;
        headRendered = false;

    }

}
