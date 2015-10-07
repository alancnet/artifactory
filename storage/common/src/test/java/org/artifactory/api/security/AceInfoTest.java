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

package org.artifactory.api.security;

import com.google.common.collect.Sets;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.artifactory.model.xstream.security.AceImpl;
import org.artifactory.security.AceInfo;
import org.artifactory.security.ArtifactoryPermission;
import org.artifactory.security.MutableAceInfo;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * AceInfo unit tests.
 *
 * @author Yossi Shaul
 */
@Test
public class AceInfoTest {

    public void adminMaskOnly() {
        MutableAceInfo aceInfo = new AceImpl();
        aceInfo.setManage(true);
        assertOnlyManage(aceInfo);

        aceInfo = new AceImpl();
        aceInfo.setPermissionsFromString(Sets.<String>newHashSet("m"));
        assertOnlyManage(aceInfo);
    }

    public void readerMaskOnly() {
        MutableAceInfo aceInfo = new AceImpl();
        aceInfo.setRead(true);

        assertOnlyRead(aceInfo);

        aceInfo = new AceImpl();
        aceInfo.setPermissionsFromString(Sets.<String>newHashSet("r"));
        assertOnlyRead(aceInfo);
    }

    public void deployerMaskOnly() {
        MutableAceInfo aceInfo = new AceImpl();
        aceInfo.setDeploy(true);
        assertOnlyDeploy(aceInfo);

        aceInfo = new AceImpl();
        aceInfo.setPermissionsFromString(Sets.<String>newHashSet("w"));
        assertOnlyDeploy(aceInfo);
    }

    public void deleterMaskOnly() {
        MutableAceInfo aceInfo = new AceImpl();
        aceInfo.setDelete(true);
        assertOnlyDelete(aceInfo);

        aceInfo = new AceImpl();
        aceInfo.setPermissionsFromString(Sets.<String>newHashSet("d"));
        assertOnlyDelete(aceInfo);
    }

    public void annotatorMaskOnly() {
        MutableAceInfo aceInfo = new AceImpl();
        aceInfo.setAnnotate(true);
        assertOnlyAnnotate(aceInfo);

        aceInfo = new AceImpl();
        aceInfo.setPermissionsFromString(Sets.<String>newHashSet("n"));
        assertOnlyAnnotate(aceInfo);
    }

    public void allMasks() {
        MutableAceInfo aceInfo = new AceImpl();
        aceInfo.setManage(true);
        aceInfo.setDeploy(true);
        aceInfo.setRead(true);
        aceInfo.setDelete(true);
        aceInfo.setAnnotate(true);

        assertTrue(aceInfo.canManage(), "Should have all roles");
        assertTrue(aceInfo.canRead(), "Should have all roles");
        assertTrue(aceInfo.canDeploy(), "Should have all roles");
        assertTrue(aceInfo.canDelete(), "Should have all roles");
        assertTrue(aceInfo.canAnnotate(), "Should have all roles");
        assertEquals(aceInfo.getPermissionsAsString(), Sets.newHashSet("m", "n", "d", "w", "r"));
    }

    public void copyConstructor() {
        MutableAceInfo orig = new AceImpl("koko", true, ArtifactoryPermission.MANAGE.getMask());
        AceInfo copy = new AceImpl(orig);

        assertEquals(orig.getPrincipal(), copy.getPrincipal());
        assertEquals(orig.getMask(), copy.getMask());
        assertEquals(orig.isGroup(), copy.isGroup());
        assertEquals(orig.getPermissionsAsString(), copy.getPermissionsAsString());
    }

    public void copyConstructorReflectionEquality() {
        MutableAceInfo orig = new AceImpl("koko", true, ArtifactoryPermission.MANAGE.getMask());
        AceInfo copy = new AceImpl(orig);

        assertTrue(EqualsBuilder.reflectionEquals(orig, copy), "Orig and copy differ");
    }

    private void assertOnlyManage(MutableAceInfo aceInfo) {
        assertTrue(aceInfo.canManage(), "Should be a manager");
        assertFalse(aceInfo.canRead(), "Shouldn't be a reader");
        assertFalse(aceInfo.canDeploy(), "Shouldn't be a deployer");
        assertFalse(aceInfo.canDelete(), "Shouldn't be a deleter");
        assertFalse(aceInfo.canAnnotate(), "Shouldn't be an annotator");

        assertEquals(aceInfo.getPermissionsAsString(), Sets.newHashSet("m"), "Should be admin permission string");
    }

    private void assertOnlyRead(MutableAceInfo aceInfo) {
        assertFalse(aceInfo.canManage(), "Shouldn't be a manager");
        assertTrue(aceInfo.canRead(), "Should be a reader");
        assertFalse(aceInfo.canDeploy(), "Shouldn't be a deployer");
        assertFalse(aceInfo.canDelete(), "Shouldn't be a deleter");
        assertFalse(aceInfo.canAnnotate(), "Shouldn't be an annotator");

        assertEquals(aceInfo.getPermissionsAsString(), Sets.newHashSet("r"), "Should be a reader permission string");
    }

    private void assertOnlyDeploy(MutableAceInfo aceInfo) {
        assertFalse(aceInfo.canManage(), "Shouldn't be a manager");
        assertFalse(aceInfo.canRead(), "Shouldn't be a reader");
        assertTrue(aceInfo.canDeploy(), "Should be a deployer");
        assertFalse(aceInfo.canDelete(), "Shouldn't be a deleter");
        assertFalse(aceInfo.canAnnotate(), "Shouldn't be an annotator");

        assertEquals(aceInfo.getPermissionsAsString(), Sets.newHashSet("w"), "Should be a deploy permission string");
    }

    private void assertOnlyDelete(MutableAceInfo aceInfo) {
        assertFalse(aceInfo.canManage(), "Shouldn't be a manager");
        assertFalse(aceInfo.canRead(), "Shouldn't be a reader");
        assertFalse(aceInfo.canDeploy(), "Shouldn't be a deployer");
        assertTrue(aceInfo.canDelete(), "Should be a deleter");
        assertFalse(aceInfo.canAnnotate(), "Shouldn't be an annotator");

        assertEquals(aceInfo.getPermissionsAsString(), Sets.newHashSet("d"), "Should be a delete permission string");
    }

    private void assertOnlyAnnotate(MutableAceInfo aceInfo) {
        assertFalse(aceInfo.canManage(), "Shouldn't be a manager");
        assertFalse(aceInfo.canRead(), "Shouldn't be a reader");
        assertFalse(aceInfo.canDeploy(), "Shouldn't be a deployer");
        assertFalse(aceInfo.canDelete(), "Shouldn't be a deleter");
        assertTrue(aceInfo.canAnnotate(), "Should be an annotator");

        assertEquals(aceInfo.getPermissionsAsString(), Sets.newHashSet("n"), "Should be a annotate permission string");
    }
}
