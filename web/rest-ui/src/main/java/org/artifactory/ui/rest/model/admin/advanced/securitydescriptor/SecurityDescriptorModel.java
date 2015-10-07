package org.artifactory.ui.rest.model.admin.advanced.securitydescriptor;

import org.artifactory.api.rest.restmodel.JsonUtil;
import org.artifactory.rest.common.model.RestModel;

/**
 * @author Chen Keinan
 */
public class SecurityDescriptorModel implements RestModel {

    private String securityXML;

    SecurityDescriptorModel() {
    }

    public SecurityDescriptorModel(String securityXML) {
        this.securityXML = securityXML;
    }

    public String getSecurityXML() {
        return securityXML;
    }

    public void setSecurityXML(String securityXML) {
        this.securityXML = securityXML;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(securityXML);
    }

}
