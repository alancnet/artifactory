package org.artifactory.storage.db.aql.service;

import org.artifactory.aql.result.AqlEagerResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Gidi Shabat
 */
@Test
public class AqlOrAndPropertiesOptimizationTest extends AqlAbstractServiceTest {

    @Test
    public void orOptimizationWithPropertyKeyFields() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"$or\":[{\"property.key\" : \"test\"},{\"property.key\" : \"file\"}]})");
        assertSize(queryResult, 0);
    }

    @Test
    public void orOptimizationWithResult() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"@*\" : {\"$eq\" : \"ant\"} , \"@build.name\" : {\"$eq\" : \"*\"}})");
        assertSize(queryResult, 1);
    }



    @Test
    public void OrOptimizationWithPropertyValueFields() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"$or\":[{\"property.value\" : \"test\"},{\"property.value\" : \"file\"}]})");
        assertSize(queryResult, 0);
    }

    @Test
    public void OrOptimizationWithPropertyValueAndPropertyKeyFields() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"$or\":[{\"property.value\" : \"test\"},{\"property.value\" : \"file\"},{\"property.key\" : \"file\"}]})");
        assertSize(queryResult, 0);
    }

    @Test
    public void OrOptimizationWithPropertyCriterias() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"$or\":[{\"@a\" : \"test\"},{\"@b\" : \"file\"},{\"@c\" : \"c\"}]})");
        assertSize(queryResult, 0);
    }

    /**
     * Note that without the optimization the query duration is grater than 5 minutes
     * and with the optimisation it is seconds
     */
    @Test
    public void QueryTimeImprovementTest() {
        long start = System.nanoTime();
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"$or\":[" +
                        "{\"@aaaaaaaaaaaaa\" : \"test\"}," +
                        "{\"@bbbbbbbbbbbbb\" : \"file\"}," +
                        "{\"@bbbbbbbbbbbbb\" : \"file\"}," +
                        "{\"@bbbbbbbbbbbbb\" : \"file\"}," +
                        "{\"@bbbbbbbbbbbbb\" : \"file\"}," +
                        "{\"@bbbbbbbbbbbbb\" : \"file\"}," +
                        "{\"@bbbbbbbbbbbbb\" : \"file\"}," +
                        "{\"@bbbbbbbbbbbbb\" : \"file\"}," +
                        "{\"@bbbbbbbbbbbbb\" : \"file\"}," +
                        "{\"@bbbbbbbbbbbbb\" : \"file\"}," +
                        "{\"@bbbbbbbbbbbbb\" : \"file\"}," +
                        "{\"@ccccccccccccc\" : \"c\"}," +
                        "{\"@ddddddddddddd\" : \"d\"}," +
                        "{\"@eeeeeeeeeeeee\" : \"e\"}," +
                        "{\"@jjjjjjjjjjjjj\" : \"j\"}," +
                        "{\"@jjjjjjjjjjjjj\" : \"j\"}," +
                        "{\"@jjjjjjjjjjjjj\" : \"j\"}," +
                        "{\"@jjjjjjjjjjjjj\" : \"j\"}," +
                        "{\"@jjjjjjjjjjjjj\" : \"j\"}," +
                        "{\"@jjjjjjjjjjjjj\" : \"j\"}" +
                        "]})");
        long end = System.nanoTime();
        Assert.assertTrue(end-start<5*1000*1000*1000);
        assertSize(queryResult, 0);
    }

    @Test
    public void OrOptimizationWithSimpleCriterias() {
        long start = System.nanoTime();
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"$or\":[" +
                        "{\"path\" : \"test\"}," +
                        "{\"@aaaaaaaaaaaaa\" : \"test\"}," +
                        "{\"@bbbbbbbbbbbbb\" : \"file\"}," +
                        "{\"@bbbbbbbbbbbbb\" : \"file\"}," +
                        "{\"@bbbbbbbbbbbbb\" : \"file\"}," +
                        "{\"@bbbbbbbbbbbbb\" : \"file\"}," +
                        "{\"@bbbbbbbbbbbbb\" : \"file\"}," +
                        "{\"@bbbbbbbbbbbbb\" : \"file\"}," +
                        "{\"@bbbbbbbbbbbbb\" : \"file\"}," +
                        "{\"@bbbbbbbbbbbbb\" : \"file\"}," +
                        "{\"path\" : \"test\"}," +
                        "{\"@bbbbbbbbbbbbb\" : \"file\"}," +
                        "{\"@ccccccccccccc\" : \"c\"}," +
                        "{\"@ddddddddddddd\" : \"d\"}," +
                        "{\"@eeeeeeeeeeeee\" : \"e\"}," +
                        "{\"@jjjjjjjjjjjjj\" : \"j\"}," +
                        "{\"@jjjjjjjjjjjjj\" : \"j\"}," +
                        "{\"@jjjjjjjjjjjjj\" : \"j\"}," +
                        "{\"@jjjjjjjjjjjjj\" : \"j\"}," +
                        "{\"@jjjjjjjjjjjjj\" : \"j\"}," +
                        "{\"@jjjjjjjjjjjjj\" : \"j\"}," +
                        "{\"path\" : \"j\"}" +
                        "]})");
        long end = System.nanoTime();
        Assert.assertTrue(end - start < 5 * 1000 * 1000 * 1000);
        assertSize(queryResult, 0);
    }

    @Test
    public void derbyPreferMarchThanEqual() {
        long start = System.nanoTime();
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"$or\":[" +
                        "{\"path\" : \"test\"}," +
                        "{\"@aaaaaaaaaaaaa\" : \"test\"}," +
                        "{\"@bbbbbbbbbbbbb\" : \"file\"}," +
                        "{\"@bbbbbbbbbbbbb\" : \"file\"}," +
                        "{\"@bbbbbbbbbbbbb\" : \"file\"}," +
                        "{\"@bbbbbbbbbbbbb\" : \"file\"}," +
                        "{\"@bbbbbbbbbbbbb\" : \"file\"}," +
                        "{\"@bbbbbbbbbbbbb\" : \"file\"}," +
                        "{\"@bbbbbbbbbbbbb\" : \"file\"}," +
                        "{\"@bbbbbbbbbbbbb\" : \"file\"}," +
                        "{\"path\" : \"test\"}," +
                        "{\"@bbbbbbbbbbbbb\" : \"file\"}," +
                        "{\"@ccccccccccccc\" : \"c\"}," +
                        "{\"@ddddddddddddd\" : \"d\"}," +
                        "{\"@eeeeeeeeeeeee\" : \"e\"}," +
                        "{\"@jjjjjjjjjjjjj\" : \"j\"}," +
                        "{\"@jjjjjjjjjjjjj\" : \"j\"}," +
                        "{\"@jjjjjjjjjjjjj\" : \"j\"}," +
                        "{\"@jjjjjjjjjjjjj\" : \"j\"}," +
                        "{\"@jjjjjjjjjjjjj\" : \"j\"}," +
                        "{\"@jjjjjjjjjjjjj\" : \"j\"}," +
                        "{\"path\" : \"j\"}" +
                        "]})");
        long end = System.nanoTime();
        Assert.assertTrue(end - start < 5 * 1000 * 1000 * 1000);
        assertSize(queryResult, 0);
    }

}