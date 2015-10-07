package org.artifactory.api.rest.restmodel;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
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
    public static String jsonToString(Object model) {
        //        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
        mapper.disableDefaultTyping();
        String jasonString = null;
        try {
            jasonString = mapper.writeValueAsString(model);
        } catch (IOException e) {
            log.error(e.toString());
        }
        return jasonString;
    }
}
