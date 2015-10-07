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

package org.artifactory.api.config;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.artifactory.sapi.common.ExportSettings;
import org.testng.annotations.Test;

import java.io.File;

import static org.testng.Assert.*;

/**
 * Tests the ExportSettinds.
 *
 * @author Yossi Shaul
 */
@Test
public class ExportSettingsTest {

    public void fileConstructor() {
        File dir = new File("base");
        ExportSettings settings = new ExportSettingsImpl(dir);
        assertEquals(settings.getBaseDir(), dir);
        assertNotNull(settings.getTime());
        assertFalse(settings.isM2Compatible());
        assertFalse(settings.isCreateArchive());
        assertFalse(settings.isIgnoreRepositoryFilteringRulesOn());
        assertTrue(settings.isIncludeMetadata());
        assertFalse(settings.isIncremental());
    }

    public void copyConstructor() {
        File base = new File("bases");
        ExportSettingsImpl orig = new ExportSettingsImpl(base);
        orig.setIgnoreRepositoryFilteringRulesOn(true);
        orig.setIncremental(true);

        ExportSettings copy = new ExportSettingsImpl(base, orig);
        assertTrue(EqualsBuilder.reflectionEquals(orig, copy), "Reflection comparison after " +
                "copy constructor failed");

        copy = new ExportSettingsImpl(new File("base2"), orig);
        assertFalse(copy.getBaseDir().equals(base), "The copy constructor shouldn't copy the base dir");
    }

}
