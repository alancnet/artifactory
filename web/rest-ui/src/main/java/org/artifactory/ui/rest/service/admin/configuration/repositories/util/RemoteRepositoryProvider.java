package org.artifactory.ui.rest.service.admin.configuration.repositories.util;

import org.apache.http.impl.client.CloseableHttpClient;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.repo.ProxyDescriptor;
import org.artifactory.security.crypto.CryptoHelper;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteNetworkRepositoryConfigModel;
import org.artifactory.util.HttpClientConfigurator;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Aviad Shikloshi
 */
public class RemoteRepositoryProvider {
    protected static final int RETRY_COUNT = 1;
    protected static final int DEFAULT_TIMEOUT = 15000;

    @Autowired
    private CentralConfigService configService;

    protected CloseableHttpClient getRemoteRepoTestHttpClient(String remoteUrl,
            RemoteNetworkRepositoryConfigModel networkConfig) {
        // In case network model was not sent in the request we are using the default values
        if (networkConfig == null) {
            networkConfig = new RemoteNetworkRepositoryConfigModel();
        }
        ProxyDescriptor proxyDescriptor = configService.getDescriptor().getProxy(networkConfig.getProxy());
        int socketTimeout =
                networkConfig.getSocketTimeout() == null ? DEFAULT_TIMEOUT : networkConfig.getSocketTimeout();
        return new HttpClientConfigurator()
                .hostFromUrl(remoteUrl)
                .connectionTimeout(socketTimeout)
                .soTimeout(socketTimeout)
                .staleCheckingEnabled(true)
                .retry(RETRY_COUNT, false)
                .localAddress(networkConfig.getLocalAddress())
                .proxy(proxyDescriptor)
                .authentication(networkConfig.getUsername(), CryptoHelper.decryptIfNeeded(networkConfig.getPassword()),
                        networkConfig.getLenientHostAuth() != null)
                .enableCookieManagement(networkConfig.getCookieManagement() != null)
                .getClient();
    }
}
