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
import org.artifactory.storage.db.build.entity.BuildDependency;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.*;

/**
 * Date: 10/31/12
 * Time: 11:03 AM
 * <p/>
 * TODO: Test for SQL constraints violations.
 *
 * @author freds
 */
@Test
public class BuildDependenciesDaoTest extends BuildsDaoBaseTest {
    private static final String SHA1_SUFFIX = "dab88fc2a043c2479a6de676a2f8179e9ea2167";
    private static final String MD5_SUFFIX = "d2a360ecad98a34b59863c1e65bcf71";

    //@Test(dependsOnMethods = "test*")
    @AfterClass
    public void fullDelete() throws SQLException {
        assertEquals(buildArtifactsDao.deleteAllBuildArtifacts(), 0);
        assertEquals(buildDependenciesDao.deleteAllBuildDependencies(), 3);
        assertEquals(buildModulesDao.deleteAllBuildModules(), 11);
        assertEquals(buildsDao.deleteAllBuilds(), 12);
    }

    public void testCreateSimpleDependency() throws SQLException, UnsupportedEncodingException {
        createAndInsertModules11();
        BuildDependency bd = new BuildDependency(1001L, 101L, "group:dep1:1.0", "compile,test", "jar",
                "1" + SHA1_SUFFIX, "1" + MD5_SUFFIX);
        Assert.assertEquals(buildDependenciesDao.createBuildDependency(bd), 1);
        List<BuildDependency> dependencies = buildDependenciesDao.findDependenciesForModule(101L);
        assertFoundDependency1001(dependencies);
    }

    private void assertFoundDependency1001(List<BuildDependency> dependencies) {
        Assert.assertEquals(dependencies.size(), 1);
        BuildDependency buildDependency = dependencies.get(0);
        Assert.assertEquals(buildDependency.getDependencyId(), 1001L);
        Assert.assertEquals(buildDependency.getDependencyNameId(), "group:dep1:1.0");
        Assert.assertEquals(buildDependency.getDependencyScopes(), "compile,test");
        Assert.assertEquals(buildDependency.getDependencyType(), "jar");
        Assert.assertEquals(buildDependency.getSha1(), "1" + SHA1_SUFFIX);
        Assert.assertEquals(buildDependency.getMd5(), "1" + MD5_SUFFIX);
    }

    public void testCreateMultipleDependencies() throws SQLException, UnsupportedEncodingException {
        long build12 = createAndInsertModules12();
        ArrayList<BuildDependency> bds = new ArrayList<BuildDependency>(4);
        bds.add(new BuildDependency(2001L, 201L, "apache:log4j:1.0", (String) null, "dll", "a" + SHA1_SUFFIX,
                "a" + MD5_SUFFIX));
        bds.add(new BuildDependency(2002L, 201L, "jfrog:bi:2.2.0", (String) null, "zip", "b" + SHA1_SUFFIX,
                "b" + MD5_SUFFIX));
        bds.add(new BuildDependency(2021L, 202L, "g:a:v", (String) null, "jar", "b" + SHA1_SUFFIX, "b" + MD5_SUFFIX));
        bds.add(new BuildDependency(2022L, 202L, "d:b:6", (String) null, "pom", "c" + SHA1_SUFFIX, "c" + MD5_SUFFIX));
        Assert.assertEquals(buildDependenciesDao.createBuildDependencies(bds), 4);
        List<BuildDependency> dependencies = buildDependenciesDao.findDependenciesForModules(
                buildModulesDao.findModuleIdsForBuild(build12));
        Assert.assertEquals(dependencies.size(), 4);
        assertBuildDependenciesFromBuild12(dependencies);
    }

