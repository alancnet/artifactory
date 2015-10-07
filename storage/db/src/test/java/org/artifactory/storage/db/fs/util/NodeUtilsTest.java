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

package org.artifactory.storage.db.fs.util;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Unit tests for {@link org.artifactory.storage.db.fs.util.NodeUtils}.
 *
 * @author Yossi Shaul
 */
@Test
public class NodeUtilsTest {

    public void emptyPath() {
        assertEquals(NodeUtils.getDepth(""), 0);
    }

    public void nullPath() {
        assertEquals(NodeUtils.getDepth(""), 0);
    }

    public void noSlashesPath() {
        assertEquals(NodeUtils.getDepth("elantris"), 1);
    }

    public void oneLevelPath() {
        assertEquals(NodeUtils.getDepth("a/b"), 2);
    }

    public void simplePath() {
        assertEquals(NodeUtils.getDepth("a/b/c"), 3);
    }

    public void trailingAndLeadingSlashesPath() {
        assertEquals(NodeUtils.getDepth("//a/b/c/"), 3);
    }

}
