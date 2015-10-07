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

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UpdateProxyService implements RestService {

    @Autowired
    private CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        AolUtils.assertNotAol("UpdateProxy");
        String proxyKey = request.getPathParamByKey("id");
        ProxyDescriptor proxyDescriptor = (ProxyDescriptor) request.getImodel();
        // update proxy
        updateProxy(proxyDescriptor, proxyKey);
        // update response feedback
        response.info("Successfully updated proxy '" + proxyKey + "'");
    }

    /**
     * add new proxy to descriptor to configuration descriptor
     *
     * @param newProxy - new proxy descriptor
     */
    private void updateProxy(ProxyDescriptor newProxy, String proxyKey) {
        boolean isDefaultProxy = newProxy.isDefaultProxy();
        MutableCentralConfigDescriptor configDescriptor = centralConfigService.getMutableDescriptor();
        ProxyDescriptor currentProxy = configDescriptor.getProxy(proxyKey);
        if (currentProxy != null) {
            populateProxyData(currentProxy, newProxy);
            configDescriptor.proxyChanged(currentProxy, isDefaultProxy);
            centralConfigService.saveEditedDescriptorAndReload(configDescriptor);
        }
    }

    /**
     * populate proxy changed data
     *
     * @param currentProxyDesc - proxy descriptor before update
     * @param newProxy  - update proxy descriptor
     */
    private void populateProxyData(ProxyDescriptor currentProxyDesc, ProxyDescriptor newProxy) {
        currentProxyDesc.setDefaultProxy(newProxy.isDefaultProxy());
        currentProxyDesc.setPort(newProxy.getPort());
        currentProxyDesc.setHost(newProxy.getHost());
        currentProxyDesc.setUsername(newProxy.getUsername());
        currentProxyDesc.setDomain(newProxy.getDomain());
        currentProxyDesc.setNtHost(newProxy.getNtHost());
        currentProxyDesc.setRedirectedToHosts(newProxy.getRedirectedToHosts());
        currentProxyDesc.setPassword(newProxy.getPassword());
    }
}
