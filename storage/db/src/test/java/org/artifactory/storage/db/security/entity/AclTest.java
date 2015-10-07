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

package org.artifactory.storage.db.security.entity;

import org.artifactory.security.ArtifactoryPermission;
import org.testng.annotations.Test;

import java.util.HashSet;

import static org.testng.Assert.*;

/**
 * Date: 10/25/12
 * Time: 5:36 PM
 *
 * @author freds
 */
@Test
public class AclTest {

    public void simpleAcl() {
        long now = System.currentTimeMillis();
        Acl acl = new Acl(1L, 2L, now, "me");
        acl.setAces(new HashSet<Ace>());
        assertEquals(acl.getAclId(), 1L);
        assertEquals(acl.getPermTargetId(), 2L);
        assertEquals(acl.getLastModified(), now);
        assertEquals(acl.getLastModifiedBy(), "me");
        assertTrue(acl.getAces().isEmpty());
    }

    public void simpleAce() {
        Ace ace = new Ace(1L, 2L, ArtifactoryPermission.READ.getMask(), 3L, 0L);
        assertEquals(ace.getAceId(), 1L);
        assertEquals(ace.getAclId(), 2L);
        assertEquals(ace.getMask(), 1);
        assertEquals(ace.getUserId(), 3L);
        assertEquals(ace.getGroupId(), 0L);
    }

    public void aceEqualsHashTest() {
        // Only the id matters in equals/hash
        Ace ace1 = new Ace(1L, 2L, ArtifactoryPermission.READ.getMask(), 3L, 0L);
        Ace ace2 = new Ace(2L, 2L, ArtifactoryPermission.READ.getMask(), 3L, 0L);
        Ace ace3 = new Ace(1L, 3L, ArtifactoryPermission.MANAGE.getMask(), 0L, 5L);
        assertNotEquals(ace1, ace2);
        assertNotEquals(ace1.hashCode(), ace2.hashCode());
        assertNotEquals(ace3, ace2);
        assertNotEquals(ace3.hashCode(), ace2.hashCode());
        assertEquals(ace1, ace3);
        assertEquals(ace1.hashCode(), ace3.hashCode());
    }

    public void aceHashInAclTest() {
        long now = System.currentTimeMillis();
        Acl acl = new Acl(1L, 2L, now, "me");
        HashSet<Ace> aces = new HashSet<Ace>();
        aces.add(new Ace(1L, 2L, ArtifactoryPermission.READ.getMask(), 3L, 0L));
        aces.add(new Ace(2L, 2L, ArtifactoryPermission.READ.getMask(), 3L, 0L));
        aces.add(new Ace(1L, 3L, ArtifactoryPermission.MANAGE.getMask(), 0L, 5L));
        acl.setAces(aces);

        assertEquals(acl.getAclId(), 1L);
        assertEquals(acl.getPermTargetId(), 2L);
        assertEquals(acl.getLastModified(), now);
        assertEquals(acl.getLastModifiedBy(), "me");
        assertEquals(acl.getAces().size(), 2);
        assertTrue(acl.getAces().contains(new Ace(1L, 7L, 0, 0L, 8L)));
        assertTrue(acl.getAces().contains(new Ace(2L, 8L, 1, 8L, 0L)));
    }

    public void maxNullInAclTest() {
        new Acl(1L, 2L, 0L, null);
        new Acl(1L, 2L, -3L, null);
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = "Ids.*negative.*")
    public void noIdInAclTest() {
        new Acl(0L, 2L, 0L, null);
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = "Ids.*negative.*")
    public void negIdInAclTest() {
        new Acl(-1L, 2L, 0L, null);
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = "Ids.*negative.*")
    public void noPermIdInAclTest() {
        new Acl(1L, 0L, 0L, null);
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = "Ids.*negative.*")
    public void negPermIdInAclTest() {
        new Acl(1L, -2L, 0L, null);
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = "Ids.*negative.*")
    public void negAceAclIdTest() {
        new Ace(-1L, 2L, ArtifactoryPermission.READ.getMask(), 3L, 0L);
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = "Ids.*negative.*")
    public void negAceAceIdTest() {
        new Ace(1L, -2L, ArtifactoryPermission.READ.getMask(), 3L, 0L);
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = "Ids.*negative.*")
    public void negAceUserIdTest() {
        new Ace(1L, 2L, ArtifactoryPermission.READ.getMask(), -3L, 0L);
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = "Ids.*negative.*")
    public void negAceGroupIdTest() {
        new Ace(1L, 2L, ArtifactoryPermission.READ.getMask(), 3L, -4L);
    }

    @Test(expectedExceptions = {IllegalArgumentException.class},
            expectedExceptionsMessageRegExp = ".*both a group and a user.*")
    public void wrongAceUserAndGroupTest() {
        new Ace(1L, 2L, ArtifactoryPermission.READ.getMask(), 3L, 4L);
    }

    @Test(expectedExceptions = {IllegalArgumentException.class},
            expectedExceptionsMessageRegExp = ".*either a group or a user.*")
    public void wrongAceNoUserOrGroupTest() {
        new Ace(1L, 2L, ArtifactoryPermission.READ.getMask(), 0L, 0L);
    }

    @Test(expectedExceptions = {IllegalStateException.class},
            expectedExceptionsMessageRegExp = ".*not initialized.*ACEs missing.*")
    public void noAcesInAclTest() {
        Acl acl = new Acl(1L, 2L, 0L, null);
        acl.getAces();
    }

    @Test(expectedExceptions = {IllegalArgumentException.class},
            expectedExceptionsMessageRegExp = ".*set aces.*null.*")
    public void setNullAcesInAclTest() {
        Acl acl = new Acl(1L, 2L, 0L, null);
        acl.setAces(null);
    }

    @Test(expectedExceptions = {IllegalStateException.class},
            expectedExceptionsMessageRegExp = ".*ACEs already set.*")
    public void doubleSetAcesInAclTest() {
        Acl acl = new Acl(1L, 2L, 0L, null);
        acl.setAces(new HashSet<Ace>());
        acl.setAces(new HashSet<Ace>());
    }

}
