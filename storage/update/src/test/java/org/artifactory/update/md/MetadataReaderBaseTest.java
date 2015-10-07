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

package org.artifactory.update.md;

import com.thoughtworks.xstream.XStream;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.fs.MetadataEntryInfo;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.testng.annotations.BeforeClass;

import java.util.List;

/**
 * Base class for the metadata reader tests.
 *
 * @author Yossi Shaul
 */
public abstract class MetadataReaderBaseTest extends ArtifactoryHomeBoundTest {

    protected XStream xstream;

    protected MetadataEntryInfo getMetadataByName(List<MetadataEntryInfo> entries, String metadataName) {
        for (MetadataEntryInfo entry : entries) {
            if (entry.getMetadataName().equals(metadataName)) {
                return entry;
            }
        }
        // fail if not found
        org.testng.Assert.fail(String.format("Metadata %s not found in %s", metadataName, entries));
        return null;
    }

    @BeforeClass
    public void setup() {
        xstream = InfoFactoryHolder.get().getFileSystemXStream();
    }
}
