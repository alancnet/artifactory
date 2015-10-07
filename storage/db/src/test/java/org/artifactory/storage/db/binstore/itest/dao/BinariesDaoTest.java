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

package org.artifactory.storage.db.binstore.itest.dao;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.artifactory.api.storage.BinariesInfo;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.storage.db.binstore.dao.BinariesDao;
import org.artifactory.storage.db.binstore.entity.BinaryData;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.testng.Assert.*;

/**
 * Low level tests of the {@link org.artifactory.storage.db.binstore.dao.BinariesDao}.
 *
 * @author Yossi Shaul
 */
@Test
public class BinariesDaoTest extends DbBaseTest {

    @Autowired
    private BinariesDao binariesDao;

    @BeforeClass
    public void setup() {
        importSql("/sql/binaries.sql");
    }

    public void binaryExists() throws SQLException {
        assertTrue(binariesDao.exists("f0d381ab0e057d4f835d639f6330a7c3e81eb6af"));
    }

    public void binaryNotExists() throws SQLException {
        assertFalse(binariesDao.exists("699a458d52a1ca32cecb4d7c1ea50b89ea9ecaf9"));
    }

    public void loadExistingNode() throws SQLException {
        BinaryData binaryData = binariesDao.load("f0d381ab0e057d4f835d639f6330a7c3e81eb6af");
        assertNotNull(binaryData);
        assertEquals(binaryData.getSha1(), "f0d381ab0e057d4f835d639f6330a7c3e81eb6af");
        assertEquals(binaryData.getMd5(), "902a360ecad98a34b59863c1e65bcf71");
        assertEquals(binaryData.getLength(), 2725);
    }

    public void loadNonExistingNode() throws SQLException {
        BinaryData binaryData = binariesDao.load("62540a41c0b21fd3739565f7d961db07b760bfb8");
        assertNull(binaryData);
    }

    @Test(dependsOnMethods = "findPotentialDeletion")
    public void createBinary() throws SQLException {
        BinaryData binaryData = new BinaryData("1bae873f4a13f2919a4205aff0722b44ead4b190",
                "666a360ecad98a34b59863c1e65bcf71", 20);
        assertFalse(binariesDao.exists(binaryData.getSha1()));
        boolean created = binariesDao.create(binaryData);
        assertTrue(created);
        assertTrue(binariesDao.exists(binaryData.getSha1()));
        BinaryData loadedData = binariesDao.load("1bae873f4a13f2919a4205aff0722b44ead4b190");
        assertNotNull(loadedData);
        assertEquals(loadedData.getSha1(), binaryData.getSha1());
        assertEquals(loadedData.getMd5(), binaryData.getMd5());
        assertEquals(loadedData.getLength(), binaryData.getLength());
    }

    @Test(expectedExceptions = SQLException.class)
    public void createExistingBinary() throws SQLException {
        BinaryData binaryData = new BinaryData("f0d381ab0e057d4f835d639f6330a7c3e81eb6af",
                "902a360ecad98a34b59863c1e65bcf71", 20);
        binariesDao.create(binaryData);
    }

    public void findPotentialDeletion() throws SQLException {
        Collection<BinaryData> potentialDeletion = binariesDao.findPotentialDeletion();
        assertEquals(potentialDeletion.size(), 3);
        Set<String> nodes = Sets.newHashSet(Iterables.transform(potentialDeletion, new Function<BinaryData, String>() {
            @Override
            public String apply(@Nullable BinaryData input) {
                return input == null ? "" : input.getSha1();
            }
        }));
        assertTrue(nodes.contains("356a192b7913b04c54574d18c28d46e6395428ab"));
        assertTrue(nodes.contains("74239116da1def240fe1d366eb535513efc1c40b"));
        assertTrue(nodes.contains("da39a3ee5e6b4b0d3255bfef95601890afd80709"));
    }

    public void AssertPotentialDeletionOrder() throws SQLException {
        Collection<BinaryData> potentialDeletion = binariesDao.findPotentialDeletion();
        assertEquals(potentialDeletion.size(), 3);
        List<String> nodes = Lists.newArrayList(
                Iterables.transform(potentialDeletion, new Function<BinaryData, String>() {
                    @Override
                    public String apply(@Nullable BinaryData input) {
                        return input == null ? "" : input.getSha1();
                    }
                }));
        // verify candidates are ordered by size
        assertEquals(nodes.get(0), "74239116da1def240fe1d366eb535513efc1c40b");
        assertEquals(nodes.get(1), "356a192b7913b04c54574d18c28d46e6395428ab");
        assertEquals(nodes.get(2), "da39a3ee5e6b4b0d3255bfef95601890afd80709");
    }

