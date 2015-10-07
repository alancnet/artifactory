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

package org.artifactory.model.xstream.fs;

import org.artifactory.fs.ZipEntryInfo;
import org.artifactory.util.TreeNode;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

/**
 * A tree node representing ZipEntry.
 *
 * @author Yossi Shaul
 */
public class ZipTreeNode implements TreeNode<ZipEntryInfo>, Serializable, Comparable<ZipTreeNode> {

    // TODO: Should be final
    private /*final*/ ZipEntryInfo zipEntry;
    private Set<ZipTreeNode> children;

    public ZipTreeNode(String entryPath, boolean directory) {
        zipEntry = new ZipEntryImpl(entryPath, directory);
    }

    @Override
    public Set<ZipTreeNode> getChildren() {
        return children;
    }

    @Override
    public ZipEntryInfo getData() {
        return zipEntry;
    }

    @Override
    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    @Override
    public boolean isLeaf() {
        return !hasChildren();
    }

    public boolean isDirectory() {
        return zipEntry.isDirectory();
    }

    @Override
    public ZipTreeNode getChild(ZipEntryInfo data) {
        if (!data.getPath().startsWith(zipEntry.getPath())) {
            return null;
        }

        return getChild(data.getName());
    }

    public String getPath() {
        return zipEntry.getPath();
    }

    public String getName() {
        return zipEntry.getName();
    }

    ZipTreeNode getChild(String relativePath) {
        if (children != null) {
            for (ZipTreeNode child : children) {
                if (child.getName().equals(relativePath)) {
                    return child;
                }
            }
        }
        return null;
    }

    public void addChild(ZipTreeNode child) {
        if (!zipEntry.isDirectory()) {
            throw new IllegalStateException("Cannot add children to a leaf node");
        }
        if (children == null) {
            children = new TreeSet<>();
        }
        children.add(child);
    }

    public void setZipEntry(ZipEntryInfo zipEntry) {
        this.zipEntry = zipEntry;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ZipTreeNode that = (ZipTreeNode) o;

        if (!zipEntry.equals(that.zipEntry)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return zipEntry.hashCode();
    }

    @Override
    public int compareTo(ZipTreeNode o) {
        if (o.isDirectory() && !isDirectory()) {
            return 1;
        }
        if (!o.isDirectory() && isDirectory()) {
            return -1;
        }
        return getName().compareTo(o.getName());
    }
}
