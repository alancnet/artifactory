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

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests the {@link MavenVersionComparator}.
 *
 * @author Yossi Shaul
 */
@Test
public class MavenVersionComparatorTest {
    private final MavenVersionComparator comparator = new MavenVersionComparator();

    public void numericMajorVersionOnly() {
        assertEquals(comparator.compare("1", "2"), -1, "2>1");
        assertEquals(comparator.compare("2", "1"), 1, "2>1");
        assertEquals(comparator.compare("3", "3"), 0, "3=3");
    }

    public void numericMajorMinor() {
        assertEquals(comparator.compare("1.13", "2.0"), -1, "2.0>1.1");
        assertEquals(comparator.compare("5.06", "5.08"), -1, "5.06<5.08");
        assertEquals(comparator.compare("5.06", "5"), 1, "5<5.06");
        assertEquals(comparator.compare("5.0", "5"), 0, "5.0=5");
    }

    public void numericMajorMinorBug() {
        assertEquals(comparator.compare("2.1.2", "2.1.3"), -1, "2.1.3>2.1.2");
        assertEquals(comparator.compare("2.2.2", "2.1.3"), 1, "2.1.3<2.2.2");
        assertEquals(comparator.compare("2.2.2", "3.2.2"), -1, "2.2.2<3.2.2");

        assertEquals(comparator.compare("2.2.2", "3"), -1, "2.2.2<3");
        assertEquals(comparator.compare("2.2.2", "3.0"), -1, "2.2.2<3.0");
        assertEquals(comparator.compare("2.2.2", "3.0.0"), -1, "2.2.2<3.0.0");
    }

    public void alphaBetaMilestoneRcGaVersions() {

        // alpha before beta
        assertEquals(comparator.compare("2.1.0-alpha", "2.1.0-alpha-1"), -1, "2.1.0-alpha<2.1.0-alpha-1");
        assertEquals(comparator.compare("2.1.0-alpha-1", "2.1.0-alpha-2"), -1, "2.1.0-alpha-1<2.1.0-alpha-2");
        assertEquals(comparator.compare("2.1.0-alpha-2", "2.1.0-beta-1"), -1, "2.1.0-alpha-2<2.1.0-beta-1");

        // beta before release candidate
        assertTrue(comparator.compare("2.1.0-beta-2", "2.1.0-rc") < -1, "2.1.0-beta-2<2.1.0-rc");
        assertTrue(comparator.compare("2.1.0-beta-2", "2.1.0-rc-1") < -1, "2.1.0-beta-2<2.1.0-rc-1");

        // beta before milestone
        assertTrue(comparator.compare("2.1.0-alpha-1", "2.1.0-m1") < -1, "2.1.0-alpha-1<2.1.0-m1");
        assertTrue(comparator.compare("2.1.0-beta-1", "2.1.0-m-1") < -1, "2.1.0-beta-1<2.1.0-m-1");

        // milestone before release candidate
        assertTrue(comparator.compare("2.1.0-m1", "2.1.0-ga") < -1, "2.1.0-m1<2.1.0-ga");
        assertTrue(comparator.compare("2.1.0-m5", "2.1.0-ga-1") < -1, "2.1.0-m5<2.1.0-ga-1");

        // release candidate before general availability
        assertEquals(comparator.compare("2.1.0-rc-1", "2.1.0-ga"), -1, "2.1.0-rc-1<2.1.0-ga");
        assertEquals(comparator.compare("2.1.0-ga", "2.1.0-ga-1"), -1, "2.1.0-ga<2.1.0-ga-1");

        // final are equals
        assertEquals(comparator.compare("5.1-ga", "5.1"), 0, "5.1-ga=5.1");
        assertEquals(comparator.compare("5.1-ga", "5.1-final"), 0, "5.1-ga=5.1-final");

        // release is better
        assertTrue(comparator.compare("4.0.0", "4.0.0-alpha") > 1, "4.0.0>4.0.0-alpha");
        assertTrue(comparator.compare("4.0.0", "4.0.0-beta") > 1, "4.0.0>4.0.0-beta");
        assertEquals(comparator.compare("4.0.0", "4.0.0-rc"), 1, "4.0.0>4.0.0-rc");
        assertEquals(comparator.compare("4.0.0", "4.0.0-rc1"), 1, "4.0.0>4.0.0-rc1");
        assertEquals(comparator.compare("4.0.0", "4.0.0-rc6"), 1, "4.0.0>4.0.0-rc6");
    }

    public void snapshotVersions() {
        assertEquals(comparator.compare("2.1.0-alpha", "2.1.0-SNAPSHOT"), 1, "2.1.0-alpha>2.1.0-SNAPSHOT");
        assertTrue(comparator.compare("2.1.0", "2.1.0-SNAPSHOT") > 1, "2.1.0>2.1.0-SNAPSHOT");
    }

}
