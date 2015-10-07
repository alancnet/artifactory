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

package org.artifactory.maven.versioning;

import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.fs.MutableFileInfo;
import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.storage.fs.tree.FileNode;
import org.artifactory.storage.fs.tree.ItemNode;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;

/**
 * Tests {@link CreationDateMavenMetadataVersionComparator}.
 *
 * @author Yossi Shaul
 */
@Test
public class CreationDateMavenMetadataVersionComparatorTest {

    public void compare() {
        CreationDateMavenMetadataVersionComparator comparator = new CreationDateMavenMetadataVersionComparator();

        MutableFileInfo olderFileInfo = InfoFactoryHolder.get().createFileInfo(new RepoPathImpl("repo", "2.0"));
        olderFileInfo.setCreated(System.currentTimeMillis());
        ItemNode older = new FileNode(olderFileInfo);

        MutableFileInfo newerFileInfo = InfoFactoryHolder.get().createFileInfo(new RepoPathImpl("repo", "1.1"));
        newerFileInfo.setCreated(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(2));
        ItemNode newer = new FileNode(newerFileInfo);

        assertEquals(comparator.compare(older, newer), -1, "The comparison should be time based");

    }
}
