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

package org.artifactory.storage.db.search.model;

import com.google.common.collect.Sets;
import org.artifactory.fs.ItemInfo;
import org.artifactory.sapi.search.ArchiveEntryRow;
import org.artifactory.sapi.search.VfsQueryRow;
import org.artifactory.storage.db.fs.entity.NodePath;

import java.util.Set;

/**
 * Date: 8/5/11
 * Time: 10:55 PM
 *
 * @author Fred Simon
 */
public class VfsQueryRowDbImpl implements VfsQueryRow {
    private final DbItemInfoProxy item;
    private final Set<ArchiveEntryRow> archiveEntries;

    public VfsQueryRowDbImpl(long nodeId, boolean file, NodePath nodePath) {
        if (file) {
            item = new DbFileInfoProxy(nodeId, nodePath);
        } else {
            item = new DbFolderInfoProxy(nodeId, nodePath);
        }
        archiveEntries = null;
    }

    public VfsQueryRowDbImpl(long nodeId, boolean file, NodePath nodePath, String entryPath, String entryName) {
        if (file) {
            item = new DbFileInfoProxy(nodeId, nodePath);
        } else {
            item = new DbFolderInfoProxy(nodeId, nodePath);
        }
        archiveEntries = Sets.newHashSet();
        addArchiveEntry(entryPath, entryName);
    }

    void addArchiveEntry(String entryPath, String entryName) {
        archiveEntries.add(new ArchiveSearchEntry(entryPath, entryName));
    }

    @Override
    public ItemInfo getItem() {
        return item;
    }

    @Override
    public Iterable<ArchiveEntryRow> getArchiveEntries() {
        return archiveEntries;
    }
}
