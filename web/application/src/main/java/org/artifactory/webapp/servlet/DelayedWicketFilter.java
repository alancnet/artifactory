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

package org.artifactory.webapp.servlet;

import org.apache.wicket.protocol.http.WicketFilter;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import java.util.concurrent.BlockingQueue;

public class DelayedWicketFilter extends WicketFilter implements DelayedInit {
    private FilterConfig filterConfig;

    @Override
    @SuppressWarnings({"unchecked"})
    public void init(final boolean isServlet, final FilterConfig filterConfig) throws ServletException {
        BlockingQueue<Filter> waiters = (BlockingQueue<Filter>) filterConfig.getServletContext()
                .getAttribute(APPLICATION_CONTEXT_LOCK_KEY);
        this.filterConfig = filterConfig;
        if (waiters != null) {
            waiters.add(this);
        } else {
            //Servlet 2.5 lazy filter initing
            delayedInit();
        }
    }

    @Override
    public void delayedInit() throws ServletException {
        super.init(false, filterConfig);
    }
}