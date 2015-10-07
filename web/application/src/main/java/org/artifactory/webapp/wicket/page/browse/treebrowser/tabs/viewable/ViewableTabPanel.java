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

package org.artifactory.webapp.wicket.page.browse.treebrowser.tabs.viewable;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.api.repo.ArchiveFileContent;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.common.wicket.component.border.fieldset.FieldSetBorder;
import org.artifactory.common.wicket.component.label.highlighter.Syntax;
import org.artifactory.common.wicket.util.WicketUtils;
import org.artifactory.mime.MimeType;
import org.artifactory.mime.NamingUtils;
import org.artifactory.util.PathUtils;
import org.artifactory.webapp.actionable.model.ArchivedFileActionableItem;

import java.io.IOException;

/**
 * This tab will be displayed when a pom file is selected from the browse tree.
 *
 * @author Yossi Shaul
 */
public class ViewableTabPanel extends Panel {

    @SpringBean
    private RepositoryService repoService;

    public ViewableTabPanel(String id, ArchivedFileActionableItem repoItem) {
        super(id);

        String content;
        String sourceFile = null;
        Syntax syntax = Syntax.plain;
        try {
            ArchiveFileContent result = repoService.getArchiveFileContent(
                    repoItem.getArchiveRepoPath(), repoItem.getPath());
            String failureReason = result.getFailureReason();
            if (failureReason == null) {
                // content successfully retrieved
                MimeType contentType = NamingUtils.getMimeType(result.getSourcePath());
                syntax = Syntax.fromContentType(contentType);
                content = result.getContent();
                sourceFile = PathUtils.getFileName(result.getSourcePath());
            } else {
                content = failureReason;
            }
        } catch (IOException e) {
            content = "Failed to retrieve source content: " + e.getMessage();
        }

        addContent(content, sourceFile, syntax);
    }

    public void addContent(String content, final String sourcePath, Syntax syntax) {
        FieldSetBorder border = new FieldSetBorder("contentBorder") {
            @Override
            public String getTitle() {
                if (sourcePath != null) {
                    return sourcePath;
                } else {
                    return super.getTitle();
                }
            }
        };

        add(border);

        border.add(WicketUtils.getSyntaxHighlighter("content", content, syntax));
    }
}