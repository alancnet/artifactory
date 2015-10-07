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

package org.artifactory.update.md.v125rc0;

import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.checksum.ChecksumInfo;
import org.artifactory.fs.MetadataEntryInfo;
import org.artifactory.fs.MutableFileInfo;
import org.artifactory.fs.StatsInfo;
import org.artifactory.repo.RepoPath;
import org.artifactory.update.md.MetadataReaderBaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Set;

import static org.testng.Assert.*;

/**
 * Tests the matadata reader from versions 1.3.3-beta-3 to 1.3.3-beta-6 (or later).
 *
 * @author Yossi Shaul
 */
@Test
public class MetadataReader125Test extends MetadataReaderBaseTest {

    public void readFolderMetadata() {
        MetadataReader125 reader = new MetadataReader125();
        File folderMetadataDirectory = getMetadataDirectory("/metadata/v125rc0/commons-cli.artifactory-metadata");
        BasicStatusHolder status = new BasicStatusHolder();
        List<MetadataEntryInfo> entries = reader.getMetadataEntries(folderMetadataDirectory, status);
        assertFalse(status.isError());
        assertNotNull(entries);
        assertEquals(entries.size(), 1, "One matadata entry expected - folder");

        // the result should be compatible with the latest FolderInfo
        org.artifactory.fs.FolderInfo folderInfo =
                (org.artifactory.fs.FolderInfo) xstream.fromXML(entries.get(0).getXmlContent());
        assertEquals(folderInfo.getName(), "commons-cli", "Name mismatch");
        RepoPath repoPath = folderInfo.getRepoPath();
        assertEquals(repoPath.getRepoKey(), "repo1-cache", "Repository key mismatch");
        assertEquals(repoPath.getPath(), "commons-cli", "Path mismatch");
    }

    public void readFileMetadata() {
        MetadataReader125 reader = new MetadataReader125();
        File fileMetadataDirectory = getMetadataDirectory(
                "/metadata/v125rc0/commons-cli-1.0.pom.artifactory-metadata");
        BasicStatusHolder status = new BasicStatusHolder();
        List<MetadataEntryInfo> entries = reader.getMetadataEntries(fileMetadataDirectory, status);
        assertFalse(status.isError());
        assertNotNull(entries);
        assertEquals(entries.size(), 2, "Two matadata entries are expected - file and stats");

        MetadataEntryInfo fileInfoEntry = getMetadataByName(entries, org.artifactory.fs.FileInfo.ROOT);
        MutableFileInfo fileInfo = (MutableFileInfo) xstream.fromXML(fileInfoEntry.getXmlContent());
        Set<ChecksumInfo> checksums = fileInfo.getChecksums();
        Assert.assertNotNull(checksums);
        Assert.assertEquals(checksums.size(), 2);
        Assert.assertEquals(fileInfo.getSha1(), "");
        Assert.assertEquals(fileInfo.getMd5(), "");

        // just make sure the stats is readable
        MetadataEntryInfo statsInfoEntry = getMetadataByName(entries, StatsInfo.ROOT);
        StatsInfo statsInfo = (StatsInfo) xstream.fromXML(statsInfoEntry.getXmlContent());
        assertEquals(statsInfo.getDownloadCount(), 99);
    }

    private File getMetadataDirectory(String path) {
        URL resource = getClass().getResource(path);
        if (resource == null) {
            throw new IllegalArgumentException("Resource not found: " + path);
        }
        return new File(resource.getFile());
    }
}