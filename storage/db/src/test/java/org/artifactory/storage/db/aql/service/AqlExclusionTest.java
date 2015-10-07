package org.artifactory.storage.db.aql.service;

import org.artifactory.aql.result.AqlEagerResult;
import org.artifactory.aql.result.rows.AqlBaseItem;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;

/**
 * @author Gidi Shabat
 */
public class AqlExclusionTest extends DbBaseTest {
    @Autowired
    private AqlServiceImpl aqlService;

    @BeforeClass
    public void setup() {
        importSql("/sql/aql_exclusion.sql");
        ReflectionTestUtils.setField(aqlService, "permissionProvider", new AqlAbstractServiceTest.AdminPermissions());
    }

    @Test
    public void simpleExclude1() {
        AqlEagerResult results = aqlService.executeQueryEager(
                "items.find({\"@license\":{\"$ne\": \"GPL\"}})"
        );
        List<AqlBaseItem> items = results.getResults();
        assertEquals(items.size(), 1);
        assertEquals(items.get(0).getName(), "b");
    }

    @Test
    public void simpleExclude2() {
        AqlEagerResult results = aqlService.executeQueryEager(
                "items.find({\"@license\":{\"$ne\": \"LGPL\"}}).sort({\"$asc\":[\"name\"]})"
        );
        List<AqlBaseItem> items = results.getResults();
        assertEquals(items.size(), 3);
        assertEquals(items.get(0).getName(), "a");
        assertEquals(items.get(1).getName(), "c");
        assertEquals(items.get(2).getName(), "d");
    }

    @Test
    public void simpleExclude3() {
        AqlEagerResult results = aqlService.executeQueryEager(
                "items.find({\"property.key\":{\"$ne\": \"license\"}}).sort({\"$asc\":[\"name\"]})"
        );
        List<AqlBaseItem> items = results.getResults();
        assertEquals(items.size(), 0);
    }

    @Test
    public void simpleExclude4() {
        AqlEagerResult results = aqlService.executeQueryEager(
                "items.find({\"property.value\":{\"$ne\": \"1.1.2\"}}).sort({\"$asc\":[\"name\"]})"
        );
        List<AqlBaseItem> items = results.getResults();
        assertEquals(items.size(), 3);
        assertEquals(items.get(0).getName(), "a");
        assertEquals(items.get(1).getName(), "b");
        assertEquals(items.get(2).getName(), "c");
    }

    @Test
    public void simpleExclude5() {
        AqlEagerResult results = aqlService.executeQueryEager(
                "items.find({\"@version\":{\"$ne\": \"1.1.1\"}}).sort({\"$asc\":[\"name\"]})"
        );
        List<AqlBaseItem> items = results.getResults();
        assertEquals(items.size(), 1);
        assertEquals(items.get(0).getName(), "d");
    }
}
