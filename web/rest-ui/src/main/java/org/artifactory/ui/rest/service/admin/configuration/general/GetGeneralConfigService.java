package org.artifactory.ui.rest.service.admin.configuration.general;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.configuration.generalconfig.GeneralConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetGeneralConfigService implements RestService {

    @Autowired
    CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        MutableCentralConfigDescriptor mutableDescriptor = centralConfigService.getMutableDescriptor();
        // update general config
        GeneralConfig generalconfig = new GeneralConfig(mutableDescriptor);
        response.iModel(generalconfig);
    }
}
