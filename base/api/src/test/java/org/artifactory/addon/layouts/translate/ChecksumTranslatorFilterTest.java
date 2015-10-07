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

package org.artifactory.addon.layouts.translate;

import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Tests the path translators checksum filter
 *
 * @author Noam Y. Tenne
 */
@Test
public class ChecksumTranslatorFilterTest extends ArtifactoryHomeBoundTest {

    public void testInvalidPaths() {
        ChecksumTranslatorFilter filter = new ChecksumTranslatorFilter();
        assertFalse(filter.filterRequired(null));
        assertFalse(filter.filterRequired(""));
        assertFalse(filter.filterRequired(" "));
        assertFalse(filter.filterRequired("momo.jar"));
        assertFalse(filter.filterRequired("momo.jar:metadata.xml"));
        assertFalse(filter.filterRequired("momo.tar.gz"));
        assertFalse(filter.filterRequired("org.momo/popo/momo.jar"));
        assertFalse(filter.filterRequired("org.momo/popo/momo.jar:metadata.xml"));
        assertFalse(filter.filterRequired("org.momo/popo/momo.tar.gz"));
        assertFalse(filter.filterRequired("org/momo/popo/momo.jar"));
        assertFalse(filter.filterRequired("org/momo/popo/momo.jar:metadata.xml"));
        assertFalse(filter.filterRequired("org/momo/popo/momo.tar.gz"));
    }

    public void testValidMd5Test() {
        String path = "org/momo/popo/momo.jar.md5";
        ChecksumTranslatorFilter filter = new ChecksumTranslatorFilter();
        assertTrue(filter.filterRequired(path));
        assertEquals(filter.getFilteredContent(path), ".md5");
        assertEquals(filter.stripPath(path), "org/momo/popo/momo.jar");
        assertEquals(filter.applyFilteredContent("org/momo/popo/momo.jar", ".md5"), path);
    }

    public void testValidSha1Test() {
        String path = "org.momo/popo/momo.jar.sha1";
        ChecksumTranslatorFilter filter = new ChecksumTranslatorFilter();
        assertTrue(filter.filterRequired(path));
        assertEquals(filter.getFilteredContent(path), ".sha1");
        assertEquals(filter.stripPath(path), "org.momo/popo/momo.jar");
        assertEquals(filter.applyFilteredContent("org.momo/popo/momo.jar", ".sha1"), path);
    }
}
