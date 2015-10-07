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

import com.google.common.collect.Lists;
import org.artifactory.storage.db.build.dao.BuildArtifactsDao;
import org.artifactory.storage.db.build.dao.BuildDependenciesDao;
import org.artifactory.storage.db.build.dao.BuildModulesDao;
import org.artifactory.storage.db.build.dao.BuildsDao;
import org.artifactory.storage.db.build.entity.BuildEntity;
import org.artifactory.storage.db.build.entity.BuildModule;
import org.artifactory.storage.db.build.entity.BuildPromotionStatus;
import org.artifactory.storage.db.build.entity.BuildProperty;
import org.artifactory.storage.db.build.entity.ModuleProperty;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.artifactory.storage.db.util.blob.BlobWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;

import static org.testng.Assert.assertEquals;

/**
 * Date: 11/11/12
 * Time: 3:49 PM
 *
 * @author freds
 */
public abstract class BuildsDaoBaseTest extends DbBaseTest {
    @Autowired
    protected BuildModulesDao buildModulesDao;

    @Autowired
    protected BuildsDao buildsDao;

    @Autowired
    protected BuildArtifactsDao buildArtifactsDao;

    @Autowired
    protected BuildDependenciesDao buildDependenciesDao;

    protected BuildEntity createBuild11() {
        long now = System.currentTimeMillis();
        BuildEntity c1 = new BuildEntity(11L, "c1", "1", now - 20000L, null, now, "this-is-me", 0L, null);
        c1.setProperties(new HashSet<BuildProperty>());
        c1.setPromotions(new HashSet<BuildPromotionStatus>());
        return c1;
    }

    protected BuildEntity createBuild12() {
        long now = System.currentTimeMillis();
        BuildEntity c2 = new BuildEntity(12L, "c2", "1", now - 20000L, null, now, "this-is-me", 0L, null);
        addBuildProps(c2);
        c2.setPromotions(new HashSet<BuildPromotionStatus>());
        return c2;
    }

    private void addBuildProps(BuildEntity b) {
        long buildId = b.getBuildId();
        HashSet<BuildProperty> props = new HashSet<BuildProperty>();
        props.add(new BuildProperty(21L + buildId, buildId, "start", "34"));
        props.add(new BuildProperty(22L + buildId, buildId, "status", "perfect"));
        b.setProperties(props);
    }

    private void addBuildPromotions(BuildEntity b) {
        long buildId = b.getBuildId();
        HashSet<BuildPromotionStatus> promos = new HashSet<BuildPromotionStatus>();
        promos.add(
                new BuildPromotionStatus(buildId, b.getCreated() + 6000L, "promoter", "promoted", "public", "Promoted",
                        null));
        promos.add(new BuildPromotionStatus(buildId, b.getCreated() + 3000L, b.getCreatedBy(), "staged", null, null,
                null));
        b.setPromotions(promos);
    }

    protected BuildEntity createBuild13() {
        long now = System.currentTimeMillis();
        BuildEntity c2 = new BuildEntity(13L, "c2", "13", now - 20000L, null, now, "this-is-me", 0L, null);
        c2.setProperties(new HashSet<BuildProperty>());
        addBuildPromotions(c2);
        return c2;
    }

    protected BuildEntity createBuild14() {
        long now = System.currentTimeMillis();
        BuildEntity c2 = new BuildEntity(14L, "c2", "14", now - 20000L, null, now, "this-is-me", 0L, null);
        addBuildProps(c2);
        addBuildPromotions(c2);
        return c2;
    }

    protected long createAndInsertModules11() throws SQLException, UnsupportedEncodingException {
        BuildEntity build11 = createBuild11();
        Assert.assertEquals(createBuild(build11, "1"), 2);
        long buildId = build11.getBuildId();
        BuildModule buildModule = new BuildModule(101L, buildId, "b11:mod1");
        buildModule.setProperties(new HashSet<ModuleProperty>());
        Assert.assertEquals(buildModulesDao.createBuildModule(buildModule), 1);
        return buildId;
    }

    protected long createAndInsertModules12() throws SQLException, UnsupportedEncodingException {
        BuildEntity build12 = createBuild12();
        long buildId = build12.getBuildId();
        assertEquals(createBuild(build12, "2"), 4);
        BuildModule buildModule1 = new BuildModule(201L, buildId, "b12:the-mod1");
        HashSet<ModuleProperty> props1 = new HashSet<ModuleProperty>();
        props1.add(new ModuleProperty(2001L, 201L, "art-name", "the-mod1"));
        props1.add(new ModuleProperty(2002L, 201L, "key", "value"));
        buildModule1.setProperties(props1);
        BuildModule buildModule2 = new BuildModule(202L, buildId, "b12:not-mod2");
        HashSet<ModuleProperty> props2 = new HashSet<ModuleProperty>();
        props2.add(new ModuleProperty(2021L, 202L, "art-name", "the-mod2"));
        props2.add(new ModuleProperty(2022L, 202L, "key1", "v1"));
        props2.add(new ModuleProperty(2023L, 202L, "key2", "v2"));
        props2.add(new ModuleProperty(2024L, 202L, "key3", "v3"));
        buildModule2.setProperties(props2);
        List<BuildModule> buildModules = Lists.asList(buildModule1, buildModule2, new BuildModule[0]);
        assertEquals(buildModulesDao.createBuildModules(buildModules), 8);
        return buildId;
    }

    protected long createAndInsertModules14() throws SQLException, UnsupportedEncodingException {
        BuildEntity build14 = createBuild14();
        long buildId = build14.getBuildId();
        assertEquals(createBuild(build14, "4"), 6);
        BuildModule buildModule1 = new BuildModule(401L, buildId, "b14:the-mod1");
        buildModule1.setProperties(new HashSet<ModuleProperty>());
        BuildModule buildModule2 = new BuildModule(402L, buildId, "b14:not-mod2");
        buildModule2.setProperties(new HashSet<ModuleProperty>());
        List<BuildModule> buildModules = Lists.asList(buildModule1, buildModule2, new BuildModule[0]);
        assertEquals(buildModulesDao.createBuildModules(buildModules), 2);
        return buildId;
    }

    protected int createBuild(BuildEntity build, String buildName) throws SQLException, UnsupportedEncodingException {
        return buildsDao.createBuild(build, new BlobWrapper("{\"name\":\"" + buildName + "\"}"));
    }

    public static class DummyBuild {
        public String name;
    }
}
