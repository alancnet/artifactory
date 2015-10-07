package org.artifactory.ui.rest.service.admin.advanced.configDescriptor;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.advanced.configdescriptor.ConfigDescriptorModel;
import org.artifactory.ui.rest.service.utils.AolUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetConfigDescriptorService implements RestService {

    @Autowired
    private CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        AolUtils.assertNotAol("GetConfigDescriptor");
        // get config descriptor model
        ConfigDescriptorModel configDescriptorModel = getConfigDescriptorModel();
        // update response data
        response.iModel(configDescriptorModel);
    }

    /**
     * get config descriptor model
     *
     * @return config descriptor model
     */
    private ConfigDescriptorModel getConfigDescriptorModel() {
        String configXml = centralConfigService.getConfigXml();
        return new ConfigDescriptorModel(configXml);
    }
}
