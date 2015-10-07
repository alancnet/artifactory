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

package org.artifactory.storage.db.build.itest.dao;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.UnmodifiableIterator;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.storage.db.build.entity.BuildEntity;
import org.artifactory.storage.db.build.entity.BuildPromotionStatus;
import org.artifactory.storage.db.build.entity.BuildProperty;
import org.fest.assertions.Assertions;
import org.jfrog.build.api.release.PromotionStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static org.testng.Assert.*;

/**
 * Date: 10/31/12
 * Time: 11:03 AM
 *
 * @author freds
 */
@Test
public class BuildsDaoTest extends BuildsDaoBaseTest {

    @BeforeClass
    public void setup() {
        importSql("/sql/builds.sql");
    }

    public void testReadBuildsById() throws SQLException {
        for (long i = 1L; i <= 5L; i++) {
            assertAnyBuild(buildsDao.getBuild(i));
        }
    }

    @Test(dependsOnMethods = "testDeleteBuilds")
    public void testReadBuildsByName() throws SQLException {
        List<Long> buildIds = buildsDao.findBuildIds("ba");
        assertEquals(buildIds.size(), 3);
        for (Long buildId : buildIds) {
            assertTrue(buildId == 1L || buildId == 3L || buildId == 5L, "Build id " + buildId + " should not be here!");
        }
        buildIds = buildsDao.findBuildIds("bb");
        assertEquals(buildIds.size(), 2);
        for (Long buildId : buildIds) {
            assertTrue(buildId == 2L || buildId == 4L, "Build id " + buildId + " should not be here!");
        }
    }

    public void testReadBuildsByFullPath() throws SQLException {
        assertBuild1(buildsDao.findBuild("ba", "1", 1349000000000L));
        assertBuild2(buildsDao.findBuild("bb", "1", 1349001000000L));
        assertBuild3(buildsDao.findBuild("ba", "2", 1349002000000L));
        assertBuild4(buildsDao.findBuild("bb", "2", 1349003000000L));
        assertBuild5(buildsDao.findBuild("ba", "3", 1349004000000L));
    }

    public void testSimpleCreateBuild() throws SQLException, UnsupportedEncodingException {
        BuildEntity build11 = createBuild11();
        assertEquals(createBuild(build11, "1"), 2);
        assertTrue(build11.isIdentical(buildsDao.getBuild(11L)));
        assertEquals(buildsDao.getJsonBuild(11L, DummyBuild.class).name, "1");
    }

    public void testCreateBuildWithProps() throws SQLException, UnsupportedEncodingException {
        BuildEntity c2 = createBuild12();
        assertEquals(createBuild(c2, "2"), 4);
        assertTrue(c2.isIdentical(buildsDao.getBuild(12L)));
        assertEquals(buildsDao.getJsonBuild(12L, DummyBuild.class).name, "2");
    }

    public void testCreateBuildWithPromotions() throws SQLException, UnsupportedEncodingException {
        BuildEntity c2 = createBuild13();
        assertEquals(createBuild(c2, "3"), 4);
        assertTrue(c2.isIdentical(buildsDao.getBuild(13L)));
        assertEquals(buildsDao.getJsonBuild(13L, DummyBuild.class).name, "3");
    }

    public void testCreateBuildWithPropsAndPromotions() throws SQLException, UnsupportedEncodingException {
        BuildEntity c2 = createBuild14();
        assertEquals(createBuild(c2, "4"), 6);
        assertTrue(c2.isIdentical(buildsDao.getBuild(14L)));
        assertEquals(buildsDao.getJsonBuild(14L, DummyBuild.class).name, "4");
    }

    public void testCreateBuildSameNameAndNumber() throws SQLException, UnsupportedEncodingException {
        long now = System.currentTimeMillis();
        BuildEntity otherBa2 = new BuildEntity(103L, "ba", "2", 1349111111111L, null, now, "me", 0L, null);
        otherBa2.setProperties(new HashSet<BuildProperty>());
        otherBa2.setPromotions(new HashSet<BuildPromotionStatus>());
        assertEquals(createBuild(otherBa2, "otherBa2"), 2);
        assertTrue(otherBa2.isIdentical(buildsDao.getBuild(103L)));
        DummyBuild dummyBuild = buildsDao.getJsonBuild(103L, DummyBuild.class);
        assertNotNull(dummyBuild);
        assertEquals(dummyBuild.name, "otherBa2");
    }

