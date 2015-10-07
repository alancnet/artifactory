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

package org.artifactory.webapp.wicket.page.home.settings;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.common.wicket.util.WicketUtils;
import org.artifactory.descriptor.repo.HttpRepoDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.util.HttpUtils;
import org.artifactory.webapp.wicket.page.base.AuthenticatedPage;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * A basic implementation of a settings generator
 *
 * @author Noam Y. Tenne
 */
public abstract class BaseSettingsPage extends AuthenticatedPage {

    @SpringBean
    private AuthorizationService authorizationService;

    @SpringBean
    private RepositoryService repositoryService;

    @SpringBean
    private CentralConfigService centralConfigService;

    protected List<? extends RepoDescriptor> virtualRepoDescriptors;

    protected List<? extends RepoDescriptor> localRepoDescriptors;

    public BaseSettingsPage() {
        virtualRepoDescriptors = getReadableVirtualRepoDescriptors();
        localRepoDescriptors = repositoryService.getLocalRepoDescriptors();
        addSettingsPanel();
    }

    /**
     * Add the settings panel or a place holder if no virtual repos are available
     */
    private void addSettingsPanel() {
        //Add a markup container, in case we don't need to add the panel
        WebMarkupContainer markupContainer = new WebMarkupContainer("settingsPanel");
        add(markupContainer);

        if (virtualRepoDescriptors.isEmpty()) {
            markupContainer.replaceWith(new Label("settingsPanel",
                    "Settings Generator is disabled: Unable to find readable virtual repositories."));
        } else {
            //Get context URL
            String contextUrl = getContextUrl();
            if (contextUrl.endsWith("/")) {
                contextUrl = StringUtils.removeEnd(contextUrl, "/");
            }

            markupContainer.replaceWith(getSettingsPanel("settingsPanel", contextUrl));
        }
    }

    /**
     * Returns a constructed settings generator panel
     *
     * @param id                ID to assign to the panel
     * @param servletContextUrl Running context URL
     * @return Initialized settings generator panel
     */
    protected abstract BaseSettingsGeneratorPanel getSettingsPanel(String id, String servletContextUrl);

    /**
     * Returns a list of virtual repositories that a readable by the current user
     *
     * @return Readable virtual repository definitions
     */
    private List<RepoDescriptor> getReadableVirtualRepoDescriptors() {
        List<RepoDescriptor> readableDescriptors = Lists.newArrayList();

        List<VirtualRepoDescriptor> virtualRepoDescriptors = repositoryService.getVirtualRepoDescriptors();
        for (VirtualRepoDescriptor virtualRepoDescriptor : virtualRepoDescriptors) {

            if (isVirtualRepoReadable(null, virtualRepoDescriptor)) {
                readableDescriptors.add(virtualRepoDescriptor);
            }
        }

        return readableDescriptors;
    }

    /**
     * Determine if the current user has read permissions on the given virtual
     *
     * @param parentVirtualRepo     Parent virtual repo if exists. Null if not
     * @param virtualRepoDescriptor Virtual repo to test
     * @return True if is readable. False if not
     */
    private boolean isVirtualRepoReadable(VirtualRepoDescriptor parentVirtualRepo,
            VirtualRepoDescriptor virtualRepoDescriptor) {
        List<RepoDescriptor> aggregatedRepos = virtualRepoDescriptor.getRepositories();
        for (RepoDescriptor aggregatedRepo : aggregatedRepos) {

            String key = aggregatedRepo.getKey();
            if (aggregatedRepo instanceof HttpRepoDescriptor) {
                if (authorizationService.canRead(InternalRepoPathFactory.repoRootPath(key + "-cache"))) {
                    return true;
                }
            } else if ((aggregatedRepo instanceof VirtualRepoDescriptor) &&
                    ((parentVirtualRepo == null) || !aggregatedRepo.equals(parentVirtualRepo))) {
                if (isVirtualRepoReadable(virtualRepoDescriptor, ((VirtualRepoDescriptor) aggregatedRepo))) {
                    return true;
                }
            } else {
                if (authorizationService.canRead(InternalRepoPathFactory.repoRootPath(key))) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getContextUrl() {
        String url = centralConfigService.getDescriptor().getUrlBase();
        if (url == null) {
            HttpServletRequest request = WicketUtils.getHttpServletRequest();
            url = HttpUtils.getServletContextUrl(request);
        }
        return url;
    }
}
