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

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.ha.HaCommonAddon;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.request.ArtifactoryResponse;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.util.HttpUtils;
import org.artifactory.util.ResourceUtils;
import org.artifactory.webapp.servlet.redirection.OldBuildsRedirectionHandler;
import org.artifactory.webapp.servlet.redirection.OldHomeRedirectionHandler;
import org.artifactory.webapp.servlet.redirection.OldLoginRedirectionHandler;
import org.artifactory.webapp.servlet.redirection.RedirectionHandler;
import org.artifactory.webapp.servlet.redirection.SamlRedirectionHandler;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class ArtifactoryFilter implements Filter {

    private boolean contextFailed = false;

    private FilterConfig filterConfig;
    private List<RedirectionHandler> redirectionHandlers = Lists.newArrayList(new SamlRedirectionHandler(),
            new OldHomeRedirectionHandler(), new OldLoginRedirectionHandler(),new OldBuildsRedirectionHandler());

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {
        // Redirect or forward if need
        for (RedirectionHandler redirectionHandler : redirectionHandlers) {
            if (redirectionHandler.shouldRedirect(request)) {
                redirectionHandler.redirect(request, response);
                return;
            }
        }
        if (filterConfig.getServletContext()
                .getAttribute(DelayedInit.APPLICATION_CONTEXT_LOCK_KEY) != null) {
            String requestURI = ((HttpServletRequest) request).getRequestURI();
            if (requestURI.endsWith("artifactory-splash.gif")) {
                ((HttpServletResponse) response).setStatus(200);
                ServletOutputStream out = response.getOutputStream();
                ResourceUtils.copyResource("/artifactory-splash.gif", out, null, getClass());
                return;
            }
            response.setContentType("text/html");
            ((HttpServletResponse) response).setStatus(HttpStatus.SC_SERVICE_UNAVAILABLE);
            ServletOutputStream out = response.getOutputStream();
            ResourceUtils.copyResource("/startup.html", out, null, getClass());
            return;
        }
        try {
            ServletContext servletContext = filterConfig.getServletContext();
            ArtifactoryContext context = RequestUtils.getArtifactoryContext(servletContext);
            if (context == null) {
                respondFailedToInitialize(response);
                return;
            }
            bind(context);
            if (response instanceof HttpServletResponse) {
                HttpServletResponse httpResponse = (HttpServletResponse) response;
                if (!httpResponse.containsHeader("Server")) {
                    //Add the server header (curl -I http://localhost:8080/artifactory/)
                    httpResponse.setHeader("Server", HttpUtils.getArtifactoryUserAgent());
                }

                // set the Artifactory instance id header
                String hostId = ContextHelper.get().beanForType(AddonsManager.class).addonByType(
                        HaCommonAddon.class).getHostId();
                httpResponse.setHeader(ArtifactoryResponse.ARTIFACTORY_ID, hostId);

                String serverId = ContextHelper.get().getServerId();
                if (StringUtils.isNotBlank(serverId) && !HaCommonAddon.ARTIFACTORY_PRO.equals(serverId)) {
                    httpResponse.setHeader(HaCommonAddon.ARTIFACTORY_NODE_ID, serverId);
                }
            }
            chain.doFilter(request, response);
        } finally {
            unbind();
        }
    }



    private void bind(ArtifactoryContext context) {
        ArtifactoryContextThreadBinder.bind(context);
        ArtifactoryHome.bind(context.getArtifactoryHome());
    }

    private void unbind() {
        ArtifactoryContextThreadBinder.unbind();
        ArtifactoryHome.unbind();
    }

    private void respondFailedToInitialize(ServletResponse response) throws IOException {
        if (!contextFailed) {
            org.slf4j.Logger log = LoggerFactory.getLogger(ArtifactoryFilter.class);
            log.error("Artifactory failed to initialize: Context is null");
            contextFailed = true;
        }

        if (response instanceof HttpServletResponse) {
            HttpUtils.sendErrorResponse((HttpServletResponse) response, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    "Artifactory failed to initialize: check Artifactory logs for errors.");
        }
    }

    @Override
    public void destroy() {
        unbind();
    }
}