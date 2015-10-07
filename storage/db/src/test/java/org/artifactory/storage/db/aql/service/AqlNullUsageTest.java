package org.artifactory.storage.db.aql.service;

import org.artifactory.aql.result.AqlEagerResult;
import org.testng.annotations.Test;

/**
 * @author Gidi Shabat
 */
public class AqlNullUsageTest extends AqlAbstractServiceTest {

    @Test
    public void findNullStatistics() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"repo\" :\"repo1\",\"stat.downloads\":{\"$eq\":null}})");
        assertSize(queryResult, 3);
    }

    @Test
    public void findNullProperties() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"type\" :\"any\",\"property.key\":{\"$eq\":null}})");
        assertSize(queryResult, 21);
    }

    @Test
    public void findNotStarProperties() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"type\" :\"any\",\"property.key\":{\"$nmatch\":\"*\"}})");
        assertSize(queryResult, 21);
    }

    @Test
    public void findNotAntProperties() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"type\" :\"any\",\"property.key\":{\"$Nmatch\":\"yossis\"}})");
        assertSize(queryResult, 24);
    }
}
