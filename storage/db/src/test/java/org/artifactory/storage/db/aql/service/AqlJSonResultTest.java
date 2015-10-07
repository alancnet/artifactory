package org.artifactory.storage.db.aql.service;

import com.beust.jcommander.internal.Sets;
import org.artifactory.aql.result.AqlJsonStreamer;
import org.artifactory.aql.result.AqlLazyResult;
import org.artifactory.util.ResourceUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Gidi Shabat
 */
@Test
public class AqlJSonResultTest extends AqlAbstractServiceTest {

    @Test
    public void findBuildWithItemsAndItemProperties() throws IOException {
        AqlLazyResult aqlLazyResult = aqlService.executeQueryLazy(
                "builds.find().include(\"module.dependency.item.*\",\"module.dependency.item.property.*\")");
        AqlJsonStreamer aqlStreamer = new AqlJsonStreamer(aqlLazyResult);
        String result = read(aqlStreamer);
        aqlStreamer.close();
        String expectation = load("/aql/stream/buildsWithItems.json");
        compareJsons(result, expectation);
    }

    @Test
    public void findBuildWithArtifacts() throws IOException {
        AqlLazyResult aqlLazyResult = aqlService.executeQueryLazy(
                "builds.find().include(\"module.dependency.*\")");
        AqlJsonStreamer aqlStreamer = new AqlJsonStreamer(aqlLazyResult);
        String result = read(aqlStreamer);
        aqlStreamer.close();
        String expectation = load("/aql/stream/buildsWithArtifacts.json");
        compareJsons(result, expectation);
    }

    @Test
    public void findBuildWithDependencies() throws IOException {
        AqlLazyResult aqlLazyResult = aqlService.executeQueryLazy(
                "builds.find().include(\"module.dependency.*\")");
        AqlJsonStreamer aqlStreamer = new AqlJsonStreamer(aqlLazyResult);
        String result = read(aqlStreamer);
        aqlStreamer.close();
        String expectation = load("/aql/stream/buildsWithDependency.json");
        compareJsons(result, expectation);
    }

    @Test
    public void findBuildWithModules() throws IOException {
        AqlLazyResult aqlLazyResult = aqlService.executeQueryLazy(
                "builds.find().include(\"module.*\")");
        AqlJsonStreamer aqlStreamer = new AqlJsonStreamer(aqlLazyResult);
        String result = read(aqlStreamer);
        aqlStreamer.close();
        String expectation = load("/aql/stream/buildsWithModules.json");
        compareJsons(result, expectation);
    }

    @Test
    public void itemsWithProperties() throws IOException {
        AqlLazyResult aqlLazyResult = aqlService.executeQueryLazy(
                "items.find().include(\"property.*\")");
        AqlJsonStreamer aqlStreamer = new AqlJsonStreamer(aqlLazyResult);
        String result = read(aqlStreamer);
        aqlStreamer.close();
        String expectation = load("/aql/stream/itemsWithProperties.json");
        compareJsons(result, expectation);
    }

    @Test
    public void buildWithProperties() throws IOException {
        AqlLazyResult aqlLazyResult = aqlService.executeQueryLazy(
                "builds.find().include(\"property.*\")");
        AqlJsonStreamer aqlStreamer = new AqlJsonStreamer(aqlLazyResult);
        String result = read(aqlStreamer);
        aqlStreamer.close();
        String expectation = load("/aql/stream/buildsWithProperties.json");
        compareJsons(result, expectation);
    }

    @Test
    public void modulesWithProperties() throws IOException {
        AqlLazyResult aqlLazyResult = aqlService.executeQueryLazy(
                "modules.find().include(\"property.*\")");
        AqlJsonStreamer aqlStreamer = new AqlJsonStreamer(aqlLazyResult);
        String result = read(aqlStreamer);
        aqlStreamer.close();
        String expectation = load("/aql/stream/modulesWithProperties.json");
        compareJsons(result, expectation);
    }

    @Test
    public void dependenciesWithStats() throws IOException {
        AqlLazyResult aqlLazyResult = aqlService.executeQueryLazy(
                "dependencies.find().include(\"item.stat.*\")");
        AqlJsonStreamer aqlStreamer = new AqlJsonStreamer(aqlLazyResult);
        String result = read(aqlStreamer);
        aqlStreamer.close();
        String expectation = load("/aql/stream/dependenciesWithStats.json");
        compareJsons(result, expectation);
    }

    @Test
    public void items() throws IOException {
        AqlLazyResult aqlLazyResult = aqlService.executeQueryLazy(
                "items.find()");
        AqlJsonStreamer aqlStreamer = new AqlJsonStreamer(aqlLazyResult);
        String result = read(aqlStreamer);
        aqlStreamer.close();
        String expectation = load("/aql/stream/dependenciesWithStats.json");
        compareJsons(result, expectation);
    }

    @Test
    public void itemsWithPropertiesStatisticAndArchive() throws IOException {
        AqlLazyResult aqlLazyResult = aqlService.executeQueryLazy(
                "items.find().include(\"stat.*\",\"property.*\",\"archive.*\")");
        AqlJsonStreamer aqlStreamer = new AqlJsonStreamer(aqlLazyResult);
        String result = read(aqlStreamer);
        aqlStreamer.close();
        String expectation = load("/aql/stream/itemWithPropertiesStatisticsAndArchive.json");
        compareJsons(result, expectation);
    }

    @Test
    public void propertiesWithItems() throws IOException {
        AqlLazyResult aqlLazyResult = aqlService.executeQueryLazy(
                "properties.find().include(\"item.*\")");
        AqlJsonStreamer aqlStreamer = new AqlJsonStreamer(aqlLazyResult);
        String result = read(aqlStreamer);
        aqlStreamer.close();
        String expectation = load("/aql/stream/propertiesWithItems.json");
        compareJsons(result, expectation);
    }

    private void compareJsons(String result, String expectation) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Map resultMap = mapper.readValue(result, Map.class);
        Map expectedMap = mapper.readValue(expectation, Map.class);
        assertMapEquals(resultMap, expectedMap);
    }

    private void assertMapEquals(Map resultMap, Map expectedMap) {
        Assert.assertTrue(contains(resultMap, expectedMap) && contains(expectedMap, resultMap));
    }

    private boolean contains(Map resultMap, Map expectedMap) {
        for (Object o : resultMap.keySet()) {
            Object result = resultMap.get(o);
            Object expected = expectedMap.get(o);
            if (result instanceof Map) {
                contains((Map) result, (Map) expected);
            } else if (result instanceof List) {

                Set<Object> resultSet = Sets.newHashSet();
                resultSet.addAll((List) result);
                Set<Object> expectedSet = Sets.newHashSet();
                resultSet.addAll((List) expected);
                contains(resultSet, expectedSet);
            } else if (!result.equals(expected)) {
                return false;
            }
        }
        return true;
    }

    private boolean contains(Set resultList, Set expectedList) {
        for (Object o : resultList) {
            if(!expectedList.contains(o)){
                return false;
            }
        }
        for (Object o : expectedList) {
            if(!expectedList.contains(o)){
                return false;
            }
        }
        return true;
    }

    private String load(String fileName) {
        return ResourceUtils.getResourceAsString(fileName);
    }

    private String read(AqlJsonStreamer aqlStreamer) throws IOException {
        StringBuilder builder = new StringBuilder();
        byte[] tempResult;
        while ((tempResult = aqlStreamer.read()) != null) {
            builder.append(new String(tempResult));
        }
        return builder.toString();
    }
}


