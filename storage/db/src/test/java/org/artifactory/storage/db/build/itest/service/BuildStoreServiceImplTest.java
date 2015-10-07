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

package org.artifactory.storage.db.build.itest.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.artifactory.build.BuildRun;
import org.artifactory.storage.build.service.BuildStoreService;
import org.artifactory.storage.db.build.service.BuildStoreServiceImpl;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.jfrog.build.api.Agent;
import org.jfrog.build.api.Artifact;
import org.jfrog.build.api.Build;
import org.jfrog.build.api.BuildAgent;
import org.jfrog.build.api.Dependency;
import org.jfrog.build.api.Module;
import org.jfrog.build.api.builder.ArtifactBuilder;
import org.jfrog.build.api.builder.BuildInfoBuilder;
import org.jfrog.build.api.builder.DependencyBuilder;
import org.jfrog.build.api.builder.ModuleBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Properties;

import static org.jfrog.build.api.BuildType.GRADLE;
import static org.testng.Assert.*;

/**
 * Date: 11/23/12
 * Time: 5:24 PM
 *
 * @author freds
 */
@Test
public class BuildStoreServiceImplTest extends DbBaseTest {
    @Autowired
    private BuildStoreService buildStoreService;

    @BeforeClass
    public void setup() {
        importSql("/sql/builds.sql");
        importSql("/sql/build-modules.sql");
    }

    @AfterClass
    public void fullDelete() {
        buildStoreService.deleteAllBuilds();
    }

    public void testAddBuild() {
        buildStoreService.addBuild(getBuildObject("test", "23",
                BuildStoreServiceImpl.formatDateToString(System.currentTimeMillis())));
    }

    public void testSearchByChecksum() {
        // TODO:
    }

    @Test(dependsOnMethods = "testSearchByChecksum")
    public void testFindBuild() {
        BuildRun buildRun = buildStoreService.getBuildRun("bb", "2",
                BuildStoreServiceImpl.formatDateToString(1349003000000L));
        assertNotNull(buildRun);
        assertEquals(buildRun.getReleaseStatus(), "rollback");
    }

    @Test(dependsOnMethods = {"testAddBuild", "testFindBuild"})
    public void testGetAllBuildNames() {
        List<String> allBuildNames = buildStoreService.getAllBuildNames();
        assertEquals(allBuildNames.size(), 3);
    }

    @Test(dependsOnMethods = "testGetAllBuildNames")
    public void testDeleteBuildB() {
        buildStoreService.deleteAllBuilds("bb");
        assertTrue(buildStoreService.findBuildsByName("bb").isEmpty());
    }

    @Test(dependsOnMethods = "testGetAllBuildNames")
    public void testDeleteOneBuildA() {
        buildStoreService.deleteBuild("ba", "2", BuildStoreServiceImpl.formatDateToString(1349002000000L));
        assertEquals(buildStoreService.findBuildsByName("ba").size(), 2);
    }

    /**
     * Returns a generic build object
     *
     * @param buildName
     * @param buildNumber
     * @param startedBuild
     * @return Build object
     */
    private Build getBuildObject(String buildName, String buildNumber, String startedBuild) {
        Dependency dependency = new DependencyBuilder().id("moo").type("bob").scopes(Sets.newHashSet("mitzi")).sha1(
                "pop").md5("shmop").requiredBy(Lists.newArrayList("pitzi")).build();

        Artifact artifact = new ArtifactBuilder("blob").type("glob").sha1("shlob").md5("mob").
                properties(new Properties()).build();

        Module module = new ModuleBuilder().id("moo").artifacts(Lists.newArrayList(artifact)).
                dependencies(Lists.newArrayList(dependency)).build();

        Properties properties = new Properties();
        properties.put("goo", "koo");

        return new BuildInfoBuilder(buildName).version("1.7.0").number(buildNumber).type(GRADLE).
                agent(new Agent("pop", "1.6")).started(startedBuild).durationMillis(6L).
                principal("bob").artifactoryPrincipal("too").url("mitz").modules(Lists.newArrayList(module)).
                properties(properties).buildAgent(new BuildAgent("agentName", "agentVersion"))
                .agent(new Agent("agentName", "agentVersion")).build();
    }
}
