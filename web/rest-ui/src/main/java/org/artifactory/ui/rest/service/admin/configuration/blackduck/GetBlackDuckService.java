package org.artifactory.ui.rest.service.admin.configuration.blackduck;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.external.BlackDuckSettingsDescriptor;
import org.artifactory.descriptor.external.ExternalProvidersDescriptor;
import org.artifactory.descriptor.repo.ProxyDescriptor;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.common.ConfigModelPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetBlackDuckService implements RestService {
    @Autowired
    private CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        RestModel blackDuck = getBlackDuckModel();
        response.iModel(blackDuck);
    }

    /**
     * get black duck descriptor and populate it data to black duck model
     *
     * @return black duck or empty model
     */
    private RestModel getBlackDuckModel() {
        CentralConfigDescriptor centralConfig = centralConfigService.getDescriptor();
        ExternalProvidersDescriptor external = centralConfig.getExternalProvidersDescriptor();
        BlackDuckSettingsDescriptor blackDuckDescriptor = null;
        if (external != null) {
            blackDuckDescriptor = external.getBlackDuckSettingsDescriptor();
        }

        if (blackDuckDescriptor == null) {
            blackDuckDescriptor = new BlackDuckSettingsDescriptor();
            // set default values
            blackDuckDescriptor.setConnectionTimeoutMillis(20000l);
            if (centralConfigService.defaultProxyDefined()) {
                ProxyDescriptor defaultProxy = centralConfigService.getDescriptor().getDefaultProxy();
                blackDuckDescriptor.setProxy(defaultProxy);
            }
        }
        return ConfigModelPopulator.populateBlackDuckInfo(blackDuckDescriptor);
    }
}
