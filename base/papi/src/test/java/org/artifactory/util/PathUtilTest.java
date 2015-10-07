/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2014 JFrog Ltd.
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

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for PathUtil
 *
 * @author Gidi Shabat
 */
@Test
public class PathUtilTest {
    //
    @Test
    public void getPathNameTest() {
        Assert.assertEquals(PathUtils.getFileName("a"), "a");
        Assert.assertEquals(PathUtils.getFileName("//a"), "a");
        Assert.assertEquals(PathUtils.getFileName("a://a"), "a");
        Assert.assertEquals(PathUtils.getFileName("a/b/c"), "c");
        Assert.assertEquals(PathUtils.getFileName("a/b/////c"), "c");
        Assert.assertEquals(PathUtils.getFileName("//a/b/////c//"), "c");
        Assert.assertEquals(PathUtils.getFileName("//a/b/////?//"), "?");
        Assert.assertEquals(PathUtils.getFileName("//a/b/////.//"), ".");
        Assert.assertEquals(PathUtils.getFileName("a:properties"), "a:properties");
        Assert.assertEquals(PathUtils.getFileName("///a:properties"), "a:properties");
        Assert.assertEquals(PathUtils.getFileName("///a/b/c:properties"), "c:properties");
        Assert.assertEquals(PathUtils.getFileName("///a\\b\\c"), "c");
        Assert.assertEquals(PathUtils.getFileName("///a\\b\\\\c"), "c");
        Assert.assertEquals(PathUtils.getFileName("text.txt"), "text.txt");
        Assert.assertEquals(PathUtils.getFileName("a/b/text.txt"), "text.txt");
        Assert.assertEquals(PathUtils.getFileName("a/b/text.txt\\"), "text.txt");
    }
}
