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

package org.artifactory.webapp.actionable.action;

import org.artifactory.repo.RepoPath;
import org.artifactory.webapp.actionable.ActionableItem;
import org.artifactory.webapp.actionable.RepoAwareActionableItem;
import org.artifactory.webapp.actionable.event.RepoAwareItemEvent;
import org.artifactory.webapp.servlet.RequestUtils;

/**
 * @author yoavl
 */
public class DownloadAction extends RepoAwareItemAction {
    public static final String ACTION_NAME = "Download";

    public DownloadAction() {
        super(ACTION_NAME);
    }

    @Override
    public void onAction(RepoAwareItemEvent e) {
    }

    @Override
    public String getActionLinkURL(ActionableItem actionableItem) {
        String downloadPath = getDownloadPath(actionableItem);
        return downloadPath;
    }

    /**
     * Extract the download path from the actionable item
     *
     * @param actionableItem Actionable item
     * @return String - download path
     */
    protected String getDownloadPath(ActionableItem actionableItem) {
        RepoPath repoPath = ((RepoAwareActionableItem) actionableItem).getRepoPath();
        String path =
                RequestUtils.getWicketServletContextUrl() + "/" + repoPath.getRepoKey() + "/" + repoPath.getPath();
        return path;
    }
}