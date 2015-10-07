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

package org.artifactory.webapp.wicket.page.browse.treebrowser.tabs.ivy;

import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleId;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.component.border.fieldset.FieldSetBorder;
import org.artifactory.common.wicket.component.border.titled.TitledBorder;
import org.artifactory.common.wicket.component.label.highlighter.Syntax;
import org.artifactory.common.wicket.util.WicketUtils;
import org.artifactory.fs.FileInfo;
import org.artifactory.ivy.IvyService;
import org.artifactory.repo.RepoPath;
import org.artifactory.webapp.actionable.FileActionable;


/**
 * @author Eli Givoni
 */
public class XmlViewTabPanel extends Panel {

    @SpringBean
    private RepositoryService repoService;

    @SpringBean
    private IvyService ivyService;

    public XmlViewTabPanel(String id, FileActionable repoItem, XmlTypes xmlType) {
        super(id);
        add(new CssClass("veiw-tab"));
        if (xmlType.equals(XmlTypes.IVY_XML)) {
            addDependencySection(repoItem);
        } else {
            WebMarkupContainer dumyContainer = new WebMarkupContainer("moduleBorder");
            dumyContainer.add(new WebMarkupContainer("moduleContent"));
            add(dumyContainer);
        }
        TitledBorder xmlBorder = xmlType.getTabBorder();
        add(xmlBorder);
        xmlBorder.add(WicketUtils.getSyntaxHighlighter("content", getContent(repoItem), Syntax.xml));
    }

    private void addDependencySection(FileActionable item) {
        TitledBorder moduleBorder = new FieldSetBorder("moduleBorder");
        RepoPath repoPath = item.getRepoPath();
        StringBuilder sb = new StringBuilder();
        ModuleDescriptor descriptor = ivyService.parseIvyFile(repoPath);
        if (descriptor == null) {
            //TODO: [by ys] display message: Failed to parse file
            sb.append(" Failed to parse file");
        } else {
            buildModuleContent(sb, descriptor);
        }
        String content = sb.toString();
        moduleBorder.add(WicketUtils.getSyntaxHighlighter("moduleContent", content, Syntax.xml));
        add(moduleBorder);
    }

    private void buildModuleContent(StringBuilder sb, ModuleDescriptor descriptor) {
        ModuleRevisionId moduleRevisionId = descriptor.getModuleRevisionId();
        ModuleId module = moduleRevisionId.getModuleId();

        sb.append("<dependency org=\"");
        sb.append(module.getOrganisation()).append("\" ");
        sb.append("name=\"");
        sb.append(module.getName()).append("\" ");
        sb.append("rev=\"");
        sb.append(moduleRevisionId.getRevision()).append("\" />");
    }


    private String getContent(FileActionable item) {
        FileInfo info = item.getFileInfo();
        return repoService.getStringContent(info);
    }

    public enum XmlTypes {
        IVY_XML("Ivy Module", "Ivy View"), GENERAL_XML("XML", "XML View");

        private String borderTitle;
        private String tabTitle;

        XmlTypes(String borderTitle, String tabTitle) {
            this.tabTitle = tabTitle;
            this.borderTitle = borderTitle;
        }

        public String getTabTitle() {
            return tabTitle;
        }

        public String getBorderTitle() {
            return borderTitle;
        }

        private TitledBorder getTabBorder() {
            return new FieldSetBorder("xmlBorder") {
                @Override
                public String getTitle() {
                    return getBorderTitle();
                }
            };
        }
    }
}
