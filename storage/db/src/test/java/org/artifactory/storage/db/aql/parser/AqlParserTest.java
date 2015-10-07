package org.artifactory.storage.db.aql.parser;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Gidi Shabat
 */
@Test
public class AqlParserTest {
    @Test
    public void successParse() throws Exception {
        AqlParser sm = new AqlParser();
        assertValid(sm, "items.find({\"repo\":{\"$match\":\"jc*\"}})");
        assertValid(sm, "items.find({\"@license\":{\"$eq\":\"GPL\"}})");
        assertValid(sm, "items.find({\"@license\":{\"$eq\":\"GPL\"}})");
        assertValid(sm,
                "items.find({\"@license\":{\"$eq\":\"GPL\"},\"@license\":{\"$eq\":\"GPL\"},\"$or\":[{\"@license\":{\"$eq\":\"GPL\"}}]})");
        assertValid(sm,
                "items.find({\"@license\":{\"$eq\":\"GPL\"},\"@license\":{\"$lt\":\"GPL\"},\"$or\":[{\"@license\":{\"$eq\":\"GPL\"},\"@license\":{\"$eq\":\"GPL\"}}]})");
        assertValid(sm,
                "items.find({\"@license\":{\"$eq\":\"GPL\"},\"$or\":[{\"@license\":{\"$eq\":\"GPL\"},\"@license\":{\"$eq\":\"GPL\"},\"@license\":{\"$eq\":\"GPL\"}}]})");
        assertValid(sm, "items.find({\"$or\":[{\"@license\":\"GPL\",\"@license\":\"GPL\"}]})");
        assertValid(sm,
                "items.find({\"$or\":[{\"repo\":\"jcentral\"},{\"type\":1},{\"$or\":[{\"$or\":[{\"@version\":\"1,1,1\"},{\"type\":1}]},{\"@version\":\"2.2.2\"}]}]})");
        assertValid(sm, "items.find({\"$msp\":[{\"repo\":\"jcentral\"},{\"type\":1}]})");
        assertValid(sm, "items.find({\"$msp\":[{\"repo\":\"jcentral\",\"type\":1}]})");
        assertValid(sm, "items.find({\"repo\":\"jcentral\",\"type\":1})");
        assertValid(sm, "items.find({\"repo\":\"jcentral\"},{\"type\":1})");
        assertValid(sm, "items.find({\"$msp\":[{\"repo\":\"jcentral\"},{\"type\":1}]})");
        assertValid(sm,
                "items.find({\"$msp\":[{\"repo\":\"jcentral\"},{\"type\":1}]}).sort({\"$asc\" : [\"name\", \"repo\" ]})");
        assertValid(sm, "items.find({\"@*\" : {\"$eq\" : \"1.1*\"}})");
        assertValid(sm, "items.find({\"@ver*\" : {\"$eq\" : \"*\" }})");
        assertValid(sm, "items.find({\"@ver*\" : {\"$eq\" : \"*\"}})");
        assertValid(sm, "items.find()");
        assertValid(sm, "items.find({\"@*\" : {\"$eq\" : \"1.1*\"}}).limit(10)");
        assertValid(sm, "items.find({\"archive.entry_name\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertValid(sm, "items.find({\"artifact.module.name\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertValid(sm, "items.find({\"artifact.module.build.name\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertValid(sm, "builds.find({\"module.dependency.name\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertValid(sm, "builds.find({\"module.dependency.item.repo\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertValid(sm, "builds.find({\"module.dependency.item.name\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertValid(sm, "modules.find({\"name\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertValid(sm, "modules.find({\"artifact.item.name\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertValid(sm, "modules.find({\"artifact.item.@test\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertValid(sm, "modules.find({\"build.number\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertValid(sm, "dependencies.find({\"item.name\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertValid(sm, "dependencies.find({\"scope\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertValid(sm, "dependencies.find({\"module.name\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertValid(sm, "build.properties.find({\"key\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertValid(sm, "build.properties.find({\"build.number\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertValid(sm, "build.properties.find({\"value\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertValid(sm, "properties.find({\"item.repo\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertValid(sm, "items.find({\"stat.downloads\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertValid(sm, "artifacts.find({\"type\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertValid(sm, "artifacts.find({\"module.name\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertValid(sm, "artifacts.find({\"item.path\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertValid(sm, "builds.find({\"module.artifact.item.@path\" : {\"$eq\" : \"a.a\"}}).limit(10)");

        assertValid(sm, "items.find({\"path\" : {\"$eq\" : \"a.a\"},\"path\" :\"path\" }).limit(10)");
        assertValid(sm, "items.find({\"path\" : {\"$eq\" : \"a.a\"},\"@path\" :\"path\" }).limit(10)");
        assertValid(sm,
                "items.find({\"path\" : {\"$eq\" : \"a.a\"},\"@path\" :\"path\" }).include(\"repo\",\"archive.entry_name\").limit(10)");
        assertValid(sm,
                "items.find({\"path\" : {\"$eq\" : \"a.a\"},\"@path\" :\"path\" }).include(\"@repo\",\"property.value\").limit(10)");
        assertValid(sm,
                "items.find({\"path\" : {\"$eq\" : \"a.a\"},\"@path\" :\"path\" }).include(\"repo\",\"@repo\",\"property.value\").limit(10)");
        assertValid(sm,
                "items.find({\"path\" : {\"$eq\" : \"a.a\"},\"@path\" :\"path\" }).include(\"archive.entry_name\").limit(10)");
        assertValid(sm, "items.find({\"@*\" : {\"$eq\" : \"1.1*\"}}).limit(10)");
        assertValid(sm, "items.find({\"@*\" : {\"$eq\" : null}}).limit(10)");
        assertValid(sm, "items.find({\"@*\" : {\"$eq\" : null}}).offset(145).limit(10)");
        assertValid(sm, "items.find({\"@*\" : {\"$eq\" : \"test\"}}).offset(145).limit(10)");
        assertValid(sm, "builds.find({\"module.artifact.item.@*\" : {\"$eq\" : \"test\"}}).offset(145).limit(10)");
        assertValid(sm, "builds.find({\"module.artifact.item.@key\" : {\"$eq\" : \"*\"}}).offset(145).limit(10)");
        assertValid(sm, "build.properties.find({\"build.module.artifact.item.@key\" : {\"$eq\" : \"*\"}}).offset(145).limit(10)");
        assertValid(sm, "build.properties.find({\"module.artifact.item.@key\" : {\"$eq\" : \"*\"}}).offset(145).limit(10)");
        assertValid(sm, "build.properties.find({\"@key\" : {\"$eq\" : \"*\"}}).offset(145).limit(10)");
        assertValid(sm, "build.properties.find({\"@*\" : {\"$eq\" : \"*\"}}).offset(145).limit(10)");
        assertValid(sm, "build.properties.find({\"@*\" : {\"$eq\" : \"sdsdsdsd*\"}}).offset(145).limit(10)");
        assertValid(sm, "module.properties.find({\"module.artifact.item.@key\" : {\"$eq\" : \"*\"}}).offset(145).limit(10)");
        assertValid(sm, "module.properties.find({\"@key\" : {\"$eq\" : \"*\"}}).offset(145).limit(10)");
        assertValid(sm, "module.properties.find({\"*\" : {\"$eq\" : \"*\"}}).offset(145).limit(10)");
        assertValid(sm, "module.properties.find({\"*\" : {\"$eq\" : \"sdsdsdsd*\"}}).offset(145).limit(10)");
        assertValid(sm, "modules.find({\"@*\" : {\"$eq\" : \"sdsdsdsd*\"}}).offset(145).limit(10)");
        assertValid(sm, "build.properties.find({\"module.number\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertValid(sm, "build.properties.find({\"repo\" : {\"$eq\" : \"a.a\"}}).limit(10)");

    }

    @Test
    public void failureOnParse() throws Exception {
        AqlParser sm = new AqlParser();
        assertInValid(sm, "artifacts({\"$names\" : [}).find({\"ver*\" : \"$eq\"})");
        assertInValid(sm, "artifacts({\"$names\").find({\"ver*\" : \"$eq\"})");
        assertInValid(sm, "artifacts().find({\"ver*\" : \"$eq\"})");
        assertInValid(sm, "artifacts().find({\"blabla\" : \"jjhj\"})");
        assertInValid(sm, "builds.find({\"module.dependency.nameg\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertInValid(sm, "modules.find({\"repo\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertInValid(sm, "dependencies.find({\"repo\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertInValid(sm, "items.find({\"downloads\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertInValid(sm, "artifacts.find({\"module.type\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertInValid(sm, "artifacts.find({\"module\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertInValid(sm, "artifacts.find({\"module.\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertInValid(sm, "items.find({\"property.item.@*\" : {\"$eq\" : \"1.1*\"}}).limit(10)");
        assertInValid(sm, "items.find().include(\"repo3\").limit(10)");
        assertInValid(sm, "items.find().include(\"repo3\").offset(45)limit(10)");
        assertInValid(sm, "items.find().include(\"repo3\").offser(45).limit(10)");
    }

    private void assertValid(AqlParser sm, String script) throws Exception {
        ParserElementResultContainer parse = sm.parse(script);
        System.out.println(parse);
    }

    private void assertInValid(AqlParser sm, String script) throws Exception {
        try {
            sm.parse(script);
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
    }
}
