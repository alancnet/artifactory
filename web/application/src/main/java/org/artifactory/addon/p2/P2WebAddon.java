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

package org.artifactory.addon.p2;

import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.markup.html.form.Form;
import org.artifactory.addon.Addon;
import org.artifactory.common.MutableStatusHolder;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;
import org.artifactory.webapp.wicket.page.config.repos.CachingDescriptorHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * Web interface of P2 addon.
 *
 * @author Yossi Shaul
 */
// TODO: [by dan] delete this when wicket dies
public interface P2WebAddon extends Addon {
    /**
     * Returns the tab panel for P2 configuration of a virtual repository.
     *
     * @param tabTitle                The title of the tab
     * @param repoDescriptor          The virtual repository descriptor
     * @param cachingDescriptorHelper
     */
    AbstractTab getVirtualRepoConfigurationTab(String tabTitle, VirtualRepoDescriptor repoDescriptor,
            CachingDescriptorHelper cachingDescriptorHelper);

    /**
     * Verifies all the remotes repositories and returns a list to be presented in the UI
     *
     * @return A list of remote repositories that are added/created/verified based on the virtual repo configuration.
     */
    List<P2Repository> verifyRemoteRepositories(MutableCentralConfigDescriptor currentDescriptor,
            VirtualRepoDescriptor virtualRepo,
            @Nullable List<P2Repository> currentList,
            @Nonnull Map<String, List<String>> subCompositeUrls,
            MutableStatusHolder statusHolder);

    /**
     * Adds the P2 configuration section to the form of the remote repository's packages config tab
     *
     * @param form Form from the remote repository's packages config tab
     */
    void createAndAddRemoteRepoConfigP2Section(Form form, RemoteRepoDescriptor descriptor);
}
