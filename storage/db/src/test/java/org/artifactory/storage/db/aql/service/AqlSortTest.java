package org.artifactory.storage.db.aql.service;

import org.artifactory.aql.result.AqlEagerResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Gidi Shabat
 */
public class AqlSortTest extends AqlAbstractServiceTest {

    @Test
    public void findBuildArtifactsSortAsc() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "artifacts.find().sort({\"$asc\" : [\"name\"]})");
        assertSize(queryResult, 5);
        assertBuildArtifacts(queryResult, "bb1mod3-art1", "dll");
        assertBuildArtifacts(queryResult, "bb1mod2-art1", "dll");
        assertBuildArtifacts(queryResult, "ba2mod4-art1", "dll");
        assertBuildArtifacts(queryResult, "ba1mod1-art2", "dll");
        assertBuildArtifacts(queryResult, "ba1mod1-art1", "dll");
    }

    @Test
    public void findBuildArtifactsSortDesc() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "artifacts.find().sort({\"$desc\" : [\"name\"]})");
        assertSize(queryResult, 5);
        assertBuildArtifacts(queryResult, "bb1mod3-art1", "dll");
        assertBuildArtifacts(queryResult, "bb1mod2-art1", "dll");
        assertBuildArtifacts(queryResult, "ba2mod4-art1", "dll");
        assertBuildArtifacts(queryResult, "ba1mod1-art2", "dll");
        assertBuildArtifacts(queryResult, "ba1mod1-art1", "dll");
    }

    @Test
    public void findBuildArtifactsWithExtraFieldsAndSorting() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "artifacts.find({\"item.repo\" : \"repo1\"}).sort({\"$desc\" : [\"name\"]})");
        assertSize(queryResult, 4);
        assertBuildArtifacts(queryResult, "bb1mod3-art1", "dll");
        assertBuildArtifacts(queryResult, "bb1mod2-art1", "dll");
        assertBuildArtifacts(queryResult, "ba2mod4-art1", "dll");
        assertBuildArtifacts(queryResult, "ba1mod1-art1", "dll");
    }

    /*complex string comparator */
    @Test
    public void sortWithcomplexStringComparator() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "artifacts.find({\"item.repo\" :{\"$match\" : \"r*p*1\"}}).sort({\"$desc\" : [\"name\"]})");
        assertSize(queryResult, 4);
        assertBuildArtifacts(queryResult, "bb1mod3-art1", "dll");
        assertBuildArtifacts(queryResult, "bb1mod2-art1", "dll");
        assertBuildArtifacts(queryResult, "ba2mod4-art1", "dll");
        assertBuildArtifacts(queryResult, "ba1mod1-art1", "dll");
    }


    @Test
    public void failOnSortThatContainsNoneResultField() {
        try {
            aqlService.executeQueryEager(
                    "items.find({\"repo\" : \"repo1\"}).sort({\"$desc\" : [\"artifact.module.build.number\", \"repo\"]})");
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains(
                    "Only the result fields are allowed to use in the sort section"));
        }
    }

    @Test
    public void rejectionSortByValue1() {
        try {
            aqlService.executeQueryEager(
                    "items.find({\"repo\" : \"repo1\"}).sort({\"$desc\" : [\"dsdsdsd\", \"repo\"]})");
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains(
                    "it looks like there is syntax error near the following sub-query: dsdsdsd"));
        }
    }


}
