package org.artifactory.storage.db.aql.api;

import org.artifactory.aql.AqlException;
import org.artifactory.aql.api.domain.sensitive.AqlApiArtifact;
import org.artifactory.aql.api.domain.sensitive.AqlApiBuild;
import org.artifactory.aql.api.domain.sensitive.AqlApiBuildProperty;
import org.artifactory.aql.api.domain.sensitive.AqlApiDependency;
import org.artifactory.aql.api.domain.sensitive.AqlApiItem;
import org.artifactory.aql.api.domain.sensitive.AqlApiProperty;
import org.artifactory.aql.api.domain.sensitive.AqlApiStatistic;
import org.artifactory.aql.model.AqlFieldEnum;
import org.artifactory.aql.model.AqlItemTypeEnum;
import org.artifactory.aql.result.AqlEagerResult;
import org.artifactory.aql.result.AqlJsonStreamer;
import org.artifactory.aql.result.AqlLazyResult;
import org.artifactory.aql.result.rows.AqlBaseItem;
import org.artifactory.aql.result.rows.AqlBuild;
import org.artifactory.aql.result.rows.AqlBuildArtifact;
import org.artifactory.aql.result.rows.AqlBuildDependency;
import org.artifactory.aql.result.rows.AqlBuildProperty;
import org.artifactory.aql.result.rows.AqlItem;
import org.artifactory.aql.result.rows.AqlProperty;
import org.artifactory.aql.result.rows.AqlStatistics;
import org.artifactory.storage.db.aql.service.AqlAbstractServiceTest.AdminPermissions;
import org.artifactory.storage.db.aql.service.AqlServiceImpl;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.artifactory.aql.api.internal.AqlBase.*;
import static org.artifactory.aql.model.AqlComparatorEnum.matches;
import static org.artifactory.aql.model.AqlFieldEnum.*;
import static org.artifactory.aql.model.AqlItemTypeEnum.file;
import static org.artifactory.aql.model.AqlItemTypeEnum.folder;
import static org.fest.assertions.Assertions.assertThat;
import static org.testng.Assert.assertTrue;

/**
 * @author Gidi Shabat
 */
public class AqlApiDomainSensitiveTest extends DbBaseTest {
    @Autowired
    private AqlServiceImpl aqlService;

    @BeforeClass
    public void setup() {
        importSql("/sql/aql_test.sql");
        ReflectionTestUtils.setField(aqlService, "permissionProvider", new AdminPermissions());
    }

    /*Artifacts search*/
    @Test
    public void findAllItemsTest() throws AqlException {
        AqlApiItem aql = AqlApiItem.create();
        AqlEagerResult<AqlItem> result = aqlService.executeQueryEager(aql);
        assertSize(result, 11);
    }

    @Test
    public void findAllSortedItemsTest() throws AqlException {
        AqlApiItem aql = AqlApiItem.create().sortBy(AqlFieldEnum.itemRepo);
        AqlEagerResult<AqlItem> result = aqlService.executeQueryEager(aql);
        assertSize(result, 11);
    }

    @Test
    public void findItemsWithSort() throws AqlException {
        AqlApiItem aql = AqlApiItem.create().
                filter(
                        AqlApiItem.path().matches("org*")
                ).
                sortBy(itemName, itemRepo).
                asc();
        AqlEagerResult<AqlItem> result = aqlService.executeQueryEager(aql);
        assertSize(result, 6);
        assertItemt(result, "repo-copy", "org/shayy/badmd5", "badmd5.jar", file);
    }

    @Test
    public void findItemsWithSha1AndMd5() throws AqlException {
        AqlApiItem aql = AqlApiItem.create().
                filter(
                        and(
                                AqlApiItem.sha1Actual().matches("*"),
                                AqlApiItem.sha1Orginal().matches("*"),
                                AqlApiItem.md5Actual().matches("*"),
                                AqlApiItem.md5Orginal().matches("*")
                        )
                ).
                sortBy(itemName, itemRepo).
                asc();
        AqlEagerResult<AqlItem> result = aqlService.executeQueryEager(aql);
        assertSize(result, 11);
        assertItemt(result, "repo1", "org/yossis/tools", "test.bin", file);
    }

