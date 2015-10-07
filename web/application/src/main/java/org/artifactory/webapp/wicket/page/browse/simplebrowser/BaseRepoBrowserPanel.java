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

package org.artifactory.webapp.wicket.page.browse.simplebrowser;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.artifactory.api.repo.BaseBrowsableItem;
import org.artifactory.api.repo.BrowsableItem;
import org.artifactory.common.wicket.component.panel.titled.TitledPanel;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.webapp.wicket.page.browse.simplebrowser.root.SimpleBrowserRootPage;
import org.artifactory.webapp.wicket.util.ItemCssClass;

/**
 * @author Tomer Cohen
 */
public abstract class BaseRepoBrowserPanel extends TitledPanel {

    public BaseRepoBrowserPanel(String id) {
        super(id);
    }

    /**
     * Creates a local repo "up directory"\.. browsable item
     *
     * @param repoPath path of starting point
     * @return local up-dir Browsable item
     */
    protected BaseBrowsableItem getPseudoUpLink(RepoPath repoPath) {
        String repoKey = repoPath.getRepoKey();
        BrowsableItem upDirItem;
        if (!repoPath.isRoot()) {
            upDirItem = new BrowsableItem(BaseBrowsableItem.UP, true,
                    0, 0, 0, InternalRepoPathFactory.create(repoKey, repoPath.getParent().getPath()));
        } else {
            upDirItem = new BrowsableItem(BaseBrowsableItem.UP, true, 0, 0, 0,
                    InternalRepoPathFactory.create(repoPath.getRepoKey(), "/"));
        }
        return upDirItem;
    }

    /**
     * @return CSS class for link according to its type (folder or file)
     */
    protected String getCssClass(BaseBrowsableItem browsableItem) {
        if (browsableItem.isFolder()) {
            return ItemCssClass.folder.name();
        } else {
            return ItemCssClass.getFileCssClass(browsableItem.getRelativePath()).name();
        }
    }

    protected BookmarkablePageLink createRootLink() {
        return new BookmarkablePageLink<SimpleBrowserRootPage>("link", SimpleBrowserRootPage.class) {
            @Override
            public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
                replaceComponentTagBody(markupStream, openTag, getDefaultModelObjectAsString(BrowsableItem.UP));
            }
        };
    }
}
