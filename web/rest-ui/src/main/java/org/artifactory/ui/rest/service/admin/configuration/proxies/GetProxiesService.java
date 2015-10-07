package org.artifactory.ui.rest.service.admin.configuration.proxies;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.repo.ProxyDescriptor;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.common.ConfigModelPopulator;
import org.artifactory.ui.rest.model.admin.configuration.proxy.Proxy;
import org.artifactory.ui.rest.model.empty.EmptyModel;
import org.artifactory.ui.rest.service.utils.AolUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetProxiesService implements RestService {
    @Autowired
    private CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        fetchSingleOrMultiProxy(response, request);
    }

    /**
     * fetch single or multi group depend on query and path param
     *
     * @param artifactoryResponse - encapsulate all data related to response
     *                            artifactoryRequest - encapsulate data related to request
     */
    private void fetchSingleOrMultiProxy(RestResponse artifactoryResponse,
            ArtifactoryRestRequest artifactoryRequest) {
        String proxyKey = artifactoryRequest.getPathParamByKey("id");
        if (isMultiProxy(proxyKey)) {
            updateResponseWithMultiProxyInfo(artifactoryResponse);
        } else {
            updateResponseWithSinglePrxyInfo(artifactoryResponse, proxyKey);
        }
    }

    /**
     * get Single Group info and update response
     *
     * @param artifactoryResponse - encapsulate Data require for response
     * @param proxyKey            - proxy Key from path param
     */
    private void updateResponseWithSinglePrxyInfo(RestResponse artifactoryResponse, String proxyKey) {
        RestModel proxy = getProxy(proxyKey);
        if (proxy == null) {
            proxy = new EmptyModel();
        }
        artifactoryResponse.iModel(proxy);
    }

    /**
     * get Multi Group info and update response
     *
     * @param artifactoryResponse - encapsulate Data require for response
     */
    private void updateResponseWithMultiProxyInfo(RestResponse artifactoryResponse) {
        List<ProxyDescriptor> proxyDescriptors = getProxyInfos();
        // add groups to List
        List<RestModel> proxyList = new ArrayList<>();
        proxyDescriptors.stream().forEach(
                proxyInfo -> proxyList.add(ConfigModelPopulator.populateProxyConfiguration(proxyInfo)));
        artifactoryResponse.iModelList(proxyList);
    }

    /**
     * check id require to get single / multi group
     *
     * @param proxyKey - single group name
     * @return if true require multi group
     */
    private boolean isMultiProxy(String proxyKey) {
        return proxyKey == null || proxyKey.length() == 0;
    }

    /**
     * return group by name
     *
     * @param proxyKey - proxy key
     * @return
     */
    private Proxy getProxy(String proxyKey) {
        ProxyDescriptor proxyDescriptor = centralConfigService.getMutableDescriptor().getProxy(proxyKey);
        Proxy proxy = ConfigModelPopulator.populateProxyConfiguration(proxyDescriptor);
        return proxy;
    }

    /**
     * return all groups / default groups
     *
     * @return - list of groups info
     */
    private List<ProxyDescriptor> getProxyInfos() {
        List<ProxyDescriptor> proxyDescriptors = centralConfigService.getMutableDescriptor().getProxies();
        return proxyDescriptors;
    }
}
