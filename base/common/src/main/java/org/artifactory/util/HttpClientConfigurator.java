/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2014 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.artifactory.util;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultRoutePlanner;
import org.apache.http.impl.conn.DefaultSchemePortResolver;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.artifactory.common.ConstantValues;
import org.artifactory.descriptor.repo.ProxyDescriptor;
import org.artifactory.security.crypto.CryptoHelper;
import org.artifactory.util.bearer.BearerSchemeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Builder for HTTP client.
 *
 * @author Yossi Shaul
 */
public class HttpClientConfigurator {
    private static final Logger log = LoggerFactory.getLogger(HttpClientConfigurator.class);

    private HttpClientBuilder builder = HttpClients.custom();
    private RequestConfig.Builder config = RequestConfig.custom();
    private String host;
    private BasicCredentialsProvider credsProvider;
    boolean explicitCookieSupport;

    public HttpClientConfigurator() {
        builder.setUserAgent(HttpUtils.getArtifactoryUserAgent());
        credsProvider = new BasicCredentialsProvider();
        handleGzipResponse(ConstantValues.httpAcceptEncodingGzip.getBoolean());
        config.setMaxRedirects(20);
        config.setCircularRedirectsAllowed(true);
    }

    public CloseableHttpClient getClient() {
        if (!explicitCookieSupport && !ConstantValues.enableCookieManagement.getBoolean()) {
            builder.disableCookieManagement();
        }
        if (hasCredentials()) {
            builder.setDefaultCredentialsProvider(credsProvider);
        }
        return builder.setDefaultRequestConfig(config.build()).build();
    }

    public HttpClientConfigurator keepAliveStrategy() {
        ConnectionKeepAliveStrategy keepAliveStrategy = getConnectionKeepAliveStrategy();
        builder.setKeepAliveStrategy(keepAliveStrategy);
        return this;
    }

