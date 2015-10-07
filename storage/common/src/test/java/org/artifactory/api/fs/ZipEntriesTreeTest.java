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

package org.artifactory.api.fs;

import com.google.common.collect.Lists;
import org.artifactory.fs.ZipEntryInfo;
import org.artifactory.model.xstream.fs.ZipEntriesTree;
import org.artifactory.model.xstream.fs.ZipEntryImpl;
import org.artifactory.model.xstream.fs.ZipTreeNode;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.zip.ZipEntry;

import static org.testng.Assert.assertNotNull;

/**
 * Tests the {@link ZipEntriesTree}.
 *
 * @author Yossi Shaul
 */
@Test
public class ZipEntriesTreeTest {

    public void buildTree() {
        ZipEntriesTree tree = new ZipEntriesTree();

        String[] paths = {
                "a/", "b/", "a/b/", "a/b/c", "b/c"
        };

        List<ZipEntryInfo> entries = Lists.newArrayList();
        for (String path : paths) {
            entries.add(new ZipEntryImpl(new ZipEntry(path)));
        }

        for (ZipEntryInfo entry : entries) {
            tree.insert(entry);
        }


        ZipTreeNode root = tree.getRoot();
        Assert.assertEquals(root.getChildren().size(), 2);
        assertNotNull(root.getChild(entries.get(2)));   // a/b
        Assert.assertEquals(root.getChild(entries.get(2)).getChildren().size(), 1);
    }

}
