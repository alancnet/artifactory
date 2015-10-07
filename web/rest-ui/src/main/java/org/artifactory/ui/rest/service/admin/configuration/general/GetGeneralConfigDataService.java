package org.artifactory.ui.rest.service.admin.configuration.general;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
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
public class GetGeneralConfigDataService implements RestService {

    @Autowired
    CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        CentralConfigDescriptor descriptor = centralConfigService.getDescriptor();
        // update general config data
        GeneralConfig generalconfig = new GeneralConfig();
        generalconfig.setServerName(descriptor.getServerName());
        generalconfig.setLogoUrl(descriptor.getLogo());
        response.iModel(generalconfig);
    }
}
