package org.artifactory.storage.db.aql.service;

import org.artifactory.aql.result.AqlEagerResult;
import org.testng.annotations.Test;

/**
 * @author Gidi Shabat
 */
public class AqlStatsOrientedSearchesTest extends AqlAbstractServiceTest {

    ///*Statistics search*/
    @Test
    public void findStatistics() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "stats.find({\"item.repo\" :\"repo1\"},{\"downloads\":{\"$gt\":0}})");
        assertSize(queryResult, 2);
        assertStatistics(queryResult, 15, "yossis");
    }

}

