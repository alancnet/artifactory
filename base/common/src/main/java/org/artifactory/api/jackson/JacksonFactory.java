/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2012 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.artifactory.api.jackson;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.introspect.Annotated;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;

/**
 * Jackson generator factory class
 *
 * @author Noam Tenne
 */
public abstract class JacksonFactory {
    private JacksonFactory() {
        // utility class
    }

    /**
     * Creates a JsonGenerator using the given output stream as a writer
     *
     * @param outputStream Stream to write to
     * @return Json Generator
     * @throws IOException
     */
    public static JsonGenerator createJsonGenerator(OutputStream outputStream) throws IOException {
        JsonFactory jsonFactory = getFactory();
        JsonGenerator jsonGenerator = jsonFactory.createJsonGenerator(outputStream, JsonEncoding.UTF8);
        updateGenerator(jsonFactory, jsonGenerator);
        return jsonGenerator;
    }

    /**
     * Creates a JsonGenerator using the given writer
     *
     * @param writer Stream to write to
     * @return Json Generator
     * @throws IOException
     */
    public static JsonGenerator createJsonGenerator(Writer writer) throws IOException {
        JsonFactory jsonFactory = getFactory();
        JsonGenerator jsonGenerator = jsonFactory.createJsonGenerator(writer);
        updateGenerator(jsonFactory, jsonGenerator);
        return jsonGenerator;
    }

    /**
     * Creates a JsonParser using the given input stream as a reader.<BR>
     * Please do not use this method directly for simple parsing tasks, instead use {@link JacksonReader}.
     *
     * @param inputStream Stream to read from
     * @return Json Parser
     * @throws IOException
     */
    public static JsonParser createJsonParser(InputStream inputStream) throws IOException {
        JsonFactory jsonFactory = getFactory();
        JsonParser jsonParser = jsonFactory.createJsonParser(inputStream);
        updateParser(jsonFactory, jsonParser);
        return jsonParser;
    }


    /**
     * Creates a JsonParser using the given byte[] as a reader.<BR>
     * Please do not use this method directly for simple parsing tasks, instead use {@link JacksonReader}.
     *
     * @param input Input to read from
     * @return Json Parser
     * @throws IOException
     */
    public static JsonParser createJsonParser(byte[] input) throws IOException {
        JsonFactory jsonFactory = getFactory();
        JsonParser jsonParser = jsonFactory.createJsonParser(input);
        updateParser(jsonFactory, jsonParser);
        return jsonParser;
    }

    /**
     * Create the JSON factory
     *
     * @return JSON factory
     */
    private static JsonFactory getFactory() {
        JsonFactory jsonFactory = new JsonFactory();
        //Do not auto-close target output when writing completes
        jsonFactory.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);

        //Do not auto-close source output when reading completes
        jsonFactory.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
        return jsonFactory;
    }

    /**
     * Update the generator with a default codec and pretty printer
     *
     * @param jsonFactory   Factory to set as codec
     * @param jsonGenerator Generator to configure
     */
    private static void updateGenerator(JsonFactory jsonFactory, JsonGenerator jsonGenerator) {
        ObjectMapper mapper = new ObjectMapper(jsonFactory);

        //Update the annotation interceptor to also include jaxb annotations as a second choice
        AnnotationIntrospector primary = new JacksonAnnotationIntrospector();
        AnnotationIntrospector secondary = new JaxbAnnotationIntrospector() {

            /**
             * BUG FIX:
             * By contract, if findSerializationInclusion didn't encounter any annotations that should change the
             * inclusion value, it should return the default value it has received; but actually returns null, which
             * overrides the NON_NULL option we set.
             * The doc states issue JACKSON-256 which was supposed to be fixed in 1.5.0. *twilight zone theme song*
             */

            @Override
            public JsonSerialize.Inclusion findSerializationInclusion(Annotated a, JsonSerialize.Inclusion defValue) {
                JsonSerialize.Inclusion inclusion = super.findSerializationInclusion(a, defValue);
                if (inclusion == null) {
                    return defValue;
                }
                return inclusion;
            }
        };
        AnnotationIntrospector pair = new AnnotationIntrospector.Pair(primary, secondary);
        mapper.getSerializationConfig().setAnnotationIntrospector(pair);
        mapper.getSerializationConfig().setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);

        jsonGenerator.setCodec(mapper);
        jsonGenerator.useDefaultPrettyPrinter();
    }

    /**
     * Update the parser with a default codec
     *
     * @param jsonFactory Factory to set as codec
     * @param jsonParser  Parser to configure
     */
    private static void updateParser(JsonFactory jsonFactory, JsonParser jsonParser) {
        AnnotationIntrospector primary = new JacksonAnnotationIntrospector();
        AnnotationIntrospector secondary = new JaxbAnnotationIntrospector();
        AnnotationIntrospector pair = new AnnotationIntrospector.Pair(primary, secondary);

        ObjectMapper mapper = new ObjectMapper(jsonFactory);
        mapper.getSerializationConfig().setAnnotationIntrospector(pair);
        mapper.getDeserializationConfig().setAnnotationIntrospector(pair);
        mapper.getDeserializationConfig().disable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);
        jsonParser.setCodec(mapper);
    }
}
