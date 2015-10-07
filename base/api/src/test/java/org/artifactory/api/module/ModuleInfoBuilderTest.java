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

package org.artifactory.api.module;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * @author Noam Y. Tenne
 */
@Test
public class ModuleInfoBuilderTest {

    public void testNoParams() {
        ModuleInfo build = new ModuleInfoBuilder().build();
        assertNull(build.getOrganization(), "Default 'organization' value of module info builder should be null.");
        assertNull(build.getModule(), "Default 'module' value of module info builder should be null.");
        assertNull(build.getBaseRevision(), "Default 'base revision' value of module info builder should be null.");
        assertNull(build.getFolderIntegrationRevision(),
                "Default 'folder integration revision' value of module info builder should be null.");
        assertNull(build.getFileIntegrationRevision(),
                "Default 'file integration revision' value of module info builder should be null.");
        assertNull(build.getClassifier(), "Default 'classifier' value of module info builder should be null.");
        assertNull(build.getExt(), "Default 'extension' value of module info builder should be null.");
        assertNull(build.getType(), "Default 'type' value of module info builder should be null.");
        assertNull(build.getCustomFields(), "Default 'custom fields' value of module info builder should be null.");
    }

    public void testNullParams() {
        ModuleInfo build = new ModuleInfoBuilder().organization(null).module(null).baseRevision(null).
                folderIntegrationRevision(null).fileIntegrationRevision(null).classifier(null).ext(null).type(null).
                build();
        assertNull(build.getOrganization(), "Expected 'organization' value of module info builder to be null.");
        assertNull(build.getModule(), "Expected 'module' value of module info builder to be null.");
        assertNull(build.getBaseRevision(), "Expected 'base revision' value of module info builder to be null.");
        assertNull(build.getFolderIntegrationRevision(),
                "Expected 'folder integration revision' value of module info builder to be null.");
        assertNull(build.getFileIntegrationRevision(),
                "Expected 'file integration revision' value of module info builder to be null.");
        assertNull(build.getClassifier(), "Expected 'classifier' value of module info builder to be null.");
        assertNull(build.getExt(), "Expected 'ext' value of module info builder to be null.");
        assertNull(build.getType(), "Expected 'type' value of module info builder to be null.");
    }

    public void testValidParams() {
        ModuleInfo build = new ModuleInfoBuilder().organization("organization").module("module").
                baseRevision("revisionBase").folderIntegrationRevision("pathRevisionIntegration").
                fileIntegrationRevision("artifactRevisionIntegration").classifier("classifier").ext("ext").
                type("type").customField("tokenName", "tokenValue").build();
        assertEquals(build.getOrganization(), "organization",
                "Unexpected 'organization' value of module info builder.");
        assertEquals(build.getModule(), "module", "Unexpected 'module' value of module info builder.");
        assertEquals(build.getBaseRevision(), "revisionBase",
                "Unexpected 'base revision' value of module info builder.");
        assertEquals(build.getFolderIntegrationRevision(), "pathRevisionIntegration",
                "Unexpected 'folder integration revision' value of module info builder.");
        assertEquals(build.getFileIntegrationRevision(), "artifactRevisionIntegration",
                "Unexpected 'file integration revision' value of module info builder.");
        assertEquals(build.getClassifier(), "classifier", "Unexpected 'classifier' value of module info builder.");
        assertEquals(build.getExt(), "ext", "Unexpected 'ext' value of module info builder.");
        assertEquals(build.getType(), "type", "Unexpected 'type' value of module info builder.");
        assertEquals(build.getCustomField("tokenName"), "tokenValue",
                "Unexpected 'custom field' value of module info builder.");
    }

    public void testCopyConstructor() throws Exception {
        ModuleInfo toCopy = new ModuleInfoBuilder().organization("organization").module("module").
                baseRevision("revisionBase").folderIntegrationRevision("pathRevisionIntegration").
                fileIntegrationRevision("artifactRevisionIntegration").classifier("classifier").ext("ext").
                type("type").customField("tokenName", "tokenValue").customField("tokenName2", "tokenValue2").build();
        ModuleInfo copy = new ModuleInfoBuilder(toCopy).build();
        assertEquals(copy.getOrganization(), "organization",
                "Unexpected 'organization' value of module info builder.");
        assertEquals(copy.getModule(), "module", "Unexpected 'module' value of module info builder.");
        assertEquals(copy.getBaseRevision(), "revisionBase",
                "Unexpected 'base revision' value of module info builder.");
        assertEquals(copy.getFolderIntegrationRevision(), "pathRevisionIntegration",
                "Unexpected 'folder integration revision' value of module info builder.");
        assertEquals(copy.getFileIntegrationRevision(), "artifactRevisionIntegration",
                "Unexpected 'file integration revision' value of module info builder.");
        assertEquals(copy.getClassifier(), "classifier", "Unexpected 'classifier' value of module info builder.");
        assertEquals(copy.getExt(), "ext", "Unexpected 'ext' value of module info builder.");
        assertEquals(copy.getType(), "type", "Unexpected 'type' value of module info builder.");
        assertEquals(copy.getCustomField("tokenName"), "tokenValue",
                "Unexpected 'custom field' value of module info builder.");
        assertEquals(copy.getCustomField("tokenName2"), "tokenValue2",
                "Unexpected 'custom field' value of module info builder.");
    }

    @Test
    public void testCopyDefaultConstruct() throws Exception {
        ModuleInfo copy = new ModuleInfoBuilder(new ModuleInfo()).build();
        assertNull(copy.getOrganization(), "Default 'organization' value of module info copyer should be null.");
        assertNull(copy.getModule(), "Default 'module' value of module info copyer should be null.");
        assertNull(copy.getBaseRevision(), "Default 'base revision' value of module info copyer should be null.");
        assertNull(copy.getFolderIntegrationRevision(),
                "Default 'folder integration revision' value of module info copyer should be null.");
        assertNull(copy.getFileIntegrationRevision(),
                "Default 'file integration revision' value of module info copyer should be null.");
        assertNull(copy.getClassifier(), "Default 'classifier' value of module info copyer should be null.");
        assertNull(copy.getExt(), "Default 'extension' value of module info copyer should be null.");
        assertNull(copy.getType(), "Default 'type' value of module info copyer should be null.");
        assertNull(copy.getCustomFields(), "Default 'custom fields' value of module info copyer should be null.");
    }
}