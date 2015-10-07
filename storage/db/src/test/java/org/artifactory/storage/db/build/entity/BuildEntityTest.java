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

import com.google.common.collect.ImmutableSortedSet;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import static org.testng.Assert.*;

/**
 * Date: 10/31/12
 * Time: 8:41 AM
 *
 * @author freds
 */
@Test
public class BuildEntityTest {

    public void simpleBuildTest() {
        BuildEntity b = new BuildEntity(1L, "a", "1", 10000000L, "h", 34L, "c", 36L, "m");
        b.setProperties(new HashSet<BuildProperty>());
        b.setPromotions(new HashSet<BuildPromotionStatus>());
        assertEquals(b.getBuildId(), 1L);
        assertEquals(b.getBuildName(), "a");
        assertEquals(b.getBuildNumber(), "1");
        assertEquals(b.getBuildDate(), 10000000L);
        assertEquals(b.getCiUrl(), "h");
        assertEquals(b.getCreated(), 34L);
        assertEquals(b.getCreatedBy(), "c");
        assertEquals(b.getModified(), 36L);
        assertEquals(b.getModifiedBy(), "m");
        assertTrue(b.getProperties().isEmpty());
        assertTrue(b.getPromotions().isEmpty());
    }

    public void maxNullBuildTest() {
        BuildEntity b = new BuildEntity(1L, "a", "1", 10000000L, null, 3L, null, 0L, null);
        assertEquals(b.getBuildId(), 1L);
        assertEquals(b.getBuildName(), "a");
        assertEquals(b.getBuildNumber(), "1");
        assertEquals(b.getBuildDate(), 10000000L);
        assertNull(b.getCiUrl());
        assertEquals(b.getCreated(), 3L);
        assertNull(b.getCreatedBy());
        assertEquals(b.getModified(), 0L);
        assertNull(b.getModifiedBy());
        b = new BuildEntity(1L, "a", "1", 10000000L, null, 3L, null, -2L, null);
        assertEquals(b.getBuildId(), 1L);
        assertEquals(b.getBuildName(), "a");
        assertEquals(b.getBuildNumber(), "1");
        assertEquals(b.getBuildDate(), 10000000L);
        assertNull(b.getCiUrl());
        assertEquals(b.getCreated(), 3L);
        assertNull(b.getCreatedBy());
        assertEquals(b.getModified(), -2L);
        assertNull(b.getModifiedBy());
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = ".*id.*negative.*")
    public void noIdBuildTest() {
        new BuildEntity(0L, "a", "1", 10000000L, null, 34L, "c", 36L, "m");
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = ".*id.*negative.*")
    public void negIdBuildTest() {
        new BuildEntity(-1L, "a", "1", 10000000L, null, 34L, "c", 36L, "m");
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = ".*name.*null.*")
    public void nullNameBuildTest() {
        new BuildEntity(1L, null, "1", 10000000L, null, 34L, "c", 36L, "m");
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = ".*name.*null.*")
    public void noNameBuildTest() {
        new BuildEntity(1L, " ", "1", 10000000L, null, 34L, "c", 36L, "m");
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = ".*number.*null.*")
    public void nullNumberBuildTest() {
        new BuildEntity(1L, "a", null, 10000000L, null, 34L, "c", 36L, "m");
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = ".*number.*null.*")
    public void noNumberBuildTest() {
        new BuildEntity(1L, "a", " ", 10000000L, null, 34L, "c", 36L, "m");
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = ".*date.*null.*")
    public void nullDateBuildTest() {
        new BuildEntity(1L, "a", "1", 0L, null, 34L, "c", 36L, "m");
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = ".*date.*null.*")
    public void noDateBuildTest() {
        new BuildEntity(1L, "a", "1", -3L, null, 34L, "c", 36L, "m");
    }

    @Test(expectedExceptions = {IllegalStateException.class},
            expectedExceptionsMessageRegExp = ".*not initialized.*Properties missing.*")
    public void noPropertiesInBuildTest() {
        BuildEntity b = new BuildEntity(1L, "a", "1", 10000000L, null, 1L, null, 0L, null);
        b.getProperties();
    }

    @Test(expectedExceptions = {IllegalArgumentException.class},
            expectedExceptionsMessageRegExp = ".*set properties.*null.*")
    public void setNullPropertiesInBuildTest() {
        BuildEntity b = new BuildEntity(1L, "a", "1", 10000000L, null, 1L, null, 0L, null);
        b.setProperties(null);
    }