    @Test
    public void findItemsWithOr() throws AqlException {
        AqlApiItem aql = AqlApiItem.create().
                filter(
                        or(
                                and(
                                        AqlApiItem.property().key().equal("yossia"),
                                        AqlApiItem.property().key().matches("*d")
                                ),
                                AqlApiItem.property().value().matches("ant")
                        )
                ).sortBy(itemName).desc();
        AqlEagerResult<AqlItem> result = aqlService.executeQueryEager(aql);
        assertSize(result, 1);
        assertItemt(result, "repo1", "ant/ant/1.5", "ant-1.5.jar", file);
    }

    @Test
    public void findItemsWithAndOr() throws AqlException {
        AqlApiItem aql = AqlApiItem.create().
                filter(

                        and(
                                AqlApiItem.type().equal("any"),
                                freezeJoin(
                                        AqlApiItem.property().key().equal("yossis"),
                                        AqlApiItem.property().value().matches("*ue1")
                                ),
                                or(
                                        AqlApiItem.property().value().matches("value1"),
                                        and(
                                                freezeJoin(
                                                        AqlApiItem.property().key().equal("yossis"),
                                                        AqlApiItem.property().value().matches("*df")
                                                )
                                        )
                                )
                        )
                );
        AqlEagerResult<AqlItem> result = aqlService.executeQueryEager(aql);
        assertSize(result, 1);
        assertItemt(result, "repo1", "org", "yossis", folder);
    }

    @Test
    public void findItemsWithAndOrUsingPropertyCriteriaClause() throws AqlException {
        AqlApiItem aql = AqlApiItem.create().
                filter(
                        and(
                                AqlApiItem.type().equal("any"),
                                AqlApiItem.property().property("yossis", matches, "*ue1"),
                                or(
                                        AqlApiItem.property().value().matches("value1"),
                                        AqlApiItem.property().property("yossis", matches, "*df")
                                )
                        )
                );
        AqlEagerResult<AqlItem> result = aqlService.executeQueryEager(aql);
        assertSize(result, 1);
        assertItemt(result, "repo1", "org", "yossis", folder);
    }

    @Test
    public void multipleCriteriaOnSamePropertyRow() throws AqlException {
        AqlApiItem aql = AqlApiItem.create().
                filter(
                        freezeJoin(
                                AqlApiItem.type().equal("any"),
                                AqlApiItem.property().key().matches("jun*"),
                                AqlApiItem.property().key().matches("*gle")
                        )
                ).sortBy(itemName, itemRepo);
        AqlEagerResult<AqlItem> result = aqlService.executeQueryEager(aql);
        assertSize(result, 1);
        assertItemt(result, "repo1", "org", "yossis", folder);
    }

    @Test
    public void findAllProperties() throws AqlException {
        AqlApiProperty aql = AqlApiProperty.create();
        AqlEagerResult<AqlProperty> result = aqlService.executeQueryEager(aql);
        assertSize(result, 9);
    }

    @Test
    public void findPropertiesWithFieldFilter() throws AqlException {
        // return only the properties with the key 'yossis' from repository 'repo1'
        AqlApiProperty aql = AqlApiProperty.create().
                filter(
                        and(
                                AqlApiProperty.item().repo().equal("repo1"),
                                propertyResultFilter(
                                        AqlApiProperty.key().equal("yossis")
                                )
                        )
                ).
                sortBy(propertyKey);
        AqlEagerResult<AqlProperty> result = aqlService.executeQueryEager(aql);
        assertSize(result, 2);
        assertProperty(result, "yossis", "pdf");
        assertProperty(result, "yossis", "value1");
    }

    @Test
    public void singleWildCardMaching() throws AqlException {
        // return only the properties with the key 'yossis' from repository 'repo1'
        AqlApiProperty aql = AqlApiProperty.create().
                filter(
                        and(
                                AqlApiProperty.item().repo().matches("rep?1"),
                                propertyResultFilter(
                                        AqlApiProperty.key().equal("yossis")
                                )
                        )
                ).
                sortBy(propertyKey);
        AqlEagerResult<AqlProperty> result = aqlService.executeQueryEager(aql);
        assertSize(result, 2);
        assertProperty(result, "yossis", "pdf");
        assertProperty(result, "yossis", "value1");
    }

