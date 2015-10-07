package org.artifactory.storage.db.aql.service;

import org.artifactory.aql.result.AqlEagerResult;
import org.testng.annotations.Test;

/**
 * @author Gidi Shabat
 */
public class AqlItemTypeTest extends AqlAbstractServiceTest {
    /**
     * By default return only files
     */
    @Test
    public void returnByDefaultOnlyFieles() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find()");
        assertSize(queryResult, 11);
    }

    /**
     * Return both files and folder using all
     */
    @Test
    public void returnFileAndFoldersUsingaAll() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"type\":\"any\"})");
        assertSize(queryResult, 26);
    }

    /**
     * Return both files and folder using file/folder
     */
    @Test
    public void returnFileAndFoldersUsingFileAnFolder() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"$or\":[{\"type\":\"file\"},{\"type\":\"folder\"}]})");
        assertSize(queryResult, 26);
    }

    /**
     * Return files
     */
    @Test
    public void returnFiles() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"type\":\"file\"})");
        assertSize(queryResult, 11);
    }

    /**
     * Return folders
     */
    @Test
    public void returnFolder() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"type\":\"folder\"})");
        assertSize(queryResult, 15);
    }

    /**
     * Return folders
     */
    @Test
    public void causingRecursiveOptimisationCleanup() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"$and\":[{\"type\" : \"any\"}],\"$or\":[{\"repo\" : \"repo1\", \"repo\" : \"repo2\" }]})");
        assertSize(queryResult, 23);
    }

    /**
     * Return folders
     */
    @Test
    public void replaceAnyCriteriaWithFileOrFolderCriterias() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"$or\":[{\"type\" : \"any\"},{\"type\" : \"file\"}],\"$or\":[{\"repo\" : \"repo1\", \"repo\" : \"repo2\" }]})");
        assertSize(queryResult, 23);
    }




}