package org.artifactory.storage.db.aql.service;

import org.artifactory.aql.AqlException;
import org.artifactory.aql.result.AqlEagerResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Gidi Shabat
 */
public class AqlComparatorTest extends AqlAbstractServiceTest {

    private static final String INVALID_SYNTAX = "Illegal syntax the 'null' values are allowed to use only with equals and not equals operators.\n";

    @Test
    public void greaterWithNull() {
        try {
            AqlEagerResult queryResult = aqlService.executeQueryEager(
                    "items.find({\"repo\" :\"repo1\",\"stat.downloads\":{\"$gt\":null}})");
            assertSize(queryResult, 3);
        } catch (AqlException e) {
            Assert.assertEquals(e.getMessage(), INVALID_SYNTAX);
        }
    }

    @Test
    public void greaterEqualsWithNull() {
        try {
            AqlEagerResult queryResult = aqlService.executeQueryEager(
                    "items.find({\"repo\" :\"repo1\",\"stat.downloads\":{\"$gte\":null}})");
            assertSize(queryResult, 3);
        } catch (AqlException e) {
            Assert.assertEquals(e.getMessage(), INVALID_SYNTAX);
        }
    }

    @Test
    public void greaterLessWithNull() {
        try {
            AqlEagerResult queryResult = aqlService.executeQueryEager(
                    "items.find({\"repo\" :\"repo1\",\"stat.downloads\":{\"$lt\":null}})");
            assertSize(queryResult, 3);
        } catch (AqlException e) {
            Assert.assertEquals(e.getMessage(), INVALID_SYNTAX);
        }
    }

    @Test
    public void lessEqualsWithNull() {
        try {
            AqlEagerResult queryResult = aqlService.executeQueryEager(
                    "items.find({\"repo\" :\"repo1\",\"stat.downloads\":{\"$lte\":null}})");
            assertSize(queryResult, 3);
        } catch (AqlException e) {
            Assert.assertEquals(e.getMessage(), INVALID_SYNTAX);
        }
    }

    @Test
    public void matchWithNull() {
        try {
            AqlEagerResult queryResult = aqlService.executeQueryEager(
                    "items.find({\"repo\" :\"repo1\",\"stat.downloads\":{\"$match\":null}})");
            assertSize(queryResult, 3);
        } catch (AqlException e) {
            Assert.assertEquals(e.getMessage(), INVALID_SYNTAX);
        }
    }

    @Test
    public void notMatchWithNull() {
        try {
            AqlEagerResult queryResult = aqlService.executeQueryEager(
                    "items.find({\"repo\" :\"repo1\",\"stat.downloads\":{\"$nmatch\":null}})");
            assertSize(queryResult, 3);
        } catch (AqlException e) {
            Assert.assertEquals(e.getMessage(), INVALID_SYNTAX);
        }
    }

    @Test
    public void equalWithNull() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"repo\" :\"repo1\",\"modified\":{\"$eq\": null }})");
        assertSize(queryResult, 0);
    }

    @Test
    public void notEqualWithNull() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"repo\" :\"repo1\",\"modified\":{\"$ne\": null }})");
        assertSize(queryResult, 4);
    }
}
