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

import org.artifactory.checksum.ChecksumType;
import org.artifactory.storage.db.build.entity.BuildArtifact;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;

/**
 * Date: 10/31/12
 * Time: 11:03 AM
 * <p/>
 * TODO: Test for SQL constraints violations.
 *
 * @author freds
 */
@Test
public class BuildArtifactsDaoTest extends BuildsDaoBaseTest {
    private static final String SHA1_SUFFIX = "cab88fc2a043c2479a6de676a2f8179e9ea2167";
    private static final String MD5_SUFFIX = "02a360ecad98a34b59863c1e65bcf71";

    @AfterClass
    public void testFullDelete() throws SQLException {
        assertEquals(buildArtifactsDao.deleteAllBuildArtifacts(), 3);
        assertEquals(buildDependenciesDao.deleteAllBuildDependencies(), 0);
        assertEquals(buildModulesDao.deleteAllBuildModules(), 11);
        assertEquals(buildsDao.deleteAllBuilds(), 12);
    }

    public void testCreateSimpleArtifact() throws SQLException, UnsupportedEncodingException {
        createAndInsertModules11();
        BuildArtifact ba = new BuildArtifact(1001L, 101L, "art1", "jar", "1" + SHA1_SUFFIX, "1" + MD5_SUFFIX);
        Assert.assertEquals(buildArtifactsDao.createBuildArtifact(ba), 1);
        List<BuildArtifact> artifacts = buildArtifactsDao.findArtifactsForModule(101L);
        assertFoundArtifact1001(artifacts);
    }

    private void assertFoundArtifact1001(List<BuildArtifact> artifacts) {
        Assert.assertEquals(artifacts.size(), 1);
        BuildArtifact buildArtifact = artifacts.get(0);
        Assert.assertEquals(buildArtifact.getArtifactId(), 1001L);
        Assert.assertEquals(buildArtifact.getArtifactName(), "art1");
        Assert.assertEquals(buildArtifact.getArtifactType(), "jar");
        Assert.assertEquals(buildArtifact.getSha1(), "1" + SHA1_SUFFIX);
        Assert.assertEquals(buildArtifact.getMd5(), "1" + MD5_SUFFIX);
    }

    public void testCreateMultipleArtifacts() throws SQLException, UnsupportedEncodingException {
        long build12 = createAndInsertModules12();
        ArrayList<BuildArtifact> bas = new ArrayList<BuildArtifact>(2);
        bas.add(new BuildArtifact(2001L, 201L, "b2-art1", "dll", "a" + SHA1_SUFFIX, "a" + MD5_SUFFIX));
        bas.add(new BuildArtifact(2002L, 201L, "b2-art2", "zip", "b" + SHA1_SUFFIX, "b" + MD5_SUFFIX));
        bas.add(new BuildArtifact(2021L, 202L, "b2-m2-art1", "jar", "b" + SHA1_SUFFIX, "b" + MD5_SUFFIX));
        bas.add(new BuildArtifact(2022L, 202L, "b2-m2-art2", "pom", "c" + SHA1_SUFFIX, "c" + MD5_SUFFIX));
        Assert.assertEquals(buildArtifactsDao.createBuildArtifacts(bas), 4);
        List<BuildArtifact> artifacts = buildArtifactsDao.findArtifactsForModules(
                buildModulesDao.findModuleIdsForBuild(build12));
        Assert.assertEquals(artifacts.size(), 4);
        assertBuildArtifactsFromBuild12(artifacts);
    }

    public void testCreateNullChecksumsDependencies() throws SQLException, UnsupportedEncodingException {
        long build14 = createAndInsertModules14();
        ArrayList<BuildArtifact> bas = new ArrayList<BuildArtifact>(2);
        bas.add(new BuildArtifact(4001L, 401L, "b2-art1", "dll", "e" + SHA1_SUFFIX, null));
        bas.add(new BuildArtifact(4021L, 402L, "b2-m2-art1", "jar", null, "f" + MD5_SUFFIX));
        bas.add(new BuildArtifact(4022L, 402L, "b2-m2-art1", null, null, null));
        Assert.assertEquals(buildArtifactsDao.createBuildArtifacts(bas), 3);
        List<BuildArtifact> artifacts = buildArtifactsDao.findArtifactsForModules(
                buildModulesDao.findModuleIdsForBuild(build14));
        Assert.assertEquals(artifacts.size(), 3);
    }

