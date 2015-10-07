package org.artifactory.storage.db.aql.service;

import org.apache.commons.lang.StringUtils;
import org.artifactory.aql.model.AqlItemTypeEnum;
import org.artifactory.aql.model.AqlPermissionProvider;
import org.artifactory.aql.result.AqlEagerResult;
import org.artifactory.aql.result.rows.AqlArchiveItem;
import org.artifactory.aql.result.rows.AqlBaseItem;
import org.artifactory.aql.result.rows.AqlBuild;
import org.artifactory.aql.result.rows.AqlBuildArtifact;
import org.artifactory.aql.result.rows.AqlBuildDependency;
import org.artifactory.aql.result.rows.AqlBuildModule;
import org.artifactory.aql.result.rows.AqlProperty;
import org.artifactory.aql.result.rows.AqlStatisticItem;
import org.artifactory.repo.RepoPath;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;

/**
 * @author Gidi Shabat
 */
public class AqlAbstractServiceTest extends DbBaseTest {
    @Autowired
    protected AqlServiceImpl aqlService;

    @BeforeClass
    public void setup() {
        importSql("/sql/aql_test.sql");
        ReflectionTestUtils.setField(aqlService, "permissionProvider", new AdminPermissions());
    }

    protected void assertItem(AqlEagerResult queryResult, String repo, String path, String name, AqlItemTypeEnum type) {
        boolean found = false;
        for (int j = 0; j < queryResult.getSize(); j++) {
            AqlBaseItem row = (AqlBaseItem) queryResult.getResult(j);
            if (row.getRepo().equals(repo) && row.getName().equals(name) &&
                    row.getPath().equals(path) && row.getType() == type) {
                found = true;
            }
        }
        Assert.assertTrue(found);
    }

    protected void assertProperty(AqlEagerResult queryResult, String key, String value) {
        boolean found = false;
        for (int j = 0; j < queryResult.getSize(); j++) {
            AqlProperty row = (AqlProperty) queryResult.getResult(j);
            if (row.getKey().equals(key) && (StringUtils.isBlank(row.getValue()) && StringUtils.isBlank(value) ||
                    (!StringUtils.isBlank(row.getValue()) && row.getValue().equals(value)))) {
                found = true;
            }
        }
        Assert.assertTrue(found);
    }

    protected void assertDependencies(AqlEagerResult queryResult, String buildDependencyName,
            String buildDependencyScope, String buildDependencyType) {
        boolean found = false;
        for (int j = 0; j < queryResult.getSize(); j++) {
            AqlBuildDependency row = (AqlBuildDependency) queryResult.getResult(j);
            if (row.getBuildDependencyName().equals(buildDependencyName) &&
                    row.getBuildDependencyScope().equals(buildDependencyScope) &&
                    row.getBuildDependencyType().equals(buildDependencyType)) {
                found = true;
            }
        }
        Assert.assertTrue(found);
    }

    protected void assertArchive(AqlEagerResult queryResult, String path, String name) {
        boolean found = false;
        for (int j = 0; j < queryResult.getSize(); j++) {
            AqlArchiveItem row = (AqlArchiveItem) queryResult.getResult(j);
            if (row.getEntryPath().equals(path) &&
                    row.getEntryName().equals(name)) {
                found = true;
            }
        }
        Assert.assertTrue(found);
    }

    protected void assertBuildArtifacts(AqlEagerResult queryResult, String buildArtifactsName,
            String buildArtifactsType) {
        boolean found = false;
        for (int j = 0; j < queryResult.getSize(); j++) {
            AqlBuildArtifact row = (AqlBuildArtifact) queryResult.getResult(j);
            if (row.getBuildArtifactName().equals(buildArtifactsName) &&
                    row.getBuildArtifactType().equals(buildArtifactsType)) {
                found = true;
            }
        }
        Assert.assertTrue(found);
    }

    protected void assertStatistics(AqlEagerResult queryResult, int downloads, String downloadBy) {
        boolean found = false;
        for (int j = 0; j < queryResult.getSize(); j++) {
            AqlStatisticItem row = (AqlStatisticItem) queryResult.getResult(j);
            if (row.getDownloadedBy().equals(downloadBy) && row.getDownloads() == (downloads)) {
                found = true;
            }
        }
        Assert.assertTrue(found);
    }

    protected void assertBuild(AqlEagerResult queryResult, String buildName, String buildNumber) {
        boolean found = false;
        for (int j = 0; j < queryResult.getSize(); j++) {
            AqlBuild row = (AqlBuild) queryResult.getResult(j);
            if (row.getBuildName().equals(buildName) &&
                    row.getBuildNumber().equals(buildNumber)) {
                found = true;
            }
        }
        Assert.assertTrue(found);
    }

    protected void assertModule(AqlEagerResult queryResult, String moduleName) {
        boolean found = false;
        for (int j = 0; j < queryResult.getSize(); j++) {
            AqlBuildModule row = (AqlBuildModule) queryResult.getResult(j);
            if ( row.getBuildModuleName().equals(moduleName)) {
                found = true;
            }
        }
        Assert.assertTrue(found);
    }

    protected void assertSize(AqlEagerResult queryResult, int i) {
        Assert.assertEquals(queryResult.getSize(), i);
    }

    public static class AdminPermissions implements AqlPermissionProvider {

        @Override
        public boolean canRead(RepoPath repoPath) {
            return true;
        }

        @Override
        public boolean isAdmin() {
            return true;
        }

        @Override
        public boolean isOss() {
            return false;
        }
    }
}
