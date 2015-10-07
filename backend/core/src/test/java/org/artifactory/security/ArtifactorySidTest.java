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

package org.artifactory.security;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * ArtifactorySid unit tests.
 *
 * @author Yossi Shaul
 */
@Test
public class ArtifactorySidTest {

    public void userConstructor() {
        ArtifactorySid sid = new ArtifactorySid("momo", false);
        assertEquals(sid.getPrincipal(), "momo");
        assertFalse(sid.isGroup(), "Default group value should be false");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void nullPrincipal() {
        new ArtifactorySid(null, false);
    }

    public void userSidCreation() {
        ArtifactorySid sid = new ArtifactorySid("momo", false);
        assertEquals(sid.getPrincipal(), "momo");
        assertFalse(sid.isGroup());
    }

    public void groupSidCreation() {
        ArtifactorySid sid = new ArtifactorySid("agroup", true);
        assertEquals(sid.getPrincipal(), "agroup");
        assertTrue(sid.isGroup());
    }

    public void userSidsEquals() {
        ArtifactorySid sid1 = new ArtifactorySid("user1", false);
        // equal entry
        ArtifactorySid sid2 = new ArtifactorySid("user1", false);
        // different user
        ArtifactorySid sid3 = new ArtifactorySid("user2", false);
        // subclass with same value
        ArtifactorySid sid4 = new ArtifactorySid("user1", false) {
        };
        // group with same name
        ArtifactorySid groupSid = new ArtifactorySid("user1", true);

        assertTrue(sid1.equals(sid2));
        assertFalse(sid1.equals(sid3), "Different user sid");
        assertFalse(sid1.equals(sid4), "Subclass should not equal");
        assertFalse(sid1.equals(groupSid), "Group and User should not equal");
    }

    public void groupSidsEquals() {
        ArtifactorySid sid1 = new ArtifactorySid("group1", true);
        // equal entry
        ArtifactorySid sid2 = new ArtifactorySid("group1", true);
        // different user
        ArtifactorySid sid3 = new ArtifactorySid("group2", false);
        // subclass with same value
        ArtifactorySid sid4 = new ArtifactorySid("group1", false) {
        };

        assertTrue(sid1.equals(sid2));
        assertFalse(sid1.equals(sid3), "Different group sid");
        assertFalse(sid1.equals(sid4), "Subclass should not equal");
    }

    public void groupAndUserNotEquals() {
        ArtifactorySid userSid = new ArtifactorySid("principal", false);
        ArtifactorySid groupSid = new ArtifactorySid("principal", true);

        assertFalse(userSid.equals(groupSid), "User and group SIDs cannot be equal");
    }
}
