package org.artifactory.ui.rest.model.admin.configuration.blackduck;

import org.artifactory.descriptor.external.BlackDuckSettingsDescriptor;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.util.JsonUtil;

/**
 * @author Chen Keinan
 */
public class BlackDuck extends BlackDuckSettingsDescriptor implements RestModel {

    public BlackDuck() {
    }

    public BlackDuck(BlackDuckSettingsDescriptor blackDuckSettingsDescriptor) {
        super.setPassword(blackDuckSettingsDescriptor.getPassword());
        super.setConnectionTimeoutMillis(blackDuckSettingsDescriptor.getConnectionTimeoutMillis());
        super.setUsername(blackDuckSettingsDescriptor.getUsername());
        super.setServerUri(blackDuckSettingsDescriptor.getServerUri());
        super.setEnableIntegration(blackDuckSettingsDescriptor.isEnableIntegration());
        super.setProxy(blackDuckSettingsDescriptor.getProxy());
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
