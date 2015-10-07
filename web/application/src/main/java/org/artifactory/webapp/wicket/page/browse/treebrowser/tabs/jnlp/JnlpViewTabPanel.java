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

package org.artifactory.webapp.wicket.page.browse.treebrowser.tabs.jnlp;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.component.PlaceHolder;
import org.artifactory.common.wicket.component.border.fieldset.FieldSetBorder;
import org.artifactory.common.wicket.component.combobox.FilteringSelect;
import org.artifactory.common.wicket.component.label.highlighter.Syntax;
import org.artifactory.common.wicket.component.links.TitledAjaxLink;
import org.artifactory.common.wicket.component.modal.ModalHandler;
import org.artifactory.common.wicket.component.modal.panel.BaseModalPanel;
import org.artifactory.common.wicket.util.WicketUtils;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;
import org.artifactory.webapp.actionable.RepoAwareActionableItem;
import org.artifactory.webapp.actionable.model.FileActionableItem;
import org.artifactory.webapp.servlet.RequestUtils;
import org.artifactory.webapp.wicket.util.JnlpUtils;

import java.util.List;

import static java.lang.String.format;

/**
 * This tab will be displayed when a jnlp file is selected from the browse tree.
 *
 * @author Yossi Shaul
 */
public class JnlpViewTabPanel extends Panel {
    @SpringBean
    private RepositoryService repoService;

    private String jnlpContent;
    private FileActionableItem fileItem;

    public JnlpViewTabPanel(String id, final FileActionableItem fileItem) {
        super(id);
        this.fileItem = fileItem;
        jnlpContent = repoService.getStringContent(fileItem.getFileInfo());

        add(new CssClass("jnlp-tab"));
        addJnlpContent();

        List<VirtualRepoDescriptor> repos = getWebStartRepos(fileItem);
        addRepositorySelect(repos);

        if (repos.isEmpty()) {
            addPlaceHolders();
        } else {
            VirtualRepoDescriptor repoDescriptor = repos.get(0);
            setRepository(repoDescriptor);
        }
    }

    private void addRepositorySelect(List<VirtualRepoDescriptor> repos) {
        switch (repos.size()) {
            case 0:
                add(new Label("repoLabel", getString("repo.none")));
                add(new PlaceHolder("repositories"));
                return;

            case 1:
                String key = repos.get(0).getKey();
                add(new Label("repoLabel", format(getString("repo.one"), key)));
                add(new PlaceHolder("repositories"));
                return;

            default:
                add(new Label("repoLabel", getString("repo.many")));
                Component select = new FilteringSelect<>(
                        "repositories", new Model<>(repos.get(0)), repos);
                select.add(new AjaxFormComponentUpdatingBehavior("onchange") {
                    @Override
                    protected void onUpdate(AjaxRequestTarget target) {
                        RepoDescriptor repo = (VirtualRepoDescriptor) getComponent().getDefaultModelObject();
                        setRepository(repo);
                        target.add(get("jnlpLinksBorder"));
                        target.add(get("scriptSnippetBorder"));
                    }
                });
                add(select);
        }
    }

    private void setRepository(RepoDescriptor repoDescriptor) {
        String servletContextUrl = RequestUtils.getWicketServletContextUrl();
        final String key = repoDescriptor.getKey();
        final String virtualRepoUrl = servletContextUrl + "/" + key + "/";

        String jnlpHref = virtualRepoUrl + fileItem.getFileInfo().getRelPath();
        JnlpUtils.AppletInfo appletInfo = JnlpUtils.getAppletInfo(jnlpContent, jnlpHref);
        boolean isJavaFxApplet = appletInfo != null;

        // add links
        MarkupContainer jnlpLinksBorder = new FieldSetBorder("jnlpLinksBorder");
        jnlpLinksBorder.add(new LaunchWebStartLink("webstart", virtualRepoUrl, key));
        Component appletLink = new LaunchAppletLink("applet", appletInfo, key);
        appletLink.setVisible(isJavaFxApplet);
        jnlpLinksBorder.add(appletLink);
        addOrReplace(jnlpLinksBorder);

        // add script snippet
        if (isJavaFxApplet) {
            MarkupContainer scriptSnippetBorder = new FieldSetBorder("scriptSnippetBorder") {
                @Override
                public String getTitle() {
                    return format("Applet's Script Snippet (%s repository)", key);
                }
            };
            scriptSnippetBorder.add(WicketUtils.getSyntaxHighlighter("scriptSnippet", appletInfo.getScriptSnippet(),
                    Syntax.xml));
            addOrReplace(scriptSnippetBorder);
        } else {
            MarkupContainer scriptSnippetBorder = new PlaceHolder("scriptSnippetBorder");
            scriptSnippetBorder.setOutputMarkupId(true);
            scriptSnippetBorder.setOutputMarkupPlaceholderTag(true);
            scriptSnippetBorder.add(new PlaceHolder("scriptSnippet"));
            addOrReplace(scriptSnippetBorder);
        }
    }

    private void addPlaceHolders() {
        MarkupContainer jnlpLinksBorder = new PlaceHolder("jnlpLinksBorder");
        jnlpLinksBorder.add(new PlaceHolder("webstart"));
        jnlpLinksBorder.add(new PlaceHolder("applet"));
        add(jnlpLinksBorder);

        MarkupContainer scriptSnippetBorder = new PlaceHolder("scriptSnippetBorder");
        scriptSnippetBorder.add(new PlaceHolder("scriptSnippet"));
        add(scriptSnippetBorder);
    }

    private void addJnlpContent() {
        MarkupContainer jnlpContentBorder = new FieldSetBorder("jnlpContentBorder");
        add(jnlpContentBorder);
        jnlpContentBorder.add(WicketUtils.getSyntaxHighlighter("jnlpContent", jnlpContent, Syntax.xml));
    }

    private List<VirtualRepoDescriptor> getWebStartRepos(RepoAwareActionableItem fileItem) {
        RepositoryService repositoryService = ContextHelper.get().getRepositoryService();
        List<VirtualRepoDescriptor> virtualRepo = repositoryService.getVirtualReposContainingRepo(fileItem.getRepo());
        return JnlpUtils.filterNonWebstartRepos(virtualRepo);
    }

    private static class LaunchAppletLink extends TitledAjaxLink {
        private final JnlpUtils.AppletInfo appletInfo;

        private LaunchAppletLink(String id, JnlpUtils.AppletInfo appletInfo, String repo) {
            super(id, "View Applet from " + repo);
            this.appletInfo = appletInfo;
        }

        @Override
        public void onClick(AjaxRequestTarget target) {
            ModalHandler modalHandler = ModalHandler.getInstanceFor(this);
            BaseModalPanel panel = new JavafxAppletPreviewPanel(appletInfo);
            modalHandler.setModalPanel(panel);
            modalHandler.showAsPage(target);
        }
    }

    private class LaunchWebStartLink extends ExternalLink {
        private LaunchWebStartLink(String id, String virtualRepoUrl, String repo) {
            super(id, virtualRepoUrl + fileItem.getRepoPath().getPath(), "Launch Web Start from " + repo);
        }
    }
}