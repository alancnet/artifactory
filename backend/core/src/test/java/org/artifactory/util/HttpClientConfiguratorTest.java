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
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.impl.conn.DefaultRoutePlanner;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.artifactory.test.TestUtils;
import org.artifactory.util.bearer.BearerSchemeFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.testng.Assert.*;

/**
 * Tests the HTTP client configurator behaviors and conditions.
 * Implementation is package protected so most test must be done using reflection.
 *
 * @author Yossi Shaul
 */
@Test
public class HttpClientConfiguratorTest extends ArtifactoryHomeBoundTest {

    @BeforeMethod
    public void setUp() throws Exception {
        //Version might not have been initialized
        if (StringUtils.isBlank(ConstantValues.artifactoryVersion.getString())) {
            ArtifactoryHome.get().getArtifactoryProperties().
                    setProperty(ConstantValues.artifactoryVersion.getPropertyName(), "momo");
        }
    }

    public void testConstructor() {
        CloseableHttpClient multiThreadedClient = new HttpClientConfigurator().getClient();
        assertNotNull(multiThreadedClient, "A valid client should have been constructed.");

        HttpClientConnectionManager connManager = getConnManager(multiThreadedClient);
        assertTrue(connManager instanceof PoolingHttpClientConnectionManager,
                "Expected a multi-threaded connection manager but found " + multiThreadedClient.getClass());
        //testClientUserAgent(multiThreadedClient);

        //validateSingleThreadedClient(new HttpClient4Configurator(false).getClient());
        //validateSingleThreadedClient(new HttpClient4Configurator().getClient());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testHostFromInvalidUrl() {
        new HttpClientConfigurator().hostFromUrl("sttp:/.com");
    }

    public void testHost() {
        HttpClient client = new HttpClientConfigurator().host("bob").getClient();
        DefaultRoutePlanner routePlanner = getRoutePlanner(client);
        assertThat(routePlanner).isInstanceOf(HttpClientConfigurator.DefaultHostRoutePlanner.class);
        assertEquals(((HttpClientConfigurator.DefaultHostRoutePlanner)
                routePlanner).getDefaultHost().getHostName(), "bob", "Unexpected host.");
    }

    public void testTokenAuthentication() {
        HttpClient client = new HttpClientConfigurator().host("bob").enableTokenAuthentication(true, null, null).getClient();
        Registry<AuthSchemeProvider> registry = getAuthSchemeRegistry(client);
        assertThat(registry.lookup("bearer")).isInstanceOf(BearerSchemeFactory.class);
        RequestConfig defaultConfig = getDefaultConfig(client);
        assertThat(defaultConfig.getTargetPreferredAuthSchemes().size()).isEqualTo(1);
        assertThat(defaultConfig.getTargetPreferredAuthSchemes().iterator().next()).isEqualTo("Bearer");
    }

    private Registry<AuthSchemeProvider> getAuthSchemeRegistry(HttpClient client) {
        return TestUtils.getField(client, "authSchemeRegistry", Registry.class);
    }

    private RequestConfig getDefaultConfig(HttpClient client) {
        return TestUtils.getField(client, "defaultConfig", RequestConfig.class);
    }

    /*private Registry<AuthSchemeProvider> getAuthSchemeRegistry(HttpClient client) {
        return TestUtils.getField(client, "authSchemeRegistry", Registry.class);
    }*/

    private DefaultRoutePlanner getRoutePlanner(HttpClient client) {
        return TestUtils.getField(client, "routePlanner", DefaultRoutePlanner.class);
    }

