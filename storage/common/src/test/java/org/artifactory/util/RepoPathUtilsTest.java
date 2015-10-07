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

package org.artifactory.util;

import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * Tests the {@link org.artifactory.util.RepoPathUtils}.
 * This test is here because it has runtime dependency on {@link org.artifactory.model.common.RepoPathImpl}.
 *
 * @author Yossi Shaul
 */
@Test
public class RepoPathUtilsTest {

    public void getAncestorParent() {
        RepoPath path = RepoPathFactory.create("test", "1/2");
        assertEquals(RepoPathUtils.getAncestor(path, 1), path.getParent());
    }

    public void getAncestorGrandParent() {
        RepoPath path = RepoPathFactory.create("test", "1/2");
        assertEquals(RepoPathUtils.getAncestor(path, 2), path.getParent().getParent());
    }

    public void getAncestorOfRoot() {
        RepoPath path = RepoPathFactory.create("test", "");
        assertNull(RepoPathUtils.getAncestor(path, 1));
    }

    public void getAncestorBeyondRoot() {
        RepoPath path = RepoPathFactory.create("test", "1/2");
        assertNull(RepoPathUtils.getAncestor(path, 3));
    }
}