    @Test(dependsOnMethods = "testCreateBuildSameNameAndNumber")
    public void testFindLatestBuildDate() throws SQLException {
        assertEquals(buildsDao.findLatestBuildDate("ba", "1"), 1349000000000L);
        assertEquals(buildsDao.findLatestBuildDate("bb", "1"), 1349001000000L);
        assertEquals(buildsDao.findLatestBuildDate("ba", "2"), 1349111111111L);
        assertEquals(buildsDao.findLatestBuildDate("bb", "2"), 1349003000000L);
        assertEquals(buildsDao.findLatestBuildDate("ba", "3"), 1349004000000L);
    }

    @Test(dependsOnMethods = {"testSimpleCreateBuild",
            "testCreateBuildWithProps",
            "testCreateBuildWithPromotions",
            "testCreateBuildWithPropsAndPromotions",
            "testFindLatestBuildDate"})
    public void testDeleteBuilds() throws SQLException, UnsupportedEncodingException {
        doDelete(11L, 2);
        doDelete(12L, 4);
        doDelete(13L, 4);
        doDelete(14L, 6);
        doDelete(103L, 2);
    }

    // Disable since index too big => Need to find another way for this
    @Test(enabled = false, expectedExceptions = SQLException.class)
    public void testCreateBuildSameNameNumberDate() throws SQLException, UnsupportedEncodingException {
        // TORE: [by fsi] use the error handling to verify we broke the correct constraint
        BuildEntity badBuild = new BuildEntity(1003L, "ba", "2", 1349002000000L, null, 1234L, "me", 0L, null);
        badBuild.setProperties(new HashSet<BuildProperty>());
        badBuild.setPromotions(new HashSet<BuildPromotionStatus>());
        createBuild(badBuild, "otherBadBa2");
    }

    @Test(expectedExceptions = SQLException.class)
    public void testCreateBuildSameId() throws SQLException, UnsupportedEncodingException {
        // TORE: [by fsi] use the error handling to verify we broke the correct constraint
        BuildEntity badBuild = new BuildEntity(1L, "bad", "1", 2L, null, 1234L, "me", 0L, null);
        badBuild.setProperties(new HashSet<BuildProperty>());
        badBuild.setPromotions(new HashSet<BuildPromotionStatus>());
        createBuild(badBuild, "1");
    }

    public void testFindBuildsForArtifactSHA1Checksum() throws SQLException {
        Collection<BuildEntity> builds = buildsDao.findBuildsForArtifactChecksum(ChecksumType.sha1,
                "acab88fc2a043c2479a6de676a2f8179e9ea2167");

        assertEquals(builds.size(), 3);
        Assertions.assertThat(builds).doesNotHaveDuplicates();
        for (BuildEntity build : builds) {
            assertTrue(build.getBuildId() == 1 || build.getBuildId() == 2 || build.getBuildId() == 3,
                    "Build id should be 1 or 2 or 3");
        }
    }

    public void testFindBuildsForArtifactMD5Checksum() throws SQLException {
        Collection<BuildEntity> builds = buildsDao.findBuildsForArtifactChecksum(ChecksumType.md5,
                "b02a360ecad98a34b59863c1e65bcf71");

        assertEquals(builds.size(), 2);
        Assertions.assertThat(builds).doesNotHaveDuplicates();
        for (BuildEntity build : builds) {
            assertTrue(build.getBuildId() == 1 || build.getBuildId() == 3, "Build id should be 1 or 3");
        }
    }

    public void testFindBuildsForDependencySHA1Checksum() throws SQLException {
        Collection<BuildEntity> builds = buildsDao.findBuildsForDependencyChecksum(ChecksumType.sha1,
                "ccab88fc2a043c2479a6de676a2f8179e9ea2167");

        assertEquals(builds.size(), 2);
        Assertions.assertThat(builds).doesNotHaveDuplicates();
        for (BuildEntity build : builds) {
            assertTrue(build.getBuildId() == 1 || build.getBuildId() == 2,
                    "Build id should be 1 or 2");
        }
    }

    public void testFindBuildsForDependencyMD5Checksum() throws SQLException {
        Collection<BuildEntity> builds = buildsDao.findBuildsForDependencyChecksum(ChecksumType.md5,
                "d02a360ecad98a34b59863c1e65bcf71");

        assertEquals(builds.size(), 2);
        Assertions.assertThat(builds).doesNotHaveDuplicates();
        for (BuildEntity build : builds) {
            assertTrue(build.getBuildId() == 2 || build.getBuildId() == 3, "Build id should be 2 or 3");
        }
    }