    /*

    public void testHostFromNullUrl() {
        CloseableHttpClient client = new HttpClient4Configurator().hostFromUrl(null).getClient();
        assertNull(client.getHostConfiguration().getHost(), "Expected a null host.");
    }

    public void testHostFromUrl() {
        HttpClient client = new HttpClient4Configurator().hostFromUrl("http://momo.com/bobson").getClient();
        assertEquals(client.getHostConfiguration().getHost(), "momo.com", "Unexpected host.");
    }

    public void testNullHost() {
        HttpClient client = new HttpClient4Configurator().host(null).getClient();
        assertNull(client.getHostConfiguration().getHost(), "Expected a null host.");
    }

    public void testHost() {
        HttpClient client = new HttpClient4Configurator().host("bob").getClient();
        assertEquals(client.getHostConfiguration().getHost(), "bob", "Unexpected host.");
    }

    public void testNullLocalAddress() {
        HttpClient client = new HttpClient4Configurator().localAddress(null).getClient();
        assertNull(client.getHostConfiguration().getLocalAddress(), "Unexpected local address.");
    }

    public void testLocalAddress() {
        HttpClient client = new HttpClient4Configurator().localAddress("localhost").getClient();
        assertNotNull(client.getHostConfiguration().getLocalAddress(), "Expected a local address.");
    }

    public void testAuthenticationWithNullCredentials() {
        HttpClient client = new HttpClient4Configurator().authentication(null).getClient();
        assertNull(client.getState().getCredentials(AuthScope.ANY), "Unexpected auth scope.");
    }

    public void testAuthenticationWithCredentialsObject() {
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials("asd", "fgh");
        HttpClient client = new HttpClient4Configurator().host("moo.com").authentication(creds).getClient();
        assertEquals(client.getState().getCredentials(AuthScope.ANY), creds, "Unexpected credentials object.");
    }

    public void testAuthenticationWithBlankUsername() {
        HttpClient client = new HttpClient4Configurator().authentication(null, null).getClient();
        assertNull(client.getState().getCredentials(AuthScope.ANY), "Unexpected auth scope.");
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = ".*no host.*")
    public void testAuthenticationWithBlankHost() {
        new HttpClient4Configurator().authentication("momo", "popo");
    }

    public void testAuthentication() {
        HttpClient client = new HttpClient4Configurator().host("momo.com").authentication("moo", "bob").getClient();
        UsernamePasswordCredentials credentials =
                (UsernamePasswordCredentials) client.getState().getCredentials(AuthScope.ANY);
        assertNotNull(credentials, "Unexpected auth scope.");
        assertEquals("moo", credentials.getUserName(), "Unexpected authentication username.");
        assertEquals("bob", credentials.getPassword(), "Unexpected authentication password.");
    }

    public void testSimpleSetters() {
        ProxyDescriptor proxyDescriptor = new ProxyDescriptor();
        proxyDescriptor.setHost("moo.com");
        proxyDescriptor.setPort(8686);

        HttpClient client = new HttpClient4Configurator()
                .defaultMaxConnectionsPerHost(234)
                .maxTotalConnections(444)
                .connectionTimeout(2121)
                .soTimeout(999)
                .staleCheckingEnabled(true)
                .retry(60, true)
                .proxy(proxyDescriptor)
                .getClient();

        HttpConnectionManager httpConnectionManager = client.getHttpConnectionManager();

        HttpClientParams clientParams = client.getParams();
        HttpConnectionManagerParams connectionManagerparams = httpConnectionManager.getParams();

        assertEquals(connectionManagerparams.getDefaultMaxConnectionsPerHost(), 234,
                "Unexpected default max connections per host.");
        assertEquals(connectionManagerparams.getMaxTotalConnections(), 444, "Unexpected max total connections.");
        assertEquals(connectionManagerparams.getConnectionTimeout(), 2121, "Unexpected connection timeout.");
        assertEquals(clientParams.getConnectionManagerTimeout(), 2121, "Unexpected connection timeout.");
        assertEquals(connectionManagerparams.getSoTimeout(), 999, "Unexpected socket timeout.");
        assertEquals(clientParams.getSoTimeout(), 999, "Unexpected socket timeout.");
        assertTrue(connectionManagerparams.isStaleCheckingEnabled(), "Stale checking should be enabled.");

        DefaultHttpMethodRetryHandler retryHandler = (DefaultHttpMethodRetryHandler) clientParams.getParameter(
                HttpMethodParams.RETRY_HANDLER);
        assertEquals(retryHandler.getRetryCount(), 60, "Unexpected retry count.");
        assertTrue(retryHandler.isRequestSentRetryEnabled(), "Request sent entry should be enabled.");

        assertEquals(client.getHostConfiguration().getProxyHost(), "moo.com", "Unexpected proxy host.");
        assertEquals(client.getHostConfiguration().getProxyPort(), 8686, "Unexpected proxy port.");
    }
    */

    private void validateSingleThreadedClient(HttpClient client) {
        assertNotNull(client, "A valid client should have been constructed.");
        assertTrue(getConnManager(client) instanceof BasicHttpClientConnectionManager,
                "Expected a single-threaded connection manager.");
        //testClientUserAgent(client);
    }

    /*private void testClientUserAgent(HttpClient client) {
        RedirectExec execChain = TestUtils.getField(client, "execChain", RedirectExec.class);
        TestUtils.getField(execChain, "requestExecutor", HttpRequestExecutor.class);
        assertEquals(client.getParams().getParameter(HttpHeaders.USER_AGENT), HttpUtils.getArtifactoryUserAgent(),
                "Unexpected client user agent.");
    }*/

    private HttpClientConnectionManager getConnManager(HttpClient client) {
        return TestUtils.getField(client, "connManager", HttpClientConnectionManager.class);
    }
}