    public void testCreateNullChecksumsDependencies() throws SQLException, UnsupportedEncodingException {
        long build14 = createAndInsertModules14();
        ArrayList<BuildDependency> bds = new ArrayList<BuildDependency>(2);
        bds.add(new BuildDependency(4001L, 401L, "apache:log4j:1.1", (String) null, "dll", null, "e" + MD5_SUFFIX));
        bds.add(new BuildDependency(4021L, 402L, "jfrog:bi:2.2.1", (String) null, null, "f" + SHA1_SUFFIX, null));
        bds.add(new BuildDependency(4022L, 402L, "jfrog:bi:2.2.1", (String) null, null, null, null));
        Assert.assertEquals(buildDependenciesDao.createBuildDependencies(bds), 3);
        List<BuildDependency> dependencies = buildDependenciesDao.findDependenciesForModules(
                buildModulesDao.findModuleIdsForBuild(build14));
        Assert.assertEquals(dependencies.size(), 3);
    }

    private void assertBuildDependenciesFromBuild12(List<BuildDependency> dependencies) {
        for (BuildDependency dependency : dependencies) {
            switch ((int) dependency.getDependencyId()) {
                case 2001:
                    assertEquals(dependency.getDependencyNameId(), "apache:log4j:1.0");
                    assertNull(dependency.getDependencyScopes());
                    assertEquals(dependency.getDependencyType(), "dll");
                    assertEquals(dependency.getSha1(), "a" + SHA1_SUFFIX);
                    assertEquals(dependency.getMd5(), "a" + MD5_SUFFIX);
                    break;
                case 2002:
                    assertEquals(dependency.getDependencyNameId(), "jfrog:bi:2.2.0");
                    assertNull(dependency.getDependencyScopes());
                    assertEquals(dependency.getDependencyType(), "zip");
                    assertEquals(dependency.getSha1(), "b" + SHA1_SUFFIX);
                    assertEquals(dependency.getMd5(), "b" + MD5_SUFFIX);
                    break;
                case 2021:
                    assertEquals(dependency.getDependencyNameId(), "g:a:v");
                    assertNull(dependency.getDependencyScopes());
                    assertEquals(dependency.getDependencyType(), "jar");
                    assertEquals(dependency.getSha1(), "b" + SHA1_SUFFIX);
                    assertEquals(dependency.getMd5(), "b" + MD5_SUFFIX);
                    break;
                case 2022:
                    assertEquals(dependency.getDependencyNameId(), "d:b:6");
                    assertNull(dependency.getDependencyScopes());
                    assertEquals(dependency.getDependencyType(), "pom");
                    assertEquals(dependency.getSha1(), "c" + SHA1_SUFFIX);
                    assertEquals(dependency.getMd5(), "c" + MD5_SUFFIX);
                    break;
                default:
                    fail("Build dependency " + dependency + " should not appear in this list!");
            }
        }
    }

    @Test(dependsOnMethods = {"testCreateSimpleDependency", "testCreateMultipleDependencies"})
    public void testFindBySha1() throws SQLException {
        assertFoundDependency1001(
                buildDependenciesDao.findDependenciesForChecksum(ChecksumType.sha1, "1" + SHA1_SUFFIX));
        List<BuildDependency> forSha1 = buildDependenciesDao.findDependenciesForChecksum(ChecksumType.sha1,
                "b" + SHA1_SUFFIX);
        Assert.assertEquals(forSha1.size(), 2);
        assertBuildDependenciesFromBuild12(forSha1);
    }

    @Test(dependsOnMethods = {"testCreateSimpleDependency", "testCreateMultipleDependencies"})
    public void testFindByMd5() throws SQLException {
        assertFoundDependency1001(buildDependenciesDao.findDependenciesForChecksum(ChecksumType.md5, "1" + MD5_SUFFIX));
        List<BuildDependency> forMd5 = buildDependenciesDao.findDependenciesForChecksum(ChecksumType.md5,
                "b" + MD5_SUFFIX);
        Assert.assertEquals(forMd5.size(), 2);
        assertBuildDependenciesFromBuild12(forMd5);
    }

    @Test(dependsOnMethods = {"testFindBySha1", "testFindByMd5"})
    public void testDeleteBuildDependencies() throws SQLException {
        Assert.assertEquals(buildDependenciesDao.deleteBuildDependencies(buildModulesDao.findModuleIdsForBuild(11L)),
                1);
        Assert.assertEquals(buildDependenciesDao.deleteBuildDependencies(buildModulesDao.findModuleIdsForBuild(12L)),
                4);
    }
}
