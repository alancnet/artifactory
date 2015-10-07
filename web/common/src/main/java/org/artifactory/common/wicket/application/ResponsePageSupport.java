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

package org.artifactory.common.wicket.application;

import org.apache.wicket.Page;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.cycle.AbstractRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.ComponentNotFoundException;
import org.apache.wicket.request.handler.IComponentRequestHandler;
import org.apache.wicket.request.handler.IPageRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

/**
 * @author Yoav Aharoni
 */
public class ResponsePageSupport extends AbstractRequestCycleListener {
    private static final Logger log = LoggerFactory.getLogger(ResponsePageSupport.class);

    private static final ThreadLocal<Page> PAGE_HOLDER = new ThreadLocal<>();

    @Override
    public void onRequestHandlerResolved(RequestCycle cycle, IRequestHandler handler) {
        if (getResponsePage() == null) {
            fetchPageFromHandler(handler);
        }
    }

    @Override
    public void onRequestHandlerScheduled(RequestCycle cycle, IRequestHandler handler) {
        fetchPageFromHandler(handler);
    }

    private void fetchPageFromHandler(IRequestHandler handler) {
        try {
            if (handler instanceof IComponentRequestHandler) {
                IRequestablePage page = ((IComponentRequestHandler) handler).getComponent().getPage();
                PAGE_HOLDER.set((Page) page);
            } else if (handler instanceof IPageRequestHandler) {
                IRequestablePage page = ((IPageRequestHandler) handler).getPage().getPage();
                PAGE_HOLDER.set((Page) page);
            }
        } catch (ComponentNotFoundException e) {
            /*
             * In the case in which the user session has expired between receiving the original page and requesting this
             * page a ComponentNotFoundException will be thrown with a non-zero page ID. It can be safely ignored
             */
            log.warn("{} . Session might have been expired", e.getMessage());
            log.debug("Failed to load component, ", e);
        }
    }

    @Override
    public void onEndRequest(RequestCycle cycle) {
        PAGE_HOLDER.remove();
    }

    @Nullable
    public static Page getResponsePage() {
        return PAGE_HOLDER.get();
    }
}
