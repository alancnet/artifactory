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

package org.artifactory.webapp.wicket.application;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.authorization.AuthorizationException;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.AbstractRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.repo.RepoPath;
import org.artifactory.webapp.servlet.RequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author Yoav Landman
 */
public class ArtifactoryRequestCycleListener extends AbstractRequestCycleListener {
    private static final Logger log = LoggerFactory.getLogger(ArtifactoryRequestCycleListener.class);

    @Override
    public IRequestHandler onException(RequestCycle cycle, Exception e) {
        if (e instanceof AuthorizationException) {
            StringBuilder builder = new StringBuilder("User ");
            try {
                AuthorizationService authService = ContextHelper.get().getAuthorizationService();
                builder.append(authService.currentUsername());
            } catch (Exception e1) {
                log.error("Error retrieving Username", e1);
            }
            builder.append(" accessed unauthorized resource. ");
            builder.append(e.getMessage());
            log.info(builder.toString());
        }
        return super.onException(cycle, e);
    }

    @Override
    public void onBeginRequest(RequestCycle cycle) {
        if (isSharedResourcesRequest(cycle)) {
            cycle.setCleanupFeedbackMessagesOnDetach(false);
        } else {
            // bind authentication
            ArtifactoryWebSession session = ArtifactoryWebSession.get();
            session.initAnonymousAuthentication();
            session.bindAuthentication();
        }
    }

    @Override
    public void onUrlMapped(RequestCycle cycle, IRequestHandler handler, Url url) {
        fixSimpleRepoBrowsingUrl(cycle.getRequest(), url);
    }

    private void fixSimpleRepoBrowsingUrl(Request request, Url url) {
        RepoPath repoPath = RequestUtils.getRepoPath((HttpServletRequest) request.getContainerRequest());
        if (repoPath != null) {
            /*ArrayList<String> prefix = new ArrayList<String>();
            String path = repoPath.getPath();
            if (StringUtils.isNotEmpty(path)) {
                int nesting = new StringTokenizer(path, "/").countTokens() + 1;
                while (nesting > 0) {
                    prefix.add("..");
                    nesting--;
                }
            } else {
                prefix.add("..");
            }
            */
            List<String> prefix = Lists.newArrayList("..");
            String filterPath = request.getFilterPath();
            if (StringUtils.isNotEmpty(filterPath) && filterPath.startsWith("/")) {
                prefix.add(filterPath.substring(1));
            }

            url.prependLeadingSegments(prefix);
        }
    }

    @Override
    public void onEndRequest(RequestCycle cycle) {
        Request request = cycle.getRequest();
        RequestUtils.removeRepoPath(request, false);
    }

    private boolean isSharedResourcesRequest(RequestCycle cycle) {
        // Use current request, as the private request field can be null
        Request currentRequest = cycle.getRequest();
        String path = currentRequest.getUrl().getPath();
        String sharedResourcesPath = ArtifactoryApplication.get().getSharedResourcesPath();
        return path.startsWith(sharedResourcesPath);
    }
}
