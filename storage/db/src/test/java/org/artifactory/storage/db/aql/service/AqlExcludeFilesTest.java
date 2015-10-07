package org.artifactory.storage.db.aql.service;

import org.artifactory.aql.result.AqlEagerResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Gidi Shabat
 */
public class AqlExcludeFilesTest extends AqlAbstractServiceTest {
    /**
     * Exclusion test: ensure that the result from exclusion plus opposite results should be equals to items  result
     */
    @Test
    public void test() {
        AqlEagerResult queryResult = aqlService.executeQueryEager("items.find()");
        int all = queryResult.getResults().size();
        queryResult = aqlService.executeQueryEager("items.find({\"property.value\" : {\"$match\" : \"*is is st*\"}})");
        int matchFilter = queryResult.getResults().size();
        queryResult = aqlService.executeQueryEager("items.find({\"property.value\" : {\"$nmatch\" : \"*is is st*\"}})");
        int notMatchFilter = queryResult.getResults().size();
        Assert.assertEquals(matchFilter + notMatchFilter, all, "Expecting that the result of two opposite criterias " +
                "(same criteria with opposite comparator exp equal and not equal) will be equal to al items result");
    }
}