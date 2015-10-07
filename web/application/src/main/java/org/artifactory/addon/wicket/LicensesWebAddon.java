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

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.artifactory.addon.Addon;
import org.artifactory.api.license.LicenseInfo;
import org.artifactory.common.wicket.component.modal.panel.BaseModalPanel;
import org.artifactory.common.wicket.component.modal.panel.EditValueButtonRefreshBehavior;
import org.artifactory.common.wicket.model.sitemap.MenuNode;
import org.artifactory.repo.RepoPath;
import org.artifactory.webapp.actionable.RepoAwareActionableItem;
import org.jfrog.build.api.Build;

import java.util.List;

/**
 * @author Tomer Cohen
 */
public interface LicensesWebAddon extends Addon {

    /**
     * Get the licenses menu node.
     *
     * @param nodeName
     * @return The licenses menu node.
     */
    MenuNode getLicensesMenuNode(String nodeName);

    /**
     * Get the licenses info tab that is displayed in the {@code builds} page
     */
    ITab getLicensesInfoTab(String title, Build build, boolean hasDeployOnLocal);

    /**
     * Get the the panel to change the panel on an artifact according to its current values, and all licenses
     * defined in Artifactory
     *
     * @param refreshBehavior The behavior to refresh a panel after the update has taken place.
     * @param path            The {@link org.artifactory.repo.RepoPath} of the artifact that should have its license updated.
     * @param currentValues   The current license names that are attached to the artifact now (if any)
     * @return A Drag and drop panel to change the current license on the artifact.
     */
    BaseModalPanel getChangeLicensePanel(EditValueButtonRefreshBehavior refreshBehavior, RepoPath path,
            List<LicenseInfo> currentValues);

    /**
     * @return A panel to control licenses fro the tree browser general info panel
     */
    Component getLicenseGeneralInfoPanel(RepoAwareActionableItem actionableItem);
}
