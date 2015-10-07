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

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests the ChecksumPolicyPassThru class.
 *
 * @author Yossi Shaul
 */
@Test
public class ChecksumPolicyPassThruTest extends ChecksumPolicyBaseTest {
    private ChecksumPolicyPassThru policy;

    @BeforeClass
    public void createChecksumPolicy() {
        policy = new ChecksumPolicyPassThru();
    }

    @Override
    ChecksumPolicy getPolicy() {
        return policy;
    }

    @Override
    public void checksumsMatch() {
        boolean ok = policy.verifyChecksum(matchedChecksums);
        Assert.assertTrue(ok, "Policy should pass if checksums are same");
    }

    @Override
    public void checksumsDoesNotMatch() {
        boolean ok = policy.verifyChecksum(notMatchedChecksums);
        Assert.assertTrue(ok, "Policy should not fail even if checksums don't match");
    }

    @Override
    public void noOriginalChecksum() {
        boolean ok = policy.verifyChecksum(noOriginalChecksum);
        Assert.assertTrue(ok, "Policy should not fail if original checksum is missing");
    }

    @Override
    public void returnedChecksum() {
        String checksum = policy.getChecksum(matchedChecksums);
        Assert.assertEquals(checksum, matchedChecksums.getOriginal(), "Should always return the original value");
        checksum = policy.getChecksum(notMatchedChecksums);
        Assert.assertEquals(checksum, notMatchedChecksums.getOriginal(), "Should always return the original value");
        checksum = policy.getChecksum(noOriginalChecksum);
        Assert.assertNull(checksum, "Should always return the original value");
    }
}
