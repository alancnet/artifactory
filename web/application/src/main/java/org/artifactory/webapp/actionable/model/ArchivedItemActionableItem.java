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

package org.artifactory.webapp.actionable.model;

import org.artifactory.fs.ZipEntryInfo;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.util.TreeNode;
import org.artifactory.webapp.actionable.RepoAwareActionableItemBase;

/**
 * Base class for zip actionable items (files and folders)
 *
 * @author Yossi Shaul
 */
public abstract class ArchivedItemActionableItem extends RepoAwareActionableItemBase {
    protected TreeNode<ZipEntryInfo> node;

    protected ArchivedItemActionableItem(RepoPath repoPath, TreeNode<ZipEntryInfo> node) {
        super(repoPath);
        this.node = node;
    }

    /**
     * @return The repo path to this entry in the archive
     */
    @Override
    public RepoPath getRepoPath() {
        return InternalRepoPathFactory.archiveResourceRepoPath(super.getRepoPath(), node.getData().getPath());
    }

    /**
     * @return The repo path of the archive containing this entry
     */
    public RepoPath getArchiveRepoPath() {
        return super.getRepoPath();
    }

}
