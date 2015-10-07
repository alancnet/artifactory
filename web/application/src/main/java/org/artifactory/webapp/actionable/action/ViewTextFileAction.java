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

import org.artifactory.fs.ItemInfo;
import org.artifactory.webapp.actionable.RepoAwareActionableItem;
import org.artifactory.webapp.actionable.event.RepoAwareItemEvent;

/**
 * This action will display a popup windows with the content of the selected text file.
 *
 * @author Yossi Shaul
 */
public class ViewTextFileAction extends ViewAction {

    @Override
    public void onAction(RepoAwareItemEvent e) {
        RepoAwareActionableItem source = e.getSource();
        ItemInfo itemInfo = source.getItemInfo();
        if (itemInfo.isFolder()) {
            e.getTarget().getPage().error("View action is not applicable on folders");
            return;
        }

        final org.artifactory.fs.FileInfo fileInfo = (org.artifactory.fs.FileInfo) itemInfo;
        String content = getContent(fileInfo);
        String title = itemInfo.getName();

        showHighlightedSourceModal(e, content, title);
    }

    private String getContent(org.artifactory.fs.FileInfo fileInfo) {
        return getRepoService().getStringContent(fileInfo);
    }

    @Override
    public String getCssClass() {
        return ViewAction.class.getSimpleName();
    }
}
