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

package org.artifactory.repo.remote.browse;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Unit tests for {@link RemoteItem}.
 *
 * @author Yossi Shaul
 */
@Test
public class RemoteItemTest {
    @Test(expectedExceptions = NullPointerException.class)
    public void nullUrl() {
        new RemoteItem(null, false);
    }

    public void getNameFromFile() {
        assertEquals(new RemoteItem("http://test/123/file.name", false).getName(), "file.name");
    }

    public void getNameFromDirectory() {
        assertEquals(new RemoteItem("http://test/123/dir/", true).getName(), "dir");
    }
}
