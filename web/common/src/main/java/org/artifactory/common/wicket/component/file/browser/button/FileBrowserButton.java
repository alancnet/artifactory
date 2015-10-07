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

package org.artifactory.common.wicket.component.file.browser.button;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.artifactory.common.wicket.component.file.browser.panel.FileBrowserPanel;
import org.artifactory.common.wicket.component.file.path.PathHelper;
import org.artifactory.common.wicket.component.file.path.PathMask;
import org.artifactory.common.wicket.component.modal.ModalHandler;
import org.artifactory.common.wicket.model.DelegatedModel;

/**
 * @author Yoav Aharoni
 */
public class FileBrowserButton extends Panel {
    private String chRoot;
    private PathHelper pathHelper;
    private PathMask mask = PathMask.ALL;

    @SuppressWarnings({"UnusedDeclaration"})
    public FileBrowserButton(String id) {
        this(id, null, new PathHelper());
    }

    public FileBrowserButton(String id, IModel model) {
        this(id, model, new PathHelper());
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public FileBrowserButton(String id, String root) {
        this(id, null, root);
    }

    public FileBrowserButton(String id, IModel model, String root) {
        this(id, model, new PathHelper(root));
    }

    protected FileBrowserButton(String id, IModel model, PathHelper pathHelper) {
        super(id);
        this.pathHelper = pathHelper;
        chRoot = pathHelper.getWorkingDirectoryPath();

        if (model != null) {
            setDefaultModel(model);
        }

        add(new BrowseLink("browseLink"));
    }

    protected void onOkClicked(AjaxRequestTarget target) {
    }

    protected void onCancelClicked(AjaxRequestTarget target) {
    }

    protected void onShowBrowserClicked(AjaxRequestTarget target) {
        ModalHandler modalHandler = ModalHandler.getInstanceFor(this);
        modalHandler.setModalPanel(new MyFileBrowserPanel());
        modalHandler.show(target);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public PathMask getMask() {
        return mask;
    }

    public void setMask(PathMask mask) {
        this.mask = mask;
    }

    private class BrowseLink extends WebComponent {
        private BrowseLink(String id) {
            super(id);
            add(new AjaxEventBehavior("onclick") {
                @Override
                protected void onEvent(AjaxRequestTarget target) {
                    onShowBrowserClicked(target);
                }
            });

            add(new AttributeModifier("title", new StringResourceModel("browse", this, null)));
        }
    }

    private class MyFileBrowserPanel extends FileBrowserPanel {
        private MyFileBrowserPanel() {
            super(new DelegatedModel(FileBrowserButton.this), pathHelper);
            super.setChRoot(chRoot);
            super.setMask(mask);
        }

        @Override
        public void onCloseButtonClicked(AjaxRequestTarget target) {
            super.onCloseButtonClicked(target);
            FileBrowserButton.this.onCancelClicked(target);
        }

        @Override
        protected void onCancelClicked(AjaxRequestTarget target) {
            super.onCancelClicked(target);
            FileBrowserButton.this.onCancelClicked(target);
            close(target);
        }

        @Override
        protected void onOkClicked(AjaxRequestTarget target) {
            super.onOkClicked(target);
            FileBrowserButton.this.onOkClicked(target);
            close(target);
        }
    }
}
