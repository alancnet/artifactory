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
import org.artifactory.mime.NamingUtils;
import org.artifactory.repo.RepoPath;
import org.artifactory.webapp.actionable.RepoAwareActionableItemBase;
import org.artifactory.webapp.actionable.action.DownloadAction;
import org.artifactory.webapp.actionable.action.ShowInTreeAction;
import org.artifactory.webapp.actionable.action.ViewTextFileAction;
import org.artifactory.webapp.wicket.util.ItemCssClass;
import org.jfrog.build.api.Artifact;

/**
 * The published module artifact actionable item
 *
 * @author Noam Y. Tenne
 */
public class ModuleArtifactActionableItem extends RepoAwareActionableItemBase {

    private Artifact artifact;
    private BuildsDiffStatus status = BuildsDiffStatus.UNCHANGED;

    /**
     * Main constructor
     *
     * @param repoPath Repo path of artifact
     * @param artifact Artifact object
     */
    public ModuleArtifactActionableItem(RepoPath repoPath, Artifact artifact) {
        super(repoPath);
        this.artifact = artifact;
    }

    public BuildsDiffStatus getStatus() {
        return status;
    }

    public void setStatus(BuildsDiffStatus status) {
        this.status = status;
    }

    @Override
    public String getDisplayName() {
        return artifact.getName();
    }

    @Override
    public String getCssClass() {
        return ItemCssClass.doc.getCssClass();
    }

    @Override
    public void filterActions(AuthorizationService authService) {
        RepoPath repoPath = getRepoPath();
        if ((repoPath != null) && (authService.canRead(repoPath))) {
            getActions().add(new ShowInTreeAction());
            getActions().add(new DownloadAction());
            if (NamingUtils.isViewable(repoPath.getPath())) {
                getActions().add(new ViewTextFileAction());
            }
        }
    }
}