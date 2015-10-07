package org.artifactory.ui.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Chen Keinan
 */
public class ModelDbMap {

    public static  Map<String,String> getBuildsMap(){
         Map<String,String> propBuildsFieldMap = new HashMap<>();
        propBuildsFieldMap.put("buildName", "build_name");
        propBuildsFieldMap.put("lastBuildTime", "build_time  ");
            propBuildsFieldMap.put("buildNumber","build_number");
            propBuildsFieldMap.put("status","status");
        return propBuildsFieldMap;
    }

    public static  Map<String,String> getModuleMap(){
        Map<String,String> propBuildsFieldMap = new HashMap<>();
        propBuildsFieldMap.put("moduleId","module_name_id");
        propBuildsFieldMap.put("numOfArtifacts","num_of_art");
        propBuildsFieldMap.put("numOfDependencies","num_of_dep");
         return propBuildsFieldMap;
    }

    public static Map<String, String> getBuildProps() {
        Map<String, String> propBuildsFieldMap = new HashMap<>();
        propBuildsFieldMap.put("key", "propsKey");
        propBuildsFieldMap.put("value", "propsValue");
        return propBuildsFieldMap;
    }
}
