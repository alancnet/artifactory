package org.artifactory.storage.db.aql.service;

import org.artifactory.aql.result.AqlEagerResult;
import org.artifactory.aql.result.rows.AqlBaseItem;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.fest.assertions.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;

/**
 * Tests items searching with AQL based mostly on properties filtering.
 *
 * @author Yossi Shaul
 */
@Test
public class AqlPropertyOrientedSearchesTest extends DbBaseTest {
    @Autowired
    private AqlServiceImpl aqlService;

    @BeforeClass
    public void setup() {
        importSql("/sql/aql_properties.sql");
        ReflectionTestUtils.setField(aqlService, "permissionProvider", new AqlAbstractServiceTest.AdminPermissions());
    }

    @Test
    public void simpleProperty() {
        AqlEagerResult results = aqlService.executeQueryEager(
                "items.find({\"@color\":{\"$eq\": \"red\"}})"
        );

        List<AqlBaseItem> items = results.getResults();
        assertEquals(items.size(), 1);
        assertEquals(items.get(0).getName(), "ant-1.5.jar");
    }

    @Test
    public void valueEquals() {
        AqlEagerResult results = aqlService.executeQueryEager(
                "items.find({\"@*\":{\"$eq\": \"red\"}})"
        );

        List<AqlBaseItem> items = results.getResults();
        assertEquals(items.size(), 1);
        assertEquals(items.get(0).getName(), "ant-1.5.jar");
    }

    @Test
    public void keyEquals() {
        AqlEagerResult results = aqlService.executeQueryEager(
                "items.find({\"@color\":{\"$eq\": \"*\"}}).sort({\"$asc\" : [\"name\"]})"
        );
        List<AqlBaseItem> items = results.getResults();
        assertEquals(items.size(), 2);
        assertEquals(items.get(0).getName(), "ant-1.5.jar");
    }

    @Test
    public void defaultKeyEquals() {
        AqlEagerResult results = aqlService.executeQueryEager(
                "items.find({\"@color\":\"*\"}).sort({\"$asc\" : [\"name\"]})"
        );
        List<AqlBaseItem> items = results.getResults();
        assertEquals(items.size(), 2);
        assertEquals(items.get(0).getName(), "ant-1.5.jar");
    }

    @Test
    public void defaultValueEquals() {
        AqlEagerResult results = aqlService.executeQueryEager(
                "items.find({\"@*\":\"red\"})"
        );
        List<AqlBaseItem> items = results.getResults();
        assertEquals(items.size(), 1);
        assertEquals(items.get(0).getName(), "ant-1.5.jar");
    }

    @Test
    public void noSuchPropertyKey() {
        AqlEagerResult results = aqlService.executeQueryEager(
                "items.find({\"@nosuchkey\":{\"$match\": \"*\"}})"
        );

        Assertions.assertThat(results.getResults()).isEmpty();
    }

    @Test
    public void noSuchPropertyValue() {
        AqlEagerResult results = aqlService.executeQueryEager(
                "items.find({\"@color\":{\"$match\": \"orange\"}})"
        );

        Assertions.assertThat(results.getResults()).isEmpty();
    }

    @Test
    public void propertyOnBothFolderAndFile() {
        AqlEagerResult results = aqlService.executeQueryEager(
                "items.find({\"type\" : \"any\",\"@color\":{\"$eq\": \"green\"}})"
        );

        assertEquals(results.getResults().size(), 2);
    }

    @Test
    public void propertyOnBothFolderAndFileFilerForFileOnly() {
        AqlEagerResult results = aqlService.executeQueryEager(
                "items.find({\"@color\":{\"$eq\": \"green\"}, \"type\": \"file\"})"
        );

        List<AqlBaseItem> items = results.getResults();
        assertEquals(items.size(), 1);
    }

    @Test
    public void propertyOrConditionOnTheValue() {
        AqlEagerResult results = aqlService.executeQueryEager(
                "items.find({" +
                        "\"$or\": [" +
                        "{\"@color\":{\"$eq\": \"red\"}}, " +
                        "{\"@color\":{\"$eq\": \"black\"}}]" +
                        "})"
        );

        List<AqlBaseItem> items = results.getResults();
        assertEquals(items.size(), 2);
    }

    @Test
    public void multiplePropertiesMatchesSameItem() {
        AqlEagerResult results = aqlService.executeQueryEager(
                "items.find({\"type\" : \"any\"," +
                        "\"$or\": [" +
                        "{\"@role\":{\"$eq\": \"manager\"}}, " +
                        "{\"@role\":{\"$match\": \"clean*\"}}]" +
                        "})"
        );

        List<AqlBaseItem> items = results.getResults();
        assertEquals(items.size(), 1);
        assertEquals(items.get(0).getName(), "org");
    }

    @Test
    public void propertyExclusion() {
        // this will return all the items, including items without properties and items that have any other property
        // or any other value for the role property
        AqlEagerResult results = aqlService.executeQueryEager(
                "items.find({\"type\" : \"any\",\"@role\":{\"$ne\": \"manager\"}})"
        );
        Assertions.assertThat(results.getResults()).hasSize(10);   // all items
    }

    @Test
    public void propertyWithCertainKeyExists() {
        AqlEagerResult results = aqlService.executeQueryEager(
                "items.find({\"type\" : \"any\",\"property.key\":{\"$eq\": \"color\"}})"
        );

        Assertions.assertThat(results.getResults()).hasSize(3);
    }

    @Test
    public void propertyMatchOnTheSamePropertyRow() {
        // without $smp the result will include the item with the LGPL license because it has another property
        AqlEagerResult results = aqlService.executeQueryEager(
                "items.find({\"$msp\": [" +
                        "{\"@license\":{\"$match\": \"*GPL\"}}, " +
                        "{\"@license\":{\"$ne\": \"LGPL\"}}" +
                        "]})"
        );

        List<AqlBaseItem> items = results.getResults();
        Assertions.assertThat(items).hasSize(1);
        Assertions.assertThat(("ant-1.5.jar").equals(items.get(0).getName()));
    }

    @Test
    public void propertyMatchWithValueNull() {
        // without $smp the result will include the item with the LGPL license because it has another property
        AqlEagerResult results = aqlService.executeQueryEager(
                "items.find({\"@license\":{\"$match\": null}})"
        );

        List<AqlBaseItem> items = results.getResults();
        Assertions.assertThat(items).hasSize(0);
    }
}
