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

import com.google.common.collect.Maps;
import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.*;

/**
 * @author Noam Y. Tenne
 */
@Test
public class ModuleInfoTest {

    public void testDefaultConstructor() {
        ModuleInfo moduleInfo = new ModuleInfo();
        assertNull(moduleInfo.getOrganization(), "Default 'organization' value of module info should be null.");
        assertNull(moduleInfo.getModule(), "Default 'module' value of module info should be null.");
        assertNull(moduleInfo.getBaseRevision(),
                "Default 'base revision' value of module info should be null.");
        assertNull(moduleInfo.getFolderIntegrationRevision(),
                "Default 'folder integration revision' value of module info should be null.");
        assertNull(moduleInfo.getFileIntegrationRevision(),
                "Default 'file integration revision' value of module info should be null.");
        assertNull(moduleInfo.getClassifier(), "Default 'classifier' value of module info should be null.");
        assertNull(moduleInfo.getExt(), "Default 'extension' value of module info should be null.");
        assertNull(moduleInfo.getType(), "Default 'type' value of module info should be null.");
        assertFalse(moduleInfo.isValid(), "Module should should not be valid.");
        assertFalse(moduleInfo.isIntegration(), "Module info should should be considered as an 'integration' module.");
        assertEquals(moduleInfo.getPrettyModuleId(), "null:null:null",
                "Unexpected module info 'pretty ID' representation.");
        assertEquals(moduleInfo.toString(), "organization = null, module = null, baseRevision = null, " +
                "folderIntegrationRevision = null, fileIntegrationRevision = null, classifier = null, ext = null, " +
                "type = null", "Unexpected module info string representation.");
    }

    public void testPartialInfo() {
        ModuleInfo moduleInfo = new ModuleInfo("organization", "module", null, null, null, null, null, null, null);
        assertEquals(moduleInfo.getOrganization(), "organization", "Unexpected 'organization' value of module info.");
        assertEquals(moduleInfo.getModule(), "module", "Unexpected 'module' value of module info.");
        assertNull(moduleInfo.getBaseRevision(), "Expected 'base revision' value of module info to be null.");
        assertNull(moduleInfo.getFolderIntegrationRevision(),
                "Expected 'folder integration revision' value of module info to be null.");
        assertNull(moduleInfo.getFileIntegrationRevision(),
                "Expected 'file integration revision' value of module info to be null.");
        assertNull(moduleInfo.getClassifier(), "Expected 'classifier' value of module info to be null.");
        assertNull(moduleInfo.getExt(), "Expected 'extension' value of module info to be null.");
        assertNull(moduleInfo.getType(), "Expected 'type' value of module info to be null.");
        assertNull(moduleInfo.getCustomFields(), "Expected 'custom fields' value of module info to be null.");
        assertFalse(moduleInfo.isValid(), "Module info should not be valid.");
        assertFalse(moduleInfo.isIntegration(), "Module info should not be considered as an 'integration' module.");
        assertEquals(moduleInfo.getPrettyModuleId(), "null:null:null",
                "Unexpected module info 'pretty ID' representation.");
        assertEquals(moduleInfo.toString(), "organization = organization, module = module, baseRevision = null, " +
                "folderIntegrationRevision = null, fileIntegrationRevision = null, classifier = null, ext = null, " +
                "type = null", "Unexpected module info string representation.");
    }

    public void testMinimalInfo() {
        ModuleInfo moduleInfo = new ModuleInfo("organization", "module", "revisionBase", null, null, null, null, null,
                null);
        assertEquals(moduleInfo.getOrganization(), "organization", "Unexpected 'organization' value of module info.");
        assertEquals(moduleInfo.getModule(), "module", "Unexpected 'module' value of module info.");
        assertEquals(moduleInfo.getBaseRevision(), "revisionBase", "Unexpected 'base revision' value of module info.");
        assertNull(moduleInfo.getFolderIntegrationRevision(),
                "Expected 'folder integration revision' value of module info to be null.");
        assertNull(moduleInfo.getFileIntegrationRevision(),
                "Expected 'file integration revision' value of module info to be null.");
        assertNull(moduleInfo.getClassifier(), "Expected 'classifier' value of module info to be null.");
        assertNull(moduleInfo.getExt(), "Expected 'extension' value of module info to be null.");
        assertNull(moduleInfo.getType(), "Expected 'type' value of module info to be null.");
        assertNull(moduleInfo.getCustomFields(), "Expected 'custom fields' value of module info to be null.");
        assertTrue(moduleInfo.isValid(), "Module info should be valid.");
        assertFalse(moduleInfo.isIntegration(), "Module info should not be considered as an 'integration' module.");
        assertEquals(moduleInfo.getPrettyModuleId(), "organization:module:revisionBase",
                "Unexpected module info 'pretty ID' representation.");
        assertEquals(moduleInfo.toString(), "organization = organization, module = module, " +
                "baseRevision = revisionBase, folderIntegrationRevision = null, fileIntegrationRevision = null, " +
                "classifier = null, ext = null, type = null", "Unexpected module info string representation.");
    }

    public void testFullInfo() {
        Map<String, String> customFields = Maps.newHashMap();
        customFields.put("customKey", "customValue");
        ModuleInfo moduleInfo = new ModuleInfo("organization", "module", "revisionBase", "pathRevisionIntegration",
                "artifactRevisionIntegration", "classifier", "ext", "type", customFields);
        assertEquals(moduleInfo.getOrganization(), "organization", "Unexpected 'organization' value of module info.");
        assertEquals(moduleInfo.getModule(), "module", "Unexpected 'module' value of module info.");
        assertEquals(moduleInfo.getBaseRevision(), "revisionBase", "Unexpected 'base revision' value of module info.");
        assertEquals(moduleInfo.getFolderIntegrationRevision(), "pathRevisionIntegration",
                "Unexpected 'folder integration revision' value of module info.");
        assertEquals(moduleInfo.getFileIntegrationRevision(), "artifactRevisionIntegration",
                "Unexpected 'file integration revision' value of module info.");
        assertEquals(moduleInfo.getClassifier(), "classifier", "Unexpected 'classifier' value of module info.");
        assertEquals(moduleInfo.getExt(), "ext", "Unexpected 'extension' value of module info.");
        assertEquals(moduleInfo.getType(), "type", "Unexpected 'type' value of module info.");
        assertEquals(moduleInfo.getCustomFields(), customFields, "Unexpected 'custom fields' value of module info.");
        assertTrue(moduleInfo.isValid(), "Module info should be valid.");
        assertTrue(moduleInfo.isIntegration(), "Module info should be considered as an 'integration' module.");
        assertEquals(moduleInfo.getPrettyModuleId(),
                "organization:module:revisionBase-artifactRevisionIntegration:classifier:type",
                "Unexpected module info 'pretty ID' representation.");
        assertEquals(moduleInfo.toString(), "organization = organization, module = module, " +
                "baseRevision = revisionBase, folderIntegrationRevision = pathRevisionIntegration, " +
                "fileIntegrationRevision = artifactRevisionIntegration, classifier = classifier, ext = ext," +
                " type = type", "Unexpected module info string representation.");
    }
}