    @Test(enabled = false)
    public void underscoreEscape() throws AqlException {
        // return only the properties with the key 'yossis' from repository 'repo1'
        AqlApiProperty aql = AqlApiProperty.create().
                filter(
                        and(
                                AqlApiProperty.item().repo().matches("rep_1"),
                                propertyResultFilter(
                                        AqlApiProperty.key().equal("yossis")
                                )
                        )
                ).
                sortBy(propertyKey);
        AqlEagerResult<AqlProperty> result = aqlService.executeQueryEager(aql);
        assertSize(result, 0);
    }

    @Test(enabled = true)
    public void nullUsage() throws AqlException {
        // return only the properties with the key 'yossis' from repository 'repo1'
        AqlApiItem aql = AqlApiItem.create().
                filter(
                        and(
                                AqlApiItem.type().equal("any"),
                                AqlApiItem.property().key().equal(null)
                        )
                ).
                include(AqlApiItem.property().key(), AqlApiItem.property().value()).
                sortBy(propertyKey).asc();
        AqlEagerResult<AqlItem> result = aqlService.executeQueryEager(aql);
        assertSize(result, 21);
    }

    @Test(enabled = true)
    public void notNullUsage() throws AqlException {
        // return only the properties with the key 'yossis' from repository 'repo1'
        AqlApiItem aql = AqlApiItem.create().
                filter(
                        and(
                                AqlApiItem.type().equal("any"),
                                AqlApiItem.property().key().notEquals((String) null)
                        )
                ).
                include(AqlApiItem.property().key(), AqlApiItem.property().value()).
                sortBy(propertyKey).asc();
        AqlEagerResult<AqlItem> result = aqlService.executeQueryEager(aql);
        assertSize(result, 31);
    }

    @Test(enabled = true)
    public void doNotHaveProperty() throws AqlException {
        // return only the properties with the key 'yossis' from repository 'repo1'
        AqlApiItem aql = AqlApiItem.create().
                filter(
                        and(
                                AqlApiItem.type().equal("any"),
                                AqlApiItem.property().key().notMatches("*")
                        )
                ).
                include(AqlApiItem.property().key(), AqlApiItem.property().value()).
                sortBy(propertyKey).asc();
        AqlEagerResult<AqlItem> result = aqlService.executeQueryEager(aql);
        assertSize(result, 21);
    }


    @Test(enabled = false)
    public void percentEscape() throws AqlException {
        // return only the properties with the key 'yossis' from repository 'repo1'
        AqlApiProperty aql = AqlApiProperty.create().
                filter(
                        and(
                                AqlApiProperty.item().repo().matches("rep%1"),
                                propertyResultFilter(
                                        AqlApiProperty.key().equal("yossis")
                                )
                        )
                ).
                sortBy(propertyKey);
        AqlEagerResult<AqlProperty> result = aqlService.executeQueryEager(aql);
        assertSize(result, 0);
    }

    @Test(enabled = true)
    public void queryWithDateTest() throws AqlException {
        // return only the properties with the key 'yossis' from repository 'repo1'
        AqlApiProperty aql = AqlApiProperty.create().
                filter(
                        AqlApiProperty.item().created().greater(new DateTime(0))
                ).
                sortBy(propertyKey);
        AqlEagerResult<AqlProperty> result = aqlService.executeQueryEager(aql);
        assertSize(result, 9);
    }


    @Test(enabled = true)
    public void lazyResultJsonInMemoryStream() throws AqlException, IOException {
        // return only the properties with the key 'yossis' from repository 'repo1'
        AqlApiItem item = AqlApiItem.create();
        item.filter(
                //or(
                AqlApiItem.artifact().module().build().name().matches("ba")
                //)
        );
        AqlLazyResult aqlLazyResult = aqlService.executeQueryLazy(item);
        AqlJsonStreamer aqlJsonStreamer = new AqlJsonStreamer(aqlLazyResult);

        byte[] read;
        StringBuilder builder=new StringBuilder();
        while((read= aqlJsonStreamer.read())!=null){
            builder.append(new String(read));
        }
        aqlJsonStreamer.close();
        String string = builder.toString();
        Assert.assertTrue(string.contains("\"size\" : 43434"));
        Assert.assertTrue(string.contains("\"repo\" : \"repo1\""));
        Assert.assertTrue(string.contains("\"actual_md5\" : \"302a360ecad98a34b59863c1e65bcf71\""));
        Assert.assertTrue(string.contains("\"created_by\" : \"yossis-1\""));
        Assert.assertTrue(string.contains("\"depth\" : 4"));
        Assert.assertTrue(string.contains("\"original_md5\" : \"302a360ecad98a34b59863c1e65bcf71\""));
        Assert.assertTrue(string.contains("\"actual_sha1\" : \"acab88fc2a043c2479a6de676a2f8179e9ea2167\""));
        Assert.assertTrue(string.contains("\"path\" : \"org/yossis/tools\""));
    }

