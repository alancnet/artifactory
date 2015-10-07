package org.artifactory.ui.rest.service.admin.configuration.proxies;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.repo.ProxyDescriptor;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.service.utils.AolUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CreateProxyService implements RestService {

    @Autowired
    private CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        AolUtils.assertNotAol("CreateProxy");
        ProxyDescriptor proxyDescriptor = (ProxyDescriptor) request.getImodel();
        // add new proxy to configuration
        addNewProxy(proxyDescriptor);
        // update response feedback
        updateResponse(response, proxyDescriptor);
    }

    /**
     * update response with created status and feedback
     *
     * @param artifactoryResponse - encapsulate all data require for response
     * @param proxyDescriptor     - proxy descriptor
     */
    private void updateResponse(RestResponse artifactoryResponse, ProxyDescriptor proxyDescriptor) {
        artifactoryResponse.info("Successfully created proxy '" + proxyDescriptor.getKey() + "'");
        artifactoryResponse.responseCode(HttpServletResponse.SC_CREATED);
    }

    /**
     * add new proxy to descriptor to configuration descriptor
     *
     * @param proxyDescriptor - new proxy descriptor
     */
    private void addNewProxy(ProxyDescriptor proxyDescriptor) {
        boolean isDefaultProxy = proxyDescriptor.isDefaultProxy();
        MutableCentralConfigDescriptor configDescriptor = centralConfigService.getMutableDescriptor();
        configDescriptor.proxyChanged(proxyDescriptor, isDefaultProxy);
        configDescriptor.addProxy(proxyDescriptor, isDefaultProxy);
        centralConfigService.saveEditedDescriptorAndReload(configDescriptor);
    }
}
