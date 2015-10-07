package org.artifactory.ui.rest.model.admin.advanced.systeminfo;

import java.util.LinkedHashMap;
import java.util.Map;

import org.artifactory.rest.common.model.BaseModel;

/**
 * @author Chen Keinan
 */
public class SystemInfo extends BaseModel {

    private Map<String, Map<String, String>> systemInfo = new LinkedHashMap<>();

    public Map<String, Map<String, String>> getSystemInfo() {
        return systemInfo;
    }

    public void setSystemInfo(Map<String, Map<String, String>> systemInfo) {
        this.systemInfo = systemInfo;
    }
}
