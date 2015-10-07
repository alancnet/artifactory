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

import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

/**
 * Tests the StringInputStream.
 *
 * @author Yossi Shaul
 */
@Test
public class StringInputStreamTest {

    @SuppressWarnings({"unchecked"})
    public void readOneLineString() throws IOException {
        StringInputStream in = new StringInputStream("a string");
        List<String> lines = IOUtils.readLines(in);
        Assert.assertEquals(lines.size(), 1, "Expecting only one line");
        Assert.assertEquals(lines.get(0), "a string", "Unexpected content: " + lines.get(0));
    }

    @SuppressWarnings({"unchecked"})
    public void readMultiLineString() throws IOException {
        StringInputStream in = new StringInputStream("line 1\nline2\nline   3");
        List<String> lines = IOUtils.readLines(in);
        Assert.assertEquals(lines.size(), 3, "Expecting 3 lines");
        Assert.assertEquals(lines.get(0), "line 1", "Unexpected content");
        Assert.assertEquals(lines.get(1), "line2", "Unexpected content");
    }

}
