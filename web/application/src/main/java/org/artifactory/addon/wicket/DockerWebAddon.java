/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2014 JFrog Ltd.
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

import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.form.Form;
import org.artifactory.addon.Addon;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.fs.FileInfo;
import org.artifactory.fs.ItemInfo;
import org.artifactory.repo.RepoPath;
import org.artifactory.webapp.actionable.action.DeleteAction;

/**
 * Webapp Docker functionality interface
 *
 * @author Shay Yaakov
 */
public interface DockerWebAddon extends Addon {

    /**
     * Assemble the repo Docker configuration section and add it to the given form
     *
     * @param form       Repo configuration form
     * @param descriptor Configured repo descriptor
     * @param isCreate   Whether it's a create new repository action
     */
    void createAndAddRepoConfigDockerSection(Form form, RepoDescriptor descriptor, boolean isCreate);

    ITab getDockerInfoTab(String tabTitle, FileInfo fileInfo);

    ITab getDockerAncestryTab(String s, FileInfo fileInfo);

    DeleteAction getDeleteAction(ItemInfo itemInfo);

    String getFolderCssClass(RepoPath repoPath, LocalRepoDescriptor repo);
}
