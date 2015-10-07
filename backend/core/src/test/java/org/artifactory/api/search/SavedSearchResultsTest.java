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

package org.artifactory.api.search;

import com.google.common.collect.Lists;
import org.artifactory.factory.InfoFactory;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.fs.FileInfo;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Tests the functionality of {@link SavedSearchResults}.
 *
 * @author Yossi Shaul
 */
@Test
public class SavedSearchResultsTest {

    public void defaultConstructor() {
        SavedSearchResults results = new SavedSearchResults("test", Lists.<FileInfo>newArrayList());
        assertEquals(results.getName(), "test");
        assertEquals(results.getResults().size(), 0);
    }

    public void addResultsWithDuplicates() {
        SavedSearchResults results = new SavedSearchResults("test", null);

        InfoFactory factory = InfoFactoryHolder.get();
        FileInfo fileInfo = factory.createFileInfo(factory.createRepoPath("repo1", "a/b/c/d"));
        results.add(fileInfo);
        assertTrue(results.contains(fileInfo), "Just added file info not found...");

        // add the same file info again and expect to only one
        results.add(fileInfo);
        assertTrue(results.contains(fileInfo), "Just added file info not found...");
        assertEquals(results.size(), 1, "Should not allow duplicates");
    }

    public void mergeResults() {
        InfoFactory factory = InfoFactoryHolder.get();
        FileInfo sharedFileInfo = factory.createFileInfo(factory.createRepoPath("repo1", "test1"));
        FileInfo samePathDifferentRepoKey = factory.createFileInfo(factory.createRepoPath("repo2", "test1"));

        SavedSearchResults results = new SavedSearchResults("test", null);
        results.add(sharedFileInfo);

        SavedSearchResults toMerge = new SavedSearchResults("test", Lists.newArrayList(
                sharedFileInfo, samePathDifferentRepoKey));

        results.merge(toMerge);

        assertEquals(results.size(), 1, "The shared path should not have been added (same relative path)");
        assertTrue(results.contains(sharedFileInfo));
        assertTrue(results.contains(samePathDifferentRepoKey));
    }

    public void subtractResults() {
        InfoFactory factory = InfoFactoryHolder.get();
        FileInfo sharedFileInfo = factory.createFileInfo(factory.createRepoPath("repo1", "test1"));
        FileInfo notInSubtract = factory.createFileInfo(factory.createRepoPath("repo1", "test2"));
        FileInfo notInOriginal = factory.createFileInfo(factory.createRepoPath("repo1", "test2/123"));

        SavedSearchResults results = new SavedSearchResults("test",
                Lists.newArrayList(sharedFileInfo, notInSubtract));

        SavedSearchResults toMerge = new SavedSearchResults("test",
                Lists.newArrayList(sharedFileInfo, notInOriginal));

        results.subtract(toMerge);

        assertFalse(results.getResults().contains(sharedFileInfo), "Shared item should have been removed");
        assertTrue(results.getResults().contains(notInSubtract));
        assertEquals(results.size(), 1, "Only one item should remain: " + results);
    }

    public void intersectResults() {
        InfoFactory factory = InfoFactoryHolder.get();
        FileInfo shared = factory.createFileInfo(factory.createRepoPath("repo1", "test1"));
        FileInfo onlyInA = factory.createFileInfo(factory.createRepoPath("repo1", "test2"));
        FileInfo onlyInB = factory.createFileInfo(factory.createRepoPath("repo1", "test2/123"));

        SavedSearchResults results = new SavedSearchResults("A",
                Lists.newArrayList(shared, onlyInA));

        SavedSearchResults toIntersect = new SavedSearchResults("B",
                Lists.newArrayList(shared, onlyInB));

        results.intersect(toIntersect);

        assertTrue(results.getResults().contains(shared), "Shared item should be the only one left");
        assertTrue(results.contains(shared), "Shared item should be the only one left");
        assertEquals(results.size(), 1, "Only one item should remain: " + results);
    }

}