    private void doDelete(long buildId, int nbDeletedRows) throws SQLException {
        assertNotNull(buildsDao.getBuild(buildId));
        assertNotNull(buildsDao.getJsonBuild(buildId, DummyBuild.class));
        assertEquals(buildsDao.deleteBuild(buildId), nbDeletedRows);
        assertNull(buildsDao.getBuild(buildId));
        assertNull(buildsDao.getJsonBuild(buildId, DummyBuild.class));
    }

    private void assertAnyBuild(BuildEntity b) throws SQLException {
        assertNotNull(b);
        switch ((int) b.getBuildId()) {
            case 1:
                assertBuild1(b);
                break;
            case 2:
                assertBuild2(b);
                break;
            case 3:
                assertBuild3(b);
                break;
            case 4:
                assertBuild4(b);
                break;
            case 5:
                assertBuild5(b);
                break;
            default:
                fail("Build with id " + b.getBuildId() + " is not known!");
        }
        assertNull(buildsDao.getJsonBuild(b.getBuildId(), DummyBuild.class));
    }

    private void assertBuild1(BuildEntity b1) {
        assertEquals(b1.getBuildId(), 1L);
        assertEquals(b1.getBuildName(), "ba");
        assertEquals(b1.getBuildNumber(), "1");
        assertEquals(b1.getBuildDate(), 1349000000000L);
        assertNull(b1.getCiUrl());
        assertEquals(b1.getCreated(), 1350000000000L);
        assertEquals(b1.getCreatedBy(), "me");
        assertEquals(b1.getModified(), 1350000000001L);
        assertEquals(b1.getModifiedBy(), "not-me");
        ImmutableSet<BuildProperty> props = b1.getProperties();
        assertEquals(props.size(), 2);
        BuildProperty bp1 = new BuildProperty(1L, 1L, "start", "0");
        BuildProperty bp2 = new BuildProperty(2L, 1L, "status", "bad");
        assertTrue(props.contains(bp1));
        assertTrue(props.contains(bp2));
        for (BuildProperty prop : props) {
            if (prop.getPropId() == 1L) {
                assertTrue(prop.isIdentical(bp1));
            }
            if (prop.getPropId() == 2L) {
                assertTrue(prop.isIdentical(bp2));
            }
        }
        assertTrue(b1.getPromotions().isEmpty());
    }

    private void assertBuild2(BuildEntity b2) {
        assertEquals(b2.getBuildId(), 2L);
        assertEquals(b2.getBuildName(), "bb");
        assertEquals(b2.getBuildNumber(), "1");
        assertEquals(b2.getBuildDate(), 1349001000000L);
        assertEquals(b2.getCiUrl(), "http://myserver/jenkins/bb/1");
        assertEquals(b2.getCreated(), 1350001000000L);
        assertEquals(b2.getCreatedBy(), "me");
        assertEquals(b2.getModified(), 0L);
        assertNull(b2.getModifiedBy());
        ImmutableSet<BuildProperty> props = b2.getProperties();
        assertEquals(props.size(), 2);
        BuildProperty bp1 = new BuildProperty(3L, 2L, "start", "1");
        BuildProperty bp2 = new BuildProperty(4L, 2L, "status", "not-too-bad");
        assertTrue(props.contains(bp1));
        assertTrue(props.contains(bp2));
        for (BuildProperty prop : props) {
            if (prop.getPropId() == 1L) {
                assertTrue(prop.isIdentical(bp1));
            }
            if (prop.getPropId() == 2L) {
                assertTrue(prop.isIdentical(bp2));
            }
        }
        assertTrue(b2.getPromotions().isEmpty());
    }

    private void assertBuild3(BuildEntity b3) {
        assertEquals(b3.getBuildId(), 3L);
        assertEquals(b3.getBuildName(), "ba");
        assertEquals(b3.getBuildNumber(), "2");
        assertEquals(b3.getBuildDate(), 1349002000000L);
        assertNull(b3.getCiUrl());
        assertEquals(b3.getCreated(), 1350002000000L);
        assertEquals(b3.getCreatedBy(), "me");
        assertEquals(b3.getModified(), 0L);
        assertNull(b3.getModifiedBy());
        assertTrue(b3.getProperties().isEmpty());
        assertEquals(b3.getPromotions().size(), 1);
        BuildPromotionStatus promotionStatus = new BuildPromotionStatus(
                3L, 1350012000000L,
                "me", "dead", null, "bad stuff", null);
        BuildPromotionStatus found = b3.getPromotions().first();
        assertTrue(found.isIdentical(promotionStatus));
    }

