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

import org.apache.commons.lang.StringUtils;
import org.artifactory.fs.ZipEntryInfo;
import org.artifactory.util.Tree;

/**
 * A tree representation of a zip file content.
 *
 * @author Yossi Shaul
 */
public class ZipEntriesTree implements Tree<ZipEntryInfo> {

    ZipTreeNode root;

    public ZipEntriesTree() {
        this.root = new ZipTreeNode("", true);
    }

    @Override
    public ZipTreeNode getRoot() {
        return root;
    }

    @Override
    public ZipTreeNode insert(ZipEntryInfo entry) {
        String[] pathElements = entry.getPath().split("/");
        ZipTreeNode parent = root;
        // get or create parent nodes
        for (int i = 0; i < pathElements.length - 1; i++) {
            parent = getOrCreateNode(parent, pathElements[i], true);
        }
        // create node for current entry
        ZipTreeNode entryNode = getOrCreateNode(parent, pathElements[pathElements.length - 1], entry.isDirectory());
        entryNode.setZipEntry(entry);
        return parent;
    }

    private ZipTreeNode getOrCreateNode(ZipTreeNode parent, String pathElement, boolean directory) {
        ZipTreeNode child = parent.getChild(pathElement);
        if (child == null) {
            String path = StringUtils.isNotBlank(parent.getPath()) ? parent.getPath() + "/" + pathElement : pathElement;
            child = new ZipTreeNode(path, directory);
            parent.addChild(child);
        }
        return child;
    }

}
