package org.artifactory.ui.rest.model.admin.configuration.proxy;

import org.artifactory.descriptor.repo.ProxyDescriptor;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.util.JsonUtil;

/**
 * @author Chen Keinan
 */
public class Proxy extends ProxyDescriptor implements RestModel {

    public Proxy() {
    }

    public Proxy(ProxyDescriptor proxyDescriptor) {
        if (proxyDescriptor != null) {
            super.setDefaultProxy(proxyDescriptor.isDefaultProxy());
            super.setKey(proxyDescriptor.getKey());
            super.setPort(proxyDescriptor.getPort());
            super.setHost(proxyDescriptor.getHost());
            super.setUsername(proxyDescriptor.getUsername());
            super.setDomain(proxyDescriptor.getDomain());
            super.setNtHost(proxyDescriptor.getNtHost());
            super.setRedirectedToHosts(proxyDescriptor.getRedirectedToHosts());
            super.setPassword(proxyDescriptor.getPassword());
        }
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}

