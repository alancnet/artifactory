package org.artifactory.storage.db.aql.service;

import org.artifactory.aql.AqlException;
import org.artifactory.aql.model.AqlPermissionProvider;
import org.artifactory.aql.result.AqlJsonStreamer;
import org.artifactory.aql.result.AqlLazyResult;
import org.artifactory.repo.RepoPath;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * @author Gidi Shabat
 */
public class AqlPermissionsTest extends AqlAbstractServiceTest {

    /**
     * Test Admin authorization
     */
    @Test
    public void testAnonymous() throws IOException {
        AnonymousPermissions permissionProvider = new AnonymousPermissions();
        ReflectionTestUtils.setField(aqlService, "permissionProvider", permissionProvider);
        // return only the properties with the key 'yossis' from repository 'repo1'
        AqlLazyResult aqlLazyResult = aqlService.executeQueryLazy("items.find()");
        AqlJsonStreamer streamResult = new AqlJsonStreamer(aqlLazyResult);

        byte[] read = streamResult.read();
        StringBuilder builder = new StringBuilder();
        while (read != null) {
            builder.append(new String(read));
            read = streamResult.read();
        }
        streamResult.close();
        String string = builder.toString();
        Assert.assertFalse(string.contains("\"repo\" : \"repo2\""));
        Assert.assertFalse(string.contains("\"repo\" : \"repo1\""));
    }


    /**
     * Test Admin authorization
     */
    @Test
    public void testAdmin() throws IOException {
        AdminPermissions permissionProvider = new AdminPermissions();
        ReflectionTestUtils.setField(aqlService, "permissionProvider", permissionProvider);
        // return only the properties with the key 'yossis' from repository 'repo1'
        AqlLazyResult aqlLazyResult = aqlService.executeQueryLazy("items.find()");
        AqlJsonStreamer streamResult = new AqlJsonStreamer(aqlLazyResult);

        byte[] read = streamResult.read();
        StringBuilder builder = new StringBuilder();
        while (read != null) {
            builder.append(new String(read));
            read = streamResult.read();
        }
        streamResult.close();
        String string = builder.toString();
        Assert.assertTrue(string.contains("\"repo\" : \"repo2\""));
        Assert.assertTrue(string.contains("\"repo\" : \"repo1\""));
    }

    /**
     * Test Admin authorization
     */
    @Test
    public void testUser() throws IOException {
        UserPermissions permissionProvider = new UserPermissions();
        ReflectionTestUtils.setField(aqlService, "permissionProvider", permissionProvider);
        // return only the properties with the key 'yossis' from repository 'repo1'
        AqlLazyResult aqlLazyResult = aqlService.executeQueryLazy("items.find({\"type\":\"any\"})");
        AqlJsonStreamer streamResult = new AqlJsonStreamer(aqlLazyResult);

        byte[] read = streamResult.read();
        StringBuilder builder = new StringBuilder();
        while (read != null) {
            builder.append(new String(read));
            read = streamResult.read();
        }
        streamResult.close();
        String string = builder.toString();
        Assert.assertTrue(string.contains("\"repo\" : \"repo2\""));
        Assert.assertFalse(string.contains("\"repo\" : \"repo1\""));
    }

    /**
     * Test Admin authorization
     */
    @Test
    public void testUserItemWithProperties() throws IOException {
        UserPermissions permissionProvider = new UserPermissions();
        ReflectionTestUtils.setField(aqlService, "permissionProvider", permissionProvider);
        // return only the properties with the key 'yossis' from repository 'repo1'
        AqlLazyResult aqlLazyResult = aqlService.executeQueryLazy(
                "items.find({\"type\":\"any\"}).include(\"property.*\")");
        AqlJsonStreamer streamResult = new AqlJsonStreamer(aqlLazyResult);

        byte[] read = streamResult.read();
        StringBuilder builder = new StringBuilder();
        while (read != null) {
            builder.append(new String(read));
            read = streamResult.read();
        }
        streamResult.close();
        String string = builder.toString();
        Assert.assertTrue(string.contains("\"repo\" : \"repo2\""));
        Assert.assertFalse(string.contains("\"repo\" : \"repo1\""));
    }

    /**
     * Test Admin authorization
     */
    @Test
    public void testAdminItemWithProperties() throws IOException {
        AdminPermissions permissionProvider = new AdminPermissions();
        ReflectionTestUtils.setField(aqlService, "permissionProvider", permissionProvider);
        // return only the properties with the key 'yossis' from repository 'repo1'
        AqlLazyResult aqlLazyResult = aqlService.executeQueryLazy(
                "items.find({\"type\":\"any\"}).include(\"property.*\")");
        AqlJsonStreamer streamResult = new AqlJsonStreamer(aqlLazyResult);

        byte[] read = streamResult.read();
        StringBuilder builder = new StringBuilder();
        while (read != null) {
            builder.append(new String(read));
            read = streamResult.read();
        }
        streamResult.close();
        String string = builder.toString();
        Assert.assertTrue(string.contains("\"repo\" : \"repo2\""));
        Assert.assertTrue(string.contains("\"repo\" : \"repo1\""));
    }

    /**
     * Test Admin authorization
     */
    @Test
    public void testAnonymousItemWithProperties() throws IOException {
        AnonymousPermissions permissionProvider = new AnonymousPermissions();
        ReflectionTestUtils.setField(aqlService, "permissionProvider", permissionProvider);
        // return only the properties with the key 'yossis' from repository 'repo1'
        AqlLazyResult aqlLazyResult = aqlService.executeQueryLazy(
                "items.find({\"type\":\"any\"}).include(\"property.*\")");
        AqlJsonStreamer streamResult = new AqlJsonStreamer(aqlLazyResult);

        byte[] read = streamResult.read();
        StringBuilder builder = new StringBuilder();
        while (read != null) {
            builder.append(new String(read));
            read = streamResult.read();
        }
        streamResult.close();
        String string = builder.toString();
        Assert.assertFalse(string.contains("\"repo\" : \"repo2\""));
        Assert.assertFalse(string.contains("\"repo\" : \"repo1\""));
    }


    /**
     * Test Admin authorization
     */
    @Test
    public void missingFieldForPermission() throws IOException {
        AnonymousPermissions permissionProvider = new AnonymousPermissions();
        ReflectionTestUtils.setField(aqlService, "permissionProvider", permissionProvider);
        // return only the properties with the key 'yossis' from repository 'repo1'
        try {
            aqlService.executeQueryLazy("items.find({\"type\":\"any\"}).include(\"property.*\",\"name\")");
        } catch (AqlException e) {
            Assert.assertTrue(e.getMessage().contains(
                    "For permissions reasons AQL demands the following fields: repo, path and name"));
        }
    }


    private class AnonymousPermissions implements AqlPermissionProvider {

        @Override
        public boolean canRead(RepoPath repoPath) {
            return false;
        }

        @Override
        public boolean isAdmin() {
            return false;
        }

        @Override
        public boolean isOss() {
            return false;
        }
    }

    private class UserPermissions implements AqlPermissionProvider {

        @Override
        public boolean canRead(RepoPath repoPath) {
            return "repo2".equals(repoPath.getRepoKey());
        }

        @Override
        public boolean isAdmin() {
            return false;
        }

        @Override
        public boolean isOss() {
            return false;
        }
    }

}
