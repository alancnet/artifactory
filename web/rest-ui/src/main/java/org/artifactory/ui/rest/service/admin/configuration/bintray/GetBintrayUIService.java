package org.artifactory.ui.rest.service.admin.configuration.bintray;

import org.artifactory.api.bintray.BintrayService;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.bintray.BintrayConfigDescriptor;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
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
public class GetBintrayUIService implements RestService {
    @Autowired
    private CentralConfigService centralConfigService;
    @Autowired
    BintrayService bintrayService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        MutableCentralConfigDescriptor mutableDescriptor = centralConfigService.getMutableDescriptor();
        BintrayConfigDescriptor bintrayConfigDescriptor = mutableDescriptor.getBintrayConfig();
        String bintrayRegistrationUrl = bintrayService.getBintrayRegistrationUrl();
        response.iModel(ConfigModelPopulator.populateBintrayInfo(bintrayConfigDescriptor, bintrayRegistrationUrl));
    }
}
