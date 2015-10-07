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

import org.apache.commons.lang.SystemUtils;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.fs.MutableFileInfo;
import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.storage.fs.tree.FileNode;
import org.artifactory.storage.fs.tree.ItemNode;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests {@link org.artifactory.maven.versioning.VersionNameMavenMetadataVersionComparator}. Additional tests for
 * various version strings are in {@link MavenVersionComparatorTest}.
 *
 * @author Yossi Shaul
 */
@Test
public class MavenMetadataVersionComparatorTest {
    static String[] versionsJetty = new String[]{
            "6.0.1", "6.1.0pre1", "6.1.0pre2", "6.0.2", "6.1.0pre3", "6.1.0rc0", "6.1.0rc1", "6.1.0rc2", "6.1.0rc3",
            "6.1.0", "6.1.1rc0", "6.1.1rc1", "6.1.1", "6.1.2pre0", "6.1.2pre1", "6.1.2rc0", "6.1.2rc1", "6.1.2rc2",
            "6.1.2rc4", "6.1.2rc5", "6.1.2", "6.1.3", "6.1.4rc0", "6.1.4rc1", "6.1H.4rc1", "6.1.4", "6.1H.4-beta",
            "6.1.5rc0", "6.1.5", "6.1H.5-beta", "6.1.6rc0", "6.1.6rc1", "6.1.6", "6.1H.6", "6.1.7", "6.1H.7", "6.1.8",
            "6.1H.8", "6.1.9", "7.0.0pre0", "7.0.0pre1", "6.1.10", "6.1.11", "6.1H.10", "7.0.0pre2", "6.1.12rc1",
            "7.0.0pre3", "6.1.12.rc2", "6.1.12.rc3", "6.1.12.rc4", "7.0.0.pre4", "6.1.12.rc5", "7.0.0.pre5", "6.1.12",
            "6.1.14", "6.1H.14", "6.1H.14.1", "6.1.15.pre0", "6.1.15.rc2", "6.1.15.rc3", "6.1.15.rc4", "6.1.15.rc5",
            "6.1.15", "6.1.16", "6.1H.22", "6.1.17", "6.1.18", "6.1.19", "6.1.20", "6.1.21", "6.1.22", "6.1.23",
            "6.1.24", "6.1.25", "6.1.26RC0", "6.1.26"
    };

    public void compare1And2() {
        VersionNameMavenMetadataVersionComparator comparator = new VersionNameMavenMetadataVersionComparator();

        MutableFileInfo olderFileInfo = InfoFactoryHolder.get().createFileInfo(new RepoPathImpl("repo", "2.0"));
        olderFileInfo.setCreated(System.currentTimeMillis());
        ItemNode older = new FileNode(olderFileInfo);

        MutableFileInfo newerFileInfo = InfoFactoryHolder.get().createFileInfo(new RepoPathImpl("repo", "1.1"));
        newerFileInfo.setCreated(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(2));
        ItemNode newer = new FileNode(newerFileInfo);

        assertEquals(comparator.compare(older, newer), 1, "The comparison should be version name based");

    }

    public void compareComplicatedJetty() {
        MavenVersionComparator versionComparator = new MavenVersionComparator();
        List<String> testList = new ArrayList<String>(versionsJetty.length);
        Collections.addAll(testList, versionsJetty);
        Iterator<String> it = testList.iterator();
        while (it.hasNext()) {
            String version = it.next();
            if (version.contains("pre")) {
                it.remove();
            }
        }
        // Without pre we are OK
        Collections.sort(testList, versionComparator);
        try {
            // On the full list will fail on comparison contract
            Arrays.sort(versionsJetty, versionComparator);
            assertTrue(!SystemUtils.IS_JAVA_1_7, "Should have get comparison contract error!");
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Comparison method violates its general contract!");
            Arrays.sort(versionsJetty);
        }
    }
}
