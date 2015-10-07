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

package org.artifactory.build;

import org.jfrog.build.api.Build;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.Date;

import static java.lang.String.valueOf;
import static org.jfrog.build.api.Build.formatBuildStarted;

/**
 * @author jbaruch
 * @since 04/05/12
 */
public class DetailedBuildRunTests {

    private DetailedBuildRunImpl original;

    @BeforeTest
    public void setup() {
        Build build = new Build();
        build.setNumber(valueOf(42));
        build.setName("test-build");
        build.setStarted(formatBuildStarted(new Date().getTime()));
        original = new DetailedBuildRunImpl(build);
    }

    @Test
    public void testCopy() throws Exception {
        DetailedBuildRun copy = original.copy();
        Assert.assertNotEquals(copy, original);//started should be different
        Assert.assertEquals(copy.getName(), original.getName());
        Assert.assertEquals(copy.getNumber(), original.getNumber());
        Assert.assertNotEquals(copy.getStarted(), original.getStarted());
    }

    @Test
    public void testCopyWithNewNumber() throws Exception {
        String newNumber = "42-r";
        DetailedBuildRun copy = original.copy(newNumber);
        Assert.assertNotEquals(copy, original);//started should be different
        Assert.assertEquals(copy.getName(), original.getName());
        Assert.assertNotEquals(copy.getNumber(), original.getNumber());
        Assert.assertEquals(copy.getNumber(), newNumber);
        Assert.assertNotEquals(copy.getStarted(), original.getStarted());
    }
}