    @Test(dependsOnMethods = "createBinary")
    public void testGetCountAndTotalSize() throws SQLException {
        BinariesInfo binariesInfo = binariesDao.getCountAndTotalSize();
        assertEquals(binariesInfo.getBinariesCount(), 6L);
        assertEquals(binariesInfo.getBinariesSize(), 3 + 2725 + 33670080 + 1 + 20);
    }

    @Test(dependsOnMethods = {
            "testGetCountAndTotalSize",
            "findChecksumsByMd5",
            "findChecksumsBySha1"
    })
    public void testDeleteEntries() throws SQLException {
        ImmutableSet<String> deleteTest = ImmutableSet.of(
                "74239116da1def240fe1d366eb535513efc1c40b",
                "f0d381ab0e057d4f835d639f6330a7c3e81eb6af",
                "da39a3ee5e6b4b0d3255bfef95601890afd80709"
        );
        int nbDeleted = 0;
        for (String sha1ToDelete : deleteTest) {
            nbDeleted += binariesDao.deleteEntry(sha1ToDelete);
        }
        assertEquals(nbDeleted, 2);
        BinariesInfo countAndTotalSize = binariesDao.getCountAndTotalSize();
        assertEquals(countAndTotalSize.getBinariesCount(), 4L);
        assertEquals(countAndTotalSize.getBinariesSize(), 3 + 2725 + 1 + 20);
    }

    public void findChecksumsBySha1() throws SQLException {
        Collection<BinaryData> nodes = binariesDao.search(ChecksumType.sha1, ImmutableList.of(
                "f0d381ab0e057d4f835d639f6330a7c3e81eb6af",
                "deaddeaddeaddeaddeaddeaddeaddeaddeaddead",
                "da39a3ee5e6b4b0d3255bfef95601890afd80709"
        ));
        assertFoundNodes(nodes);
    }

    // RTFACT-6364 - Oracle limits the number of elements in the IN clause to 1000
    public void findChecksumsBySha1ThousandLimit() throws SQLException {
        List<String> sha1s = Lists.newArrayListWithCapacity(2000);
        for (int i = 0; i < 999; i++) {
            sha1s.add(randomSha1());
        }
        binariesDao.search(ChecksumType.sha1, sha1s);

        // 1000
        sha1s.add(randomSha1());
        binariesDao.search(ChecksumType.sha1, sha1s);

        // 1001 (fails if not chunked in Oracle)
        sha1s.add(randomSha1());
        binariesDao.search(ChecksumType.sha1, sha1s);
    }

    private void assertFoundNodes(Collection<BinaryData> nodes) {
        assertNotNull(nodes);
        assertEquals(nodes.size(), 2);
        for (BinaryData node : nodes) {
            BinaryData expected = null;
            switch ((int) node.getLength()) {
                case 2725:
                    expected = new BinaryData("f0d381ab0e057d4f835d639f6330a7c3e81eb6af",
                            "902a360ecad98a34b59863c1e65bcf71", 2725);
                    break;
                case 0:
                    expected = new BinaryData("da39a3ee5e6b4b0d3255bfef95601890afd80709",
                            "602a360ecad98a34b59863c1e65bcf71", 0);
                    break;
                default:
                    fail("Binary data " + node + " unexpected!");
            }
            assertEquals(node.getSha1(), expected.getSha1());
            assertEquals(node.getMd5(), expected.getMd5());
            assertEquals(node.getLength(), expected.getLength());
        }
    }

    public void testFindEmptyChecksumsBySha1() throws SQLException {
        Collection<BinaryData> nodes = binariesDao.search(ChecksumType.sha1, new ArrayList<String>(1));
        assertNotNull(nodes);
        assertTrue(nodes.isEmpty());
    }

    public void findChecksumsByMd5() throws SQLException {
        Collection<BinaryData> nodes = binariesDao.search(ChecksumType.md5, ImmutableList.of(
                "902a360ecad98a34b59863c1e65bcf71",
                "602a360ecad98a34b59863c1e65bcf71",
                "402a360ecad98a34b59863c1e65bc222"
        ));
        assertFoundNodes(nodes);
    }

    public void testFindEmptyChecksumsByMd5() throws SQLException {
        Collection<BinaryData> nodes = binariesDao.search(ChecksumType.md5, new ArrayList<String>(1));
        assertNotNull(nodes);
        assertTrue(nodes.isEmpty());
    }
}
