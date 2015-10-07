package org.artifactory.storage.db.aql.service;

import org.artifactory.aql.result.AqlEagerResult;
import org.testng.annotations.Test;

/**
 * @author Gidi Shabat
 */
public class AqlOffsetLimitTest extends AqlAbstractServiceTest {

    /**
     * All properties for sanity
     */
    @Test
    public void allPropertiesForSanity() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "properties.find()");
        assertSize(queryResult, 9);
    }

    /**
     * offset with limit test
     */
    @Test
    public void offset4LimitWith4() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "properties.find().sort({\"$asc\":[\"key\"]}).offset(4).limit(4)");
        assertSize(queryResult, 4);

        assertProperty(queryResult, "string", "this is string");
    }


    /**
     * offset with limit test
     */
    @Test
    public void offset4LimitWith5() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "properties.find().sort({\"$asc\":[\"key\"]}).offset(4).limit(5)");
        assertSize(queryResult, 5);
        assertProperty(queryResult, "yossis", "value1");
    }

    /**
     * just limit test
     */
    @Test
    public void justLimitTest() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "properties.find().sort({\"$asc\":[\"key\"]}).limit(5)");
        assertSize(queryResult, 5);
        assertProperty(queryResult, "jungle", "value2");
    }

    /**
     * just limit test
     */
    @Test
    public void justLimitOfsetAndSortTest() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "properties.find().sort({\"$asc\":[\"key\",\"value\"]}).limit(5)");
        assertSize(queryResult, 5);
        assertProperty(queryResult, "jungle", "value2");
    }

    /**
     * just limit test
     */
    @Test
    public void justOffsetTest() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "properties.find().offset(4)");
        assertSize(queryResult, 5);
    }
}