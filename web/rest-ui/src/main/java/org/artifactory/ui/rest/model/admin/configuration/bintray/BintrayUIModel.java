package org.artifactory.ui.rest.model.admin.configuration.bintray;

import org.artifactory.descriptor.bintray.BintrayConfigDescriptor;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.util.JsonUtil;

/**
 * @author Chen Keinan
 */
public class BintrayUIModel extends BintrayConfigDescriptor implements RestModel {

    private String BintrayAuth;
    private String bintrayConfigUrl;

    public BintrayUIModel() {
    }

    public BintrayUIModel(BintrayConfigDescriptor bintrayConfigDescriptor) {
        if (bintrayConfigDescriptor != null) {
            super.setApiKey(bintrayConfigDescriptor.getApiKey());
            super.setFileUploadLimit(bintrayConfigDescriptor.getFileUploadLimit());
            super.setUserName(bintrayConfigDescriptor.getUserName());
        }
    }

    public void setBintrayAuth(String bintrayAuth) {
        BintrayAuth = bintrayAuth;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }

    @Override
    public String getBintrayAuth() {
        return BintrayAuth;
    }

    public String getBintrayConfigUrl() {
        return bintrayConfigUrl;
    }

    public void setBintrayConfigUrl(String bintrayConfigUrl) {
        this.bintrayConfigUrl = bintrayConfigUrl;
    }
}
