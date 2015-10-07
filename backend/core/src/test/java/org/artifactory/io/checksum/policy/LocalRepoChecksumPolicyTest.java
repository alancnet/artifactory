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

package org.artifactory.io.checksum.policy;

import com.google.common.collect.Sets;
import org.artifactory.checksum.ChecksumInfo;
import org.artifactory.descriptor.repo.LocalRepoChecksumPolicyType;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Set;

import static org.artifactory.checksum.ChecksumType.md5;
import static org.artifactory.checksum.ChecksumType.sha1;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * Tests the LocalRepoChecksumPolicyTest class.
 *
 * @author Yossi Shaul
 */
@Test
public class LocalRepoChecksumPolicyTest extends ArtifactoryHomeBoundTest {
    public static final String SERVER_SHA1 = "a234567890123456789012345678901234567890";
    public static final String CLIENT_SHA1 = "b234567890123456789012345678901234567890";
    public static final String SERVER_MD5 = "a2345678901234567890123456789012";
    public static final String CLIENT_MD5 = "b2345678901234567890123456789012";

    private Set<ChecksumInfo> checksums;

    @BeforeClass
    public void createChecksumPolicy() {
        checksums = Sets.newHashSet(
                new ChecksumInfo(sha1, CLIENT_SHA1, SERVER_SHA1),
                new ChecksumInfo(md5, CLIENT_MD5, SERVER_MD5));
    }

    public void clientPolicyType() {
        LocalRepoChecksumPolicy policy = new LocalRepoChecksumPolicy(); // client type is the default
        assertEquals(policy.getChecksum(sha1, checksums), CLIENT_SHA1);
        assertEquals(policy.getChecksum(md5, checksums), CLIENT_MD5);
    }

    public void serverPolicyType() {
        LocalRepoChecksumPolicy policy = new LocalRepoChecksumPolicy();
        policy.setPolicyType(LocalRepoChecksumPolicyType.SERVER);
        assertEquals(policy.getChecksum(sha1, checksums), SERVER_SHA1);
        assertEquals(policy.getChecksum(md5, checksums), SERVER_MD5);
    }

    public void checksumTypeNotFound() {
        LocalRepoChecksumPolicy policy = new LocalRepoChecksumPolicy();
        Set<ChecksumInfo> empty = Sets.newHashSet();
        assertNull(policy.getChecksum(sha1, empty));
        assertNull(policy.getChecksum(md5, empty));
    }

    public void checksumValueNotFound() {
        LocalRepoChecksumPolicy policy = new LocalRepoChecksumPolicy();
        Set<ChecksumInfo> noOriginal = Sets.newHashSet(new ChecksumInfo(sha1, null, SERVER_SHA1));
        assertNull(policy.getChecksum(sha1, noOriginal));

        policy.setPolicyType(LocalRepoChecksumPolicyType.SERVER);
        assertEquals(policy.getChecksum(sha1, noOriginal), SERVER_SHA1);
    }

    public void checksumOfMetadata() {
        LocalRepoChecksumPolicy policy = new LocalRepoChecksumPolicy();
        RepoPath metadataPath = InternalRepoPathFactory.create("repo", "test/test/1.0/maven-metadata.xml");
        assertEquals(policy.getChecksum(sha1, checksums, metadataPath), SERVER_SHA1);

        policy.setPolicyType(LocalRepoChecksumPolicyType.SERVER);
        assertEquals(policy.getChecksum(sha1, checksums, metadataPath), SERVER_SHA1);
    }

    public void checksumOfSnapshotMetadata() {
        LocalRepoChecksumPolicy policy = new LocalRepoChecksumPolicy();
        RepoPath metadataPath = InternalRepoPathFactory.create("repo", "test/test/1.0-SNAPSHOT/maven-metadata.xml");
        assertEquals(policy.getChecksum(sha1, checksums, metadataPath), SERVER_SHA1);

        policy.setPolicyType(LocalRepoChecksumPolicyType.SERVER);
        assertEquals(policy.getChecksum(sha1, checksums, metadataPath), SERVER_SHA1);
    }
}