    private void assertBuild4(BuildEntity b4) {
        assertEquals(b4.getBuildId(), 4L);
        assertEquals(b4.getBuildName(), "bb");
        assertEquals(b4.getBuildNumber(), "2");
        assertEquals(b4.getBuildDate(), 1349003000000L);
        assertEquals(b4.getCiUrl(), "http://myserver/jenkins/bb/2");
        assertEquals(b4.getCreated(), 1350003000000L);
        assertEquals(b4.getCreatedBy(), "me");
        assertEquals(b4.getModified(), 0L);
        assertNull(b4.getModifiedBy());
        assertTrue(b4.getProperties().isEmpty());
        ImmutableSortedSet<BuildPromotionStatus> promotions = b4.getPromotions();
        assertEquals(promotions.size(), 3);
        ImmutableSortedSet<BuildPromotionStatus> expectedStatus = ImmutableSortedSet.of(
                new BuildPromotionStatus(
                        4L, 1350003000000L,
                        null, "staging", null, null, null),
                new BuildPromotionStatus(
                        4L, 1350013000000L,
                        "promoter", "promoted", "qa-local", "sending to QA", "me"),
                new BuildPromotionStatus(
                        4L, 1350023000000L,
                        "tester", "rollback", "lost-local", "Refused by QA", null)
        );
        assertEquals(promotions, expectedStatus);
        UnmodifiableIterator<BuildPromotionStatus> it1 = expectedStatus.iterator();
        UnmodifiableIterator<BuildPromotionStatus> promosIt = promotions.iterator();
        for (int i = 0; i < 3; i++) {
            assertTrue(promosIt.next().isIdentical(it1.next()));
        }
    }

    private void assertBuild5(BuildEntity b5) {
        assertEquals(b5.getBuildId(), 5L);
        assertEquals(b5.getBuildName(), "ba");
        assertEquals(b5.getBuildNumber(), "3");
        assertEquals(b5.getBuildDate(), 1349004000000L);
        assertNull(b5.getCiUrl());
        assertEquals(b5.getCreated(), 1350004000000L);
        assertEquals(b5.getCreatedBy(), "me");
        assertEquals(b5.getModified(), 0L);
        assertNull(b5.getModifiedBy());
        ImmutableSet<BuildProperty> props = b5.getProperties();
        assertEquals(props.size(), 2);
        BuildProperty bp1 = new BuildProperty(5L, 5L, "start", "4");
        BuildProperty bp2 = new BuildProperty(6L, 5L, "status", "good");
        assertTrue(props.contains(bp1));
        assertTrue(props.contains(bp2));
        for (BuildProperty prop : props) {
            if (prop.getPropId() == 1L) {
                assertTrue(prop.isIdentical(bp1));
            }
            if (prop.getPropId() == 2L) {
                assertTrue(prop.isIdentical(bp2));
            }
        }
        ImmutableSortedSet<BuildPromotionStatus> promotions = b5.getPromotions();
        assertEquals(promotions.size(), 2);
        BuildPromotionStatus first = new BuildPromotionStatus(
                5L, 1350024000000L,
                null, "staging", null, null, "jenkins");
        BuildPromotionStatus second = new BuildPromotionStatus(
                5L, 1350034000000L,
                "promoter", PromotionStatus.RELEASED, "public", "Full release", "rel");
        assertTrue(promotions.first().isIdentical(first));
        assertTrue(promotions.last().isIdentical(second));
    }

    //@Test(dependsOnMethods = { ".*test.*" })
    @AfterClass
    public void fullDelete() throws SQLException {
        assertEquals(buildArtifactsDao.deleteAllBuildArtifacts(), 6);
        assertEquals(buildDependenciesDao.deleteAllBuildDependencies(), 5);
        assertEquals(buildModulesDao.deleteAllBuildModules(), 4);
        assertEquals(buildsDao.deleteAllBuilds(), 17);
    }

}
