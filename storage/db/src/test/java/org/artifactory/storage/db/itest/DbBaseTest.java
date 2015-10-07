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

package org.artifactory.storage.db.itest;

import ch.qos.logback.classic.util.ContextInitializer;
import org.apache.commons.lang.RandomStringUtils;
import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.storage.StorageProperties;
import org.artifactory.storage.db.DbServiceImpl;
import org.artifactory.storage.db.spring.ArtifactoryTomcatDataSource;
import org.artifactory.storage.db.util.DbUtils;
import org.artifactory.storage.db.util.JdbcHelper;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.artifactory.test.TestUtils;
import org.artifactory.util.ResourceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;

import static org.testng.Assert.assertEquals;

/**
 * Base class for the low level database integration tests.
 *
 * @author Yossi Shaul
 */
//@TestExecutionListeners(TransactionalTestExecutionListener.class)
//@Transactional
//@TransactionConfiguration(defaultRollback = false)
@Test(groups = "dbtest")
@ContextConfiguration(locations = {"classpath:spring/db-test-context.xml"})
public abstract class DbBaseTest extends AbstractTestNGSpringContextTests {

    @Autowired
    protected JdbcHelper jdbcHelper;

    @Autowired
    protected DbServiceImpl dbService;

    @Autowired
    @Qualifier("storageProperties")
    protected StorageProperties storageProperties;

    private ArtifactoryHomeBoundTest artifactoryHomeBoundTest;

    private DummyArtifactoryContext dummyArtifactoryContext;

    static {
        // use the itest logback config
        URL url = DbBaseTest.class.getClassLoader().getResource("logback-dbtest.xml");
        if (url == null) {
            throw new RuntimeException("Could not find logback-dbtest.xml");
        }
        System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY, url.getPath());
    }

    @BeforeClass
    @Override
    protected void springTestContextPrepareTestInstance() throws Exception {
        artifactoryHomeBoundTest = createArtifactoryHomeTest();
        artifactoryHomeBoundTest.bindArtifactoryHome();

        super.springTestContextPrepareTestInstance();

        dummyArtifactoryContext = new DummyArtifactoryContext(applicationContext);

        try (Connection connection = jdbcHelper.getDataSource().getConnection()) {
            DbTestUtils.refreshOrRecreateSchema(connection, storageProperties.getDbType());
        }
        TestUtils.invokeMethodNoArgs(dbService, "initializeIdGenerator");
    }

    @AfterClass
    public void verifyDbResourcesReleased() throws IOException, SQLException {
        // make sure there are no active connections
        /*PoolingDataSource ds = (PoolingDataSource) jdbcHelper.getDataSource();
        GenericObjectPool pool = TestUtils.getField(ds, "_pool", GenericObjectPool.class);
        assertEquals(pool.getNumActive(), 0, "Found " + pool.getNumActive() + " active connections after test ended:\n"
                + TestUtils.invokeMethodNoArgs(pool, "debugInfo"));*/
        ArtifactoryTomcatDataSource ds = (ArtifactoryTomcatDataSource) jdbcHelper.getDataSource();
        assertEquals(ds.getActiveConnectionsCount(), 0, "Found " + ds.getActiveConnectionsCount() +
                " active connections after test ended");
        artifactoryHomeBoundTest.unbindArtifactoryHome();
    }

    protected void addBean(Object bean, Class<?>... types) {
        dummyArtifactoryContext.addBean(bean, types);
    }

    protected ArtifactoryHomeBoundTest createArtifactoryHomeTest() throws IOException {
        return new ArtifactoryHomeBoundTest();
    }

    protected String randomMd5() {
        return randomHex(32);
    }

    protected String randomSha1() {
        return randomHex(40);
    }

    private String randomHex(int count) {
        return RandomStringUtils.random(count, "abcdef0123456789");
    }

    @BeforeMethod
    public void bindArtifactoryHome() {
        artifactoryHomeBoundTest.bindArtifactoryHome();
    }

    @AfterMethod
    public void unbindArtifactoryHome() {
        artifactoryHomeBoundTest.unbindArtifactoryHome();
    }

    protected void importSql(String resourcePath) {
        InputStream resource = ResourceUtils.getResource(resourcePath);
        Connection con = null;
        try {
            con = jdbcHelper.getDataSource().getConnection();
            DbUtils.executeSqlStream(con, resource);
            // update the id generator
            TestUtils.invokeMethodNoArgs(dbService, "initializeIdGenerator");
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            DbUtils.close(con);
        }
    }

    @BeforeMethod
    public void bindDummyContext() {
        ArtifactoryContextThreadBinder.bind(dummyArtifactoryContext);
    }

    @AfterMethod
    public void unbindDummyContext() {
        ArtifactoryContextThreadBinder.unbind();
    }

}
