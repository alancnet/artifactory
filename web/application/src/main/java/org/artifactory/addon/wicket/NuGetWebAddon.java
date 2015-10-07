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

package org.artifactory.addon.wicket;

import org.apache.http.client.methods.HttpRequestBase;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.artifactory.addon.Addon;
import org.artifactory.descriptor.repo.HttpRepoDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;
import org.artifactory.repo.RepoPath;

/**
 * Webapp NuGet functionality interface
 *
 * @author Noam Y. Tenne
 */
public interface NuGetWebAddon extends Addon {

    /**
     * Assemble the repo NuGet configuration section and add it to the given form
     *
     * @param form           Repo configuration form
     * @param repoDescriptor Configured repo descriptor
     * @param isCreate
     */
    void createAndAddRepoConfigNuGetSection(Form form, RepoDescriptor repoDescriptor, boolean isCreate);

    /**
     * Returns the virtual repository's NuGet configuration tab
     *
     * @param tabTitle       Tab title
     * @param repoDescriptor Descriptor of currently edited/created virtual repository
     * @return Constructed tab
     */
    ITab getVirtualRepoConfigurationTab(String tabTitle, VirtualRepoDescriptor repoDescriptor);

    /**
     * Creates an HTTP method to test the connectivity of the remote repo. Type of method and URL are subject to the
     * repo configuration
     *
     * @param repoUrl Repo URL; always ends with forward slash
     * @param repo    Descriptor of currently configured repo
     * @return HTTP method to execute
     */
    HttpRequestBase getRemoteRepoTestMethod(String repoUrl, HttpRepoDescriptor repo);

    /**
     * Returns the NuPkg information tab
     *
     * @param tabTitle      The title of the tab
     * @param nuPkgRepoPath Repo path of the nupkg to display
     * @return Tab if successfully assembled, none if not
     */
    ITab getNuPkgInfoTab(String tabTitle, RepoPath nuPkgRepoPath);

    /**
     * Returns the NuGet full access URL label which clients need to use
     *
     * @param repoDescriptor
     */
    Label getNuGetUrlLabel(RepoDescriptor repoDescriptor);

    WebMarkupContainer getVirtualRepoConfigurationSection(String id, RepoDescriptor descriptor, Form form,
            boolean isCreate);

    /**
     * build a distribution management section for the General Info tab
     *
     * @param id The panel id
     * @param repoPath The repo path of the root repository
     */
    WebMarkupContainer buildDistributionManagementPanel(String id, RepoPath repoPath);
}
