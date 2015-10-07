package org.artifactory.storage.db.aql.service;

import org.artifactory.aql.AqlException;
import org.artifactory.aql.result.AqlEagerResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Gidi Shabat
 */
public class AqlValueTypesTest extends AqlAbstractServiceTest {

    @Test
    public void badIntFormat() {
        try {
            aqlService.executeQueryEager(
                    "items.find({\"repo\" :\"repo1\",\"stat.downloads\":{\"$eq\":\"null\"}})");
            Assert.fail();
        } catch (AqlException e) {
            Assert.assertEquals(e.getMessage(), "AQL Expect integer value but found:null\n");
        }
    }

    @Test
    public void dateFormat() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"repo\" :\"repo1\",\"modified\":{\"$eq\":\"12-12-12\"}})");
        assertSize(queryResult, 0);
    }


    @Test
    public void longFormat() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"repo\" :\"repo1\",\"size\":{\"$eq\":\"1111111111111\"}})");
        assertSize(queryResult, 0);
    }

    @Test
    public void intFormat() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"repo\" :\"repo1\",\"stat.downloads\":{\"$eq\":\"1\"}})");
        assertSize(queryResult, 0);
    }

    @Test
    public void badDateFormat() {
        try {
            aqlService.executeQueryEager(
                    "items.find({\"repo\" :\"repo1\",\"modified\":{\"$eq\":\"null\"}})");
            Assert.fail();
        } catch (AqlException e) {
            Assert.assertEquals(e.getMessage(), "Invalid Date format: null, AQL expect ISODateTimeFormat");
        }
    }

    @Test
    public void badLongFormat() {
        try {
            aqlService.executeQueryEager(
                    "items.find({\"repo\" :\"repo1\",\"size\":{\"$eq\":\"null\"}})");
            Assert.fail();
        } catch (AqlException e) {
            Assert.assertEquals(e.getMessage(), "AQL Expect long value but found:null\n");
        }
    }

    @Test
    public void badFileTypeFormat() {
        try {
            aqlService.executeQueryEager(
                    "items.find({\"repo\" :\"repo1\",\"type\":{\"$eq\":\"null\"}})");
            Assert.fail();
        } catch (AqlException e) {
            Assert.assertEquals(e.getMessage(), "Invalid file type: null, valid types are : file, folder, any");
        }
    }

    @Test
    public void fileTypeFormat() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"repo\" :\"repo1\",\"type\":{\"$eq\":\"any\"}})");
        assertSize(queryResult, 14);
    }
}
