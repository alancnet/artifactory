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

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.artifactory.common.wicket.component.TextContentPanel;
import org.artifactory.common.wicket.component.label.highlighter.Syntax;
import org.artifactory.common.wicket.component.modal.panel.bordered.nesting.CodeModalPanel;
import org.artifactory.common.wicket.util.WicketUtils;
import org.artifactory.fs.ItemInfo;
import org.artifactory.mime.MimeType;
import org.artifactory.mime.NamingUtils;
import org.artifactory.webapp.actionable.event.RepoAwareItemEvent;

/**
 * Base class for actions viewing text resources.
 *
 * @author yoavl
 */
public abstract class ViewAction extends RepoAwareItemAction {
    public static final String ACTION_NAME = "View";

    public ViewAction() {
        this(ACTION_NAME);
    }

    public ViewAction(String actionName) {
        super(actionName);
    }

    protected void showHighlightedSourceModal(RepoAwareItemEvent e, String content, String title) {
        MimeType mimeType = getMimeType(e);
        Syntax syntax = Syntax.fromContentType(mimeType);
        showHighlightedSourceModal(e, content, title, syntax);
    }

    protected void showHighlightedSourceModal(RepoAwareItemEvent e, String content, String title, Syntax syntax) {
        final String id = e.getTargetComponents().getModalWindow().getContentId();
        showModal(e, title, WicketUtils.getSyntaxHighlighter(id, content, syntax));
    }

    protected void showPlainTextModal(RepoAwareItemEvent e, String content, String title, Syntax syntax) {
        final String id = e.getTargetComponents().getModalWindow().getContentId();
        showModal(e, title, new TextContentPanel(id).setContent(content));
    }

    private void showModal(RepoAwareItemEvent e, String title, Component content) {
        ModalWindow modalWindow = e.getTargetComponents().getModalWindow();
        CodeModalPanel modelPanel = new CodeModalPanel(content);
        modelPanel.setTitle(title);
        modalWindow.setContent(modelPanel);
        AjaxRequestTarget target = e.getTarget();
        modalWindow.show(target);
    }

    protected MimeType getMimeType(RepoAwareItemEvent e) {
        ItemInfo itemInfo = e.getSource().getItemInfo();
        return NamingUtils.getMimeType(itemInfo.getName());
    }
}
