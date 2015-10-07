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

package org.artifactory.mime;

import com.google.common.collect.ImmutableSet;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Tests {@link MimeTypes}.
 *
 * @author Yossi Shaul
 */
@Test
public class MimeTypesTest {
    private final MimeType TEXT_PLAIN = new MimeType("text/plain", ImmutableSet.of("txt"), true, false, false, "text",
            null);
    private MimeTypes holder;

    @BeforeMethod
    public void setup() {
        ImmutableSet<MimeType> types = ImmutableSet.of(
                TEXT_PLAIN,
                new MimeType("text/xml", ImmutableSet.of("xml"), true, false, false, "xml", "xml"),
                new MimeType("text/dup1", ImmutableSet.of("duplicate"), true, false, false, null, null),
                new MimeType("text/dup2", ImmutableSet.of("duplicate"), true, false, false, null, null)
        );

        holder = new MimeTypes(types);
        assertEquals(holder.getMimeTypes().size(), 4);
    }


    public void getByMimeEntryName() {
        assertNull(holder.getByMime("text/nothere"), "Unexpected mime entry was found");
        assertNotNull(holder.getByMime("text/plain"), "Expected mime entry text/plain not found");
    }

    public void getForUnknownExtension() {
        assertNull(holder.getByExtension("text"), "Unexpected mime entry was found");
    }

    public void getByExtension() {
        assertEquals(holder.getByExtension("txt"), TEXT_PLAIN, "Unexpected mime type was found");
        assertEquals(holder.getByExtension("duplicate").getType(), "text/dup2",
                "Should have taken the second duplicate");
    }

    public void getByExtensionUpperCase() {
        assertNotNull(holder.getByExtension("TXT"), "Couldn't find mime type when using different caps");
        assertEquals(holder.getByExtension("TXT"), TEXT_PLAIN, "Unexpected mime type was found");
    }
}
