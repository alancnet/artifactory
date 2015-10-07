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
import org.artifactory.storage.db.build.entity.BuildModule;
import org.artifactory.storage.db.build.entity.ModuleProperty;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.testng.Assert.*;

/**
 * Date: 10/31/12
 * Time: 11:03 AM
 *
 * @author freds
 */
public class BuildModulesDaoTest extends BuildsDaoBaseTest {

    @BeforeClass
    public void setup() {
        importSql("/sql/builds.sql");
        importSql("/sql/build-modules.sql");
    }

    @AfterClass
    public void testFullDelete() throws SQLException {
        assertEquals(buildArtifactsDao.deleteAllBuildArtifacts(), 6);
        assertEquals(buildDependenciesDao.deleteAllBuildDependencies(), 5);
        assertEquals(buildModulesDao.deleteAllBuildModules(), 23);
        assertEquals(buildsDao.deleteAllBuilds(), 23);
    }

    public void testReadBuildModulesById() throws SQLException {
        for (long i = 1L; i <= 5L; i++) {
            assertAnyBuildModuleList(buildModulesDao.findModulesForBuild(i));
        }
    }

    public void testSimpleCreateBuildModule() throws SQLException, UnsupportedEncodingException {
        long buildId = createAndInsertModules11();
        List<BuildModule> modulesForBuild = buildModulesDao.findModulesForBuild(buildId);
        assertEquals(modulesForBuild.size(), 1);
        BuildModule buildModule1 = modulesForBuild.get(0);
        assertEquals(buildModule1.getModuleId(), 101L);
        assertEquals(buildModule1.getModuleNameId(), "b11:mod1");
        assertTrue(buildModule1.getProperties().isEmpty());
    }

    public void testCreateTwoBuildModulesWithProperties() throws SQLException, UnsupportedEncodingException {
        long buildId = createAndInsertModules12();
        List<BuildModule> modulesForBuild = buildModulesDao.findModulesForBuild(buildId);
        assertEquals(modulesForBuild.size(), 2);
        for (BuildModule module : modulesForBuild) {
            switch ((int) module.getModuleId()) {
                case 201:
                    assertEquals(module.getModuleId(), 201L);
                    assertEquals(module.getModuleNameId(), "b12:the-mod1");
                    assertEquals(module.getProperties().size(), 2);
                    break;
                case 202:
                    assertEquals(module.getModuleId(), 202L);
                    assertEquals(module.getModuleNameId(), "b12:not-mod2");
                    assertEquals(module.getProperties().size(), 4);
                    break;
                default:
                    fail("Build module " + module + " unexpected!");
            }
        }
    }

    @Test(dependsOnMethods = {"testSimpleCreateBuildModule", "testCreateTwoBuildModulesWithProperties"})
    public void testDeleteBuildModules() throws SQLException, UnsupportedEncodingException {
        doDelete(11L, 1, 1);
        doDelete(12L, 2, 8);
    }

    private void doDelete(long buildId, int nbModules, int nbExpectedDeletedRows) throws SQLException {
        List<BuildModule> modulesForBuild = buildModulesDao.findModulesForBuild(buildId);
        assertNotNull(modulesForBuild);
        assertEquals(modulesForBuild.size(), nbModules);
        assertEquals(buildModulesDao.deleteBuildModules(buildId), nbExpectedDeletedRows);
        assertTrue(buildModulesDao.findModulesForBuild(buildId).isEmpty());
    }

    private void assertAnyBuildModuleList(List<BuildModule> b) throws SQLException {
        assertNotNull(b);
        assertFalse(b.isEmpty());
        // to simplify we sort the results by build id since each db will return in different order
        Collections.sort(b, new Comparator<BuildModule>() {
            @Override
            public int compare(BuildModule o1, BuildModule o2) {
                if (o1.getBuildId() != o2.getBuildId()) {
                    return Long.compare(o1.getBuildId(), o2.getBuildId());
                } else {
                    return Long.compare(o1.getModuleId(), o2.getModuleId());
                }
            }
        });
        long buildId = b.get(0).getBuildId();
        switch ((int) buildId) {
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
                fail("Build with id " + buildId + " is not known!");
        }
    }

    private void assertBuild1(List<BuildModule> b1) {
        assertBuildModuleData(b1, "a", new int[]{3}, new String[]{"bad"});
    }

    private void assertBuild2(List<BuildModule> b2) {
        assertBuildModuleData(b2, "b", new int[]{2, 2}, new String[]{"not-too-bad", "quite-good"});
    }

    private void assertBuild3(List<BuildModule> b3) {
        assertBuildModuleData(b3, "a", new int[]{0, 0}, new String[0]);
    }

    private void assertBuild4(List<BuildModule> b4) {
        assertBuildModuleData(b4, "b", new int[]{0, 0}, new String[0]);
    }

    private void assertBuild5(List<BuildModule> b5) {
        assertBuildModuleData(b5, "a", new int[]{2, 2, 2}, new String[]{"good", "good", "good"});
    }

    private void assertBuildModuleData(List<BuildModule> buildModules, String shortBuildName,
            int[] nbModulePropsExpected, String[] moduleStatusExpected) {
        assertEquals(buildModules.size(), nbModulePropsExpected.length);
        int i = 0;
        for (BuildModule module : buildModules) {
            String artName = "mod" + shortBuildName + (i + 1);
            String moduleName = "b" + shortBuildName + ":" + artName;
            assertEquals(module.getModuleNameId(), moduleName);
            ImmutableSet<ModuleProperty> modProps = module.getProperties();
            assertEquals(modProps.size(), nbModulePropsExpected[i]);
            if (modProps.size() >= 2) {
                boolean foundName = false;
                boolean foundStatus = false;
                for (ModuleProperty modProp : modProps) {
                    if ("art-name".equals(modProp.getPropKey())) {
                        assertFalse(foundName, "Got 2 times the name property " + modProp);
                        assertEquals(modProp.getPropValue(), artName);
                        foundName = true;
                    }
                    if (modProp.getPropKey().startsWith("status")) {
                        assertFalse(foundStatus, "Got 2 times the status property " + modProp);
                        assertEquals(modProp.getPropValue(), moduleStatusExpected[i],
                                "Property " + modProp + " was not expected value " + moduleStatusExpected[i]);
                        foundStatus = true;
                    }
                }
                assertTrue(foundName, "Did not find the art-name property");
                assertTrue(foundStatus, "Did not find the status property");
            }
            i++;
        }
    }
}