    @Test(enabled = true)
    public void lazyResultStream() throws AqlException, IOException {
        // return only the properties with the key 'yossis' from repository 'repo1'
        AqlApiItem item = AqlApiItem.create();
        item.filter(
                //or(
                AqlApiItem.artifact().module().build().name().matches("ba")
                //)
        ).limit(2000);
        AqlLazyResult aqlLazyResult = aqlService.executeQueryLazy(item);
        AqlJsonStreamer streamResult = new AqlJsonStreamer(aqlLazyResult);

        byte[] read = streamResult.read();
        StringBuilder builder = new StringBuilder();
        while (read != null) {
            builder.append(new String(read));
            read = streamResult.read();
        }
        streamResult.close();
        String string = builder.toString();
        Assert.assertTrue(string.contains("\"size\" : 43434"));
        Assert.assertTrue(string.contains("\"repo\" : \"repo1\""));
        Assert.assertTrue(string.contains("\"actual_md5\" : \"302a360ecad98a34b59863c1e65bcf71\""));
        Assert.assertTrue(string.contains("\"created_by\" : \"yossis-1\""));
        Assert.assertTrue(string.contains("\"depth\" : 4"));
        Assert.assertTrue(string.contains("\"original_md5\" : \"302a360ecad98a34b59863c1e65bcf71\""));
        Assert.assertTrue(string.contains("\"actual_sha1\" : \"acab88fc2a043c2479a6de676a2f8179e9ea2167\""));
        Assert.assertTrue(string.contains("\"path\" : \"org/yossis/tools\""));
        Assert.assertTrue(string.contains("\"total\" : 1"));
        Assert.assertTrue(!string.contains("\"limit\" : 20000"));
    }

    @Test(enabled = true)
    public void lazyResultStreamWithLimit() throws AqlException, IOException {
        // return only the properties with the key 'yossis' from repository 'repo1'
        AqlApiItem item = AqlApiItem.create();
        item.filter(
                //or(
                AqlApiItem.artifact().module().build().name().matches("ba")
                //)
        ).limit(2);
        AqlLazyResult aqlLazyResult = aqlService.executeQueryLazy(item);
        AqlJsonStreamer streamResult = new AqlJsonStreamer(aqlLazyResult);

        byte[] read = streamResult.read();
        StringBuilder builder = new StringBuilder();
        while (read != null) {
            builder.append(new String(read));
            read = streamResult.read();
        }
        streamResult.close();
        String string = builder.toString();
        Assert.assertTrue(string.contains("\"size\" : 43434"));
        Assert.assertTrue(string.contains("\"repo\" : \"repo1\""));
        Assert.assertTrue(string.contains("\"actual_md5\" : \"302a360ecad98a34b59863c1e65bcf71\""));
        Assert.assertTrue(string.contains("\"created_by\" : \"yossis-1\""));
        Assert.assertTrue(string.contains("\"depth\" : 4"));
        Assert.assertTrue(string.contains("\"original_md5\" : \"302a360ecad98a34b59863c1e65bcf71\""));
        Assert.assertTrue(string.contains("\"actual_sha1\" : \"acab88fc2a043c2479a6de676a2f8179e9ea2167\""));
        Assert.assertTrue(string.contains("\"path\" : \"org/yossis/tools\""));
        Assert.assertTrue(string.contains("\"total\" : 1"));
        Assert.assertTrue(string.contains("\"limit\" : 2"));
    }

    @Test
    public void findArtifactsBiggerThan() throws AqlException {
        AqlApiItem aql = AqlApiItem.create().
                filter(
                        AqlApiItem.size().greater(43434)
                );
        AqlEagerResult<AqlItem> result = aqlService.executeQueryEager(aql);
        assertSize(result, 5);
        for (AqlItem artifact : result.getResults()) {
            assertThat(artifact.getSize()).isGreaterThan(43434);
        }
    }

