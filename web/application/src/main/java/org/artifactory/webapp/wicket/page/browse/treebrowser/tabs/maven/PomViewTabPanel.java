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

package org.artifactory.webapp.wicket.page.browse.treebrowser.tabs.maven;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.component.border.fieldset.FieldSetBorder;
import org.artifactory.common.wicket.util.WicketUtils;
import org.artifactory.fs.FileInfo;
import org.artifactory.webapp.actionable.FileActionable;

import static org.artifactory.common.wicket.component.label.highlighter.Syntax.xml;

/**
 * This tab will be displayed when a pom file is selected from the browse tree.
 *
 * @author Yossi Shaul
 */
public class PomViewTabPanel extends Panel {

    @SpringBean
    private RepositoryService repoService;

    /**
     * Main constructor
     *
     * @param id       ID to assign to the panel
     * @param repoItem Selected repo item
     */
    public PomViewTabPanel(String id, FileActionable repoItem) {
        super(id);
        add(new CssClass("veiw-tab"));

        FileInfo fileInfo = repoItem.getFileInfo();
        addPomContent(fileInfo);
    }

    /**
     * Adds the complete pom display
     *
     * @param fileInfo Pom file info
     */
    public void addPomContent(FileInfo fileInfo) {
        FieldSetBorder border = new FieldSetBorder("pomBorder");
        add(border);

        String content = repoService.getStringContent(fileInfo);
        border.add(WicketUtils.getSyntaxHighlighter("pomContent", content, xml));
    }
}