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

package org.artifactory.storage.fs.tree;

import org.artifactory.api.repo.RootNodesFilterResult;
import org.artifactory.fs.FileInfo;
import org.artifactory.fs.ItemInfo;

import java.util.Collections;
import java.util.List;

/**
 * A file node is a file system node with {@link org.artifactory.fs.FileInfo} as its data.
 * The node is detached from the database and may not exist anymore.
 *
 * @author Yossi Shaul
 */
public class FileNode extends ItemNode {
    public FileNode(FileInfo itemInfo) {
        super(itemInfo);
    }

    @Override
    public List<ItemNode> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public List<ItemNode> getChildren(boolean returnAcceptedNode,RootNodesFilterResult rootNodesFilterResult) {
        return Collections.emptyList();
    }

    @Override
    public List<ItemInfo> getChildrenInfo() {
        return Collections.emptyList();
    }

    @Override
    public boolean hasChildren() {
        return false;
    }

    @Override
    public FileInfo getItemInfo() {
        return (FileInfo) super.getItemInfo();
    }
}