    @Test
    public void findArtifactsBiggerThanWithLimit() throws AqlException {
        AqlApiItem aql = AqlApiItem.create().
                filter(
                        AqlApiItem.size().greater(43434)
                )
                .limit(2);
        AqlEagerResult<AqlItem> result = aqlService.executeQueryEager(aql);
        assertSize(result, 2);
        for (AqlItem artifact : result.getResults()) {
            assertThat(artifact.getSize()).isGreaterThan(43434);
        }
    }

    @Test
    public void findArtifactsBiggerThanWithOffset() throws AqlException {
        AqlApiItem aql = AqlApiItem.create().
                filter(
                        AqlApiItem.size().greater(43434)
                )
                .offset(4);
        AqlEagerResult<AqlItem> result = aqlService.executeQueryEager(aql);
        assertSize(result, 1);
        for (AqlItem artifact : result.getResults()) {
            assertThat(artifact.getSize()).isGreaterThan(43434);
        }
    }

    @Test
    public void findArtifactsBiggerThanWithOffsetAndLimit() throws AqlException {
        AqlApiItem aql = AqlApiItem.create().
                filter(
                        AqlApiItem.size().greater(43434)
                )
                .limit(1)
                .offset(1);
        AqlEagerResult<AqlItem> result = aqlService.executeQueryEager(aql);
        assertSize(result, 1);
        for (AqlItem artifact : result.getResults()) {
            assertThat(artifact.getSize()).isGreaterThan(43434);
        }
    }


    @Test
    public void findArtifactsUsinfInclude() throws AqlException {
        AqlApiItem aql = AqlApiItem.create().
                filter(
                        AqlApiItem.size().greater(43434)
                )
                .limit(2).include(AqlApiItem.created());
        AqlEagerResult<AqlItem> result = aqlService.executeQueryEager(aql);
        assertSize(result, 2);
        for (AqlItem artifact : result.getResults()) {
            assertThat(artifact.getSize()).isGreaterThan(43434);
        }
    }

    @Test
    public void usageOffTypeEqualAll() throws AqlException {
        AqlApiItem aql = AqlApiItem.create().
                filter(
                        AqlApiItem.type().equal("any")
                )
                .include(AqlApiItem.created());
        AqlEagerResult<AqlItem> result = aqlService.executeQueryEager(aql);
        assertSize(result, 26);
    }

    @Test
    public void findBuild() throws AqlException {
        AqlApiBuild aql = AqlApiBuild.create().
                filter(
                        AqlApiBuild.url().equal("http://myserver/jenkins/bb/1")
                );
        AqlEagerResult<AqlBuild> result = aqlService.executeQueryEager(aql);
        assertSize(result, 1);
        assertBuild(result, "1", "bb");
    }

    @Test
    public void findAqlApiBuildProperty() throws AqlException {
        AqlApiBuildProperty aql = AqlApiBuildProperty.create().
                filter(
                        AqlApiBuildProperty.value().matches("*")
                );
        AqlEagerResult<AqlBuildProperty> result = aqlService.executeQueryEager(aql);
        assertSize(result, 6);
        assertBuildProperty(result, "start", "0");
        assertBuildProperty(result, "start", "1");
        assertBuildProperty(result, "start", "4");
        assertBuildProperty(result, "status", "bad");
        assertBuildProperty(result, "status", "good");
        assertBuildProperty(result, "status", "not-too-bad");
    }

    @Test
    public void findAqlApiDependency() throws AqlException {
        AqlApiDependency aql = AqlApiDependency.create().
                filter(
                        AqlApiDependency.name().matches("*")
                );
        AqlEagerResult<AqlBuildDependency> result = aqlService.executeQueryEager(aql);
        assertSize(result, 5);
        assertDependency(result, "ba1mod3-art1", "dll");
    }

    @Test
    public void findAqlApiStatistics() throws AqlException {
        AqlApiStatistic aql = AqlApiStatistic.create().
                filter(
                        AqlApiStatistic.downloads().greater(1)
                );
        AqlEagerResult<AqlStatistics> result = aqlService.executeQueryEager(aql);
        assertSize(result, 2);
        assertStatistic(result, 9, "yossis");
        assertStatistic(result, 15, "yossis");
    }

