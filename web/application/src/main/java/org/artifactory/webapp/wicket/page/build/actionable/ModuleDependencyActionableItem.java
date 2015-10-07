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

package org.artifactory.webapp.wicket.page.build.actionable;

import org.artifactory.api.build.diff.BuildsDiffStatus;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.repo.RepoPath;
import org.artifactory.util.CollectionUtils;
import org.artifactory.util.PathUtils;
import org.artifactory.webapp.actionable.RepoAwareActionableItemBase;
import org.artifactory.webapp.actionable.action.DownloadAction;
import org.artifactory.webapp.actionable.action.ShowInTreeAction;
import org.artifactory.webapp.wicket.util.ItemCssClass;
import org.jfrog.build.api.Dependency;

import java.util.Set;

/**
 * The published module dependency actionable item
 *
 * @author Noam Y. Tenne
 */
public class ModuleDependencyActionableItem extends RepoAwareActionableItemBase {

    private Dependency dependency;
    private BuildsDiffStatus status = BuildsDiffStatus.UNCHANGED;

    /**
     * Main constructor
     *
     * @param repoPath   Repo path of dependency
     * @param dependency Dependency object
     * @param status     The status of the dependency
     */
    public ModuleDependencyActionableItem(RepoPath repoPath, Dependency dependency, BuildsDiffStatus status) {
        super(repoPath);
        this.dependency = dependency;
        this.status = status;
    }

    public BuildsDiffStatus getStatus() {
        return status;
    }

    public void setStatus(BuildsDiffStatus status) {
        this.status = status;
    }

    @Override
    public String getDisplayName() {
        return dependency.getId();
    }

    public Dependency getDependency() {
        return dependency;
    }

    @Override
    public String getCssClass() {
        return ItemCssClass.doc.getCssClass();
    }

    @Override
    public void filterActions(AuthorizationService authService) {
        ShowInTreeAction showInTreeAction = new ShowInTreeAction();
        DownloadAction downloadAction = new DownloadAction();
        RepoPath repoPath = getRepoPath();
        if ((repoPath == null) || (!authService.canRead(repoPath))) {
            showInTreeAction.setEnabled(false);
            downloadAction.setEnabled(false);
        }
        getActions().add(showInTreeAction);
        getActions().add(downloadAction);
    }

    /**
     * Returns the scopes of the dependency
     *
     * @return Dependency scope
     */
    public String getDependencyScope() {
        Set<String> scopes = dependency.getScopes();
        if (CollectionUtils.isNullOrEmpty(scopes)) {
            return "";
        }
        return PathUtils.collectionToDelimitedString(dependency.getScopes());
    }
}