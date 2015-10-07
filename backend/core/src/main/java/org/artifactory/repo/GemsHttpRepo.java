package org.artifactory.repo;

import org.apache.http.HttpHost;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.artifactory.descriptor.repo.HttpRepoDescriptor;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.security.crypto.CryptoHelper;
import org.artifactory.util.HttpClientConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @Author Chen Keinan
 */
public class GemsHttpRepo extends HttpRepo {
    private static final Logger log = LoggerFactory.getLogger(HttpRepo.class);

    private PoolingHttpClientConnectionManager connectionMgr;

    public GemsHttpRepo(HttpRepoDescriptor descriptor,
            InternalRepositoryService repositoryService, boolean globalOfflineMode,
            RemoteRepo oldRemoteRepo) {

        super(descriptor, repositoryService, globalOfflineMode, oldRemoteRepo);
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    protected CloseableHttpClient createHttpClient() {
        createConnectionMgr();
        getRepositoryService().registerConnectionPoolMgr(connectionMgr);
        return new HttpClientConfigurator()
                .hostFromUrl(getUrl())
                .defaultMaxConnectionsPerHost(50)
                .maxTotalConnections(50)
                .connectionTimeout(getSocketTimeoutMillis())
                .soTimeout(getSocketTimeoutMillis())
                .staleCheckingEnabled(true)
                .retry(1, false)
                .localAddress(getLocalAddress())
                .proxy(getProxy())
                .authentication(getUsername(), CryptoHelper.decryptIfNeeded(getPassword()), isAllowAnyHostAuth())
                .enableCookieManagement(isEnableCookieManagement())
                .connectionMgr(connectionMgr)
                .keepAliveStrategy()
                .getClient();
    }

    /**
     * create custom Http Client connection pool to be used by Http Client
     */
    private void createConnectionMgr() {
        connectionMgr = new PoolingHttpClientConnectionManager();
        connectionMgr.setMaxTotal(50);
        connectionMgr.setDefaultMaxPerRoute(20);
        HttpHost localhost = new HttpHost("localhost", 80);
        connectionMgr.setMaxPerRoute(new HttpRoute(localhost), 50);
    }
}