    @Test
    public void findArtifact() throws AqlException {
        AqlApiArtifact aql = AqlApiArtifact.create().
                filter(
                        AqlApiArtifact.type().equal("dll")
                );
        AqlEagerResult<AqlBuildArtifact> result = aqlService.executeQueryEager(aql);
        assertSize(result, 5);
        assertArtifact(result, "ba1mod1-art1", "dll");
    }

    @Test
    public void findItemsUsingIncludeStatistics() throws AqlException {
        AqlApiItem aql = AqlApiItem.create().
                filter(
                        AqlApiItem.size().greater(43434)
                )
                .limit(2).include(AqlApiItem.created(), AqlApiItem.statistic().downloads());
        AqlEagerResult<AqlItem> result = aqlService.executeQueryEager(aql);
        assertSize(result, 2);
        for (AqlItem artifact : result.getResults()) {
            assertThat(artifact.getSize()).isGreaterThan(43434);
        }
    }


    private void assertSize(AqlEagerResult queryResult, int i) {
        Assert.assertEquals(queryResult.getSize(), i);
    }

    private void assertItemt(AqlEagerResult queryResult, String repo, String path, String name, AqlItemTypeEnum type) {
        boolean found = false;
        for (int j = 0; j < queryResult.getSize(); j++) {
            AqlBaseItem row = (AqlBaseItem) queryResult.getResult(j);
            if (row.getRepo().equals(repo) && row.getName().equals(name) &&
                    row.getPath().equals(path) && row.getType() == type) {
                found = true;
            }
        }
        assertTrue(found);
    }

    private void assertProperty(AqlEagerResult queryResult, String key, String value) {
        boolean found = false;
        for (int j = 0; j < queryResult.getSize(); j++) {
            AqlProperty row = (AqlProperty) queryResult.getResult(j);
            if (row.getKey().equals(key) &&
                    row.getValue().equals(value)) {
                found = true;
            }
        }
        assertTrue(found);
    }

    private void assertDependency(AqlEagerResult queryResult, String name, String type) {
        boolean found = false;
        for (int j = 0; j < queryResult.getSize(); j++) {
            AqlBuildDependency row = (AqlBuildDependency) queryResult.getResult(j);
            if (row.getBuildDependencyName().equals(name) &&
                    row.getBuildDependencyType().equals(type)) {
                found = true;
            }
        }
        assertTrue(found);
    }

    private void assertBuild(AqlEagerResult queryResult, String buildNumber, String buildName) {
        boolean found = false;
        for (int j = 0; j < queryResult.getSize(); j++) {
            AqlBuild row = (AqlBuild) queryResult.getResult(j);
            if (row.getBuildName().equals(buildName) &&
                    row.getBuildNumber().equals(buildNumber)) {
                found = true;
            }
        }
        assertTrue(found);
    }

    private void assertArtifact(AqlEagerResult queryResult, String name, String type) {
        boolean found = false;
        for (int j = 0; j < queryResult.getSize(); j++) {
            AqlBuildArtifact row = (AqlBuildArtifact) queryResult.getResult(j);
            if (row.getBuildArtifactName().equals(name) &&
                    row.getBuildArtifactType().equals(type)) {
                found = true;
            }
        }
        assertTrue(found);
    }

    private void assertStatistic(AqlEagerResult queryResult, int downloads, String downloadBy) {
        boolean found = false;
        for (int j = 0; j < queryResult.getSize(); j++) {
            AqlStatistics row = (AqlStatistics) queryResult.getResult(j);
            if (row.getDownloads() == downloads &&
                    row.getDownloadedBy().equals(downloadBy)) {
                found = true;
            }
        }
        assertTrue(found);
    }

    private void assertBuildProperty(AqlEagerResult queryResult, String key, String value) {
        boolean found = false;
        for (int j = 0; j < queryResult.getSize(); j++) {
            AqlBuildProperty row = (AqlBuildProperty) queryResult.getResult(j);
            if (row.getBuildPropKey().equals(key) &&
                    row.getBuildPropValue().equals(value)) {
                found = true;
            }
        }
        assertTrue(found);
    }
}