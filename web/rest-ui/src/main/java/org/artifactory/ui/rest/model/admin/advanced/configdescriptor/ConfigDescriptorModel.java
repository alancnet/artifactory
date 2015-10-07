package org.artifactory.ui.rest.model.admin.advanced.configdescriptor;

import org.artifactory.api.rest.restmodel.JsonUtil;
import org.artifactory.rest.common.model.RestModel;

/**
 * @author Chen Keinan
 */
public class ConfigDescriptorModel implements RestModel {

    private String configXml;

    public ConfigDescriptorModel() {
    }

    public ConfigDescriptorModel(String configXml) {
        this.configXml = configXml;
    }

    public String getConfigXml() {
        return configXml;
    }

    public void setConfigXml(String configXml) {
        this.configXml = configXml;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(configXml);
    }
}
