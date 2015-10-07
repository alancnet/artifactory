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

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.ChallengeState;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.routing.RouteInfo;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Request interceptor to perform preemptive authentication against an http proxy.
 *
 * @author Yossi Shaul
 */
public class ProxyPreemptiveAuthInterceptor implements HttpRequestInterceptor {
    private static final Logger log = LoggerFactory.getLogger(ProxyPreemptiveAuthInterceptor.class);

    @Override
    public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
        HttpClientContext clientContext = HttpClientContext.adapt(context);
        AuthState proxyAuthState = clientContext.getProxyAuthState();

        // If there's no auth scheme available yet, try to initialize it preemptively
        if (proxyAuthState.getAuthScheme() == null) {
            CredentialsProvider credsProvider = clientContext.getCredentialsProvider();
            RouteInfo route = clientContext.getHttpRoute();
            if (route == null) {
                log.debug("No route found for {}", clientContext.getTargetHost());
                return;
            }

            HttpHost proxyHost = route.getProxyHost();
            if (proxyHost == null) {
                log.warn("No proxy host found in route {} for host {}", route, clientContext.getTargetHost());
                return;
            }

            Credentials creds = credsProvider.getCredentials(
                    new AuthScope(proxyHost.getHostName(), proxyHost.getPort()));
            if (creds == null) {
                log.info("No credentials found for proxy: " + proxyHost);
                return;
            }
            proxyAuthState.update(new BasicScheme(ChallengeState.PROXY), creds);
        }
    }
}
