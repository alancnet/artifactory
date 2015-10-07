package org.artifactory.ui.rest.model.admin.configuration.proxy;

import com.google.common.collect.Lists;
import org.artifactory.rest.common.model.RestModel;

import java.util.List;

/**
 * @author Gidi Shabat
 */
public class DeleteProxiesModel implements RestModel {
    private List<String> proxyKeys = Lists.newArrayList();

    public List<String> getProxyKeys() {
        return proxyKeys;
    }

    public void addProxy(String proxyKey) {
        proxyKeys.add(proxyKey);
    }
}

