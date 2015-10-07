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

package org.artifactory.repo.service.mover;

import org.artifactory.model.common.RepoPathImpl;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Tests the {@link org.artifactory.repo.service.mover.RepoPathIterator}.
 *
 * @author Yossi Shaul
 */
@Test
public class RepoPathIteratorTest {

    public void rootRepoPath() {
        RepoPathImpl root = new RepoPathImpl("root", "");
        RepoPathIterator iter = new RepoPathIterator(root);
        assertTrue(iter.hasNext());
        assertEquals(iter.next(), root);
        assertFalse(iter.hasNext());
    }

    public void twoLevelsRepoPath() {
        RepoPathImpl orig = new RepoPathImpl("root", "1/2");
        RepoPathIterator iter = new RepoPathIterator(orig);
        assertTrue(iter.hasNext());
        assertEquals(iter.next(), orig.getParent().getParent());
        assertTrue(iter.hasNext());
        assertEquals(iter.next(), orig.getParent());
        assertTrue(iter.hasNext());
        assertEquals(iter.next(), orig);
        assertFalse(iter.hasNext());
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void nullPath() {
        new RepoPathIterator(null);
    }

}
