package org.artifactory.rest.common.util;

import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.model.RestSpecialFields;
import org.artifactory.rest.common.service.IgnoreSpecialFields;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.ser.FilterProvider;
import org.codehaus.jackson.map.ser.impl.SimpleBeanPropertyFilter;
import org.codehaus.jackson.map.ser.impl.SimpleFilterProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Chen Keinan
 */
public class JsonUtil {
    private static final Logger log = LoggerFactory.getLogger(JsonUtil.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * json to String exclude null data
     *
     * @param model - model data to String
     * @return - model data with json format
     */
    public static String jsonToString(RestModel model) {
        mapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
        mapper.disableDefaultTyping();
        String jasonString = null;
        try {
            jasonString = mapper.writeValueAsString(model);
        } catch (IOException e) {
            log.debug(e.toString());
        }
        return jasonString;
    }

    /**
     * jsonToString exclude null data end edit fields
     *
     * @param model - model data to String
     * @return - model data with json format
     */
    public static String jsonToStringIgnoreSpecialFields(RestModel model) {
        String[] ExcludedFieldsFromView = getExcludedFields(model);
        ObjectMapper specialMapper = new ObjectMapper();
        specialMapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
        String data = null;
        FilterProvider filters = new SimpleFilterProvider()
                .addFilter("exclude fields",
                        SimpleBeanPropertyFilter.serializeAllExcept(
                                (ExcludedFieldsFromView)));
        ObjectWriter writer = specialMapper.writer(filters);
        try {
            data = writer.writeValueAsString(model);
        } catch (IOException e) {
            log.debug(e.getMessage());
        }
        return data;
    }

    /**
     * return json with ignored fields
     *
     * @param model model data
     * @return
     */
    private static String[] getExcludedFields(RestModel model) {
        if (model instanceof RestSpecialFields) {
            if (!((RestSpecialFields) model).ignoreSpecialFields()) {
                return new String[]{};
            }
        }
        IgnoreSpecialFields ignoreSpecialFields = model.getClass().getAnnotation(IgnoreSpecialFields.class);
        return ignoreSpecialFields.value();
    }

    public static RestModel mapDataToModel(String data, Class valueType) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        RestModel model = (RestModel) mapper.readValue(data, valueType);
        return model;
    }
}
