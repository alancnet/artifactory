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

package org.artifactory.mime.version;

import org.artifactory.mime.MimeType;
import org.artifactory.mime.MimeTypes;
import org.artifactory.mime.MimeTypesReader;
import org.artifactory.util.ResourceUtils;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Tests {@link MimeTypesVersion}.
 *
 * @author Yossi Shaul
 */
@Test
public class MimeTypesVersionTest {

    public void findVersion1() {
        String version1 = ResourceUtils.getResourceAsString("/org/artifactory/mime/version/mimetypes-v1.xml");

        MimeTypesVersion version = MimeTypesVersion.findVersion(version1);
        assertNotNull(version);
        assertEquals(version, MimeTypesVersion.v1);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void findNonExistentVersion() {
        String fakeVersion = ResourceUtils.getResourceAsString("/org/artifactory/mime/version/mimetypes-v1.xml");
        fakeVersion = fakeVersion.replace("version=\"1\"", "version=\"9789456\"");
        MimeTypesVersion.findVersion(fakeVersion);
    }

    public void versionString() {
        assertEquals(MimeTypesVersion.v1.versionString(), "1");
        assertEquals(MimeTypesVersion.v2.versionString(), "2");
    }

    public void convertVersion1() {
        String version1 = ResourceUtils.getResourceAsString("/org/artifactory/mime/version/mimetypes-v1.xml");

        String latest = MimeTypesVersion.v1.convert(version1);

        assertEquals(MimeTypesVersion.findVersion(latest), MimeTypesVersion.getCurrent(), "Not current version");
        assertTrue(latest.contains("<mimetypes version=\"" + MimeTypesVersion.getCurrent().versionString()
                + "\">"), "Unexpected converted string: " + latest);

        //make sure the result is readable
        MimeTypes mimeTypes = new MimeTypesReader().read(latest);
        assertNotNull(mimeTypes);
    }

    public void convertVersion2() {
        String xml = ResourceUtils.getResourceAsString("/org/artifactory/mime/version/mimetypes-v2.xml");
        MimeTypesVersion version2 = MimeTypesVersion.findVersion(xml);
        String latest = version2.convert(xml);

        assertEquals(MimeTypesVersion.findVersion(latest), MimeTypesVersion.getCurrent(), "Not current version");
        assertTrue(latest.contains("<mimetypes version=\"" + MimeTypesVersion.getCurrent().versionString() + "\">"),
                "Unexpected converted string: " + latest);

        MimeTypes mimeTypes = new MimeTypesReader().read(latest);
        assertNotNull(mimeTypes);

        MimeType zip = mimeTypes.getByMime("application/zip");
        assertNotNull(zip, "Zip entry not found");
        assertTrue(zip.isIndex(), "Zip should have been converted to indexed");

        MimeType nupkg = mimeTypes.getByMime("application/x-nupkg");
        assertNotNull(nupkg, "NuPkg entry not found");
        assertTrue(nupkg.isArchive(), "NuPkg not created as archive");
        assertTrue(nupkg.isIndex(), "NuPkg not created as indexed");

        MimeType nuspec = mimeTypes.getByMime("application/x-nuspec+xml");
        assertNotNull(nuspec, "Nuspec entry not found");
        assertTrue(nuspec.isViewable(), "Nuspec not created as viewable");
    }
}
