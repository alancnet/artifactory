package org.artifactory.storage.db.aql.service;

import org.artifactory.aql.AqlException;
import org.artifactory.aql.model.AqlPermissionProvider;
import org.artifactory.repo.RepoPath;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Gidi Shabat
 */
public class AqlRestrictionTest extends AqlAbstractServiceTest {


    @Test
    public void usageOfSortInOssShouldBeBlocked() {
        OssVersion ossVersionPermisionProvider = new OssVersion();
        ReflectionTestUtils.setField(aqlService, "permissionProvider", ossVersionPermisionProvider);
        try {
            aqlService.executeQueryEager("items.find().sort({\"$asc\":[\"name\"]})");
            Assert.fail();
        } catch (AqlException e) {
            Assert.assertEquals(e.getMessage(), "Sorting is not supported by AQL in the open source version\n");
        }
    }

    @Test
    public void usageOfPropertyResultFilterInOssShouldBeBlocked() {
        OssVersion ossVersionPermisionProvider = new OssVersion();
        ReflectionTestUtils.setField(aqlService, "permissionProvider", ossVersionPermisionProvider);
        try {
            aqlService.executeQueryEager("items.find().include(\"@version\")");
            Assert.fail();
        } catch (AqlException e) {
            Assert.assertEquals(e.getMessage(),
                    "Filtering properties result is not supported by AQL in the open source version\n");
        }
    }

    @Test
    public void usageOfSortInNoneOssShouldNotBeBlocked() {
        ProVersion proVersionPermisionProvider = new ProVersion();
        ReflectionTestUtils.setField(aqlService, "permissionProvider", proVersionPermisionProvider);
        aqlService.executeQueryEager("items.find().sort({\"$asc\":[\"name\"]})");
    }

    @Test
    public void usageOfPropertyResultFilterInNoneOssShouldNotBeBlocked() {
        ProVersion proVersionPermisionProvider = new ProVersion();
        ReflectionTestUtils.setField(aqlService, "permissionProvider", proVersionPermisionProvider);
        aqlService.executeQueryEager("items.find().include(\"@version\")");
    }


    private class OssVersion implements AqlPermissionProvider {

        @Override
        public boolean canRead(RepoPath repoPath) {
            return "repo2".equals(repoPath.getRepoKey());
        }

        @Override
        public boolean isAdmin() {
            return true;
        }

        @Override
        public boolean isOss() {
            return true;
        }
    }

    private class ProVersion implements AqlPermissionProvider {

        @Override
        public boolean canRead(RepoPath repoPath) {
            return "repo2".equals(repoPath.getRepoKey());
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