    @Test(expectedExceptions = {IllegalStateException.class},
            expectedExceptionsMessageRegExp = ".*Properties already set.*")
    public void doubleSetPropertiesInBuildTest() {
        BuildEntity b = new BuildEntity(1L, "a", "1", 10000000L, null, 1L, null, 0L, null);
        b.setProperties(new HashSet<BuildProperty>());
        b.setProperties(new HashSet<BuildProperty>());
    }


    @Test(expectedExceptions = {IllegalStateException.class},
            expectedExceptionsMessageRegExp = ".*not initialized.*Promotions missing.*")
    public void noPromotionsInBuildTest() {
        BuildEntity b = new BuildEntity(1L, "a", "1", 10000000L, null, 1L, null, 0L, null);
        b.getPromotions();
    }

    @Test(expectedExceptions = {IllegalArgumentException.class},
            expectedExceptionsMessageRegExp = ".*set promotion.*null.*")
    public void setNullPromotionsInBuildTest() {
        BuildEntity b = new BuildEntity(1L, "a", "1", 10000000L, null, 1L, null, 0L, null);
        b.setPromotions(null);
    }

    @Test(expectedExceptions = {IllegalStateException.class},
            expectedExceptionsMessageRegExp = ".*Promotions already set.*")
    public void doubleSetPromotionsInBuildTest() {
        BuildEntity b = new BuildEntity(1L, "a", "1", 10000000L, null, 1L, null, 0L, null);
        b.setPromotions(new HashSet<BuildPromotionStatus>());
        b.setPromotions(new HashSet<BuildPromotionStatus>());
    }

    public void buildPropertiesTest() {
        BuildEntity b = new BuildEntity(1L, "a", "1", 10000000L, "h", 34L, "c", 36L, "m");
        Collection<BuildProperty> properties = new ArrayList<BuildProperty>();
        properties.add(new BuildProperty(1L, 1L, "k1", "v1"));
        properties.add(new BuildProperty(2L, 1L, "k2", "v2"));
        BuildProperty badApple = new BuildProperty(2L, 1L, "k-bad", "v2");
        properties.add(badApple);
        b.setProperties(properties);
        assertEquals(b.getBuildId(), 1L);
        assertEquals(b.getBuildName(), "a");
        assertEquals(b.getBuildNumber(), "1");
        assertEquals(b.getBuildDate(), 10000000L);
        assertEquals(b.getCiUrl(), "h");
        assertEquals(b.getCreated(), 34L);
        assertEquals(b.getCreatedBy(), "c");
        assertEquals(b.getModified(), 36L);
        assertEquals(b.getModifiedBy(), "m");
        assertEquals(b.getProperties().size(), 2);
        assertTrue(b.getProperties().contains(badApple));
        for (BuildProperty buildProperty : b.getProperties()) {
            assertFalse(buildProperty.isIdentical(badApple));
        }
    }

    public void buildPromotionsTest() {
        BuildEntity b = new BuildEntity(1L, "a", "1", 10000000L, "h", 34L, "c", 36L, "m");
        Collection<BuildPromotionStatus> promotions = new ArrayList<BuildPromotionStatus>();
        BuildPromotionStatus first = new BuildPromotionStatus(1L, 2L, "me", "rel", null, null, null);
        BuildPromotionStatus second = new BuildPromotionStatus(1L, 3L, "me", "roll", null, null, null);
        // Insert second first ;)
        promotions.add(second);
        promotions.add(first);
        BuildPromotionStatus badApple = new BuildPromotionStatus(1L, 2L, "me", "k-bad", null, null, null);
        promotions.add(badApple);
        b.setPromotions(promotions);
        assertEquals(b.getBuildId(), 1L);
        assertEquals(b.getBuildName(), "a");
        assertEquals(b.getBuildNumber(), "1");
        assertEquals(b.getBuildDate(), 10000000L);
        assertEquals(b.getCiUrl(), "h");
        assertEquals(b.getCreated(), 34L);
        assertEquals(b.getCreatedBy(), "c");
        assertEquals(b.getModified(), 36L);
        assertEquals(b.getModifiedBy(), "m");
        ImmutableSortedSet<BuildPromotionStatus> statuses = b.getPromotions();
        assertEquals(statuses.size(), 2);
        assertTrue(statuses.contains(badApple));
        assertTrue(statuses.first().isIdentical(first));
        assertTrue(statuses.last().isIdentical(second));
        for (BuildPromotionStatus buildPromo : statuses) {
            assertFalse(buildPromo.isIdentical(badApple));
        }
    }
}