    private void assertBuildArtifactsFromBuild12(List<BuildArtifact> artifacts) {
        for (BuildArtifact artifact : artifacts) {
            switch ((int) artifact.getArtifactId()) {
                case 2001:
                    Assert.assertEquals(artifact.getArtifactName(), "b2-art1");
                    Assert.assertEquals(artifact.getArtifactType(), "dll");
                    Assert.assertEquals(artifact.getSha1(), "a" + SHA1_SUFFIX);
                    Assert.assertEquals(artifact.getMd5(), "a" + MD5_SUFFIX);
                    break;
                case 2002:
                    Assert.assertEquals(artifact.getArtifactName(), "b2-art2");
                    Assert.assertEquals(artifact.getArtifactType(), "zip");
                    Assert.assertEquals(artifact.getSha1(), "b" + SHA1_SUFFIX);
                    Assert.assertEquals(artifact.getMd5(), "b" + MD5_SUFFIX);
                    break;
                case 2021:
                    Assert.assertEquals(artifact.getArtifactName(), "b2-m2-art1");
                    Assert.assertEquals(artifact.getArtifactType(), "jar");
                    Assert.assertEquals(artifact.getSha1(), "b" + SHA1_SUFFIX);
                    Assert.assertEquals(artifact.getMd5(), "b" + MD5_SUFFIX);
                    break;
                case 2022:
                    Assert.assertEquals(artifact.getArtifactName(), "b2-m2-art2");
                    Assert.assertEquals(artifact.getArtifactType(), "pom");
                    Assert.assertEquals(artifact.getSha1(), "c" + SHA1_SUFFIX);
                    Assert.assertEquals(artifact.getMd5(), "c" + MD5_SUFFIX);
                    break;
                default:
                    Assert.fail("Build artifact " + artifact + " should not appear in this list!");
            }
        }
    }

    @Test(dependsOnMethods = {"testCreateSimpleArtifact", "testCreateMultipleArtifacts"})
    public void testFindBySha1() throws SQLException {
        assertFoundArtifact1001(buildArtifactsDao.findArtifactsForChecksum(ChecksumType.sha1, "1" + SHA1_SUFFIX));
        List<BuildArtifact> forSha1 = buildArtifactsDao.findArtifactsForChecksum(ChecksumType.sha1, "b" + SHA1_SUFFIX);
        Assert.assertEquals(forSha1.size(), 2);
        assertBuildArtifactsFromBuild12(forSha1);
    }

    @Test(dependsOnMethods = {"testCreateSimpleArtifact", "testCreateMultipleArtifacts"})
    public void testFindByMd5() throws SQLException {
        assertFoundArtifact1001(buildArtifactsDao.findArtifactsForChecksum(ChecksumType.md5, "1" + MD5_SUFFIX));
        List<BuildArtifact> forMd5 = buildArtifactsDao.findArtifactsForChecksum(ChecksumType.md5, "b" + MD5_SUFFIX);
        Assert.assertEquals(forMd5.size(), 2);
        assertBuildArtifactsFromBuild12(forMd5);
    }

    @Test(dependsOnMethods = {"testFindBySha1", "testFindByMd5"})
    public void testDeleteBuildArtifacts() throws SQLException {
        Assert.assertEquals(buildArtifactsDao.deleteBuildArtifacts(buildModulesDao.findModuleIdsForBuild(11L)), 1);
        Assert.assertEquals(buildArtifactsDao.deleteBuildArtifacts(buildModulesDao.findModuleIdsForBuild(12L)), 4);
    }
}
