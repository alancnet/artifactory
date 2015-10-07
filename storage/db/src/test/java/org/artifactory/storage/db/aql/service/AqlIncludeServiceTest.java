package org.artifactory.storage.db.aql.service;

import org.artifactory.aql.result.AqlEagerResult;
import org.artifactory.aql.result.rows.AqlBaseFullRowImpl;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.artifactory.aql.model.AqlItemTypeEnum.file;

/**
 * @author Gidi Shabat
 */
public class AqlIncludeServiceTest extends AqlAbstractServiceTest {
    /**
     * Add extra field from other domain and add result filter on the property key
     */
    @Test
    public void includeWithExtraFieldFromOtherDomainAndPropertyKeyFilter() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "properties.find({\"value\" : {\"$match\" : \"*is is st*\"}}).include(\"item.name\",\"string\")");
        assertSize(queryResult, 1);
        assertProperty(queryResult, "string", "this is string");
    }

    /**
     * Override the default fields by adding extra field that belong to the main domain
     */
    @Test
    public void includeWithExtraFieldFromSameDomainAndPropertyKeyFilter() {
        // Should remove the default fields and add only the fields from the include and property filter
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"property.value\" : {\"$match\" : \"*is is st*\"}}).include(\"name\",\"@string\")");
        assertSize(queryResult, 1);
        String repo = ((AqlBaseFullRowImpl) queryResult.getResults().get(0)).getRepo();
        Assert.assertNull(repo);
    }

    /**
     * return result that contains all the field in the domain
     */
    @Test
    public void includeExpendDomain() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"property.value\" : {\"$match\" : \"*is is st*\"}}).include(\"*\",\"@string\")");
        assertSize(queryResult, 1);
        String actualMd5 = ((AqlBaseFullRowImpl) queryResult.getResults().get(0)).getActualMd5();
        Assert.assertEquals(actualMd5, "902a360ecad98a34b59863c1e65bcf71");
    }

    /**
     * return result that contains all the field in the domain
     */
    @Test
    public void includeDomainWithoutAsterisk() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"property.value\" : {\"$match\" : \"*is is st*\"}}).include(\"stat\")");
        assertSize(queryResult, 1);
        int downloads = ((AqlBaseFullRowImpl) queryResult.getResults().get(0)).getDownloads();
        Assert.assertEquals(downloads, 9);
    }

    /**
     * return result that contains all the domain's default fields and the build fields
     */
    @Test
    public void includeExtraDomain() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"artifact.module.build.number\" : {\"$match\" : \"2\"}}).include(\"artifact.module.build.*\")");
        assertSize(queryResult, 1);
        String buildNumber = ((AqlBaseFullRowImpl) queryResult.getResults().get(0)).getBuildNumber();
        String buildName = ((AqlBaseFullRowImpl) queryResult.getResults().get(0)).getBuildName();
        Assert.assertEquals(buildNumber, "2");
        Assert.assertEquals(buildName, "ba");
    }

    /**
     * return result that contains all the domain's default fields and extra fields
     */
    @Test
    public void findPropertiesUsingNamesExtensionWithOneExtension() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "properties.find({\"value\" : {\"$match\" : \"*is is st*\"}}).include(\"item.name\")");
        assertSize(queryResult, 3);

        AqlBaseFullRowImpl row = (AqlBaseFullRowImpl) queryResult.getResults().get(0);
        // Make sure that the extra field (item.name)is not null;
        Assert.assertTrue(row.getName() != null);
        Assert.assertTrue(row.getPath() == null);
        assertProperty(queryResult, "build.name", "ant");
    }

    /**
     * return result that contains all the domain's default fields and two extra fields
     */
    @Test
    public void findPropertiesUsingNamesExtensionWithTwoExtensions() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "properties.find({\"value\" : {\"$match\" : \"*is is st*\"}}).include(\"item.name\",\"item.path\")");
        assertSize(queryResult, 3);

        AqlBaseFullRowImpl row = (AqlBaseFullRowImpl) queryResult.getResults().get(0);
        // Make sure that the extra field (item.name)is not null;
        Assert.assertTrue(row.getName() != null);
        Assert.assertTrue(row.getPath() != null);
        assertProperty(queryResult, "build.name", "ant");
    }

    /**
     * Test the sort mechanism with the include mechanism
     */
    @Test
    public void ensureSortOnIncludedFields() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "properties.find({\"value\" : {\"$match\" : \"*is is st*\"}})." +
                        "include(\"item.name\",\"item.path\",\"item.size\")." +
                        "sort({\"$asc\": [\"item.size\"]})");
        assertSize(queryResult, 3);

        AqlBaseFullRowImpl row = (AqlBaseFullRowImpl) queryResult.getResults().get(0);
        // Make sure that the extra field (item.name)is not null;
        Assert.assertTrue(row.getName() != null);
        Assert.assertTrue(row.getPath() != null);
        assertProperty(queryResult, "build.name", "ant");
    }

    /**
     * Test the sort mechanism with the include domain mechanism
     */
    @Test
    public void ensureSortOnIncludedDomain() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"artifact.module.build.number\" : {\"$match\" : \"2\"}}).include(\"property.*\").sort({\"$asc\" : [\"property.key\"]})");
        assertSize(queryResult, 1);
        AqlBaseFullRowImpl row = (AqlBaseFullRowImpl) queryResult.getResults().get(0);
        // Make sure that the extra field (item.name)is not null;
        Assert.assertTrue(row.getKey() != null);
        Assert.assertTrue(row.getValue() != null);
        assertItem(queryResult, "repo1", "org/yossis/tools", "test.bin", file);
    }

    /**
     * Test the sort mechanism with the include domain mechanism
     */
    @Test
    public void includePropertiesWithAtAndAsterisk() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"artifact.module.build.number\" : {\"$match\" : \"2\"}})." +
                        "include(\"@*\").sort({\"$asc\" : [\"property.key\"]})");
        assertSize(queryResult, 1);
        AqlBaseFullRowImpl row = (AqlBaseFullRowImpl) queryResult.getResults().get(0);
        // Make sure that the extra field (item.name)is not null;
        Assert.assertTrue(row.getKey() != null);
        Assert.assertTrue(row.getValue() != null);
        assertItem(queryResult, "repo1", "org/yossis/tools", "test.bin", file);
    }

    /**
     * Test the sort mechanism with the include domain mechanism
     */
    @Test
    public void multipleResultFilter() {
        AqlEagerResult<AqlBaseFullRowImpl> queryResult = aqlService.executeQueryEager(
                "items.find()." +
                        "include(\"@build.name\",\"@build.number\")");
        assertSize(queryResult, 2);
        for (AqlBaseFullRowImpl row : queryResult.getResults()) {
            Assert.assertTrue("build.number".equals(row.getKey()) || "build.name".equals(row.getKey()));
        }
    }
}
