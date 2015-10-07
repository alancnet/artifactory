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
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.artifactory.addon.Addon;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.fs.FileInfo;
import org.artifactory.repo.RepoPath;

/**
 * Python (PyPI) web configurations
 *
 * @author Yoav Luft
 */
public interface PypiWebAddon extends Addon {

    void createAndAddPypiConfigSection(Form form, RepoDescriptor repo, boolean isCreate);

    WebMarkupContainer buildDistributionManagementPanel(String id, RepoPath repoPath);

    ITab createPypiPackageInfoTab(String title, RepoPath packagePath);

    boolean isPypiFile(FileInfo fileInfo);
}
