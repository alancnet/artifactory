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

package org.artifactory.api.fs;

import com.google.common.collect.Sets;
import org.artifactory.checksum.ChecksumInfo;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.model.xstream.fs.FileInfoImpl;
import org.artifactory.repo.RepoPath;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;

/**
 * Unit tests for the FileInfoImpl.
 *
 * @author Yossi Shaul
 */
@Test
public class FileInfoImplTest extends ArtifactoryHomeBoundTest {
    public static final String DUMMY_SHA1 = "1234567890123456789012345678901234567890";
    public static final String DUMMY2_SHA1 = "3234567890123456789012345678901234567890";

    public void differentChecksumNotIdentical() {
        RepoPath path = new RepoPathImpl("repo", "test.jar");

        FileInfoImpl fileInfo1 = new FileInfoImpl(path);
        fileInfo1.setChecksums(
                Sets.newHashSet(new org.artifactory.checksum.ChecksumInfo(ChecksumType.sha1, null, DUMMY_SHA1)));

        FileInfoImpl fileInfo2 = new FileInfoImpl(path);
        fileInfo2.setChecksums(Sets.newHashSet(new ChecksumInfo(ChecksumType.sha1, "originalchecksum", DUMMY2_SHA1)));

        assertFalse(fileInfo1.isIdentical(fileInfo2), "Should not be identical - checksum info is not");
    }
}
