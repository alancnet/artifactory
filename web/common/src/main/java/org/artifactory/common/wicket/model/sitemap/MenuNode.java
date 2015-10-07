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

import org.apache.commons.collections.iterators.IteratorEnumeration;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.authorization.IAuthorizationStrategy;

import javax.swing.tree.TreeNode;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static org.artifactory.common.wicket.util.CookieUtils.*;

/**
 * @author Yoav Aharoni
 */
public class MenuNode implements TreeNode, Serializable {
    private final Class<? extends Page> pageClass;
    private final String name;
    private MenuNode parent;
    private List<MenuNode> children = new ArrayList<>();
    private String cookieName;

    public MenuNode(String name) {
        this.name = name;
        pageClass = null;
    }

    public MenuNode(String name, Class<? extends Page> pageClass) {
        this.name = name;
        this.pageClass = pageClass;
    }

    public Class<? extends Page> getPageClass() {
        return pageClass;
    }

    public String getName() {
        return name;
    }

    @Override
    public MenuNode getParent() {
        return parent;
    }

    public void setParent(MenuNode parent) {
        this.parent = parent;
    }

    public List<MenuNode> getChildren() {
        return children;
    }

    public void addChild(MenuNode child) {
        //Allow a null and ignore - helps with transperancy of addons
        if (child != null) {
            child.setParent(this);
            children.add(child);
        }
    }

    public void removeChild(MenuNode child) {
        children.remove(child);
    }

    public String getCssClass() {
        return pageClass == null ? null : pageClass.getSimpleName();
    }

    public boolean isEnabled() {
        if (pageClass == null) {
            // sub menu, check if any child is enabled
            for (MenuNode child : children) {
                if (child.isEnabled()) {
                    return true;
                }
            }
            return false;
        }

        return getAuthorizationStrategy().isInstantiationAuthorized(pageClass);
    }

    public void onNewLink(Component link) {
    }

    @SuppressWarnings({"RedundantIfStatement"})
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MenuNode menuNode = (MenuNode) o;

        if (!name.equals(menuNode.name)) {
            return false;
        }
        if (pageClass != null ? !pageClass.equals(menuNode.pageClass) : menuNode.pageClass != null) {
            return false;
        }
        if (parent != null ? !parent.equals(menuNode.parent) : menuNode.parent != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = pageClass != null ? pageClass.hashCode() : 0;
        result = 31 * result + name.hashCode();
        result = 31 * result + (parent != null ? parent.hashCode() : 0);
        return result;
    }

    @Override
    public MenuNode getChildAt(int childIndex) {
        return children.get(childIndex);
    }

    @Override
    public int getChildCount() {
        return children.size();
    }

    @Override
    @SuppressWarnings({"SuspiciousMethodCalls"})
    public int getIndex(TreeNode node) {
        return children.indexOf(node);
    }

    @Override
    public boolean getAllowsChildren() {
        return true;
    }

    @Override
    public boolean isLeaf() {
        return children.isEmpty();
    }

    @Override
    public Enumeration children() {
        return new IteratorEnumeration(children.iterator());
    }

    public String getCookieName() {
        if (cookieName == null) {
            cookieName = generateCookieName();
        }
        return cookieName;
    }

    private String generateCookieName() {
        if (parent == null) {
            return "menu";
        }
        return parent.getCookieName() + "." + parent.getIndex(this);
    }

    public Boolean isOpened() {
        if (isLeaf()) {
            return false;
        }

        String cookieValue = getCookie(getCookieName());
        if (StringUtils.isEmpty(cookieValue)) {
            return null;
        }

        return Boolean.TRUE.toString().equals(cookieValue);
    }

    public void setOpened(Boolean opened) {
        if (isLeaf()) {
            return;
        }

        if (opened == null) {
            clearCookie(getCookieName());
            return;
        }

        setCookie(getCookieName(), opened);
    }

    protected IAuthorizationStrategy getAuthorizationStrategy() {
        return Application.get().getSecuritySettings().getAuthorizationStrategy();
    }
}
