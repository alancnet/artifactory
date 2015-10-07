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

package org.artifactory.common.wicket.model.sitemap;

import org.apache.wicket.Page;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Yoav Aharoni
 */
public class SiteMap {
    private Map<Class<? extends Page>, MenuNode> pagesCache = new HashMap<>();
    private MenuNode root;

    public Collection<MenuNode> getPages() {
        return pagesCache.values();
    }

    public MenuNode getPageNode(Class<? extends Page> pageClass) {
        return pagesCache.get(pageClass);
    }

    public MenuNode getRoot() {
        return root;
    }

    public void setRoot(MenuNode root) {
        this.root = root;
    }

    public void visitPageNodes(MenuNode node, Iterator<MenuNode> iterator, MenuNodeVisitor visitor) {
        if (node == null) {
            return;
        }
        visitor.visit(node, iterator);
        for (Iterator<MenuNode> childIterator = node.getChildren().iterator(); childIterator.hasNext(); ) {
            MenuNode child = childIterator.next();
            visitPageNodes(child, childIterator, visitor);
        }
    }

    public void visitPageNodes(MenuNodeVisitor visitor) {
        visitPageNodes(getRoot(), null, visitor);
    }

    public void cachePageNodes() {
        visitPageNodes(new MenuNodeVisitor() {
            @Override
            public void visit(MenuNode node, Iterator<MenuNode> iterator) {
                Class<? extends Page> pageClass = node.getPageClass();
                if (pageClass != null) {
                    pagesCache.put(pageClass, node);
                }
            }
        });
    }
}
