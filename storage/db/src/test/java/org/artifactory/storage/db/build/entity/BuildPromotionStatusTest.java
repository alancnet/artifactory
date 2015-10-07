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

package org.artifactory.storage.db.build.entity;

import com.google.common.collect.ImmutableList;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;

import static org.testng.Assert.*;

/**
 * Date: 11/23/12
 * Time: 10:47 AM
 *
 * @author freds
 */
@Test
public class BuildPromotionStatusTest {
    public void simpleBuildPromotionTest() {
        BuildPromotionStatus bp = new BuildPromotionStatus(1L, 2L, "me", "s", "r", "c", "u");
        assertEquals(bp.getBuildId(), 1L);
        assertEquals(bp.getCreated(), 2L);
        assertEquals(bp.getCreatedBy(), "me");
        assertEquals(bp.getStatus(), "s");
        assertEquals(bp.getRepository(), "r");
        assertEquals(bp.getComment(), "c");
        assertEquals(bp.getCiUser(), "u");
    }

    public void maxNullsBuildPromotionTest() {
        BuildPromotionStatus bp = new BuildPromotionStatus(1L, 2L, null, "s", null, null, null);
        assertEquals(bp.getBuildId(), 1L);
        assertEquals(bp.getCreated(), 2L);
        assertEquals(bp.getStatus(), "s");
        assertNull(bp.getCreatedBy());
        assertNull(bp.getRepository());
        assertNull(bp.getComment());
        assertNull(bp.getCiUser());
    }

    public void equalsBuildPromotionsTest() {
        BuildPromotionStatus bp1 = new BuildPromotionStatus(1L, 2L, null, "s", null, null, null);
        BuildPromotionStatus bp2 = new BuildPromotionStatus(2L, 2L, null, "s", null, null, null);
        BuildPromotionStatus bp3 = new BuildPromotionStatus(2L, 1L, null, "s", null, null, null);

        ImmutableList<BuildPromotionStatus> allEqualsToBp2 = ImmutableList.of(
                new BuildPromotionStatus(2L, 2L, null, "s1", null, null, null),
                new BuildPromotionStatus(2L, 2L, "me", "s", null, null, null),
                new BuildPromotionStatus(2L, 2L, null, "s", "re", null, null),
                new BuildPromotionStatus(2L, 2L, null, "s", null, "co", null),
                new BuildPromotionStatus(2L, 2L, null, "s", null, null, "ci"));

        assertNotEquals(bp1, bp2);
        assertNotEquals(bp1.hashCode(), bp2.hashCode());
        assertFalse(bp1.isIdentical(bp2));
        assertTrue(bp1.compareTo(bp2) < 0);

        assertNotEquals(bp1, bp3);
        assertNotEquals(bp1.hashCode(), bp3.hashCode());
        assertFalse(bp1.isIdentical(bp3));
        assertTrue(bp1.compareTo(bp3) < 0);

        assertNotEquals(bp3, bp2);
        assertNotEquals(bp3.hashCode(), bp2.hashCode());
        assertFalse(bp3.isIdentical(bp2));
        assertTrue(bp3.compareTo(bp2) < 0);

        for (BuildPromotionStatus status : allEqualsToBp2) {
            assertEquals(bp2, status);
            assertEquals(bp2.hashCode(), status.hashCode());
            assertFalse(bp2.isIdentical(status));
            assertEquals(status.compareTo(bp2), 0);
            assertTrue(status.compareTo(bp1) > 0);
            assertTrue(status.compareTo(bp3) > 0);
        }
    }

    public void testBuildPromotionsOrderTest() {
        // The order should be 1, 3, 2
        BuildPromotionStatus bp1 = new BuildPromotionStatus(1L, 2L, null, "s", null, null, null);
        BuildPromotionStatus bp2 = new BuildPromotionStatus(2L, 2L, null, "s", null, null, null);
        BuildPromotionStatus bp3 = new BuildPromotionStatus(2L, 1L, null, "s", null, null, null);

        ArrayList<BuildPromotionStatus> statuses = new ArrayList<BuildPromotionStatus>(3);
        statuses.add(bp2);
        statuses.add(bp1);
        statuses.add(bp3);
        Collections.sort(statuses);
        assertEquals(statuses.get(0), bp1);
        assertEquals(statuses.get(1), bp3);
        assertEquals(statuses.get(2), bp2);
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = ".*id.*zero.*")
    public void noBuildIdBuildPromotionTest() {
        new BuildPromotionStatus(0L, 1L, null, "s", null, null, null);
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = ".*id.*negative.*")
    public void negBuildIdBuildPromotionTest() {
        new BuildPromotionStatus(-2L, 1L, null, "s", null, null, null);
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = ".*date.*zero.*")
    public void noCreatedBuildPromotionTest() {
        new BuildPromotionStatus(1L, 0L, null, "s", null, null, null);
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = ".*date.*negative.*")
    public void negCreatedBuildPromotionTest() {
        new BuildPromotionStatus(1L, -1L, null, "s", null, null, null);
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = ".*status.*null.*")
    public void nullStatusBuildPromotionTest() {
        new BuildPromotionStatus(1L, 1L, null, null, null, null, null);
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = ".*status.*empty.*")
    public void emptyStatusBuildPromotionTest() {
        new BuildPromotionStatus(1L, 1L, null, " ", null, null, null);
    }
}
