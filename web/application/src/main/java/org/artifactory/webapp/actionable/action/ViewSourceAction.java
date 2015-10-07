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

import org.artifactory.api.repo.ArchiveFileContent;
import org.artifactory.api.search.archive.ArchiveSearchResult;
import org.artifactory.common.wicket.component.label.highlighter.Syntax;
import org.artifactory.fs.FileInfo;
import org.artifactory.mime.MimeType;
import org.artifactory.mime.NamingUtils;
import org.artifactory.webapp.actionable.event.RepoAwareItemEvent;
import org.artifactory.webapp.wicket.page.search.actionable.ActionableArchiveSearchResult;

import java.io.IOException;

/**
 * Action to view source of class files or text files inside an archive.
 *
 * @author Yossi Shaul
 */
public class ViewSourceAction extends ViewAction {

    public ViewSourceAction() {
        super("View Source");
    }

    @Override
    public void onAction(RepoAwareItemEvent event) {
        ActionableArchiveSearchResult source = (ActionableArchiveSearchResult) event.getSource();
        ArchiveSearchResult searchResult = source.getSearchResult();

        org.artifactory.fs.ItemInfo itemInfo = searchResult.getItemInfo();
        if (itemInfo.isFolder()) {
            event.getTarget().getPage().error("View action is not applicable on folders");
            return;
        }

        FileInfo fileInfo = (FileInfo) itemInfo;
        Syntax syntax = null;
        String title = itemInfo.getName();
        try {
            ArchiveFileContent result = getRepoService().getArchiveFileContent(fileInfo.getRepoPath(),
                    searchResult.getEntryPath());
            final String failureReason = result.getFailureReason();
            if (failureReason == null) {
                // content successfully retrieved
                title = result.getSourceArchive() + "!/" + result.getSourcePath();
                MimeType contentType = NamingUtils.getMimeType(result.getSourcePath());
                syntax = Syntax.fromContentType(contentType);
                showHighlightedSourceModal(event, result.getContent(), title, syntax);
            } else {
                showPlainTextModal(event, failureReason, title, syntax);
            }
        } catch (IOException e) {
            String content = "Failed to retrieve source content: " + e.getMessage();
            showPlainTextModal(event, content, title, syntax);

        }

    }

    @Override
    public String getCssClass() {
        return ViewAction.class.getSimpleName();
    }

}