    /**
     * provide a custom keep-alive strategy
     *
     * @return keep-alive strategy to be used for connection pool
     */
    private ConnectionKeepAliveStrategy getConnectionKeepAliveStrategy() {
        return new ConnectionKeepAliveStrategy() {
            @Override
            public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
                // Honor 'keep-alive' header
                HeaderElementIterator it = new BasicHeaderElementIterator(
                        response.headerIterator(HTTP.CONN_KEEP_ALIVE));
                while (it.hasNext()) {
                    HeaderElement he = it.nextElement();
                    String param = he.getName();
                    String value = he.getValue();
                    if (value != null && param.equalsIgnoreCase("timeout")) {
                        try {
                            return Long.parseLong(value) * 1000;
                        } catch (NumberFormatException ignore) {
                        }
                    }
                }
                HttpHost target = (HttpHost) context.getAttribute(
                        HttpClientContext.HTTP_TARGET_HOST);
                if ("www.naughty-server.com".equalsIgnoreCase(target.getHostName())) {
                    return 5 * 1000;
                } else {
                    return 30 * 1000;
                }
            }
        };
    }

    /**
     * set customized Pooling Http Client Connection Manager
     *
     * @param connectionManager -  custom PoolingHttpClientConnectionManager
     * @return Http Client Configurator
     */
    public HttpClientConfigurator connectionMgr(PoolingHttpClientConnectionManager connectionManager) {
        builder.setConnectionManager(connectionManager);
        return this;
    }

    /**
     * Disable the automatic gzip compression on read.
     * Once disabled cannot be activated.
     */
    public HttpClientConfigurator handleGzipResponse(boolean handleGzipResponse) {
        if (!handleGzipResponse) {
            builder.disableContentCompression();
        }
        return this;
    }

    /**
     * May throw a runtime exception when the given URL is invalid.
     */
    public HttpClientConfigurator hostFromUrl(String urlStr) {
        if (StringUtils.isNotBlank(urlStr)) {
            try {
                URL url = new URL(urlStr);
                host(url.getHost());
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("Cannot parse the url " + urlStr, e);
            }
        }
        return this;
    }

    /**
     * Ignores blank values
     */
    public HttpClientConfigurator host(String host) {
        if (StringUtils.isNotBlank(host)) {
            this.host = host;
            builder.setRoutePlanner(new DefaultHostRoutePlanner(host));
        }
        return this;
    }

    public HttpClientConfigurator defaultMaxConnectionsPerHost(int maxConnectionsPerHost) {
        builder.setMaxConnPerRoute(maxConnectionsPerHost);
        return this;
    }

    public HttpClientConfigurator maxTotalConnections(int maxTotalConnections) {
        builder.setMaxConnTotal(maxTotalConnections);
        return this;
    }

    public HttpClientConfigurator connectionTimeout(int connectionTimeout) {
        config.setConnectTimeout(connectionTimeout);
        return this;
    }

    public HttpClientConfigurator soTimeout(int soTimeout) {
        config.setSocketTimeout(soTimeout);
        return this;
    }

    /**
     * see {@link org.apache.http.client.config.RequestConfig#isStaleConnectionCheckEnabled()}
     */
    public HttpClientConfigurator staleCheckingEnabled(boolean staleCheckingEnabled) {
        config.setStaleConnectionCheckEnabled(staleCheckingEnabled);
        return this;
    }

    /**
     * Disable request retries on service unavailability.
     */
    public HttpClientConfigurator noRetry() {
        return retry(0, false);
    }

    /**
     * Number of retry attempts. Default is 3 retries.
     *
     * @param retryCount Number of retry attempts. 0 means no retries.
     */
    public HttpClientConfigurator retry(int retryCount, boolean requestSentRetryEnabled) {
        if (retryCount == 0) {
            builder.disableAutomaticRetries();
        } else {
            builder.setRetryHandler(new DefaultHttpRequestRetryHandler(retryCount, requestSentRetryEnabled));
        }
        return this;
    }

    /**
     * Ignores blank or invalid input
     */
    public HttpClientConfigurator localAddress(String localAddress) {
        if (StringUtils.isNotBlank(localAddress)) {
            try {
                InetAddress address = InetAddress.getByName(localAddress);
                config.setLocalAddress(address);
            } catch (UnknownHostException e) {
                throw new IllegalArgumentException("Invalid local address: " + localAddress, e);
            }
        }
        return this;
    }

    /**
     * Ignores null credentials
     */
    public HttpClientConfigurator authentication(UsernamePasswordCredentials creds) {
        if (creds != null) {
            authentication(creds.getUserName(), creds.getPassword());
        }

        return this;
    }

    /**
     * Configures preemptive authentication on this client. Ignores blank username input.
     */
    public HttpClientConfigurator authentication(String username, String password) {
        return authentication(username, password, false);
    }

    /**
     * Configures preemptive authentication on this client. Ignores blank username input.
     */
    public HttpClientConfigurator authentication(String username, String password, boolean allowAnyHost) {
        if (StringUtils.isNotBlank(username)) {
            if (StringUtils.isBlank(host)) {
                throw new IllegalStateException("Cannot configure authentication when host is not set.");
            }

            AuthScope authscope = allowAnyHost ?
                    new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM) :
                    new AuthScope(host, AuthScope.ANY_PORT, AuthScope.ANY_REALM);
            credsProvider.setCredentials(
                    authscope, new UsernamePasswordCredentials(username, password));

            builder.addInterceptorFirst(new PreemptiveAuthInterceptor());
        }
        return this;
    }

    /**
     * Enable cookie management for this client.
     */
    public HttpClientConfigurator enableCookieManagement(boolean enableCookieManagement) {
        if (enableCookieManagement) {
            config.setCookieSpec(CookieSpecs.BROWSER_COMPATIBILITY);
        } else {
            config.setCookieSpec(null);
        }
        explicitCookieSupport = enableCookieManagement;
        return this;
    }

    public HttpClientConfigurator enableTokenAuthentication(boolean enableTokenAuthentication,
            String username, String password) {
        if (enableTokenAuthentication) {
            if (StringUtils.isBlank(host)) {
                throw new IllegalStateException("Cannot configure authentication when host is not set.");
            }

            config.setTargetPreferredAuthSchemes(Collections.singletonList("Bearer"));

            // We need dummy credentials otherwise we won't respond to a challenge properly
            AuthScope authScope = new AuthScope(host, AuthScope.ANY_PORT, AuthScope.ANY_REALM);
            // Dummy:dummy is the specification for forcing token authentication
            UsernamePasswordCredentials dummyCredentials = new UsernamePasswordCredentials("dummy", "dummy");
            credsProvider.setCredentials(authScope, dummyCredentials);

            // The real credentials are passed to the Bearer that will get the token with them
            UsernamePasswordCredentials realCredentials = null;
            if (StringUtils.isNotBlank(username)) {
                realCredentials = new UsernamePasswordCredentials(username, password);
            }
            Registry<AuthSchemeProvider> bearerRegistry = RegistryBuilder.<AuthSchemeProvider>create()
                    .register("Bearer", new BearerSchemeFactory(realCredentials))
                    .build();

            builder.setDefaultAuthSchemeRegistry(bearerRegistry);
        }
        return this;
    }

    public HttpClientConfigurator proxy(@Nullable ProxyDescriptor proxyDescriptor) {
        configureProxy(proxyDescriptor);
        return this;
    }

    private void configureProxy(ProxyDescriptor proxy) {
        if (proxy != null) {
            config.setProxy(new HttpHost(proxy.getHost(), proxy.getPort()));
            if (proxy.getUsername() != null) {
                Credentials creds = null;
                if (proxy.getDomain() == null) {
                    creds = new UsernamePasswordCredentials(proxy.getUsername(),
                            CryptoHelper.decryptIfNeeded(proxy.getPassword()));
                    //This will demote the NTLM authentication scheme so that the proxy won't barf
                    //when we try to give it traditional credentials. If the proxy doesn't do NTLM
                    //then this won't hurt it (jcej at tragus dot org)
                    List<String> authPrefs = Arrays.asList(AuthSchemes.DIGEST, AuthSchemes.BASIC, AuthSchemes.NTLM);
                    config.setProxyPreferredAuthSchemes(authPrefs);
                    // preemptive proxy authentication
                    builder.addInterceptorFirst(new ProxyPreemptiveAuthInterceptor());
                } else {
                    try {
                        String ntHost =
                                StringUtils.isBlank(proxy.getNtHost()) ? InetAddress.getLocalHost().getHostName() :
                                        proxy.getNtHost();
                        creds = new NTCredentials(proxy.getUsername(),
                                CryptoHelper.decryptIfNeeded(proxy.getPassword()), ntHost, proxy.getDomain());
                    } catch (UnknownHostException e) {
                        log.error("Failed to determine required local hostname for NTLM credentials.", e);
                    }
                }
                if (creds != null) {
                    credsProvider.setCredentials(
                            new AuthScope(proxy.getHost(), proxy.getPort(), AuthScope.ANY_REALM), creds);
                    if (proxy.getRedirectedToHostsList() != null) {
                        for (String hostName : proxy.getRedirectedToHostsList()) {
                            credsProvider.setCredentials(
                                    new AuthScope(hostName, AuthScope.ANY_PORT, AuthScope.ANY_REALM), creds);
                        }
                    }
                }
            }
        }
    }

    private boolean hasCredentials() {
        return credsProvider.getCredentials(AuthScope.ANY) != null;
    }

    static class DefaultHostRoutePlanner extends DefaultRoutePlanner {

        private final HttpHost defaultHost;

        public DefaultHostRoutePlanner(String defaultHost) {
            super(DefaultSchemePortResolver.INSTANCE);
            this.defaultHost = new HttpHost(defaultHost);
        }

        @Override
        public HttpRoute determineRoute(HttpHost host, HttpRequest request, HttpContext context) throws HttpException {
            if (host == null) {
                host = defaultHost;
            }
            return super.determineRoute(host, request, context);
        }

        public HttpHost getDefaultHost() {
            return defaultHost;
        }
    }

}
